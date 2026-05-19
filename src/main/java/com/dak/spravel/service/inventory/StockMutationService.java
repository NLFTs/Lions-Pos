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

    private boolean isAdmin(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getSlug().equals("super_admin") || role.getSlug().equals("admin"));
    }

    // 🔥 MAP TO RESPONSE (Sudah fix Type Mismatch Enum & Aman dari NullPointer)
    public StockMutationResponse mapToResponse(StockMutation stockMutation) {
        if (stockMutation == null) return null;

        StockMutationResponse response = new StockMutationResponse();
        response.setId(stockMutation.getId());
        response.setQty(stockMutation.getQty());
        response.setNotes(stockMutation.getNotes());
        response.setReferenceId(stockMutation.getReferenceId());
        
        // Amankan convert Enum ke String memakai .name()
        if (stockMutation.getType() != null) response.setType(stockMutation.getType().name());
        if (stockMutation.getReferenceType() != null) response.setReferenceType(stockMutation.getReferenceType().name());
        
        // fromLocationType berbentuk String di Entity
        response.setFromLocationId(stockMutation.getFromLocationId());
        response.setFromLocationType(stockMutation.getFromLocationType().name());
        
        // toLocationType berbentuk Enum Location di Entity, convert ke String DTO
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

    // GET ALL BY PARTNER (Sudah diconvert ke DTO Response biar gak error di Controller)
    public List<StockMutationResponse> findAll() {
        User currentUser = getAuthenticatedUser();
        return stockMutationRepository.findByPartner(currentUser.getPartner()).stream()
                .map(this::mapToResponse)
                .toList();
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

    // GET BY PRODUCT (Sudah dikonversi ke DTO Response)
    public List<StockMutationResponse> findByProductId(Long productId) {
        User currentUser = getAuthenticatedUser();

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

    // GET BY PARTNER ID (Sudah dikonversi ke DTO Response)
    public List<StockMutationResponse> findByPartnerId(Long partnerId) {
        User currentUser = getAuthenticatedUser();

        if (currentUser.getPartner() == null ||
                !currentUser.getPartner().getId().equals(partnerId)) {
            throw new RuntimeException("Akses Ditolak: Anda tidak bisa mengakses data partner lain.");
        }

        return stockMutationRepository.findByPartner(currentUser.getPartner()).stream()
                .map(this::mapToResponse)
                .toList();
    }

    // 🔥 RECORD MUTATION (Sudah fix kebal dari case-sensitive crash & aman tipe data)
    @Transactional
    public void recordMutation(Product product, Partners partner, String type, 
                              String fromType, Long fromId, 
                              String toType, Long toId, 
                              Long qty, String refType, Long refId, 
                              String notes, User user) {
        StockMutation mutation = new StockMutation();
        mutation.setProduct(product);
        mutation.setPartner(partner);
        
        // .toUpperCase() mengamankan dari typo input huruf kecil pemicu crash
        mutation.setType(StockMutation.Type.valueOf(type.toUpperCase()));
        
        // fromLocationType di entity berupa String mentah
        mutation.setFromLocationType(fromType != null ? StockMutation.Location.valueOf(fromType.toUpperCase()) : null);
        mutation.setFromLocationId(fromId);
        
        // toLocationType di entity berupa Enum StockMutation.Location, wajib diconvert khusus
        if (toType != null) {
            mutation.setToLocationType(StockMutation.Location.valueOf(toType.toUpperCase()));
        }
        mutation.setToLocationId(toId);
        
        mutation.setQty(refId);
        mutation.setReferenceType(StockMutation.ReferenceType.valueOf(refType.toUpperCase()));
        mutation.setReferenceId(refId);
        mutation.setNotes(notes);
        mutation.setCreatedBy(user);
        
        stockMutationRepository.save(mutation);
    }
}