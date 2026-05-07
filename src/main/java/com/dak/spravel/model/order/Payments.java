package com.dak.spravel.model.order;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import com.dak.spravel.model.auth.User;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "payments")
public class Payments   {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "payments_orders",
        joinColumns = @JoinColumn(name = "payment_id"),
        inverseJoinColumns = @JoinColumn(name = "order_id")
    )
    private Set<Orders> orders;

    @Column(nullable = false)
    private Method method;

    public enum Method {
        CASH, TRANSFER
    }
    
    @Column(nullable = false)
    private Status status = Status.PENDING;

    public enum Status {
        PENDING, COMPLETED, VERIFIED, FAILED
    }

    @Column(nullable = false , precision = 19, scale = 4)
    private BigDecimal amount;

    // CASH ONLY
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal cashTendered;

    @Column(name = "change_due", precision = 19, scale = 4)
    private BigDecimal changeDue;

    // TRANSFER ONLY
    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "reference_no")
    private String referenceNo;

    @Column(name = "proof_url", columnDefinition = "TEXT")
    private String proofUrl;

    // Jika ingin dengan bank transfer --Service
    // if (paymentMethod == PaymentMethod.BANK_TRANSFER) {
    // payment.setBankName(request.getBankName());
    // payment.setReferenceNo(request.getReferenceNo());
    // payment.setProofUrl(request.getProofUrl());
    // }

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