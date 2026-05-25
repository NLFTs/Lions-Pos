package com.dak.spravel.service.inventory;

import com.dak.spravel.dto.request.inventory.TransferRequestDTO;
import com.dak.spravel.dto.request.inventory.TransferRequestItemDTO;
import com.dak.spravel.dto.response.components.PartnerSimpleDto;
import com.dak.spravel.dto.response.components.UserSimpleDto;
import com.dak.spravel.dto.response.inventoryresponse.TransferRequestResponse;
import com.dak.spravel.handler.ResourceNotFoundException;
import com.dak.spravel.model.auth.User;
import com.dak.spravel.model.catalog.Product;
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
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

    private User getAuthenticatedUser() {
        Authentication auth =
            SecurityContextHolder.getContext().getAuthentication();
        if (
            auth == null ||
            !auth.isAuthenticated() ||
            "anonymousUser".equals(auth.getName())
        ) {
            throw new RuntimeException("User tidak terautentikasi");
        }

        return userRepository
            .findByUsername(auth.getName())
            .orElseThrow(() ->
                new RuntimeException("User tidak ditemukan di database")
            );
    }

    private User getAuthenticatedAdminPartnerOrEmployee() {
        User user = getAuthenticatedUser();
        boolean isAuthorized = user.getRoles().stream()
                .anyMatch(role -> role.getSlug().equalsIgnoreCase("owner") ||
                        role.getSlug().equalsIgnoreCase("employee"));

        boolean isStaff = !user.getRoles().stream().anyMatch(role -> role.getSlug().equalsIgnoreCase("admin") || role.getSlug().equalsIgnoreCase("super_admin"));

        if (!isAuthorized || !isStaff) {
            throw new RuntimeException(
                "Akses Ditolak: Hanya Owner atau Employee yang diizinkan."
            );
        }
        return user;
    }

    private boolean isAdmin(User user) {
        return user
            .getRoles()
            .stream()
            .anyMatch(
                role ->
                    role.getSlug().equalsIgnoreCase("super_admin") ||
                    role.getSlug().equalsIgnoreCase("admin")
            );
    }

    private boolean isEmployee(User user) {
        return user.getRoles().stream()
                .anyMatch(role ->
                        role.getSlug().equalsIgnoreCase("employee"));
    }

    private boolean isBranchAccessible(
            TransferRequest.Location locationType,
            Long locationId,
            User user
    ) {

        if (user.getBranch() == null) {
            return false;
        }

        return locationType == TransferRequest.Location.BRANCH
                && user.getBranch().getId().equals(locationId);
    }

    private TransferRequest getValidatedTransferRequest(
        Long id,
        User currentUser
    ) {
        TransferRequest transferRequest = transferRequestRepository
            .findById(id)
            .orElseThrow(() ->
                new ResourceNotFoundException("TransferRequest", id)
            );

        if (
            currentUser.getPartner() == null ||
            !transferRequest
                .getPartner()
                .getId()
                .equals(currentUser.getPartner().getId())
        ) {
            throw new RuntimeException(
                "Akses Ditolak: Transfer request bukan milik partner Anda."
            );
        }

        return transferRequest;
    }

    // Khusus Untuk Super Admin
    public List<TransferRequestResponse> findAllTransferRequest() {

        User currentUser = getAuthenticatedUser();

        if (!isAdmin(currentUser)) {
            throw new RuntimeException(
                    "Akses Ditolak: Hanya Admin yang boleh melihat semua Transfer Request."
            );
        }

        return transferRequestRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Otak Mapping Utama ke DTO Response
    public TransferRequestResponse mapToResponse(
        TransferRequest transferRequest
    ) {
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
                transferRequest.getItems()
                        .stream()
                        .map(this::mapItemToResponse)
                        .collect(Collectors.toList());

        // Bangun Response dengan data audit trail penuh sesuai field entity
        return TransferRequestResponse.builder()
            .id(transferRequest.getId())
            .partner(partnerDto)
            .fromLocationType(
                transferRequest.getFromLocationType() != null
                    ? transferRequest.getFromLocationType().name()
                    : null
            )
            .fromLocationId(transferRequest.getFromLocationId())
            .toLocationType(
                transferRequest.getToLocationType() != null
                    ? transferRequest.getToLocationType().name()
                    : null
            )
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
            // Menghubungkan ke getter user peng-approve
            .receivedBy(mapUserToSimpleDto(transferRequest.getReceivedByUser()))
            // Menghubungkan ke getter user penerima
            .items(itemResponses)
            .build();
    }

    private TransferRequestResponse.TransferRequestItemResponse mapItemToResponse(
        TransferRequestItem item
    ) {
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

        // Mengamankan tipe data antara Long (Response DTO) dan BigDecimal/Long dari Model
        response.setQtyRequested(
            item.getQtyRequested() != null
                ? item.getQtyRequested().longValue()
                : null
        );
        response.setQtyReceived(
            item.getQtyReceived() != null
                ? item.getQtyReceived().longValue()
                : null
        );

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
            throw new RuntimeException(
                "User tidak terasosiasi dengan Partner."
            );
        }
        List<TransferRequest> transferRequests = transferRequestRepository.findByPartnerIdAndDeletedAtIsNull(
                                currentUser.getPartner().getId()
                        );

        // FILTER KHUSUS EMPLOYEE
        if (isEmployee(currentUser)) {

            if (currentUser.getBranch() == null) {
                throw new RuntimeException("Employee tidak memiliki branch.");
            }

            Long branchId = currentUser.getBranch().getId();

            transferRequests = transferRequests.stream()
                    .filter(tr ->
                            ("BRANCH".equalsIgnoreCase(
                                    tr.getFromLocationType() != null
                                            ? tr.getFromLocationType().name()
                                            : null)
                                    && branchId.equals(tr.getFromLocationId()))
                                    ||
                                    ("BRANCH".equalsIgnoreCase(
                                            tr.getToLocationType() != null
                                                    ? tr.getToLocationType().name()
                                                    : null)
                                            && branchId.equals(tr.getToLocationId()))
                    )
                    .toList();
        }

        return transferRequests;
    }

    // GET ALL PAGINATED
    public Page<TransferRequestResponse> findAll(int page, int size) {
        User currentUser = getAuthenticatedUser();
        PageRequest pageRequest = PageRequest.of(
            page,
            size,
            Sort.by("id").descending()
        );

        if (isAdmin(currentUser)) {
            return transferRequestRepository
                .findAll(pageRequest)
                .map(this::mapToResponse);
        }

        if (currentUser.getPartner() == null) {
            throw new RuntimeException(
                "User tidak terasosiasi dengan Partner."
            );
        }

        Page<TransferRequestResponse> result =
                transferRequestRepository
                        .findByPartnerIdAndDeletedAtIsNull(
                                currentUser.getPartner().getId(),
                                pageRequest
                        )
                        .map(this::mapToResponse);

        if (isEmployee(currentUser)) {

            if (currentUser.getBranch() == null) {
                throw new RuntimeException("Employee tidak memiliki branch.");
            }

            Long branchId = currentUser.getBranch().getId();

            List<TransferRequestResponse> filtered =
                    result.getContent().stream()
                            .filter(tr ->
                                    ("BRANCH".equalsIgnoreCase(tr.getFromLocationType())
                                            && branchId.equals(tr.getFromLocationId()))
                                            ||
                                            ("BRANCH".equalsIgnoreCase(tr.getToLocationType())
                                                    && branchId.equals(tr.getToLocationId()))
                            )
                            .toList();

            return new org.springframework.data.domain.PageImpl<>(
                    filtered,
                    pageRequest,
                    filtered.size()
            );
        }

        return result;
    }

    // GET BY ID
    public TransferRequestResponse findById(Long id) {
        User currentUser = getAuthenticatedUser();
        TransferRequest transferRequest =
                transferRequestRepository.findByIdWithItems(id)
                        .orElseThrow(() -> new ResourceNotFoundException("TransferRequest", id));

        if (!isAdmin(currentUser)) {
            if (currentUser.getPartner() == null ||
                    !transferRequest.getPartner().getId().equals(currentUser.getPartner().getId())) {
                throw new RuntimeException("Akses Ditolak: Transfer request bukan milik partner Anda.");
            }
        }
        if (isEmployee(currentUser)) {

            boolean hasAccess =
                    isBranchAccessible(
                            transferRequest.getFromLocationType(),
                            transferRequest.getFromLocationId(),
                            currentUser
                    )
                            ||
                            isBranchAccessible(
                                    transferRequest.getToLocationType(),
                                    transferRequest.getToLocationId(),
                                    currentUser
                            );

            if (!hasAccess) {
                throw new RuntimeException(
                        "Akses Ditolak: Transfer request bukan branch Anda."
                );
            }
        }
        return mapToResponse(transferRequest);
    }

    // GET BY PARTNER ID
    public List<TransferRequest> findByPartnerId(Long partnerId) {

        User currentUser = getAuthenticatedAdminPartnerOrEmployee();

        if (currentUser.getPartner() == null ||
                !currentUser.getPartner().getId().equals(partnerId)) {
            throw new RuntimeException(
                    "Akses Ditolak: Anda tidak bisa mengakses data partner lain."
            );
        }

        List<TransferRequest> transferRequests =
                transferRequestRepository.findByPartnerIdAndDeletedAtIsNull(partnerId);

        // FILTER EMPLOYEE
        if (isEmployee(currentUser)) {

            if (currentUser.getBranch() == null) {
                throw new RuntimeException("Employee tidak memiliki branch.");
            }

            Long branchId = currentUser.getBranch().getId();

            transferRequests = transferRequests.stream()
                    .filter(tr ->
                            (
                                    tr.getFromLocationType() == TransferRequest.Location.BRANCH
                                            && branchId.equals(tr.getFromLocationId())
                            )
                                    ||
                                    (
                                            tr.getToLocationType() == TransferRequest.Location.BRANCH
                                                    && branchId.equals(tr.getToLocationId())
                                    )
                    )
                    .toList();
        }

        return transferRequests;
    }

    // CREATE (Modifikasi mutakhir: Menembak langsung ke field partnerId Long & menyimpan detail items)
    @Transactional
    public TransferRequestResponse create(TransferRequestDTO request) {

        User currentUser = getAuthenticatedAdminPartnerOrEmployee();

        Partners partner = currentUser.getPartner();

        TransferRequest transferRequest = new TransferRequest();

        transferRequest.setPartner(partner);

        // 1. Deteksi Otomatis Lokasi Asal (From) via Database
        Long fromId = request.getFromLocationId();

        if (warehousesRepository.existsById(fromId)) {

            transferRequest.setFromLocationType(
                    TransferRequest.Location.WAREHOUSE
            );

            transferRequest.setFromLocationId(fromId);

        } else if (branchesRepository.existsById(fromId)) {

            transferRequest.setFromLocationType(
                    TransferRequest.Location.BRANCH
            );

            transferRequest.setFromLocationId(fromId);

        } else {

            throw new RuntimeException(
                    "Gagal: ID lokasi asal (" + fromId + ") tidak ditemukan di Gudang maupun Cabang!"
            );
        }

        Long toId = request.getToLocationId();

        if (warehousesRepository.existsById(toId)) {

            transferRequest.setToLocationType(
                    TransferRequest.Location.WAREHOUSE
            );

            transferRequest.setToLocationId(toId);

        } else if (branchesRepository.existsById(toId)) {

            transferRequest.setToLocationType(
                    TransferRequest.Location.BRANCH
            );

            transferRequest.setToLocationId(toId);

        } else {

            throw new RuntimeException(
                    "Gagal: ID lokasi tujuan (" + toId + ") tidak ditemukan di Gudang maupun Cabang!"
            );
        }

        // VALIDASI KHUSUS EMPLOYEE
        if (isEmployee(currentUser)) {

            if (currentUser.getBranch() == null) {

                throw new RuntimeException(
                        "Employee tidak memiliki branch."
                );
            }

            Long branchId = currentUser.getBranch().getId();

            boolean hasAccess =

                    (
                            transferRequest.getFromLocationType()
                                    == TransferRequest.Location.BRANCH

                                    &&

                                    branchId.equals(
                                            transferRequest.getFromLocationId()
                                    )
                    )

                            ||

                            (
                                    transferRequest.getToLocationType()
                                            == TransferRequest.Location.BRANCH

                                            &&

                                            branchId.equals(
                                                    transferRequest.getToLocationId()
                                            )
                            );

            if (!hasAccess) {

                throw new RuntimeException(
                        "Akses Ditolak: Employee hanya boleh membuat transfer untuk branch miliknya."
                );
            }
        }

        transferRequest.setNotes(request.getNotes());

        transferRequest.setStatus(
                TransferRequest.Status.PENDING
        );

        transferRequest.setRequestedAt(
                LocalDateTime.now()
        );

        transferRequest.setCreatedAt(
                LocalDateTime.now()
        );

        transferRequest.setCreatedBy(currentUser);

        TransferRequest savedTR =
                transferRequestRepository.save(transferRequest);

        return mapToResponse(savedTR);
    }
    // RECEIVE TRANSFER (Konfirmasi Penerimaan Stok & Pencatatan Mutasi otomatis)
    @Transactional
    public TransferRequestResponse receiveTransfer(
        Long transferRequestId,
        List<TransferRequestItemDTO> receivedItemsPayload
    ) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();

        TransferRequest tr = transferRequestRepository.findById(transferRequestId)
                .orElseThrow(() -> new RuntimeException("Transfer Request tidak ditemukan"));

        if (isEmployee(currentUser)) {

            boolean hasAccess =
                    isBranchAccessible(
                            tr.getFromLocationType(),
                            tr.getFromLocationId(),
                            currentUser
                    )
                            ||
                            isBranchAccessible(
                                    tr.getToLocationType(),
                                    tr.getToLocationId(),
                                    currentUser
                            );

            if (!hasAccess) {
                throw new RuntimeException(
                        "Akses Ditolak: Transfer request bukan branch Anda."
                );
            }
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

            // BUG FIX #1: Update stok FROM (kurangi) dan TO (tambah), catat mutation
            Product product = item.getProduct();
            Partners partner = tr.getPartner();

            String fromType = tr.getFromLocationType().name().toLowerCase();
            Long fromId = tr.getFromLocationId();
            String toType = tr.getToLocationType().name().toLowerCase();
            Long toId = tr.getToLocationId();

            // Kurangi stok lokasi asal
            stockBalanceService.adjustStock(product.getId(),
                    tr.getFromLocationType().name(), fromId, -realQtyReceived);

            // Tambah stok lokasi tujuan
            stockBalanceService.adjustStock(product.getId(),
                    tr.getToLocationType().name(), toId, realQtyReceived);

            // Catat stock mutation type=TRANSFER
            stockMutationService.recordMutation(
                    product, partner,
                    "TRANSFER",
                    fromType, fromId,
                    toType, toId,
                    realQtyReceived,
                    "transfer_request", tr.getId(),
                    "Transfer #" + tr.getId() + " diterima",
                    currentUser
            );
        }

        transferRequestItemRepository.saveAll(tr.getItems());

        TransferRequest updatedTR = transferRequestRepository.save(tr);
        return mapToResponse(updatedTR);
    }

    // SOFT DELETE
    @Transactional
    public void delete(Long id) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();
        TransferRequest transferRequest = getValidatedTransferRequest(
            id,
            currentUser
        );
        transferRequest.setDeletedAt(LocalDateTime.now());
        transferRequest.setDeletedBy(currentUser);
        transferRequestRepository.save(transferRequest);
    }

    // UPDATE STATUS (approve: pending→approved, in_transit, dll)
    @Transactional
    public TransferRequestResponse updateStatus(Long id, String newStatus) {
        User currentUser = getAuthenticatedUser();
        TransferRequest tr = transferRequestRepository
            .findById(id)
            .orElseThrow(() ->
                new RuntimeException("Transfer Request tidak ditemukan")
            );

        if (!isAdmin(currentUser)) {
            if (currentUser.getPartner() == null ||
                    !tr.getPartner().getId().equals(currentUser.getPartner().getId())) {
                throw new RuntimeException("Akses Ditolak: Transfer request bukan milik partner Anda.");
            }
        }

        if (isEmployee(currentUser)) {

            boolean hasAccess = isBranchAccessible(tr.getFromLocationType(),
                            tr.getFromLocationId(), currentUser
                    )
                            ||
                            isBranchAccessible(
                                    tr.getToLocationType(),
                                    tr.getToLocationId(),
                                    currentUser
                            );

            if (!hasAccess) {
                throw new RuntimeException(
                        "Akses Ditolak: Transfer request bukan branch Anda."
                );
            }
        }
        try {
            TransferRequest.Status status = TransferRequest.Status.valueOf(
                newStatus.toUpperCase()
            );
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
