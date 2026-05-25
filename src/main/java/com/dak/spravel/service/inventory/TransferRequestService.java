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
import java.util.stream.Collectors;
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

    // =========================
    // AUTH HELPERS (STANDARD)
    // =========================

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new RuntimeException("User tidak terautentikasi");
        }

        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan di database"));
    }

    private User getAuthenticatedOwner() {
        User user = getAuthenticatedUser();

        boolean isOwner = user.getRoles().stream()
                .anyMatch(r -> r.getSlug().equalsIgnoreCase("owner"));

        if (!isOwner) {
            throw new RuntimeException("Akses Ditolak: hanya Owner yang diizinkan");
        }

        return user;
    }

    private User getAuthenticatedAdmin() {
        User user = getAuthenticatedUser();

        boolean isAdmin = user.getRoles().stream()
                .anyMatch(r -> r.getSlug().equalsIgnoreCase("admin")
                        || r.getSlug().equalsIgnoreCase("super_admin"));

        if (!isAdmin) {
            throw new RuntimeException("Akses Ditolak: hanya Admin yang diizinkan");
        }

        return user;
    }

    private boolean isEmployee(User user) {
        return user.getRoles().stream()
                .anyMatch(r -> r.getSlug().equalsIgnoreCase("employee"));
    }

    private boolean isBranchAccessible(
            TransferRequest.Location type,
            Long locationId,
            User user
    ) {
        return user.getBranch() != null
                && type == TransferRequest.Location.BRANCH
                && user.getBranch().getId().equals(locationId);
    }

    // =========================
    // VALIDATION HELPER
    // =========================

    private TransferRequest getValidatedTransferRequest(Long id, User user) {
        TransferRequest tr = transferRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TransferRequest", id));

        if (user.getPartner() == null ||
                !tr.getPartner().getId().equals(user.getPartner().getId())) {
            throw new RuntimeException("Akses Ditolak: bukan milik partner Anda");
        }

        return tr;
    }

    // =========================
    // FIND ALL (ADMIN)
    // =========================

    public List<TransferRequestResponse> findAllAdmin() {
        getAuthenticatedAdmin();

        return transferRequestRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // =========================
    // FIND ALL (OWNER)
    // =========================

    public List<TransferRequest> findAll() {
        User user = getAuthenticatedUser();

        if (user.getPartner() == null) {
            throw new RuntimeException("User tidak terasosiasi dengan Partner");
        }

        List<TransferRequest> data =
                transferRequestRepository.findByPartnerIdAndDeletedAtIsNull(
                        user.getPartner().getId()
                );

        if (isEmployee(user)) {
            Long branchId = user.getBranch().getId();

            data = data.stream()
                    .filter(tr ->
                            isBranchAccessible(tr.getFromLocationType(), tr.getFromLocationId(), user) ||
                                    isBranchAccessible(tr.getToLocationType(), tr.getToLocationId(), user)
                    )
                    .toList();
        }

        return data;
    }

    // =========================
    // FIND BY ID
    // =========================

    public TransferRequestResponse findById(Long id) {
        User user = getAuthenticatedUser();

        TransferRequest tr = transferRequestRepository.findByIdWithItems(id)
                .orElseThrow(() -> new ResourceNotFoundException("TransferRequest", id));

        if (!isAuthenticatedOwnerAccess(tr, user)) {
            throw new RuntimeException("Akses Ditolak");
        }

        if (isEmployee(user)) {
            boolean access =
                    isBranchAccessible(tr.getFromLocationType(), tr.getFromLocationId(), user) ||
                            isBranchAccessible(tr.getToLocationType(), tr.getToLocationId(), user);

            if (!access) {
                throw new RuntimeException("Akses Ditolak: bukan branch Anda");
            }
        }

        return mapToResponse(tr);
    }

    private boolean isAuthenticatedOwnerAccess(TransferRequest tr, User user) {
        return user.getPartner() != null &&
                tr.getPartner().getId().equals(user.getPartner().getId());
    }

    // =========================
    // CREATE
    // =========================

    @Transactional
    public TransferRequestResponse create(TransferRequestDTO request) {
        User user = getAuthenticatedOwner();

        Partners partner = user.getPartner();

        TransferRequest tr = new TransferRequest();
        tr.setPartner(partner);

        // from
        Long fromId = request.getFromLocationId();

        if (warehousesRepository.existsById(fromId)) {
            tr.setFromLocationType(TransferRequest.Location.WAREHOUSE);
        } else if (branchesRepository.existsById(fromId)) {
            tr.setFromLocationType(TransferRequest.Location.BRANCH);
        } else {
            throw new RuntimeException("From location tidak valid");
        }
        tr.setFromLocationId(fromId);

        // to
        Long toId = request.getToLocationId();

        if (warehousesRepository.existsById(toId)) {
            tr.setToLocationType(TransferRequest.Location.WAREHOUSE);
        } else if (branchesRepository.existsById(toId)) {
            tr.setToLocationType(TransferRequest.Location.BRANCH);
        } else {
            throw new RuntimeException("To location tidak valid");
        }
        tr.setToLocationId(toId);

        tr.setStatus(TransferRequest.Status.PENDING);
        tr.setRequestedAt(LocalDateTime.now());
        tr.setCreatedAt(LocalDateTime.now());
        tr.setCreatedBy(user);
        tr.setNotes(request.getNotes());

        return mapToResponse(transferRequestRepository.save(tr));
    }

    // =========================
    // RECEIVE TRANSFER
    // =========================

    @Transactional
    public TransferRequestResponse receiveTransfer(
            Long id,
            List<TransferRequestItemDTO> items
    ) {
        User user = getAuthenticatedOwner();

        TransferRequest tr = transferRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Not found"));

        tr.setStatus(TransferRequest.Status.RECEIVED);
        tr.setReceivedAt(LocalDateTime.now());
        tr.setReceivedByUser(user);

        for (TransferRequestItem item : tr.getItems()) {

            Long qty = items.stream()
                    .filter(i -> i.getProductId().equals(item.getProduct().getId()))
                    .map(i -> i.getQtyRequested().longValue())
                    .findFirst()
                    .orElse(item.getQtyRequested().longValue());

            item.setQtyReceived(qty);

            stockBalanceService.adjustStock(
                    item.getProduct().getId(),
                    tr.getFromLocationType().name(),
                    tr.getFromLocationId(),
                    -qty
            );

            stockBalanceService.adjustStock(
                    item.getProduct().getId(),
                    tr.getToLocationType().name(),
                    tr.getToLocationId(),
                    qty
            );

            stockMutationService.recordMutation(
                    item.getProduct(),
                    tr.getPartner(),
                    "TRANSFER",
                    tr.getFromLocationType().name().toLowerCase(),
                    tr.getFromLocationId(),
                    tr.getToLocationType().name().toLowerCase(),
                    tr.getToLocationId(),
                    qty,
                    "transfer_request",
                    tr.getId(),
                    "Transfer diterima",
                    user
            );
        }

        transferRequestItemRepository.saveAll(tr.getItems());

        return mapToResponse(transferRequestRepository.save(tr));
    }

    // =========================
    // DELETE
    // =========================

    @Transactional
    public void delete(Long id) {
        User user = getAuthenticatedOwner();

        TransferRequest tr = getValidatedTransferRequest(id, user);

        tr.setDeletedAt(LocalDateTime.now());
        tr.setDeletedBy(user);

        transferRequestRepository.save(tr);
    }

    // =========================
    // STATUS UPDATE
    // =========================

    @Transactional
    public TransferRequestResponse updateStatus(Long id, String status) {
        User user = getAuthenticatedOwner();

        TransferRequest tr = getValidatedTransferRequest(id, user);

        tr.setStatus(TransferRequest.Status.valueOf(status.toUpperCase()));
        tr.setUpdatedAt(LocalDateTime.now());
        tr.setUpdatedBy(user);

        return mapToResponse(transferRequestRepository.save(tr));
    }

    public TransferRequestResponse mapToResponse(TransferRequest tr) {
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
