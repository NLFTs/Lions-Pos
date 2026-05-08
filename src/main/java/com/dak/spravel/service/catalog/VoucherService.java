package com.dak.spravel.service.catalog;

import com.dak.spravel.dto.request.product.VoucherRequest;
import com.dak.spravel.handler.ResourceNotFoundException;
import com.dak.spravel.model.auth.User;
import com.dak.spravel.model.catalog.Voucher;
import com.dak.spravel.model.common.Partners;
import com.dak.spravel.repository.auth.UserRepository;
import com.dak.spravel.repository.catalog.VoucherRepository;
import com.dak.spravel.repository.common.PartnerRepository;
import com.dak.spravel.util.AuditHelper;
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
    private final PartnerRepository partnerRepository;
    private final UserRepository userRepository;


    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new RuntimeException("User tidak terautentikasi");
        }

        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));


        if (isAdmin(user)) {
            throw new RuntimeException("Akses Ditolak: Admin tidak boleh mengelola Voucher");
        }

        return user;
    }

    private boolean isAdmin(User user) {
        return user.getRoles().stream()
                .anyMatch(role ->
                        role.getSlug().equals("super_admin")
                                || role.getSlug().equals("admin")
                );
    }

    private Voucher getValidatedVoucher(Long id, User user) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher", id));

        if (user.getPartner() == null ||
                !voucher.getPartner().getId().equals(user.getPartner().getId())) {
            throw new RuntimeException("Akses Ditolak: beda partner");
        }

        return voucher;
    }


    public List<Voucher> findAll() {
        User user = getAuthenticatedUser();
        return voucherRepository.findAllByPartnerId(user.getPartner().getId());
    }

    public Voucher findById(Long id) {
        User user = getAuthenticatedUser();
        return getValidatedVoucher(id, user);
    }


    @Transactional
    public Voucher create(VoucherRequest request) {

        User user = getAuthenticatedUser();

        Partners partner = user.getPartner();
        if (partner == null) {
            throw new RuntimeException("User tidak punya partner");
        }

        Voucher voucher = new Voucher();

        voucher.setPartner(partner);
        voucher.setCode(request.getCode());
        voucher.setName(request.getName());
        voucher.setDiscountType(Voucher.DiscountType.valueOf(request.getDiscountType().toUpperCase()));
        voucher.setDiscountValue(request.getDiscountValue());
        voucher.setMinPurchase(request.getMinPurchase());
        voucher.setMaxDiscount(request.getMaxDiscount());
        voucher.setValidFrom(request.getValidFrom());
        voucher.setValidUntil(request.getValidUntil());
        voucher.setQuota(0);
        voucher.setUsed_count(0);
        voucher.setIs_active(true);

        AuditHelper.setCreated(voucher);

        return voucherRepository.save(voucher);
    }


    @Transactional
    public Voucher update(Long id, VoucherRequest request) {

        User user = getAuthenticatedUser();

        Voucher voucher = getValidatedVoucher(id, user);

        voucher.setCode(request.getCode());
        voucher.setName(request.getName());
        voucher.setDiscountType(Voucher.DiscountType.valueOf(request.getDiscountType().toUpperCase()));
        voucher.setDiscountValue(request.getDiscountValue());
        voucher.setMinPurchase(request.getMinPurchase());
        voucher.setMaxDiscount(request.getMaxDiscount());
        voucher.setValidFrom(request.getValidFrom());
        voucher.setValidUntil(request.getValidUntil());

        AuditHelper.setUpdated(voucher);

        return voucherRepository.save(voucher);
    }


    @Transactional
    public void delete(Long id) {

        User user = getAuthenticatedUser();

        Voucher voucher = getValidatedVoucher(id, user);

        voucherRepository.delete(voucher);
    }



}