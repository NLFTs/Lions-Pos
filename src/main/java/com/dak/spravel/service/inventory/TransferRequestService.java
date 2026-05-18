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
import com.dak.spravel.repository.catalog.ProductRepository;
import com.dak.spravel.repository.inventory.TransferRequestItemRepository;
import com.dak.spravel.repository.inventory.TransferRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransferRequestService {

    private final TransferRequestRepository transferRequestRepository;
    private final TransferRequestItemRepository transferRequestItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new RuntimeException("User tidak terautentikasi");
        }

        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan di database"));
    }

    private User getAuthenticatedSuperAdmin(){
        User user = getAuthenticatedUser();
        boolean isSuperAdmin = user.getRoles().stream()
        .anyMatch(role -> role.getSlug().equalsIgnoreCase("admin"));
        if (!isSuperAdmin) throw new RuntimeException("Akses Di Tolak: Anda Bukan Super Admin"); {
            return user;
        }
    }

    private User getAuthenticatedAdminPartnerOrEmployee() {
        User user = getAuthenticatedUser();
        boolean isAuthorized = user.getRoles().stream()
                .anyMatch(role -> role.getSlug().equalsIgnoreCase("admin-partners") || 
                                role.getSlug().equalsIgnoreCase("employee-partners"));
        
        boolean isStaff = !user.getRoles().stream().anyMatch(role -> role.getSlug().equalsIgnoreCase("admin"));

        if (!isAuthorized || !isStaff) {
            throw new RuntimeException("Akses Ditolak: Hanya Admin Partner atau Employee yang diizinkan.");
        }
        return user;
    }

    private boolean isAdmin(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getSlug().equals("super_admin") || role.getSlug().equals("admin"));
    }

    private boolean isAdminPartnerAndEmployee(User user) {
        // Cek slug super_admin atau admin (sesuaikan dengan seeder lo)
        return user.getRoles().stream()
                .anyMatch(role -> role.getSlug().equals("employee") || role.getSlug().equals("admin-partners"));
    }


    private TransferRequest getValidatedTransferRequest(Long id, User currentUser) {
        TransferRequest transferRequest = transferRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TransferRequest", id));

        if (currentUser.getPartner() == null ||
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

    public TransferRequestResponse mapToResponse(TransferRequest transferRequest) {

    // Partner DTO
    PartnerSimpleDto partnerDto = null;

    if (transferRequest.getPartner() != null) {
        partnerDto = new PartnerSimpleDto();
        partnerDto.setId(transferRequest.getPartner().getId());
        partnerDto.setName(transferRequest.getPartner().getName());
    }

    // Items Response
    List<TransferRequestResponse.TransferRequestItemResponse> itemResponses =
            transferRequestItemRepository
                    .findByTransferRequestId(transferRequest.getId())
                    .stream()
                    .map(this::mapItemToResponse)
                    .collect(Collectors.toList());

    return TransferRequestResponse.builder()
            .id(transferRequest.getId())
            .partner(partnerDto)
            .fromLocationType(transferRequest.getFromLocationType())
            .fromLocationId(transferRequest.getFromLocationId())
            .toLocationType(transferRequest.getToLocationType())
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
            .approvedBy(null)
            .receivedBy(null)
            .items(itemResponses)
            .build();
    }

    private TransferRequestResponse.TransferRequestItemResponse
    mapItemToResponse(TransferRequestItem item) {

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
        response.setQtyRequested(item.getQtyRequested());
        response.setQtyReceived(item.getQtyReceived());

        return response;
    }

    private UserSimpleDto mapUserToSimpleDto(User user) {

    if (user == null) {
        return null;
    }

    UserSimpleDto dto = new UserSimpleDto();
    dto.setId(user.getId());
    dto.setUsername(user.getUsername());

    return dto;
}
    // GET ALL
    public List<TransferRequest> findAll() {
        User currentUser = getAuthenticatedUser();
        return transferRequestRepository.findByPartnerIdAndDeletedAtIsNull(currentUser.getPartner().getId());
    }

    // GET ALL PAGINATED
    public Page<TransferRequestResponse> findAll(int page, int size) {
        User currentUser = getAuthenticatedUser();
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("id").descending());

        if (isAdmin(currentUser)) {
            return transferRequestRepository.findAll(pageRequest).map(this::mapToResponse);
        }

        if (currentUser.getPartner() == null) {
            throw new RuntimeException("User tidak terasosiasi dengan Partner.");
        }

        return transferRequestRepository.findByPartnerIdAndDeletedAtIsNull(currentUser.getPartner().getId(), pageRequest)
                .map(this::mapToResponse);
    }

    // GET BY ID
    public TransferRequestResponse findById(Long id) {
        User currentUser = getAuthenticatedUser();
        TransferRequest transferRequest = transferRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TransferRequest", id));

        if (!isAdmin(currentUser)) {
            if (currentUser.getPartner() == null ||
                !transferRequest.getPartner().getId().equals(currentUser.getPartner().getId())) {
                throw new RuntimeException("Akses Ditolak: Transfer request bukan milik partner Anda.");
            }
        }
        return mapToResponse(transferRequest);
    }

    // GET BY PARTNER
    public List<TransferRequest> findByPartnerId(Long partnerId) {
        User currentUser = getAuthenticatedUser();

        if (currentUser.getPartner() == null ||
                !currentUser.getPartner().getId().equals(partnerId)) {
            throw new RuntimeException("Akses Ditolak: Anda tidak bisa mengakses data partner lain.");
        }

        return transferRequestRepository.findByPartnerIdAndDeletedAtIsNull(partnerId);
    }

    // CREATE
    @Transactional
    public TransferRequestResponse create(TransferRequestDTO request) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();

        Partners partner = currentUser.getPartner();
        if (partner == null) {
            throw new RuntimeException("User ini tidak terasosiasi dengan Partner manapun.");
        }

        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setPartner(partner);
        transferRequest.setFromLocationType(request.getFromLocationType());
        transferRequest.setFromLocationId(request.getFromLocationId());
        transferRequest.setToLocationType(request.getToLocationType());
        transferRequest.setToLocationId(request.getToLocationId());
        transferRequest.setNotes(request.getNotes());
        transferRequest.setStatus(TransferRequest.Status.PENDING);
        transferRequest.setRequestedAt(LocalDateTime.now());

        TransferRequest saved = transferRequestRepository.save(transferRequest);

        List<TransferRequestItem> items = new ArrayList<>();
        for (TransferRequestItemDTO itemDTO : request.getItems()) {
            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", itemDTO.getProductId()));

            if (!product.getPartner().getId().equals(partner.getId())) {
                throw new RuntimeException("Akses Ditolak: Product bukan milik partner Anda.");
            }

            TransferRequestItem item = new TransferRequestItem();
            item.setTransferRequest(saved);
            item.setProduct(product);
            item.setQtyRequested(itemDTO.getQtyRequested());
            items.add(item);
        }
        transferRequestItemRepository.saveAll(items);
        transferRequest.setItems(items);

        return mapToResponse(transferRequest);
    }

    // UPDATE STATUS
    @Transactional
    public TransferRequestResponse updateStatus(Long id, String status) {
        User currentUser = getAuthenticatedUser();
        TransferRequest transferRequest = getValidatedTransferRequest(id, currentUser);

        TransferRequest.Status newStatus = TransferRequest.Status.valueOf(status.toUpperCase());
        transferRequest.setStatus(newStatus);

        if (newStatus == TransferRequest.Status.APPROVED) {
            transferRequest.setApprovedAt(LocalDateTime.now());
        } else if (newStatus == TransferRequest.Status.RECEIVED) {
            transferRequest.setReceivedAt(LocalDateTime.now());
        }

        return mapToResponse(transferRequestRepository.save(transferRequest));
    }

    // SOFT DELETE
    public void delete(Long id) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();
        TransferRequest transferRequest = getValidatedTransferRequest(id, currentUser);
        transferRequest.setDeletedAt(LocalDateTime.now());
        transferRequestRepository.save(transferRequest);
    }
}