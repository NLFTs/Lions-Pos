package com.dak.spravel.service.inventory;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dak.spravel.dto.response.inventoryresponse.StockMutationResponse;
import com.dak.spravel.handler.ResourceNotFoundException;
import com.dak.spravel.model.auth.User;
import com.dak.spravel.model.catalog.Product;
import com.dak.spravel.model.common.Partners;
import com.dak.spravel.model.inventory.StockMutation;
import com.dak.spravel.repository.auth.UserRepository;
import com.dak.spravel.repository.catalog.ProductRepository;
import com.dak.spravel.repository.inventory.StockMutationRepository;

import lombok.RequiredArgsConstructor;

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
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan di database"));
    }

    private User getAuthenticatedSuperAdmin(){
        User user = getAuthenticatedUser();
        boolean isSuperAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getSlug().equalsIgnoreCase("admin") || role.getSlug().equalsIgnoreCase("super_admin"));
        if (!isSuperAdmin) throw new RuntimeException("Akses Ditolak: Anda Bukan Super Admin"); 
        return user;
    }

    // 🛠️ HELPER TAMBAHAN: Validasi agar Admin Partner dan Employee murni bisa lolos barengan
    private User getAuthenticatedAdminPartnerOrEmployee() {
        User user = getAuthenticatedUser();
        boolean isAuthorized = user.getRoles().stream()
                .anyMatch(role -> role.getSlug().equalsIgnoreCase("admin-partners") ||
                        role.getSlug().equalsIgnoreCase("employee-partners") ||
                        role.getSlug().equalsIgnoreCase("employee"));
        boolean isNotSuperAdmin = user.getRoles().stream()
                .noneMatch(role -> role.getSlug().equalsIgnoreCase("admin") || role.getSlug().equalsIgnoreCase("super_admin"));
        
        if (!isAuthorized || !isNotSuperAdmin) {
            throw new RuntimeException("Akses Ditolak: Hanya Admin Partner atau Employee yang diizinkan.");
        }
        return user;
    }

    private boolean isAdmin(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getSlug().equals("super_admin") || role.getSlug().equals("admin"));
    }

    // 🔥 MAP TO RESPONSE
    public StockMutationResponse mapToResponse(StockMutation stockMutation) {
        if (stockMutation == null) return null;

        StockMutationResponse response = new StockMutationResponse();
        response.setId(stockMutation.getId());
        response.setQty(stockMutation.getQty());
        response.setNotes(stockMutation.getNotes());
        response.setReferenceId(stockMutation.getReferenceId());
        
        if (stockMutation.getType() != null) response.setType(stockMutation.getType().name());
        if (stockMutation.getReferenceType() != null) response.setReferenceType(stockMutation.getReferenceType().name());
        
        response.setFromLocationId(stockMutation.getFromLocationId());
        if (stockMutation.getFromLocationType() != null) {
            response.setFromLocationType(stockMutation.getFromLocationType().name());
        }
        
        response.setToLocationId(stockMutation.getToLocationId());
        if (stockMutation.getToLocationType() != null) {
            response.setToLocationType(stockMutation.getToLocationType().name());
        }

        return response;
    }

    // GET ALL FOR SUPER ADMIN
    public List<StockMutationResponse> findAllStockMutation() {
        getAuthenticatedSuperAdmin();
        return stockMutationRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    // 🔄 GET ALL BY PARTNER (Bisa diakses Employee, otomatis terfilter milik Partner-nya)
    public List<StockMutationResponse> findAll() {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();
        return stockMutationRepository.findByPartner(currentUser.getPartner()).stream()
                .map(this::mapToResponse)
                .toList();
    }

    // 🔄 GET ALL PAGINATED
    public Page<StockMutationResponse> findAll(int page, int size) {
        User currentUser = getAuthenticatedUser();
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("id").descending());

        if (isAdmin(currentUser)) {
            return stockMutationRepository.findAll(pageRequest).map(this::mapToResponse);
        }

        // Jalur operasional Admin Partner & Employee
        User activeUser = getAuthenticatedAdminPartnerOrEmployee();
        if (activeUser.getPartner() == null) {
            throw new RuntimeException("User tidak terasosiasi dengan Partner.");
        }

        return stockMutationRepository.findByPartnerId(activeUser.getPartner().getId(), pageRequest)
                .map(this::mapToResponse);
    }

    // 🔄 GET BY ID
    public StockMutationResponse findById(Long id) {
        User currentUser = getAuthenticatedUser();
        StockMutation mutation = stockMutationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StockMutation", id));

        if (!isAdmin(currentUser)) {
            User activeUser = getAuthenticatedAdminPartnerOrEmployee();
            if (activeUser.getPartner() == null ||
                !mutation.getPartner().getId().equals(activeUser.getPartner().getId())) {
                throw new RuntimeException("Akses Ditolak: Stock mutation bukan milik partner Anda.");
            }
        }
        return mapToResponse(mutation);
    }

    // 🔄 GET BY PRODUCT
    public List<StockMutationResponse> findByProductId(Long productId) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));

        if (currentUser.getPartner() == null ||
                !product.getPartner().getId().equals(currentUser.getPartner().getId())) {
            throw new RuntimeException("Akses Ditolak: Product bukan milik partner Anda.");
        }

        return stockMutationRepository.findByProductId(productId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    // 🔄 GET BY PARTNER ID
    public List<StockMutationResponse> findByPartnerId(Long partnerId) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();

        if (currentUser.getPartner() == null ||
                !currentUser.getPartner().getId().equals(partnerId)) {
            throw new RuntimeException("Akses Ditolak: Anda tidak bisa mengakses data partner lain.");
        }

        return stockMutationRepository.findByPartner(currentUser.getPartner()).stream()
                .map(this::mapToResponse)
                .toList();
    }

    // ⚙️ RECORD MUTATION (Biarkan berjalan internal, tidak diblokir untuk employee karena dipicu oleh system flow)
    @Transactional
    public void recordMutation(Product product, Partners partner, String type, 
                              String fromType, Long fromId, 
                              String toType, Long toId, 
                              Long qty, String refType, Long refId, 
                              String notes, User user) {
        StockMutation mutation = new StockMutation();
        mutation.setProduct(product);
        mutation.setPartner(partner);
        
        mutation.setType(StockMutation.Type.valueOf(type.toUpperCase()));
        
        mutation.setFromLocationType(fromType != null ? StockMutation.Location.valueOf(fromType.toUpperCase()) : null);
        mutation.setFromLocationId(fromId);
        
        if (toType != null) {
            mutation.setToLocationType(StockMutation.Location.valueOf(toType.toUpperCase()));
        }
        mutation.setToLocationId(toId);
        
        mutation.setQty(qty); // 🛠️ FIX: Sebelumya terisi refId, diganti jadi parameter qty yang benar
        mutation.setReferenceType(StockMutation.ReferenceType.valueOf(refType.toUpperCase()));
        mutation.setReferenceId(refId);
        mutation.setNotes(notes);
        mutation.setCreatedBy(user);
        
        stockMutationRepository.save(mutation);
    }
}