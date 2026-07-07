package com.fts.twin.service.inventory;

import com.fts.twin.dto.request.inventory.TransferRequestDTO;
import com.fts.twin.dto.request.inventory.TransferRequestItemDTO;
import com.fts.twin.dto.response.components.UserSimpleDto;
import com.fts.twin.dto.response.inventoryresponse.TransferRequestResponse;
import com.fts.twin.handler.ResourceNotFoundException;
import com.fts.twin.model.auth.User;
import com.fts.twin.model.catalog.Product;
import com.fts.twin.model.common.Partners;
import com.fts.twin.model.inventory.StockBalance;
import com.fts.twin.model.inventory.TransferRequest;
import com.fts.twin.model.inventory.TransferRequestItem;
import com.fts.twin.repository.auth.UserRepository;
import com.fts.twin.repository.inventory.BranchesRepository;
import com.fts.twin.repository.inventory.TransferRequestItemRepository;
import com.fts.twin.repository.inventory.TransferRequestRepository;
import com.fts.twin.repository.inventory.WarehousesRepository;
import com.fts.twin.repository.inventory.StockBalanceRepository;
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

        return tr;
    }

    // ─── VALIDASI LOKASI ASAL DAN TUJUAN SECARA UMUM (UMUM & MULTI-TENANT) ───
    private void validateLocations(TransferRequest.Location fromType, Long fromId, TransferRequest.Location toType, Long toId, Partners partner) {
        // 1. Validasi Lokasi Asal
        if (fromType == TransferRequest.Location.BRANCH) {
            var branch = branchesRepository.findById(fromId)
                    .orElseThrow(() -> new ResourceNotFoundException("Branch Asal", fromId));
            if (partner != null && (branch.getPartners() == null || !branch.getPartners().getId().equals(partner.getId()))) {
                throw new RuntimeException("Validasi Gagal: Cabang asal bukan milik partner Anda.");
            }
        } else if (fromType == TransferRequest.Location.WAREHOUSE) {
            var warehouse = warehousesRepository.findById(fromId)
                    .orElseThrow(() -> new ResourceNotFoundException("Warehouse Asal", fromId));
            if (partner != null && (warehouse.getPartners() == null || !warehouse.getPartners().getId().equals(partner.getId()))) {
                throw new RuntimeException("Validasi Gagal: Gudang asal bukan milik partner Anda.");
            }
        }

        // 2. Validasi Lokasi Tujuan
        if (toType == TransferRequest.Location.BRANCH) {
            var branch = branchesRepository.findById(toId)
                    .orElseThrow(() -> new ResourceNotFoundException("Branch Tujuan", toId));
            if (partner != null && (branch.getPartners() == null || !branch.getPartners().getId().equals(partner.getId()))) {
                throw new RuntimeException("Validasi Gagal: Cabang tujuan bukan milik partner Anda.");
            }
        } else if (toType == TransferRequest.Location.WAREHOUSE) {
            var warehouse = warehousesRepository.findById(toId)
                    .orElseThrow(() -> new ResourceNotFoundException("Warehouse Tujuan", toId));
            if (partner != null && (warehouse.getPartners() == null || !warehouse.getPartners().getId().equals(partner.getId()))) {
                throw new RuntimeException("Validasi Gagal: Gudang tujuan bukan milik partner Anda.");
            }
        }
    }

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

        validateLocations(tr.getFromLocationType(), tr.getFromLocationId(), tr.getToLocationType(), tr.getToLocationId(), partner);

        boolean isManagerOrOwner = currentUser.getBranch() != null || 
                                    currentUser.getRoles().stream().anyMatch(r -> r.getSlug().contains("owner") || r.getSlug().contains("pengelola-cabang"));
        
        boolean isInterBranch = (tr.getFromLocationType() == TransferRequest.Location.BRANCH && tr.getToLocationType() == TransferRequest.Location.BRANCH);

        if (isManagerOrOwner && !isInterBranch) {
            // Pengelola Cabang / Owner & rutenya melibatkan gudang -> Auto Approved!
            tr.setStatus(TransferRequest.Status.APPROVED);
            tr.setApprovedAt(LocalDateTime.now());
            tr.setApprovedByUser(currentUser);
        } else {
            // Staff biasa, atau rutenya Cabang ke Cabang -> Harus disetujui Owner
            tr.setStatus(TransferRequest.Status.PENDING);
        }

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
            // Izinkan jika user adalah owner, atau dia pengelola cabang (slug mengandung 'pengelola-cabang')
            boolean allowedToApprove = currentUser.getRoles().stream()
                    .anyMatch(r -> r.getSlug().contains("owner") || r.getSlug().contains("pengelola-cabang") || r.getSlug().contains("admin"));
            
            if (!allowedToApprove) {
                throw new RuntimeException("Akses Ditolak: Hanya Pengelola Cabang dan Owner Pusat yang berhak menyetujui transfer ini.");
            }
            tr.setApprovedAt(LocalDateTime.now());
            tr.setApprovedByUser(currentUser);
        }
        else if (newStatus == TransferRequest.Status.IN_TRANSIT) {
            if (tr.getStatus() != TransferRequest.Status.APPROVED) {
                throw new RuntimeException("Gagal: Dokumen pemindahan stok belum disetujui.");
            }

            // PROSES PEMOTONGAN FISIK STOK DI LOKASI ASAL
            List<TransferRequestItem> trItems = transferRequestItemRepository.findByTransferRequestId(tr.getId());
            for (TransferRequestItem item : trItems) {
                var stockAsalOpt = stockBalanceRepository.findByProductIdAndLocationTypeAndLocationId(
                        item.getProductId(),
                        tr.getFromLocationType().name(),
                        tr.getFromLocationId()
                );

                if (stockAsalOpt.isEmpty()) {
                    throw new RuntimeException("Gagal Kirim: Saldo baki stok produk di lokasi asal belum di-inisialisasi.");
                }

                var stockAsal = stockAsalOpt.get();
                long qtyToSend = item.getQtyRequested();

                if (stockAsal.getQty() < qtyToSend) {
                    throw new RuntimeException("Gagal Kirim: Saldo stok di lokasi asal tidak mencukupi. Sisa saat ini: " + stockAsal.getQty());
                }

                stockAsal.setQty(stockAsal.getQty() - qtyToSend);
                stockBalanceRepository.save(stockAsal);
            }
        } 
        else if (newStatus == TransferRequest.Status.CANCELLED) {
            // Izinkan pembatalan oleh pemilik dokumen / pengelola
        }
        else {
            if (currentUser.getBranch() != null && !currentUser.getRoles().stream().anyMatch(r -> r.getSlug().contains("pengelola"))) {
                throw new RuntimeException("Akses Ditolak: Anda tidak memiliki wewenang untuk mengubah status ini.");
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
            throw new RuntimeException("Gagal: Barang belum diproses kirim (IN_TRANSIT) oleh lokasi asal pembekal.");
        }

        boolean isOwner = currentUser.getRoles().stream()
            .anyMatch(role -> role.getName().toLowerCase().contains("owner") || role.getName().toLowerCase().contains("admin-partners"));

        Long userLocationId = currentUser.getBranch().getId();

        if (currentUser.getPartner() != null) {
            if (tr.getStatus().equals(TransferRequest.Status.RECEIVED)) {
                if (!isOwner) {
                    if (userLocationId == null || !userLocationId.equals(tr.getToLocationId()) && !currentUser.getRoles().stream()
                        .anyMatch(role -> role.getName().toLowerCase().contains("pengelola-cabang"))) {
                        throw new RuntimeException("Akses Ditolak: Anda bukan pengelola di lokasi penerima ini.");
                    }
                }
            }
    
            if (tr.getStatus().equals(TransferRequest.Status.IN_TRANSIT)) {
                if (!isOwner) {
                    if (userLocationId == null || !userLocationId.equals(tr.getToLocationId()) && !currentUser.getRoles().stream()
                        .anyMatch(role -> role.getName().toLowerCase().contains("pengelola-cabang"))) {
                        throw new RuntimeException("Akses Ditolak: Anda bukan pengelola di lokasi penerima ini.");
                    }
                } 
            }  
        } else if (currentUser.getPartner() == null) {
            throw new RuntimeException("Akses Ditolak: Anda bukan pengelola di lokasi penerima ini.");
        }

        List<TransferRequestItem> trItems = transferRequestItemRepository.findByTransferRequestId(tr.getId());
    
        tr.setStatus(TransferRequest.Status.RECEIVED);
        tr.setReceivedAt(LocalDateTime.now());
        tr.setReceivedByUser(currentUser);
    
        for (TransferRequestItem item : trItems) {
            Long qtyReceived = items.stream()
                    .filter(i -> i.getProductId().equals(item.getProductId()))
                    .map(i -> i.getQtyRequested().longValue())
                    .findFirst()
                    .orElse(item.getQtyRequested());
    
            item.setQtyReceived(qtyReceived);
    
            var currentStockOpt = stockBalanceRepository.findByProductIdAndLocationTypeAndLocationId(
                    item.getProductId(),
                    tr.getToLocationType().name(),
                    tr.getToLocationId()
            );
    
            StockBalance stockTujuan;
            if (currentStockOpt.isEmpty()) {
                stockTujuan = new StockBalance();
                stockTujuan.setProduct(item.getProduct());
                stockTujuan.setLocationType(tr.getToLocationType().name());
                stockTujuan.setLocationId(tr.getToLocationId());
                stockTujuan.setQty(0L);
                stockTujuan.setCreatedAt(LocalDateTime.now());
                stockTujuan.setCreatedBy(currentUser);
                
                // Pasang referensi cabang jika lokasi tujuan merupakan cabang operasional
                if (tr.getToLocationType() == TransferRequest.Location.BRANCH && currentUser.getBranch() != null) {
                    stockTujuan.setLocationType(TransferRequest.Location.BRANCH.name());
                    stockTujuan.setLocationId(currentUser.getBranch().getId());
                }
            } else {
                stockTujuan = currentStockOpt.get();
            }
    
            stockTujuan.setQty(stockTujuan.getQty() + qtyReceived);
            stockTujuan.setUpdatedAt(LocalDateTime.now());
            stockTujuan.setUpdatedBy(currentUser);
            stockBalanceRepository.save(stockTujuan);
    
            long selisih = item.getQtyRequested() - qtyReceived;
            if (selisih > 0) {
                var stockAsalOpt = stockBalanceRepository.findByProductIdAndLocationTypeAndLocationId(
                        item.getProductId(),
                        tr.getFromLocationType().name(),
                        tr.getFromLocationId()
                );
                if (stockAsalOpt.isPresent()) {
                    var stockAsal = stockAsalOpt.get();
                    stockAsal.setQty(stockAsal.getQty() + selisih);
                    stockBalanceRepository.save(stockAsal);
                }
            }
    
            Product product = item.getProduct();
            if (product == null) {
                product = new Product();
                product.setId(item.getProductId());
                product.setPartner(tr.getPartner());
            }
    
            stockMutationService.recordMutation(
                    product,
                    tr.getPartner(),
                    "TRANSFER",
                    tr.getFromLocationType().name(),
                    tr.getFromLocationId(),
                    tr.getToLocationType().name(),
                    tr.getToLocationId(),
                    qtyReceived,
                    "TRANSFER_REQUEST",
                    tr.getId(),
                    "Permintaan transfer stok selesai diterima oleh " + currentUser.getUsername(),
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

    private String resolveLocationName(TransferRequest.Location type, Long locationId) {
        if (locationId == null || type == null) return null;
        if (type == TransferRequest.Location.BRANCH) {
            return branchesRepository.findById(locationId).map(b -> b.getName()).orElse("Cabang #" + locationId);
        } else if (type == TransferRequest.Location.WAREHOUSE) {
            return warehousesRepository.findById(locationId).map(w -> w.getName()).orElse("Gudang #" + locationId);
        }
        return "Lokasi #" + locationId;
    }

    public TransferRequestResponse mapToResponse(TransferRequest tr) {
        if (tr == null) return null;

        UserSimpleDto createdByDto = null;
        if (tr.getCreatedBy() != null) {
            createdByDto = new UserSimpleDto();
            createdByDto.setId(tr.getCreatedBy().getId());
            createdByDto.setUsername(tr.getCreatedBy().getUsername());
        }

        UserSimpleDto approvedByDto = null;
        if (tr.getApprovedByUser() != null) {
            approvedByDto = new UserSimpleDto();
            approvedByDto.setId(tr.getApprovedByUser().getId());
            approvedByDto.setUsername(tr.getApprovedByUser().getUsername());
        }

        UserSimpleDto receivedByDto = null;
        if (tr.getReceivedByUser() != null) {
            receivedByDto = new com.fts.twin.dto.response.components.UserSimpleDto();
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