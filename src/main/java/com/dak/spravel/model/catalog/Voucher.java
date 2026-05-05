package com.dak.spravel.model.catalog;

import com.dak.spravel.model.auth.User;
import com.dak.spravel.model.common.Partners;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "vouchers",indexes=@Index(name = "idx_vouchers_code", columnList = "code"))
public class Voucher  {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id", referencedColumnName = "id", nullable = false)
    private Partners partner;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;
    
    @Column( nullable = false)
    private DiscountType discountType; // "percent" atau "fixed"

    public enum DiscountType {
        PERCENT,
        FIXED
    }

    @Column( nullable = false, precision = 15, scale = 2)
    private BigDecimal discountValue;

    @Column( precision = 15, scale = 2)
    private BigDecimal minPurchase = BigDecimal.ZERO;

    @Column( precision = 15, scale = 2)
    private BigDecimal maxDiscount; //null= no cap

    @Column(nullable = true)
    private Integer quota = 0; // 0 = unlimited

    private Integer used_count = 0;

    private LocalDateTime valid_from;

    private LocalDateTime valid_until;

    private Boolean is_active = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by", referencedColumnName = "id")
    private User updatedBy;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", referencedColumnName = "id", updatable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deleted_by", referencedColumnName = "id")
    private User deletedBy;
    
}
