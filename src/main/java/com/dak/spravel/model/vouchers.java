package com.dak.spravel.model;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;



@Data
@AllArgsConstructor
@Entity
@Table(name = "vouchers",indexes=@Index(name = "idx_vouchers_code", columnList = "code"))
public class vouchers {
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

@Column(unique = true, updatable = false, nullable = false)
    private UUID uid; 

@Column(name = "partner_id", nullable = false)
private UUID partner_id;

@Column(nullable = false, unique = true)
private String code;

@Column(nullable = false)
private String name;

@Column(name = "discount_type", nullable = false)
private String discountType; // "percent" atau "fixed"

@Column(name = "discount_value", nullable = false, precision = 15, scale = 2)
private BigDecimal discountValue;

@Column(name = "min_purchase", precision = 15, scale = 2)
private BigDecimal minPurchase = BigDecimal.ZERO;

@Column(name = "max_discount", precision = 15, scale = 2)
private BigDecimal maxDiscount; //null= no cap

private Integer quota = 0; // 0 = unlimited

@Column(name = "used_count")
private Integer used_count = 0;

@Column(name = "valid_from")
private LocalDate valid_from;

@Column(name = "valid_until")
private LocalDate valid_until;

@Column(name = "is_active")
private Boolean is_active = true;

@CreatedDate
@Column(updatable = false)
private Timestamp createdAt;

private Timestamp updatedAt;

private Timestamp deletedAt;

@Column(name = "created_by", updatable = false)
private UUID createdBy;

@Column(name = "updated_by")
private UUID updatedBy;

@Column(name = "deleted_by")
private UUID deletedBy;

 @PrePersist
private void onCreate() {
    this.createdAt = new Timestamp(System.currentTimeMillis());
    this.updatedAt = new Timestamp(System.currentTimeMillis());
     if (this.uid == null) {
            this.uid = UUID.randomUUID();
        }
}

@PreUpdate
private void onUpdate() {
    this.updatedAt = new Timestamp(System.currentTimeMillis());
}
}
