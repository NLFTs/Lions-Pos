package com.dak.spravel.model.catalog;

import com.dak.spravel.model.base.BaseEntitySimple;
import com.dak.spravel.model.common.Partners;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "vouchers",indexes=@Index(name = "idx_vouchers_code", columnList = "code"))
public class Voucher extends BaseEntitySimple {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID uid; 
    public void init() {
        if (this.uid == null) {
            this.uid = UUID.randomUUID();
        }
    }

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

    private LocalDate valid_from;

    private LocalDate valid_until;

    private Boolean is_active = true;
    
}
