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

        return user;
    }

    private User getAuthenticatedSuperAdmin(){
        User user = getAuthenticatedUser();
        boolean isSuperAdmin = user.getRoles().stream()
        .anyMatch(role -> role.getSlug().equalsIgnoreCase("admin"));
        if (!isSuperAdmin) throw new RuntimeException("Akses Di Tolak: Anda Bukan Super Admin"); 
        return user;
    }

    private User getAuthenticatedAdminPartnerOrEmployee(){
        User user = getAuthenticatedUser();
        boolean isAuthorized = user.getRoles().stream()
        .anyMatch(role -> role.getSlug().equalsIgnoreCase("admin")||
    role.getSlug().equalsIgnoreCase("employee"));

    boolean isStaff = !user.getRoles().stream().anyMatch(role -> role.getSlug().equalsIgnoreCase("admin"));
    if (!isAuthorized || !isStaff) {
        throw new RuntimeException("Akses Di Tolak: Hanya Admin Partner Atau Employee Yang Di Izinkan");
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
    public Page<StockMutation> findAll(int page, int size) {
        User currentUser = getAuthenticatedUser();
        return stockMutationRepository.findByPartnerId(currentUser.getPartner().getId(), PageRequest.of(page, size, Sort.by("createdAt").descending()));
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

        return stockMutationRepository.findByPartner(currentUser.getPartner());
    }

}