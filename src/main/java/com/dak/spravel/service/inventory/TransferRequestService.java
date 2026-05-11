package com.dak.spravel.service.inventory;

import com.dak.spravel.dto.request.inventory.TransferRequestDTO;
import com.dak.spravel.dto.request.inventory.TransferRequestItemDTO;
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

@Service
@RequiredArgsConstructor
public class TransferRequestService {

    private final TransferRequestRepository transferRequestRepository;
    private final TransferRequestItemRepository transferRequestItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    // AUTH
    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new RuntimeException("User tidak terautentikasi");
        }

        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan di database"));

        if (isAdmin(user)) {
            throw new RuntimeException("Akses Ditolak: Admin tidak diperbolehkan mengelola Transfer Request.");
        }

        return user;
    }

    private boolean isAdmin(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getSlug().equals("super_admin") || role.getSlug().equals("admin"));
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

    // GET ALL
    public List<TransferRequest> findAll() {
        User currentUser = getAuthenticatedUser();
        return transferRequestRepository.findByPartnerIdAndDeletedAtIsNull(
                currentUser.getPartner().getId()
        );
    }

    // PAGINATED
    public Page<TransferRequest> findAll(int page, int size) {
        return transferRequestRepository.findAll(
                PageRequest.of(page, size, Sort.by("createdAt").descending())
        );
    }

    // GET BY ID
    public TransferRequest findById(Long id) {
        User currentUser = getAuthenticatedUser();
        return getValidatedTransferRequest(id, currentUser);
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
    public TransferRequest create(TransferRequestDTO request) {

        User currentUser = getAuthenticatedUser();

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
        saved.setItems(items);

        return saved;
    }

    // UPDATE STATUS
    @Transactional
    public TransferRequest updateStatus(Long id, String status) {

        User currentUser = getAuthenticatedUser();
        TransferRequest transferRequest = getValidatedTransferRequest(id, currentUser);

        TransferRequest.Status newStatus =
                TransferRequest.Status.valueOf(status.toUpperCase());

        transferRequest.setStatus(newStatus);

        if (newStatus == TransferRequest.Status.APPROVED) {
            transferRequest.setApprovedAt(LocalDateTime.now());
        } else if (newStatus == TransferRequest.Status.RECEIVED) {
            transferRequest.setReceivedAt(LocalDateTime.now());
        }

        return transferRequestRepository.save(transferRequest);
    }

    // DELETE (SOFT DELETE)
    public void delete(Long id) {

        User currentUser = getAuthenticatedUser();
        TransferRequest transferRequest = getValidatedTransferRequest(id, currentUser);

        transferRequest.setDeletedAt(LocalDateTime.now());
        transferRequestRepository.save(transferRequest);
    }
}