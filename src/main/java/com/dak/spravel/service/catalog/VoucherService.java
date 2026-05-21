package com.dak.spravel.service.catalog;

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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
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

           boolean isAdmin = user.getRoles().stream()
                    .anyMatch(role -> role.getSlug().equals("super_admin") || role.getSlug().equals("admin"));

            if (isAdmin) {
                throw new RuntimeException("Akses Ditolak: Admin tidak diperbolehkan mengelola Voucher.");
            }

            // ✅ TAMBAH — Employee dilarang kelola voucher
            boolean isEmployee = user.getRoles().stream()
                    .anyMatch(role ->
                            role.getSlug().equalsIgnoreCase("employee") ||
                                    role.getSlug().equalsIgnoreCase("employee-partners")
                    );

            if (isEmployee) {
                throw new RuntimeException(
                        "Akses Ditolak: Employee tidak dapat mengelola Voucher."
                );
            }

            return user;
        }
    // Helper: Validasi Kepemilikan Voucher
    private Voucher getValidatedVoucher(Long id, User currentUser) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher", id));

        if (currentUser.getPartner() == null ||
                !voucher.getPartner().getId().equals(currentUser.getPartner().getId())) {
            throw new RuntimeException("Akses Ditolak: Voucher ini milik partner lain.");
        }

        return voucher;
    }

    // CREATE
    @Transactional
    public VoucherResponse create(VoucherRequest request) {
        User currentUser = getAuthenticatedUser();
        Partners partner = currentUser.getPartner();

        if (partner == null) {
            throw new RuntimeException("User tidak memiliki Partner.");
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

    // UPDATE
    @Transactional
    public VoucherResponse update(Long id, VoucherRequest request) {
        User currentUser = getAuthenticatedUser();
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

    // GET BY ID
    public VoucherResponse getById(Long id) {
        User currentUser = getAuthenticatedUser();
        return mapToResponse(getValidatedVoucher(id, currentUser));
    }

    // GET ALL
    public List<VoucherResponse> getAll() {
        User currentUser = getAuthenticatedUser();
        return voucherRepository.findAllByPartner(currentUser.getPartner())
                .stream().map(this::mapToResponse).toList();
    }

    // DELETE
    @Transactional
    public void delete(Long id) {
        User currentUser = getAuthenticatedUser();
        Voucher voucher = getValidatedVoucher(id, currentUser);
        AuditHelper.setDeleted(voucher);
        voucherRepository.delete(voucher);
    }

    // MAPPING
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

    // PRIVATE UTILS
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