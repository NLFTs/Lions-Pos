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
        checkPermission(currentUser, "transfer_request.index");

        if (currentUser.getPartner() == null) {
            return transferRequestRepository.findAll().stream().map(this::mapToResponse).toList();
        }

        List<TransferRequest> data = transferRequestRepository.findByPartnerIdAndDeletedAtIsNull(
                currentUser.getPartner().getId()
        );

        // 🛡️ BRANCH ISOLATION
        if (currentUser.getBranch() != null) {
            Long branchId = currentUser.getBranch().getId();
            data = data.stream()
                    .filter(tr ->
                        (tr.getFromLocationType() == TransferRequest.Location.BRANCH && branchId.equals(tr.getFromLocationId())) ||
                        (tr.getToLocationType() == TransferRequest.Location.BRANCH && branchId.equals(tr.getToLocationId()))
                    ).toList();
        }
        // 🛡️ WAREHOUSE ISOLATION
        else if (currentUser.getWarehouse() != null) {
            Long warehouseId = currentUser.getWarehouse().getId();
            data = data.stream()
                    .filter(tr ->
                        (tr.getFromLocationType() == TransferRequest.Location.WAREHOUSE && warehouseId.equals(tr.getFromLocationId())) ||
                        (tr.getToLocationType() == TransferRequest.Location.WAREHOUSE && warehouseId.equals(tr.getToLocationId()))
                    ).toList();
        }

        return data.stream().map(this::mapToResponse).toList();
    }

    public TransferRequestResponse findById(Long id) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "transfer_request.show");

        TransferRequest tr = transferRequestRepository.findByIdWithItems(id)
                .orElseThrow(() -> new ResourceNotFoundException("TransferRequest", id));

        if (currentUser.getPartner() != null) {
            if (tr.getPartner() == null || !tr.getPartner().getId().equals(currentUser.getPartner().getId())) {
                throw new RuntimeException("Akses Ditolak: Data Transfer bukan milik partner Anda.");
            }

            // Branch isolation
            if (currentUser.getBranch() != null) {
                Long branchId = currentUser.getBranch().getId();
                boolean access =
                    (tr.getFromLocationType() == TransferRequest.Location.BRANCH && branchId.equals(tr.getFromLocationId())) ||
                    (tr.getToLocationType() == TransferRequest.Location.BRANCH && branchId.equals(tr.getToLocationId()));
                if (!access) throw new RuntimeException("Akses Ditolak: Transfer ini tidak melibatkan cabang Anda.");
            }
            // Warehouse isolation
            else if (currentUser.getWarehouse() != null) {
                Long warehouseId = currentUser.getWarehouse().getId();
                boolean access =
                    (tr.getFromLocationType() == TransferRequest.Location.WAREHOUSE && warehouseId.equals(tr.getFromLocationId())) ||
                    (tr.getToLocationType() == TransferRequest.Location.WAREHOUSE && warehouseId.equals(tr.getToLocationId()));
                if (!access) throw new RuntimeException("Akses Ditolak: Transfer ini tidak melibatkan gudang Anda.");
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

        TransferRequest saved = transferRequestRepository.save(tr);

        // Simpan items
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            List<TransferRequestItem> items = request.getItems().stream().map(dto -> {
                TransferRequestItem item = new TransferRequestItem();
                item.setTransferRequest(saved);
                item.setProductId(dto.getProductId());
                item.setQtyRequested(dto.getQtyRequested().longValue());
                return item;
            }).toList();
            transferRequestItemRepository.saveAll(items);
        }

        return mapToResponse(transferRequestRepository.findByIdWithItems(saved.getId()).orElse(saved));
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
            
            // Branch Guard: hanya staff cabang tujuan yang bisa terima
            if (currentUser.getBranch() != null) {
                if (tr.getToLocationType() != TransferRequest.Location.BRANCH ||
                    !currentUser.getBranch().getId().equals(tr.getToLocationId())) {
                    throw new RuntimeException("Akses Ditolak: Anda hanya berhak memproses serah terima di cabang tugas Anda.");
                }
            }
            // Warehouse Guard: hanya staff gudang tujuan yang bisa terima
            else if (currentUser.getWarehouse() != null) {
                if (tr.getToLocationType() != TransferRequest.Location.WAREHOUSE ||
                    !currentUser.getWarehouse().getId().equals(tr.getToLocationId())) {
                    throw new RuntimeException("Akses Ditolak: Anda hanya berhak memproses serah terima di gudang tugas Anda.");
                }
            }
        }

        tr.setStatus(TransferRequest.Status.RECEIVED);
        tr.setReceivedAt(LocalDateTime.now());
        tr.setReceivedByUser(currentUser);

        // Load items dengan fetch
        List<TransferRequestItem> trItems = transferRequestItemRepository.findByTransferRequestId(tr.getId());

        for (TransferRequestItem item : trItems) {
            // Gunakan qty dari request jika ada, fallback ke qty_requested
            Long qty = items.stream()
                    .filter(i -> i.getProductId().equals(item.getProductId()))
                    .map(i -> i.getQtyRequested().longValue())
                    .findFirst()
                    .orElse(item.getQtyRequested());

            item.setQtyReceived(qty);

            stockBalanceService.adjustStock(
                    item.getProductId(),
                    tr.getFromLocationType().name().toUpperCase(),
                    tr.getFromLocationId(),
                    -qty
            );

            stockBalanceService.adjustStock(
                    item.getProductId(),
                    tr.getToLocationType().name().toUpperCase(),
                    tr.getToLocationId(),
                    qty
            );

            // Ambil product untuk mutation record
            com.dak.spravel.model.catalog.Product product = item.getProduct();
            if (product == null) {
                product = new com.dak.spravel.model.catalog.Product();
                product.setId(item.getProductId());
                product.setPartner(tr.getPartner());
            }

            stockMutationService.recordMutation(
                    product,
                    tr.getPartner(),
                    "TRANSFER",
                    tr.getFromLocationType().name().toUpperCase(),
                    tr.getFromLocationId(),
                    tr.getToLocationType().name().toUpperCase(),
                    tr.getToLocationId(),
                    qty,
                    "TRANSFER_REQUEST",
                    tr.getId(),
                    "Transfer diterima oleh " + currentUser.getUsername(),
                    currentUser
            );
        }

        transferRequestItemRepository.saveAll(trItems);
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

    private String resolveLocationName(TransferRequest.Location type, Long locationId) {
        if (type == null || locationId == null) return null;
        if (type == TransferRequest.Location.BRANCH) {
            return branchesRepository.findById(locationId).map(b -> b.getName()).orElse("Branch #" + locationId);
        }
        return warehousesRepository.findById(locationId).map(w -> w.getName()).orElse("Warehouse #" + locationId);
    }

    public TransferRequestResponse mapToResponse(TransferRequest tr) {
        if (tr == null) return null;

        com.dak.spravel.dto.response.components.UserSimpleDto createdByDto = null;
        if (tr.getCreatedBy() != null) {
            createdByDto = new com.dak.spravel.dto.response.components.UserSimpleDto();
            createdByDto.setId(tr.getCreatedBy().getId());
            createdByDto.setUsername(tr.getCreatedBy().getUsername());
        }

        com.dak.spravel.dto.response.components.UserSimpleDto approvedByDto = null;
        if (tr.getApprovedByUser() != null) {
            approvedByDto = new com.dak.spravel.dto.response.components.UserSimpleDto();
            approvedByDto.setId(tr.getApprovedByUser().getId());
            approvedByDto.setUsername(tr.getApprovedByUser().getUsername());
        }

        com.dak.spravel.dto.response.components.UserSimpleDto receivedByDto = null;
        if (tr.getReceivedByUser() != null) {
            receivedByDto = new com.dak.spravel.dto.response.components.UserSimpleDto();
            receivedByDto.setId(tr.getReceivedByUser().getId());
            receivedByDto.setUsername(tr.getReceivedByUser().getUsername());
        }

        List<TransferRequestResponse.TransferRequestItemResponse> itemResponses = null;
        if (tr.getItems() != null) {
            itemResponses = tr.getItems().stream().map(item -> {
                TransferRequestResponse.TransferRequestItemResponse ir = new TransferRequestResponse.TransferRequestItemResponse();
                ir.setId(item.getId());
                ir.setQtyRequested(item.getQtyRequested() != null ? item.getQtyRequested().longValue() : 0L);
                ir.setQtyReceived(item.getQtyReceived());
                if (item.getProduct() != null) {
                    TransferRequestResponse.ProductSimpleDto pd = new TransferRequestResponse.ProductSimpleDto();
                    pd.setId(item.getProduct().getId());
                    pd.setName(item.getProduct().getName());
                    pd.setSku(item.getProduct().getSku());
                    ir.setProduct(pd);
                }
                return ir;
            }).toList();
        }

        return TransferRequestResponse.builder()
                .id(tr.getId())
                .fromLocationType(tr.getFromLocationType() != null ? tr.getFromLocationType().name().toLowerCase() : null)
                .fromLocationId(tr.getFromLocationId())
                .fromLocationName(resolveLocationName(tr.getFromLocationType(), tr.getFromLocationId()))
                .toLocationType(tr.getToLocationType() != null ? tr.getToLocationType().name().toLowerCase() : null)
                .toLocationId(tr.getToLocationId())
                .toLocationName(resolveLocationName(tr.getToLocationType(), tr.getToLocationId()))
                .status(tr.getStatus())
                .notes(tr.getNotes())
                .requestedAt(tr.getRequestedAt())
                .approvedAt(tr.getApprovedAt())
                .receivedAt(tr.getReceivedAt())
                .createdAt(tr.getCreatedAt())
                .updatedAt(tr.getUpdatedAt())
                .createdBy(createdByDto)
                .approvedBy(approvedByDto)
                .receivedBy(receivedByDto)
                .items(itemResponses)
                .build();
    }
}