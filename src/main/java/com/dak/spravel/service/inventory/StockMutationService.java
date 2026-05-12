package com.dak.spravel.service.inventory;

import com.dak.spravel.dto.request.inventory.StockMutationRequestDTO;
import com.dak.spravel.handler.ResourceNotFoundException;
import com.dak.spravel.model.auth.User;
import com.dak.spravel.model.catalog.Product;
import com.dak.spravel.model.common.Partners;
import com.dak.spravel.model.inventory.StockMutation;
import com.dak.spravel.repository.auth.UserRepository;
import com.dak.spravel.repository.catalog.ProductRepository;
import com.dak.spravel.repository.inventory.StockMutationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockMutationService {

    private final StockMutationRepository stockMutationRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new RuntimeException("User tidak terautentikasi");
        }

        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan di database"));

        if (isAdmin(user)) {
            throw new RuntimeException("Akses Ditolak: Admin tidak diperbolehkan mengelola Stock Mutation.");
        }

        return user;
    }

    private boolean isAdmin(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getSlug().equals("super_admin") || role.getSlug().equals("admin"));
    }

    private StockMutation getValidatedMutation(Long id, User currentUser) {
        StockMutation mutation = stockMutationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StockMutation", id));
        if (currentUser.getPartner() == null ||
                !mutation.getPartner().getId().equals(currentUser.getPartner().getId())) {
            throw new RuntimeException("Akses Ditolak: Stock mutation bukan milik partner Anda.");
        }

        return mutation;
    }

    // GET ALL
    public List<StockMutation> findAll() {
        User currentUser = getAuthenticatedUser();
        return stockMutationRepository.findByPartnerId(currentUser.getPartner().getId());
    }

    // GET ALL PAGINATED
    public Page<StockMutation> findAll(int page, int size) {
        User currentUser = getAuthenticatedUser();
        return stockMutationRepository.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending()));
    }

    // GET BY ID
    public StockMutation findById(Long id) {
        User currentUser = getAuthenticatedUser();
        return getValidatedMutation(id, currentUser);
    }

    // GET BY PRODUCT
    public List<StockMutation> findByProductId(Long productId) {
        User currentUser = getAuthenticatedUser();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));

        if (currentUser.getPartner() == null ||
                !product.getPartner().getId().equals(currentUser.getPartner().getId())) {
            throw new RuntimeException("Akses Ditolak: Product bukan milik partner Anda.");
        }

        return stockMutationRepository.findByProductId(productId);
    }

    // GET BY PARTNER
    public List<StockMutation> findByPartnerId(Long partnerId) {
        User currentUser = getAuthenticatedUser();

        if (currentUser.getPartner() == null ||
                !currentUser.getPartner().getId().equals(partnerId)) {
            throw new RuntimeException("Akses Ditolak: Anda tidak bisa mengakses data partner lain.");
        }

        return stockMutationRepository.findByPartnerId(partnerId);
    }

    // CREATE
    public StockMutation create(StockMutationRequestDTO request) {
        User currentUser = getAuthenticatedUser();

        Partners partner = currentUser.getPartner();
        if (partner == null) {
            throw new RuntimeException("User ini tidak terasosiasi dengan Partner manapun.");
        }

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", request.getProductId()));

        if (!product.getPartner().getId().equals(partner.getId())) {
            throw new RuntimeException("Akses Ditolak: Product bukan milik partner Anda.");
        }

        StockMutation mutation = new StockMutation();
        mutation.setProduct(product);
        mutation.setPartner(partner);
        mutation.setType(request.getType());
        mutation.setFromLocationType(request.getFromLocationType());
        mutation.setFromLocationId(request.getFromLocationId());
        mutation.setToLocationType(request.getToLocationType());
        mutation.setToLocationId(request.getToLocationId());
        mutation.setQty(request.getQty());
        mutation.setReferenceType(request.getReferenceType());
        mutation.setReferenceId(request.getReferenceId());
        mutation.setNotes(request.getNotes());

        return stockMutationRepository.save(mutation);
    }

    // DELETE
    public void delete(Long id) {
        User currentUser = getAuthenticatedUser();
        StockMutation mutation = getValidatedMutation(id, currentUser);
        stockMutationRepository.deleteById(mutation.getId());
    }
}