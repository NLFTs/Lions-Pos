package com.dak.spravel.dto.request.product;

import com.dak.spravel.model.catalog.Voucher;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class VoucherRequest {
    private Long partnerId;
    private String code;
    private String name;

    private String discountType; // "PERCENT" / "FIXED"
    private BigDecimal discountValue;

    private BigDecimal minPurchase;
    private BigDecimal maxDiscount;

    private Integer quota;

    private LocalDate validFrom;
    private LocalDate validUntil;
}
