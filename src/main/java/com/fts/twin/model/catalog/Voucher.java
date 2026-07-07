package com.fts.twin.model.catalog;

import com.fts.twin.model.auth.User;
import com.fts.twin.model.common.Partners;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
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
    @JsonIgnoreProperties({"createdBy", "updatedBy", "deletedBy", "plan", "slug", "isActive", "hibernateLazyInitializer"})
    private Partners partner;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;
    
    @Column( nullable = true)
    @Enumerated(EnumType.STRING)
    private DiscountType discountType;

    public enum DiscountType {
        PERCENT,
        FIXED
    }

    @Column( nullable = false, precision = 15, scale = 2)
    private BigDecimal discountValue;

    @Column( precision = 15, scale = 2)
    private BigDecimal minPurchase = BigDecimal.ZERO;

    @Column( precision = 15, scale = 2)
    private BigDecimal maxDiscount;

    @Column(nullable = true)
    private Integer quota = 0;

    private Integer used_count = 0;

    private LocalDateTime validFrom;

    private LocalDateTime validUntil;

    private Boolean is_active = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @ToString.Exclude 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by", referencedColumnName = "id")
    @JsonIgnoreProperties({"createdBy", "updatedBy", "deletedBy", "password", "roles"})
    private User updatedBy;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", referencedColumnName = "id", updatable = false)
    @JsonIgnoreProperties({"createdBy", "updatedBy", "deletedBy", "password", "roles"})
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deleted_by", referencedColumnName = "id")
    @JsonIgnoreProperties({"createdBy", "updatedBy", "deletedBy", "password", "roles"})
    private User deletedBy;

}
