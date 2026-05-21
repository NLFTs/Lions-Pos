package com.dak.spravel.service.inventory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dak.spravel.dto.request.inventory.TransferRequestDTO;
import com.dak.spravel.dto.request.inventory.TransferRequestItemDTO;
import com.dak.spravel.dto.response.components.PartnerSimpleDto;
import com.dak.spravel.dto.response.components.UserSimpleDto;
import com.dak.spravel.dto.response.inventoryresponse.TransferRequestResponse;
import com.dak.spravel.handler.ResourceNotFoundException;
import com.dak.spravel.model.auth.User;
import com.dak.spravel.model.inventory.TransferRequest;
import com.dak.spravel.model.inventory.TransferRequestItem;
import com.dak.spravel.repository.auth.UserRepository;
import com.dak.spravel.repository.catalog.ProductRepository;
import com.dak.spravel.repository.inventory.BranchesRepository;
import com.dak.spravel.repository.inventory.StockMutationRepository;
import com.dak.spravel.repository.inventory.TransferRequestItemRepository;
import com.dak.spravel.repository.inventory.TransferRequestRepository;
import com.dak.spravel.repository.inventory.WarehousesRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransferRequestService {

    private final TransferRequestRepository transferRequestRepository;
    private final TransferRequestItemRepository transferRequestItemRepository;
    private final StockMutationRepository stockMutationRepository;
    private final WarehousesRepository warehousesRepository;
    private final BranchesRepository branchesRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new RuntimeException("User tidak terautentikasi");
        }

        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan di database"));
    }

    private User getAuthenticatedAdminPartnerOrEmployee() {
        User user = getAuthenticatedUser();
        boolean isAuthorized = user.getRoles().stream()
                .anyMatch(role -> role.getSlug().equalsIgnoreCase("admin-partners") || 
                                role.getSlug().equalsIgnoreCase("employee-partners") ||
                                role.getSlug().equalsIgnoreCase("employee"));
        
        boolean isStaff = !user.getRoles().stream().anyMatch(role -> role.getSlug().equalsIgnoreCase("admin") || role.getSlug().equalsIgnoreCase("super_admin"));

        if (!isAuthorized || !isStaff) {
            throw new RuntimeException("Akses Ditolak: Hanya Admin Partner atau Employee yang diizinkan.");
        }
        return user;
    }

    private boolean isAdmin(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getSlug().equalsIgnoreCase("super_admin") || role.getSlug().equalsIgnoreCase("admin"));
    }

    private boolean isAdminPartnerAndEmployee(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getSlug().equalsIgnoreCase("employee") || 
                                 role.getSlug().equalsIgnoreCase("admin-partners") ||
                                 role.getSlug().equalsIgnoreCase("employee-partners"));
    }

    // Cek apakah user adalah Employee murni
    private boolean isEmployee(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getSlug().equalsIgnoreCase("employee"));
    }

    private TransferRequest getValidatedTransferRequest(Long id, User currentUser) {
        TransferRequest transferRequest = transferRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TransferRequest", id));

        if (currentUser.getPartner() == null || transferRequest.getPartner() == null ||
                !transferRequest.getPartner().getId().equals(currentUser.getPartner().getId())) {
            throw new RuntimeException("Akses Ditolak: Transfer request bukan milik partner Anda.");
        }

        return transferRequest;
    }

    // Khusus Untuk Super Admin
    public List<TransferRequestResponse> findAllTransferRequest(){
        User currentUser = getAuthenticatedUser();

        if (isAdminPartnerAndEmployee(currentUser)) {
            throw new RuntimeException("Akses Di Tolak: Admin Partners Dan Employee Tidak Di Perbolehkan Melihat Semua Transfer Request");
        }
        List<TransferRequest> allTransferRequests = transferRequestRepository.findAll();
        return allTransferRequests.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Otak Mapping Utama ke DTO Response
    public TransferRequestResponse mapToResponse(TransferRequest transferRequest) {
        if (transferRequest == null) return null;

        // Partner DTO Mapping dengan Pengaman Lazy-Load
        PartnerSimpleDto partnerDto = null;
        if (transferRequest.getPartner() != null) {
            partnerDto = new PartnerSimpleDto();
            partnerDto.setId(transferRequest.getPartner().getId());
            partnerDto.setName(transferRequest.getPartner().getName());
        } else if (transferRequest.getPartnerId() != null) {
            partnerDto = new PartnerSimpleDto();
            partnerDto.setId(transferRequest.getPartnerId());
            partnerDto.setName("Partner ID " + transferRequest.getPartnerId());
        }

        // Items Detail Mapping
        List<TransferRequestResponse.TransferRequestItemResponse> itemResponses =
                transferRequestItemRepository
                        .findByTransferRequestId(transferRequest.getId())
                        .stream()
                        .map(this::mapItemToResponse)
                        .collect(Collectors.toList());

        return TransferRequestResponse.builder()
                .id(transferRequest.getId())
                .partner(partnerDto)
                .fromLocationType(transferRequest.getFromLocationType() != null ? transferRequest.getFromLocationType().name() : null)
                .fromLocationId(transferRequest.getFromLocationId())
                .toLocationType(transferRequest.getToLocationType() != null ? transferRequest.getToLocationType().name() : null)
                .toLocationId(transferRequest.getToLocationId())
                .status(transferRequest.getStatus())
                .notes(transferRequest.getNotes())
                .requestedAt(transferRequest.getRequestedAt())
                .approvedAt(transferRequest.getApprovedAt())
                .receivedAt(transferRequest.getReceivedAt())
                .createdAt(transferRequest.getCreatedAt())
                .updatedAt(transferRequest.getUpdatedAt())
                .deletedAt(transferRequest.getDeletedAt())
                .createdBy(mapUserToSimpleDto(transferRequest.getCreatedBy()))
                .updatedBy(mapUserToSimpleDto(transferRequest.getUpdatedBy()))
                .deletedBy(mapUserToSimpleDto(transferRequest.getDeletedBy()))
                .approvedBy(mapUserToSimpleDto(transferRequest.getApprovedByUser()))
                .receivedBy(mapUserToSimpleDto(transferRequest.getReceivedByUser()))
                .items(itemResponses)
                .build();
    }

    private TransferRequestResponse.TransferRequestItemResponse mapItemToResponse(TransferRequestItem item) {
        if (item == null) return null;

        TransferRequestResponse.ProductSimpleDto productDto = null;
        if (item.getProduct() != null) {
            productDto = new TransferRequestResponse.ProductSimpleDto();
            productDto.setId(item.getProduct().getId());
            productDto.setName(item.getProduct().getName());
            productDto.setSku(item.getProduct().getSku());
        }

        TransferRequestResponse.TransferRequestItemResponse response =
                new TransferRequestResponse.TransferRequestItemResponse();
        response.setId(item.getId());
        response.setProduct(productDto);
        
        response.setQtyRequested(item.getQtyRequested() != null ? item.getQtyRequested().longValue() : null);
        response.setQtyReceived(item.getQtyReceived() != null ? item.getQtyReceived().longValue() : null);

        return response;
    }

    private UserSimpleDto mapUserToSimpleDto(User user) {
        if (user == null) return null;

        UserSimpleDto dto = new UserSimpleDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        return dto;
    }

    // GET ALL (Filter per Mitra)
    public List<TransferRequest> findAll() {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();
        if (currentUser.getPartner() == null) {
            throw new RuntimeException("User tidak terasosiasi dengan Partner.");
        }
        return transferRequestRepository.findByPartnerIdAndDeletedAtIsNull(currentUser.getPartner().getId());
    }

    // GET ALL PAGINATED
    public Page<TransferRequestResponse> findAll(int page, int size) {
        User currentUser = getAuthenticatedUser();
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("id").descending());

        if (isAdmin(currentUser)) {
            return transferRequestRepository.findAll(pageRequest).map(this::mapToResponse);
        }

        User activeUser = getAuthenticatedAdminPartnerOrEmployee();
        if (activeUser.getPartner() == null) {
            throw new RuntimeException("User tidak terasosiasi dengan Partner.");
        }

        return transferRequestRepository.findByPartnerIdAndDeletedAtIsNull(activeUser.getPartner().getId(), pageRequest)
                .map(this::mapToResponse);
    }

    // GET BY ID
    public TransferRequestResponse findById(Long id) {
        User currentUser = getAuthenticatedUser();
        TransferRequest transferRequest = transferRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TransferRequest", id));

        if (!isAdmin(currentUser)) {
            User activeUser = getAuthenticatedAdminPartnerOrEmployee();
            
            // Pengaman Null Check diganti menggunakan pembanding ID/Object Field langsung
            Long requestPartnerId = transferRequest.getPartner() != null ? transferRequest.getPartner().getId() : transferRequest.getPartnerId();
            if (activeUser.getPartner() == null || requestPartnerId == null ||
                !requestPartnerId.equals(activeUser.getPartner().getId())) {
                throw new RuntimeException("Akses Ditolak: Transfer request bukan milik partner Anda.");
            }
        }
        return mapToResponse(transferRequest);
    }

    // GET BY PARTNER ID
    public List<TransferRequest> findByPartnerId(Long partnerId) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();

        if (currentUser.getPartner() == null ||
                !currentUser.getPartner().getId().equals(partnerId)) {
            throw new RuntimeException("Akses Ditolak: Anda tidak bisa mengakses data partner lain.");
        }

        return transferRequestRepository.findByPartnerIdAndDeletedAtIsNull(partnerId);
    }

    // CREATE (Modifikasi mutakhir: Menembak langsung ke field partnerId Long & menyimpan detail items)
    @Transactional
    public TransferRequestResponse create(TransferRequestDTO request) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();
        TransferRequest transferRequest = new TransferRequest();

        // Mengatasi konflik insertable = false dengan mengisi field primitif Long-nya secara langsung
        if (request.getPartnerId() != null) {
            transferRequest.setPartnerId(request.getPartnerId());
        } else if (currentUser.getPartner() != null) {
            transferRequest.setPartnerId(currentUser.getPartner().getId());
        }

        if (transferRequest.getPartnerId() == null) {
            throw new RuntimeException("Gagal: partner_id wajib diisi di JSON atau terikat pada User login.");
        }
        
        Long fromId = request.getFromLocationId();
        if (warehousesRepository.existsById(fromId)) {
            transferRequest.setFromLocationType(TransferRequest.Location.WAREHOUSE);
            transferRequest.setFromLocationId(fromId);
        } else if (branchesRepository.existsById(fromId)) {
            transferRequest.setFromLocationType(TransferRequest.Location.BRANCH);
            transferRequest.setFromLocationId(fromId);
        } else {
            throw new RuntimeException("Gagal: ID lokasi asal (" + fromId + ") tidak ditemukan di Gudang maupun Cabang!");
        }

        Long toId = request.getToLocationId();
        if (warehousesRepository.existsById(toId)) {
            transferRequest.setToLocationType(TransferRequest.Location.WAREHOUSE);
            transferRequest.setToLocationId(toId);
        } else if (branchesRepository.existsById(toId)) {
            transferRequest.setToLocationType(TransferRequest.Location.BRANCH);
            transferRequest.setToLocationId(toId);
        } else {
            throw new RuntimeException("Gagal: ID lokasi tujuan (" + toId + ") tidak ditemukan di Gudang maupun Cabang!");
        }

        transferRequest.setNotes(request.getNotes());
        transferRequest.setStatus(TransferRequest.Status.PENDING);
        transferRequest.setRequestedAt(LocalDateTime.now());
        transferRequest.setCreatedAt(LocalDateTime.now());
        transferRequest.setCreatedBy(currentUser);

        // Save Parent utama dulu untuk generate ID Transfer Request
        TransferRequest savedTR = transferRequestRepository.save(transferRequest);

        // =============================================================
        // 🔥 PERBAIKAN LOGIKA: AMBIL PRODUK ASLI DARI DATABASE 🔥
        // =============================================================
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            List<TransferRequestItem> itemsToSave = request.getItems().stream().map(itemDto -> {
                TransferRequestItem itemEntity = new TransferRequestItem();
                
                // 1. Ikat ke parent-nya yang baru saja di-save
                itemEntity.setTransferRequest(savedTR); 
                
                // 2. Ambil produk asli dari database via repository
                com.dak.spravel.model.catalog.Product product = productRepository.findById(itemDto.getProductId())
                        .orElseThrow(() -> new RuntimeException("Gagal: Produk dengan ID " + itemDto.getProductId() + " tidak ditemukan!"));
                
                // 3. Masukkan produk asli ke dalam entitas item
                itemEntity.setProduct(product);
                
                // 4. Map quantity menggunakan tipe data Long sesuai konfigurasi entity kamu
                itemEntity.setQtyRequested(itemDto.getQtyRequested() != null ? itemDto.getQtyRequested().longValue() : 0L);
                itemEntity.setQtyReceived(0L); 
                
                return itemEntity;
            }).collect(Collectors.toList());

            // Kirim list items ke database detail
            transferRequestItemRepository.saveAll(itemsToSave);
        }
        // =============================================================

        // Return hasil akhir via otak mapping utama (otomatis me-load items yang barusan disimpan)
        return mapToResponse(savedTR);
    }

    // RECEIVE TRANSFER (Konfirmasi Penerimaan Stok)
    @Transactional
    public TransferRequestResponse receiveTransfer(Long transferRequestId, List<TransferRequestItemDTO> receivedItemsPayload) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();
        
        TransferRequest tr = transferRequestRepository.findById(transferRequestId)
                .orElseThrow(() -> new RuntimeException("Transfer Request tidak ditemukan"));

        if (currentUser.getPartner() == null || tr.getPartner() == null || !tr.getPartner().getId().equals(currentUser.getPartner().getId())) {
            throw new RuntimeException("Akses Ditolak: Transfer request bukan milik partner Anda.");
        }

        if (tr.getStatus() != TransferRequest.Status.PENDING && tr.getStatus() != TransferRequest.Status.IN_TRANSIT) {
            throw new RuntimeException("Gagal: Transfer Request sudah diproses sebelumnya atau telah dibatalkan.");
        }

        tr.setStatus(TransferRequest.Status.RECEIVED);
        tr.setReceivedAt(LocalDateTime.now());
        tr.setReceivedByUser(currentUser);
        tr.setUpdatedAt(LocalDateTime.now());
        tr.setUpdatedBy(currentUser);

        for (TransferRequestItem item : tr.getItems()) {
            Long realQtyReceived = receivedItemsPayload.stream()
                    .filter(p -> p.getProductId().equals(item.getProduct().getId()))
                    .map(p -> p.getQtyRequested() != null ? p.getQtyRequested().longValue() : 0L) 
                    .findFirst()
                    .orElse(item.getQtyRequested() != null ? item.getQtyRequested().longValue() : 0L); 

            item.setQtyReceived(realQtyReceived);
        }

        transferRequestItemRepository.saveAll(tr.getItems());
        TransferRequest updatedTR = transferRequestRepository.save(tr);
        return mapToResponse(updatedTR);
    }

    // SOFT DELETE
    @Transactional
    public void delete(Long id) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();
        TransferRequest transferRequest = getValidatedTransferRequest(id, currentUser);
        transferRequest.setDeletedAt(LocalDateTime.now());
        transferRequest.setDeletedBy(currentUser);
        transferRequestRepository.save(transferRequest);
    }

    // UPDATE STATUS (approve: pending→approved, in_transit, dll)
    @Transactional
    public TransferRequestResponse updateStatus(Long id, String newStatus) {
        User currentUser = getAuthenticatedUser();
        TransferRequest tr = transferRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transfer Request tidak ditemukan"));

        if (!isAdmin(currentUser)) {
            User activeUser = getAuthenticatedAdminPartnerOrEmployee();
            
            Long requestPartnerId = tr.getPartner() != null ? tr.getPartner().getId() : tr.getPartnerId();
            if (activeUser.getPartner() == null || requestPartnerId == null ||
                !requestPartnerId.equals(activeUser.getPartner().getId())) {
                throw new RuntimeException("Akses Ditolak: Transfer request bukan milik partner Anda.");
            }

            // VALIDASI UTAMA EMPLOYEE
            if (isEmployee(activeUser)) {
                String statusUpper = newStatus.toUpperCase();
                if (!statusUpper.equals("IN_TRANSIT") && !statusUpper.equals("RECEIVED")) {
                    throw new RuntimeException("Akses Ditolak: Employee hanya diizinkan mengubah status menjadi IN_TRANSIT atau RECEIVED.");
                }
            }
        }

        try {
            TransferRequest.Status status = TransferRequest.Status.valueOf(newStatus.toUpperCase());
            tr.setStatus(status);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Status tidak valid: " + newStatus);
        }

        if ("approved".equalsIgnoreCase(newStatus)) {
            tr.setApprovedAt(LocalDateTime.now());
            tr.setApprovedByUser(currentUser);
        }
        
        if ("received".equalsIgnoreCase(newStatus)) {
            tr.setReceivedAt(LocalDateTime.now());
            tr.setReceivedByUser(currentUser);
        }

        tr.setUpdatedAt(LocalDateTime.now());
        tr.setUpdatedBy(currentUser);
        TransferRequest saved = transferRequestRepository.save(tr);
        return mapToResponse(saved);
    }
}