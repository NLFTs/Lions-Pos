package com.dak.spravel.service.catalog;

import com.dak.spravel.dto.request.product.VoucherRequest;
import com.dak.spravel.handler.ResourceNotFoundException;
import com.dak.spravel.model.auth.User;
import com.dak.spravel.model.catalog.Voucher;
import com.dak.spravel.model.common.Partners;
import com.dak.spravel.repository.auth.UserRepository;
import com.dak.spravel.repository.catalog.VoucherRepository;
import com.dak.spravel.util.AuditHelper;
import java.security.SecureRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VoucherService {

    private final VoucherRepository voucherRepository;
    private final UserRepository userRepository;

    // Helper: Ambil User & Block Admin
    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new RuntimeException("User tidak terautentikasi");
        }
        
        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan di database"));

        // VALIDASI: Admin dilarang masuk
        boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getSlug().equals("super_admin") || role.getSlug().equals("admin"));
        
        if (isAdmin) {
            throw new RuntimeException("Akses Ditolak: Admin tidak diperbolehkan mengelola Voucher.");
        }

        return user;
    }

    // Helper: Validasi Kepemilikan Voucher
    private Voucher getValidatedVoucher(Long id, User currentUser) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher", id));

        // Cross-Partner Check
        if (currentUser.getPartner() == null || 
            !voucher.getPartner().getId().equals(currentUser.getPartner().getId())) {
            throw new RuntimeException("Akses Ditolak: Voucher ini milik partner lain.");
        }
        
        return voucher;
    }

    @Transactional
    public Voucher create(VoucherRequest request) {
        User currentUser = getAuthenticatedUser();
        Partners partner = currentUser.getPartner();

        if (partner == null) {
            throw new RuntimeException("User tidak memiliki Partner.");
        }

        // Cek kode voucher (harus unik global atau per partner, biasanya global)
        if (request.getCode() != null && voucherRepository.existsByCode(request.getCode().trim())) {
            throw new RuntimeException("Voucher code already exists");
        }

        validateRequest(request);

        Voucher voucher = new Voucher();
        mapToEntity(voucher, request, partner);
        AuditHelper.setCreated(voucher);

        return voucherRepository.save(voucher);
    }

    @Transactional
    public Voucher update(Long id, VoucherRequest request) {
        User currentUser = getAuthenticatedUser();
        Voucher voucher = getValidatedVoucher(id, currentUser);

        validateRequest(request);

        // Jika ganti kode, cek keunikan
        if (request.getCode() != null && !voucher.getCode().equals(request.getCode().trim())
                && voucherRepository.existsByCode(request.getCode().trim())) {
            throw new RuntimeException("Voucher code already exists");
        }

        mapToEntity(voucher, request, currentUser.getPartner());
        AuditHelper.setUpdated(voucher);
                
        return voucherRepository.save(voucher);
    }

    public Voucher getById(Long id) {
        User currentUser = getAuthenticatedUser();
        return getValidatedVoucher(id, currentUser);
    }

    public List<Voucher> getAll() {
        User currentUser = getAuthenticatedUser();
        // Hanya ambil voucher milik partner si user
        return voucherRepository.findAllByPartner(currentUser.getPartner());
    }

    @Transactional
    public void delete(Long id) {
        User currentUser = getAuthenticatedUser();
        Voucher voucher = getValidatedVoucher(id, currentUser);
        
        AuditHelper.setDeleted(voucher);
        voucherRepository.delete(voucher);
    }

    // --- PRIVATE UTILS ---

    private void mapToEntity(Voucher voucher, VoucherRequest request, Partners partner) {
        voucher.setPartner(partner);
        
        // Handle Random Code jika kosong
        if(request.getCode() == null || request.getCode().trim().isEmpty()) {
            if (voucher.getCode() == null) { // Hanya generate jika voucher baru (create)
                String randomCode = generateRandomVoucherCode(10);
                while (voucherRepository.existsByCode(randomCode)) {
                    randomCode = generateRandomVoucherCode(10);
                }
                voucher.setCode(randomCode);
            }
        } else {
            voucher.setCode(request.getCode().trim());
        }

        voucher.setName(request.getName());
        voucher.setDiscountType(Voucher.DiscountType.valueOf(request.getDiscountType().toUpperCase()));
        voucher.setDiscountValue(request.getDiscountValue());
        voucher.setMinPurchase(request.getMinPurchase());
        voucher.setMaxDiscount(request.getMaxDiscount());
        voucher.setQuota(request.getQuota());
        voucher.setValidFrom(request.getValidFrom());
        voucher.setValidUntil(request.getValidUntil());
    }

    private void validateRequest(VoucherRequest request) {
        if (request.getDiscountType() == null) throw new RuntimeException("Discount type is required");
        if (request.getDiscountValue() == null) throw new RuntimeException("Discount value is required");

        if (request.getValidFrom() != null && request.getValidUntil() != null) {
            if (request.getValidFrom().isAfter(request.getValidUntil())) {
                throw new RuntimeException("validFrom cannot be after validUntil");
            }
        }
    }

    private String generateRandomVoucherCode(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder result = new StringBuilder();
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < length; i++) {
            result.append(characters.charAt(random.nextInt(characters.length())));
        }
        return result.toString();
    }
}