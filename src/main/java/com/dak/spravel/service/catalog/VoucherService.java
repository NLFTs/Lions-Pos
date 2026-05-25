package com.dak.spravel.service.catalog;

import java.security.SecureRandom;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dak.spravel.dto.request.product.VoucherRequest;
import com.dak.spravel.dto.response.catalogresponse.VoucherResponse;
import com.dak.spravel.dto.response.components.PartnerSimpleDto;
import com.dak.spravel.dto.response.components.UserSimpleDto;
import com.dak.spravel.handler.ResourceNotFoundException;
import com.dak.spravel.model.auth.User;
import com.dak.spravel.model.catalog.Voucher;
import com.dak.spravel.model.common.Partners;
import com.dak.spravel.repository.auth.UserRepository;
import com.dak.spravel.repository.catalog.VoucherRepository;
import com.dak.spravel.util.AuditHelper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VoucherService {

    private final VoucherRepository voucherRepository;
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

    // ─── 🛡️ MULTI-TENANT GUARD (ANTI NULL POINTER UNTUK SUPER ADMIN) ───────────

    private Voucher getValidatedVoucher(Long id, User currentUser) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher", id));

        // 👑 Super Admin global bebas bypass pengecekan tenant id
        if (currentUser.getPartner() == null) {
            return voucher;
        }

        if (voucher.getPartner() == null || !voucher.getPartner().getId().equals(currentUser.getPartner().getId())) {
            throw new RuntimeException("Akses Ditolak: Voucher ini milik partner lain.");
        }

        return voucher;
    }

    // ─── 🚀 MAIN METHODS (SUDAH DISERAGAMKAN POLANYA) ──────────────────────

    // ==========================================
    // CREATE (🔒 Berbasis Permission)
    // ==========================================
    @Transactional
    public VoucherResponse create(VoucherRequest request) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "voucher.store"); // 💡 Siapapun boleh input asal diberi izin Owner via UI
        
        Partners partner = currentUser.getPartner();
        if (partner == null) {
            throw new RuntimeException("Akses Ditolak: Super Admin Global tidak diperbolehkan membuat voucher tanpa scope partner.");
        }

        if (request.getCode() != null && voucherRepository.existsByCode(request.getCode().trim())) {
            throw new RuntimeException("Voucher code already exists");
        }

        validateRequest(request);

        Voucher voucher = new Voucher();
        mapToEntity(voucher, request, partner);
        AuditHelper.setCreated(voucher);

        return mapToResponse(voucherRepository.save(voucher));
    }

    // ==========================================
    // UPDATE (🔒 Berbasis Permission)
    // ==========================================
    @Transactional
    public VoucherResponse update(Long id, VoucherRequest request) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "voucher.update");
        
        Voucher voucher = getValidatedVoucher(id, currentUser);
        validateRequest(request);

        if (request.getCode() != null && !voucher.getCode().equals(request.getCode().trim())
                && voucherRepository.existsByCode(request.getCode().trim())) {
            throw new RuntimeException("Voucher code already exists");
        }

        mapToEntity(voucher, request, currentUser.getPartner());
        AuditHelper.setUpdated(voucher);

        return mapToResponse(voucherRepository.save(voucher));
    }

    // ==========================================
    // GET BY ID (🔒 Berbasis Permission)
    // ==========================================
    public VoucherResponse getById(Long id) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "voucher.show");
        
        return mapToResponse(getValidatedVoucher(id, currentUser));
    }

    // ==========================================
    // GET ALL (🔒 Berbasis Permission)
    // ==========================================
    public List<VoucherResponse> getAll() {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "voucher.index"); // 💡 Sikat pake permission index
        
        // 👑 Jika yang akses Super Admin Global, tampilin seluruh voucher semua tenant tanpa terkecuali
        if (currentUser.getPartner() == null) {
            return voucherRepository.findAll().stream()
                    .map(this::mapToResponse)
                    .toList();
        }

        // 🏢 Jika Partner, hanya ambil voucher milik dia sendiri
        return voucherRepository.findAllByPartner(currentUser.getPartner())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // ==========================================
    // DELETE (🔒 Berbasis Permission)
    // ==========================================
    @Transactional
    public void delete(Long id) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "voucher.delete"); // 💡 Sikat pake permission delete

        Voucher voucher = getValidatedVoucher(id, currentUser);
        AuditHelper.setDeleted(voucher);
        voucherRepository.delete(voucher);
    }

    // ─── 🔄 UTILS & MAPPERS SECTION ──────────────────────────────────────────

    public VoucherResponse mapToResponse(Voucher voucher) {
        if (voucher == null) return null;

        VoucherResponse response = new VoucherResponse();
        response.setId(voucher.getId());
        response.setCode(voucher.getCode());
        response.setName(voucher.getName());
        response.setDiscountType(voucher.getDiscountType());
        response.setDiscountValue(voucher.getDiscountValue());
        response.setMinPurchase(voucher.getMinPurchase());
        response.setMaxDiscount(voucher.getMaxDiscount());
        response.setQuota(voucher.getQuota());
        response.setUsedCount(voucher.getUsed_count());
        response.setValidFrom(voucher.getValidFrom());
        response.setValidUntil(voucher.getValidUntil());
        response.setIsActive(voucher.getIs_active());
        response.setCreatedAt(voucher.getCreatedAt());
        response.setUpdatedAt(voucher.getUpdatedAt());
        response.setDeletedAt(voucher.getDeletedAt());

        if (voucher.getPartner() != null) {
            PartnerSimpleDto partnerDto = new PartnerSimpleDto();
            partnerDto.setId(voucher.getPartner().getId());
            partnerDto.setName(voucher.getPartner().getName());
            response.setPartner(partnerDto);
        }

        response.setCreatedBy(mapUserToDto(voucher.getCreatedBy()));
        response.setUpdatedBy(mapUserToDto(voucher.getUpdatedBy()));
        response.setDeletedBy(mapUserToDto(voucher.getDeletedBy()));

        return response;
    }

    private UserSimpleDto mapUserToDto(User user) {
        if (user == null) return null;
        UserSimpleDto dto = new UserSimpleDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        return dto;
    }

    private void mapToEntity(Voucher voucher, VoucherRequest request, Partners partner) {
        voucher.setPartner(partner);

        if (request.getCode() == null || request.getCode().trim().isEmpty()) {
            if (voucher.getCode() == null) {
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