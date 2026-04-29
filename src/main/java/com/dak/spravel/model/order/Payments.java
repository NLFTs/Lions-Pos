package com.dak.spravel.model.order;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
public class Payments {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

     @Column(unique = true , nullable = false , updatable = false)
    private UUID uid;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(nullable = false)
    private String method;

    @Column(nullable = false)
    private BigDecimal amount;

    // CASH ONLY
    @Column(name = "cash_tendered")
    private BigDecimal cashTendered;

    @Column(name = "change_due")
    private BigDecimal changeDue;

    // TRANSFER ONLY
    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "reference_no")
    private String referenceNo;

    @Column(name = "proof_url")
    private String proofUrl;

    @Column(nullable = false)
    private String status = "pending"; 

  
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @Column(name = "verified_by")
    private UUID verifiedBy;


    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = "pending";
        }
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}