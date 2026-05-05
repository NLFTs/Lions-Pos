package com.dak.spravel.model.order;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import com.dak.spravel.model.auth.User;
import com.fasterxml.jackson.annotation.JsonIgnore;

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
        PENDING, COMPLETED, FAILED
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

     @Column(updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Column(nullable = true)
    private LocalDateTime deletedAt;

    @CreatedBy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", referencedColumnName = "id", updatable = false)
    @JsonIgnore
    private User createdBy;

    @LastModifiedBy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by", referencedColumnName = "id")
    @JsonIgnore
    private User updatedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deleted_by", referencedColumnName = "id")
    @JsonIgnore
    private User deletedBy;
}