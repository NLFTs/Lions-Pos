package com.dak.spravel.service.inventory;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    // ─── 🔒 PUSAT VALIDASI AUTH & PERMISSION (MURNI DINAMIS) ───────────────────

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new RuntimeException("User tidak terautentikasi");
        }
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan di database"));
    }

    // 🔥 KUNCI DINAMIS: Check permission dinamis dari database tanpa kaku nge-lock nama role
    private void checkPermission(User user, String permissionSlug) {
        // 👑 Raja Super Admin (partner null) bypass seluruh jenis gate permission
        if (user.getPartner() == null) {
            return;
        }

        boolean hasPerm = user.getRoles().stream()
                .filter(role -> role.getPermissions() != null)
                .flatMap(role -> role.getPermissions().stream())
                .anyMatch(perm -> perm.getSlug().equalsIgnoreCase(permissionSlug));

        if (!hasPerm) {
            throw new RuntimeException("Akses Ditolak: Anda tidak memiliki hak akses '" + permissionSlug + "'!");
        }
    }

    private void checkSuperAdminOnly(User user) {
        if (user.getPartner() != null) {
            throw new RuntimeException("Akses Ditolak: Fitur ini khusus Super Admin Global.");
        }
    }

    // ─── 🔄 MAP TO RESPONSE ───────────────────────────────────────────────────

    public StockMutationResponse mapToResponse(StockMutation stockMutation) {
        if (stockMutation == null) return null;

        StockMutationResponse response = new StockMutationResponse();
        response.setId(stockMutation.getId());
        response.setQty(stockMutation.getQty());
        response.setNotes(stockMutation.getNotes());
        response.setReferenceId(stockMutation.getReferenceId());

        if (stockMutation.getType() != null) response.setType(stockMutation.getType().name());
        if (stockMutation.getReferenceType() != null) {
            response.setReferenceType(stockMutation.getReferenceType().name());
        }

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

    // ─── 🚀 MAIN METHODSCORE (SUDAH DISERAGAMKAN POLANYA) ──────────────────────

    // KHUSUS SUPER ADMIN GLOBAL

    public List<StockMutationResponse> findAllStockMutation() {
        User currentUser = getAuthenticatedUser();
        checkSuperAdminOnly(currentUser);

        return stockMutationRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    // OPERASIONAL TENANT / PARTNER (BERBASIS PERMISSION SLUG)

    public List<StockMutationResponse> findAll() {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "stock_mutation.index"); // 💡 Saring via permission index

        if (currentUser.getPartner() == null) {
            return stockMutationRepository.findAll().stream().map(this::mapToResponse).toList();
        }

        return stockMutationRepository.findByPartner(currentUser.getPartner()).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public Page<StockMutationResponse> findAll(int page, int size) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "stock_mutation.index");

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        if (currentUser.getPartner() == null) {
            return stockMutationRepository.findAll(pageable).map(this::mapToResponse);
        }

        return stockMutationRepository.findByPartnerId(currentUser.getPartner().getId(), pageable)
                .map(this::mapToResponse);
    }

    public StockMutationResponse findById(Long id) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "stock_mutation.show");

        StockMutation mutation = stockMutationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StockMutation", id));

        // 🛡️ Multi-Tenant Guard Check
        if (currentUser.getPartner() != null) {
            if (mutation.getPartner() == null || !mutation.getPartner().getId().equals(currentUser.getPartner().getId())) {
                throw new RuntimeException("Akses Ditolak: Stock mutation bukan milik partner Anda.");
            }
        }

        return mapToResponse(mutation);
    }

    public List<StockMutationResponse> findByProductId(Long productId) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "stock_mutation.show");

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));

        // 🛡️ Multi-Tenant Guard Check
        if (currentUser.getPartner() != null) {
            if (product.getPartner() == null || !product.getPartner().getId().equals(currentUser.getPartner().getId())) {
                throw new RuntimeException("Akses Ditolak: Product bukan milik partner Anda.");
            }
        }

        return stockMutationRepository.findByProductId(productId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<StockMutationResponse> findByPartnerId(Long partnerId) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "stock_mutation.index");

        // 🛡️ Multi-Tenant Guard Check (Super Admin boleh bebas lolos nyari target partner manapun)
        if (currentUser.getPartner() != null && !currentUser.getPartner().getId().equals(partnerId)) {
            throw new RuntimeException("Akses Ditolak: Anda tidak bisa mengakses data partner lain.");
        }

        return stockMutationRepository.findByPartnerId(partnerId, PageRequest.of(0, 1000, Sort.by("id").descending()))
                .getContent().stream()
                .map(this::mapToResponse)
                .toList();
    }

    // ⚙️ INTERNAL MUTATION RECORD SYSTEM (Dipicu otomatis dari transaksi gudang/cabang/kasir)
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
        mutation.setQty(qty);
        mutation.setReferenceType(StockMutation.ReferenceType.valueOf(refType.toUpperCase()));
        mutation.setReferenceId(refId);
        mutation.setNotes(notes);
        mutation.setCreatedBy(user);
        stockMutationRepository.save(mutation);
    }
}