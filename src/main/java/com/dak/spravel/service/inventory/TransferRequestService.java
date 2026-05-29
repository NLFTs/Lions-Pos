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
import com.dak.spravel.repository.inventory.StockBalanceRepository;
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
    private final StockMutationService stockMutationService;
    private final StockBalanceRepository stockBalanceRepository;

    // ─── 🔒 PUSAT VALIDASI AUTH & PERMISSION ───────────────────────────────────

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new RuntimeException("User tidak terautentikasi");
        }
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan di database"));
    }

    private void checkPermission(User user, String permissionSlug) {
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

    // ─── 🛡️ MULTI-TENANT GUARD ──────────────────────────────────────────────────

    private TransferRequest getValidatedTransferRequest(Long id, User currentUser) {
        TransferRequest tr = transferRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TransferRequest", id));

        if (currentUser.getPartner() == null) {
            return tr;
        }

        if (tr.getPartner() == null || !tr.getPartner().getId().equals(currentUser.getPartner().getId())) {
            throw new RuntimeException("Akses Ditolak: Data Transfer Request bukan milik partner Anda");
        }

        if (currentUser.getBranch() != null) {
            Long branchId = currentUser.getBranch().getId();
            boolean involved = (tr.getFromLocationType() == TransferRequest.Location.BRANCH && branchId.equals(tr.getFromLocationId())) ||
                               (tr.getToLocationType() == TransferRequest.Location.BRANCH && branchId.equals(tr.getToLocationId()));
            if (!involved) {
                throw new RuntimeException("Akses Ditolak: Anda tidak memiliki izin melihat transfer lokasi lain.");
            }
        }
        else if (currentUser.getWarehouse() != null) {
            Long warehouseId = currentUser.getWarehouse().getId();
            boolean involved = (tr.getFromLocationType() == TransferRequest.Location.WAREHOUSE && warehouseId.equals(tr.getFromLocationId())) ||
                               (tr.getToLocationType() == TransferRequest.Location.WAREHOUSE && warehouseId.equals(tr.getToLocationId()));
            if (!involved) {
                throw new RuntimeException("Akses Ditolak: Anda tidak memiliki izin melihat transfer lokasi lain.");
            }
        }

        return tr;
    }

    // ─── 🚀 MAIN METHODS CORE ──────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<TransferRequestResponse> findAllAdmin() {
        User currentUser = getAuthenticatedUser();
        checkSuperAdminOnly(currentUser);

        return transferRequestRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TransferRequestResponse> findAll() {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "transfer_request.index");

        if (currentUser.getPartner() == null) {
            return transferRequestRepository.findAll().stream().map(this::mapToResponse).toList();
        }

        List<TransferRequest> data = transferRequestRepository.findByPartnerIdAndDeletedAtIsNull(
                currentUser.getPartner().getId()
        );

        if (currentUser.getBranch() != null) {
            Long branchId = currentUser.getBranch().getId();
            data = data.stream()
                    .filter(tr ->
                        (tr.getFromLocationType() == TransferRequest.Location.BRANCH && branchId.equals(tr.getFromLocationId())) ||
                        (tr.getToLocationType() == TransferRequest.Location.BRANCH && branchId.equals(tr.getToLocationId()))
                    ).toList();
        }
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

    @Transactional(readOnly = true)
    public TransferRequestResponse findById(Long id) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "transfer_request.show");

        TransferRequest tr = getValidatedTransferRequest(id, currentUser);
        return mapToResponse(tr);
    }

    @Transactional
    public TransferRequestResponse create(TransferRequestDTO request) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "transfer_request.store");

        Partners partner = currentUser.getPartner();
        if (partner == null) {
            throw new RuntimeException("Akses Ditolak: Super Admin Global tidak diperbolehkan membuat dokumen transfer langsung.");
        }

        TransferRequest tr = new TransferRequest();
        tr.setPartner(partner);

        if (currentUser.getBranch() != null) {
            tr.setToLocationType(TransferRequest.Location.BRANCH);
            tr.setToLocationId(currentUser.getBranch().getId());

            if (request.getFromLocationType() == null || request.getFromLocationId() == null) {
                throw new RuntimeException("Lokasi asal sumber barang wajib ditentukan.");
            }
            tr.setFromLocationType(TransferRequest.Location.valueOf(request.getFromLocationType().toUpperCase()));
            tr.setFromLocationId(request.getFromLocationId());

        } else if (currentUser.getWarehouse() != null) {
            tr.setToLocationType(TransferRequest.Location.WAREHOUSE);
            tr.setToLocationId(currentUser.getWarehouse().getId());

            if (request.getFromLocationType() == null || request.getFromLocationId() == null) {
                throw new RuntimeException("Lokasi asal sumber barang wajib ditentukan.");
            }
            tr.setFromLocationType(TransferRequest.Location.valueOf(request.getFromLocationType().toUpperCase()));
            tr.setFromLocationId(request.getFromLocationId());

        } else {
            if (request.getFromLocationType() == null || request.getFromLocationId() == null ||
                request.getToLocationType() == null || request.getToLocationId() == null) {
                throw new RuntimeException("Lokasi asal dan lokasi tujuan wajib diisi lengkap.");
            }
            tr.setFromLocationType(TransferRequest.Location.valueOf(request.getFromLocationType().toUpperCase()));
            tr.setFromLocationId(request.getFromLocationId());
            tr.setToLocationType(TransferRequest.Location.valueOf(request.getToLocationType().toUpperCase()));
            tr.setToLocationId(request.getToLocationId());
        }

        if (tr.getFromLocationType() == tr.getToLocationType() && tr.getFromLocationId().equals(tr.getToLocationId())) {
            throw new RuntimeException("Lokasi asal dan lokasi tujuan tidak boleh sama.");
        }

        tr.setStatus(TransferRequest.Status.PENDING);
        tr.setRequestedAt(LocalDateTime.now());
        tr.setCreatedAt(LocalDateTime.now());
        tr.setCreatedBy(currentUser);
        tr.setNotes(request.getNotes());

        TransferRequest saved = transferRequestRepository.save(tr);

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

    @Transactional
    public TransferRequestResponse updateStatus(Long id, String status) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "transfer_request.update");

        TransferRequest tr = getValidatedTransferRequest(id, currentUser);
        TransferRequest.Status newStatus = TransferRequest.Status.valueOf(status.toUpperCase());
        
        if (newStatus == TransferRequest.Status.APPROVED) {
            if (currentUser.getBranch() != null || currentUser.getWarehouse() != null) {
                throw new RuntimeException("Akses Ditolak: Staff Lapangan tidak berhak menyetujui rute transfer ini. Hanya Owner Pusat.");
            }
            tr.setApprovedAt(LocalDateTime.now());
            tr.setApprovedByUser(currentUser);
        }
        else if (newStatus == TransferRequest.Status.IN_TRANSIT) {
            if (tr.getStatus() != TransferRequest.Status.APPROVED) {
                throw new RuntimeException("Gagal: Dokumen transfer belum disetujui Owner, barang tidak boleh dikirim.");
            }
            
            if (currentUser.getPartner() != null) {
                if (currentUser.getBranch() != null) {
                    if (tr.getFromLocationType() != TransferRequest.Location.BRANCH || !currentUser.getBranch().getId().equals(tr.getFromLocationId())) {
                        throw new RuntimeException("Akses Ditolak: Hanya staff di lokasi ASAL yang berhak memproses pengiriman.");
                    }
                } else if (currentUser.getWarehouse() != null) {
                    if (tr.getFromLocationType() != TransferRequest.Location.WAREHOUSE || !currentUser.getWarehouse().getId().equals(tr.getFromLocationId())) {
                        throw new RuntimeException("Akses Ditolak: Hanya staff di lokasi ASAL yang berhak memproses pengiriman.");
                    }
                }
            }
        } 
        else {
            if (currentUser.getBranch() != null || currentUser.getWarehouse() != null) {
                throw new RuntimeException("Akses Ditolak: Staff Lapangan tidak berhak mengubah ke status ini.");
            }
        }

        tr.setStatus(newStatus);
        tr.setUpdatedAt(LocalDateTime.now());
        tr.setUpdatedBy(currentUser);

        return mapToResponse(transferRequestRepository.save(tr));
    }

    @Transactional
    public TransferRequestResponse receiveTransfer(Long id, List<TransferRequestItemDTO> items) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "transfer_request.update");

        TransferRequest tr = getValidatedTransferRequest(id, currentUser);

        if (tr.getStatus() != TransferRequest.Status.IN_TRANSIT) {
            throw new RuntimeException("Gagal: Barang belum dikirim (IN_TRANSIT) oleh lokasi asal.");
        }

        if (currentUser.getPartner() != null) {
            if (currentUser.getBranch() != null) {
                if (tr.getToLocationType() != TransferRequest.Location.BRANCH || !currentUser.getBranch().getId().equals(tr.getToLocationId())) {
                    throw new RuntimeException("Akses Ditolak: Anda hanya berhak memproses serah terima di lokasi TUJUAN Anda.");
                }
            }
            else if (currentUser.getWarehouse() != null) {
                if (tr.getToLocationType() != TransferRequest.Location.WAREHOUSE || !currentUser.getWarehouse().getId().equals(tr.getToLocationId())) {
                    throw new RuntimeException("Akses Ditolak: Anda hanya berhak memproses serah terima di lokasi TUJUAN Anda.");
                }
            }
        }

        List<TransferRequestItem> trItems = transferRequestItemRepository.findByTransferRequestId(tr.getId());

        // 🛡️ VALIDASI STOK ASAL DAN KESIAPAN TUJUAN
        for (TransferRequestItem item : trItems) {
            var currentStockOpt = stockBalanceRepository.findByProductIdAndLocationTypeAndLocationId(
                    item.getProductId(),
                    tr.getToLocationType().name().toUpperCase(),
                    tr.getToLocationId()
            );

            if (currentStockOpt.isEmpty()) {
                throw new RuntimeException("Gagal Terima: Sistem mendeteksi master stok produk di lokasi tujuan belum di-inisialisasi.");
            }
        }

        tr.setStatus(TransferRequest.Status.RECEIVED);
        tr.setReceivedAt(LocalDateTime.now());
        tr.setReceivedByUser(currentUser);

        for (TransferRequestItem item : trItems) {
            Long qty = items.stream()
                    .filter(i -> i.getProductId().equals(item.getProductId()))
                    .map(i -> i.getQtyRequested().longValue())
                    .findFirst()
                    .orElse(item.getQtyRequested());

            item.setQtyReceived(qty);

            // 🔄 MANIPULASI STOK FISIK DI DATABASE SECARA NYATA
            // 1. Kurangi stok lokasi asal
            var stockAsalOpt = stockBalanceRepository.findByProductIdAndLocationTypeAndLocationId(
                    item.getProductId(),
                    tr.getFromLocationType().name().toUpperCase(),
                    tr.getFromLocationId()
            );
            if (stockAsalOpt.isPresent()) {
                var stockAsal = stockAsalOpt.get();
                if (stockAsal.getQty() < qty) {
                    throw new RuntimeException("Gagal Terima: Stok barang di lokasi asal tidak mencukupi untuk ditransfer.");
                }
                stockAsal.setQty(stockAsal.getQty() - qty);
                stockBalanceRepository.save(stockAsal);
            }

            // 2. Tambah stok lokasi tujuan
            var stockTujuanOpt = stockBalanceRepository.findByProductIdAndLocationTypeAndLocationId(
                    item.getProductId(),
                    tr.getToLocationType().name().toUpperCase(),
                    tr.getToLocationId()
            );
            if (stockTujuanOpt.isPresent()) {
                var stockTujuan = stockTujuanOpt.get();
                stockTujuan.setQty(stockTujuan.getQty() + qty);
                stockBalanceRepository.save(stockTujuan);
            }

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
                    "Transfer request dikonfirmasi selesai oleh " + currentUser.getUsername(),
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