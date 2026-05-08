package com.dak.spravel.service.catalog;

import com.dak.spravel.dto.request.product.VoucherRequest;
import com.dak.spravel.model.catalog.Voucher;
import com.dak.spravel.model.common.Partners;
import com.dak.spravel.repository.catalog.VoucherRepository;
import com.dak.spravel.repository.common.PartnerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VoucherService {

    private final VoucherRepository voucherRepository;
    private final PartnerRepository partnerRepository;


     public Voucher create(VoucherRequest request) {

        validateRequest(request);

        if (voucherRepository.existsByCode(request.getCode())) {
            throw new RuntimeException("Voucher code already exists");
        }

        Partners partner = partnerRepository.findById(request.getPartnerId())
                .orElseThrow(() -> new RuntimeException("Partner not found"));

        Voucher voucher = new Voucher();
        mapToEntity(voucher, request, partner);

        return voucherRepository.save(voucher);
    }


    public Voucher update(Long id, VoucherRequest request) {

        validateRequest(request);

        Voucher voucher = getById(id);

        if (!voucher.getCode().equals(request.getCode())
                && voucherRepository.existsByCode(request.getCode())) {
            throw new RuntimeException("Voucher code already exists");
        }

        Partners partner = partnerRepository.findById(request.getPartnerId())
                .orElseThrow(() -> new RuntimeException("Partner not found"));

        mapToEntity(voucher, request, partner);

        return voucherRepository.save(voucher);
    }


    public Voucher getById(Long id) {
        return voucherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Voucher not found"));
    }

    public Voucher getByCode(String code) {
        return voucherRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Voucher not found"));
    }

    public List<Voucher> getAll() {
        return voucherRepository.findAll();
    }


    public void delete(Long id) {
        Voucher voucher = getById(id);
        voucherRepository.delete(voucher);
    }


    private void mapToEntity(Voucher voucher, VoucherRequest request, Partners partner) {
        voucher.setPartner(partner);
        voucher.setCode(request.getCode());
        voucher.setName(request.getName());

        voucher.setDiscountType(
                Voucher.DiscountType.valueOf(request.getDiscountType().toUpperCase())
        );

        voucher.setDiscountValue(request.getDiscountValue());
        voucher.setMinPurchase(request.getMinPurchase());
        voucher.setMaxDiscount(request.getMaxDiscount());
        voucher.setQuota(request.getQuota());
    }

    private void validateRequest(VoucherRequest request) {

        if (request.getDiscountType() == null) {
            throw new RuntimeException("Discount type is required");
        }

        if (request.getDiscountValue() == null) {
            throw new RuntimeException("Discount value is required");
        }

        if (request.getValidFrom() != null && request.getValidUntil() != null) {
            if (request.getValidFrom().isAfter(request.getValidUntil())) {
                throw new RuntimeException("validFrom cannot be after validUntil");
            }
        }

        if (request.getQuota() != null && request.getQuota() < 0) {
            throw new RuntimeException("Quota cannot be negative");
        }
    }
}