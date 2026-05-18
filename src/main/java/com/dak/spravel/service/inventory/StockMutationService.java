package com.dak.spravel.service.inventory;

import com.dak.spravel.dto.response.inventoryresponse.StockMutationResponse;
import com.dak.spravel.handler.ResourceNotFoundException;
import com.dak.spravel.model.auth.User;
import com.dak.spravel.model.catalog.Product;
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

import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.math.BigDecimal;
import com.dak.spravel.model.common.Partners;


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

        return user;
    }

    private User getAuthenticatedSuperAdmin(){
        User user = getAuthenticatedUser();
        boolean isSuperAdmin = user.getRoles().stream()
        .anyMatch(role -> role.getSlug().equalsIgnoreCase("admin"));
        if (!isSuperAdmin) throw new RuntimeException("Akses Di Tolak: Anda Bukan Super Admin"); 
        return user;
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
        return user.getRoles().stream()
                .anyMatch(role -> role.getSlug().equals("employee") || role.getSlug().equals("admin-partners"));
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

    // Map To Response
    public StockMutationResponse mapToResponse(StockMutation stockMutation) {
    if (stockMutation == null) return null;

    StockMutationResponse response = new StockMutationResponse();

    response.setId(stockMutation.getId());
    response.setQty(stockMutation.getQty());
    response.setType(stockMutation.getType());
    response.setNotes(stockMutation.getNotes());
    response.setReferenceId(stockMutation.getReferenceId());
    response.setReferenceType(stockMutation.getReferenceType());
    response.setFromLocationId(stockMutation.getFromLocationId());
    response.setFromLocationType(stockMutation.getFromLocationType());
    response.setToLocationId(stockMutation.getToLocationId());
    response.setToLocationType(stockMutation.getToLocationType());

    return response;
}


    public List<StockMutationResponse> findAllStockMutation() {
    getAuthenticatedSuperAdmin();

    return stockMutationRepository.findAll()
            .stream()
            .map(this::mapToResponse)
            .toList();
}

    // GET ALL
    public List<StockMutation> findAll() {
        User currentUser = getAuthenticatedUser();
        return stockMutationRepository.findByPartner(currentUser.getPartner());
    }

    // GET ALL PAGINATED
    public Page<StockMutationResponse> findAll(int page, int size) {
        User currentUser = getAuthenticatedUser();
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("id").descending());

        if (isAdmin(currentUser)) {
            return stockMutationRepository.findAll(pageRequest).map(this::mapToResponse);
        }

        if (currentUser.getPartner() == null) {
            throw new RuntimeException("User tidak terasosiasi dengan Partner.");
        }

        return stockMutationRepository.findByPartnerId(currentUser.getPartner().getId(), pageRequest)
                .map(this::mapToResponse);
    }

    // GET BY ID
    public StockMutationResponse findById(Long id) {
        User currentUser = getAuthenticatedUser();
        StockMutation mutation = stockMutationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StockMutation", id));

        if (!isAdmin(currentUser)) {
            if (currentUser.getPartner() == null ||
                !mutation.getPartner().getId().equals(currentUser.getPartner().getId())) {
                throw new RuntimeException("Akses Ditolak: Stock mutation bukan milik partner Anda.");
            }
        }
        return mapToResponse(mutation);
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

        return stockMutationRepository.findByPartner(currentUser.getPartner());
    }

    @Transactional
    public void recordMutation(Product product, Partners partner, String type, 
                              String fromType, Long fromId, 
                              String toType, Long toId, 
                              BigDecimal qty, String refType, Long refId, 
                              String notes, User user) {
        StockMutation mutation = new StockMutation();
        mutation.setProduct(product);
        mutation.setPartner(partner);
        mutation.setType(type);
        mutation.setFromLocationType(fromType);
        mutation.setFromLocationId(fromId);
        mutation.setToLocationType(toType);
        mutation.setToLocationId(toId);
        mutation.setQty(qty);
        mutation.setReferenceType(refType);
        mutation.setReferenceId(refId);
        mutation.setNotes(notes);
        mutation.setCreatedBy(user);
        stockMutationRepository.save(mutation);
    }
}