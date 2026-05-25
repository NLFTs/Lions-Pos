package com.dak.spravel.service.inventory;

import com.dak.spravel.dto.request.inventory.TransferRequestDTO;
import com.dak.spravel.dto.request.inventory.TransferRequestItemDTO;
import com.dak.spravel.dto.response.inventoryresponse.TransferRequestResponse;
import com.dak.spravel.handler.ResourceNotFoundException;
import com.dak.spravel.model.auth.User;
import com.dak.spravel.model.common.Partners;
import com.dak.spravel.model.inventory.TransferRequest;
import com.dak.spravel.model.inventory.TransferRequestItem;
import com.dak.spravel.repository.auth.UserRepository;
import com.dak.spravel.repository.inventory.BranchesRepository;
import com.dak.spravel.repository.inventory.TransferRequestItemRepository;
import com.dak.spravel.repository.inventory.TransferRequestRepository;
import com.dak.spravel.repository.inventory.WarehousesRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransferRequestService {

    private final TransferRequestRepository transferRequestRepository;
    private final TransferRequestItemRepository transferRequestItemRepository;
    private final WarehousesRepository warehousesRepository;
    private final BranchesRepository branchesRepository;
    private final UserRepository userRepository;
    private final StockBalanceService stockBalanceService;
    private final StockMutationService stockMutationService;

    // ─── 🔒 PUSAT VALIDASI AUTH & PERMISSION (MURNI DINAMIS) ───────────────────

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new RuntimeException("User tidak terautentikasi");
        }
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan di database"));
    }

    // 🔥 KUNCI DINAMIS: Check permission dinamis dari database tanpa kaku nge-lock nama role
    private void checkPermission(User user, String permissionSlug) {
        // 👑 Raja Super Admin (partner null) bypass seluruh jenis gate permission
        if (user.getPartner() == null) {
            return;
        }

        boolean hasPerm = user.getRoles().stream()
                .filter(role -> role.getPermissions() != null)
                .flatMap(role -> role.getPermissions().stream())
                .anyMatch(perm -> perm.getSlug().equalsIgnoreCase(permissionSlug));

        if (!hasPerm) {
            throw new RuntimeException("Akses Ditolak: Anda tidak memiliki hak akses '" + permissionSlug + "'!");
        }
    }

    private void checkSuperAdminOnly(User user) {
        if (user.getPartner() != null) {
            throw new RuntimeException("Akses Ditolak: Fitur ini khusus Super Admin Global.");
        }
    }

    // Helper: Validasi Akses Branch Kerja Karyawan (Dinamis tanpa hardcode role string)
    private boolean isBranchAccessible(TransferRequest.Location type, Long locationId, User user) {
        return user.getBranch() != null
                && type == TransferRequest.Location.BRANCH
                && user.getBranch().getId().equals(locationId);
    }

    // ─── 🛡️ MULTI-TENANT GUARD (ANTI NULL POINTER UNTUK SUPER ADMIN) ───────────

    private TransferRequest getValidatedTransferRequest(Long id, User currentUser) {
        TransferRequest tr = transferRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TransferRequest", id));

        // 👑 Super Admin global bebas bypass pengecekan tenant id
        if (currentUser.getPartner() == null) {
            return tr;
        }

        if (tr.getPartner() == null || !tr.getPartner().getId().equals(currentUser.getPartner().getId())) {
            throw new RuntimeException("Akses Ditolak: Data Transfer Request bukan milik partner Anda");
        }

        return tr;
    }

    // ─── 🚀 MAIN METHODSCORE (SUDAH DISERAGAMKAN POLANYA) ──────────────────────

    // KHUSUS SUPER ADMIN GLOBAL

    public List<TransferRequestResponse> findAllAdmin() {
        User currentUser = getAuthenticatedUser();
        checkSuperAdminOnly(currentUser);

        return transferRequestRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // OPERASIONAL TENANT / PARTNER (BERBASIS PERMISSION MATRIKS)

    public List<TransferRequestResponse> findAll() {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "transfer_request.index"); // 💡 Saring via permission index

        // 👑 Handling Super Admin Global: Tarik semua riwayat mutasi tanpa filter
        if (currentUser.getPartner() == null) {
            return transferRequestRepository.findAll().stream().map(this::mapToResponse).toList();
        }

        // 🏢 Handling Tenant Context
        List<TransferRequest> data = transferRequestRepository.findByPartnerIdAndDeletedAtIsNull(
                currentUser.getPartner().getId()
        );

        // 🛡️ BRANCH ISOLATION: Jika user tertempel ke branch tertentu, filter agar hanya melihat area kerjanya
        if (currentUser.getBranch() != null) {
            data = data.stream()
                    .filter(tr -> isBranchAccessible(tr.getFromLocationType(), tr.getFromLocationId(), currentUser) ||
                                  isBranchAccessible(tr.getToLocationType(), tr.getToLocationId(), currentUser))
                    .toList();
        }

        return data.stream().map(this::mapToResponse).toList();
    }

    public TransferRequestResponse findById(Long id) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "transfer_request.show");

        TransferRequest tr = transferRequestRepository.findByIdWithItems(id)
                .orElseThrow(() -> new ResourceNotFoundException("TransferRequest", id));

        // Multi-Tenant Isolation
        if (currentUser.getPartner() != null) {
            if (tr.getPartner() == null || !tr.getPartner().getId().equals(currentUser.getPartner().getId())) {
                throw new RuntimeException("Akses Ditolak: Data Transfer bukan milik partner Anda.");
            }

            // Branch Context Isolation Guard
            if (currentUser.getBranch() != null) {
                boolean access = isBranchAccessible(tr.getFromLocationType(), tr.getFromLocationId(), currentUser) ||
                                 isBranchAccessible(tr.getToLocationType(), tr.getToLocationId(), currentUser);
                if (!access) {
                    throw new RuntimeException("Akses Ditolak: Mutasi barang tidak melibatkan cabang penugasan Anda.");
                }
            }
        }

        return mapToResponse(tr);
    }

    // FORMULASI REQUEST TRANSFER BARANG BARU (DRAFT / PENDING)
    @Transactional
    public TransferRequestResponse create(TransferRequestDTO request) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "transfer_request.store"); // 💡 Siapapun boleh input asal diberi izin Owner

        Partners partner = currentUser.getPartner();
        if (partner == null) {
            throw new RuntimeException("Akses Ditolak: Super Admin Global tidak diperbolehkan membuat dokumen transfer langsung.");
        }

        TransferRequest tr = new TransferRequest();
        tr.setPartner(partner);

        // Resolusi Lokasi Asal Pengirim (FROM)
        Long fromId = request.getFromLocationId();
        if (warehousesRepository.existsById(fromId)) {
            tr.setFromLocationType(TransferRequest.Location.WAREHOUSE);
        } else if (branchesRepository.existsById(fromId)) {
            tr.setFromLocationType(TransferRequest.Location.BRANCH);
        } else {
            throw new RuntimeException("Lokasi asal pengirim (From Location) tidak valid.");
        }
        tr.setFromLocationId(fromId);

        // Resolusi Lokasi Target Penerima (TO)
        Long toId = request.getToLocationId();
        if (warehousesRepository.existsById(toId)) {
            tr.setFromLocationType(TransferRequest.Location.WAREHOUSE); // Sesuai mapping entity asli lu
            tr.setToLocationType(TransferRequest.Location.WAREHOUSE);
        } else if (branchesRepository.existsById(toId)) {
            tr.setToLocationType(TransferRequest.Location.BRANCH);
        } else {
            throw new RuntimeException("Lokasi target penerima (To Location) tidak valid.");
        }
        tr.setToLocationId(toId);

        tr.setStatus(TransferRequest.Status.PENDING);
        tr.setRequestedAt(LocalDateTime.now());
        tr.setCreatedAt(LocalDateTime.now());
        tr.setCreatedBy(currentUser);
        tr.setNotes(request.getNotes());

        return mapToResponse(transferRequestRepository.save(tr));
    }

    // EKSEKUSI PENERIMAAN BARANG DI LOKASI TUJUAN & AMANDEMEN STOK BALANCE REAL-TIME
    @Transactional
    public TransferRequestResponse receiveTransfer(Long id, List<TransferRequestItemDTO> items) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "transfer_request.update"); // 💡 Siapapun boleh approve serah terima asal punya permission

        TransferRequest tr = transferRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TransferRequest", id));

        // Tenant Protection Guard
        if (currentUser.getPartner() != null) {
            if (tr.getPartner() == null || !tr.getPartner().getId().equals(currentUser.getPartner().getId())) {
                throw new RuntimeException("Akses Ditolak: Dokumen transfer bukan milik partner Anda.");
            }
            
            // Branch Protection Guard: Memastikan hanya staff target cabang penerima yang berhak klik tombol terima barang
            if (currentUser.getBranch() != null && !isBranchAccessible(tr.getToLocationType(), tr.getToLocationId(), currentUser)) {
                throw new RuntimeException("Akses Ditolak: Anda hanya berhak memproses serah terima barang di cabang tugas Anda sendiri.");
            }
        }

        tr.setStatus(TransferRequest.Status.RECEIVED);
        tr.setReceivedAt(LocalDateTime.now());
        tr.setReceivedByUser(currentUser);

        for (TransferRequestItem item : tr.getItems()) {
            Long qty = items.stream()
                    .filter(i -> i.getProductId().equals(item.getProduct().getId()))
                    .map(i -> i.getQtyRequested().longValue())
                    .findFirst()
                    .orElse(item.getQtyRequested().longValue());

            item.setQtyReceived(qty);

            // 🛠️ EKSEKUSI POTONG STOK DI LOKASI ASAL (FROM)
            stockBalanceService.adjustStock(
                    item.getProduct().getId(),
                    tr.getFromLocationType().name().toUpperCase(),
                    tr.getFromLocationId(),
                    -qty
            );

            // 🛠️ EKSEKUSI TAMBAH STOK DI LOKASI TUJUAN (TO)
            stockBalanceService.adjustStock(
                    item.getProduct().getId(),
                    tr.getToLocationType().name().toUpperCase(),
                    tr.getToLocationId(),
                    qty
            );

            // Suntik rekaman audit log mutasi barang Spravel otomatis
            stockMutationService.recordMutation(
                    item.getProduct(),
                    tr.getPartner(),
                    "TRANSFER",
                    tr.getFromLocationType().name().toUpperCase(),
                    tr.getFromLocationId(),
                    tr.getToLocationType().name().toUpperCase(),
                    tr.getToLocationId(),
                    qty,
                    "TRANSFER_REQUEST",
                    tr.getId(),
                    "Transfer Request Sukses Diterima Oleh " + currentUser.getUsername(),
                    currentUser
            );
        }

        transferRequestItemRepository.saveAll(tr.getItems());
        return mapToResponse(transferRequestRepository.save(tr));
    }

    @Transactional
    public void delete(Long id) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "transfer_request.delete");

        TransferRequest tr = getValidatedTransferRequest(id, currentUser);

        tr.setDeletedAt(LocalDateTime.now());
        tr.setDeletedBy(currentUser);

        transferRequestRepository.save(tr);
    }

    @Transactional
    public TransferRequestResponse updateStatus(Long id, String status) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "transfer_request.update");

        TransferRequest tr = getValidatedTransferRequest(id, currentUser);

        tr.setStatus(TransferRequest.Status.valueOf(status.toUpperCase()));
        tr.setUpdatedAt(LocalDateTime.now());
        tr.setUpdatedBy(currentUser);

        return mapToResponse(transferRequestRepository.save(tr));
    }

    // ─── 🔄 PRIVATE MAPPERS SECTION ───────────────────────────────────────────

    public TransferRequestResponse mapToResponse(TransferRequest tr) {
        if (tr == null) return null;
        
        return TransferRequestResponse.builder()
                .id(tr.getId())
                .status(tr.getStatus())
                .notes(tr.getNotes())
                .requestedAt(tr.getRequestedAt())
                .createdAt(tr.getCreatedAt())
                .updatedAt(tr.getUpdatedAt())
                .build();
    }
}