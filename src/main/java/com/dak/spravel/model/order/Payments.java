package com.dak.spravel.model.order;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.util.Set;
import com.dak.spravel.model.base.BaseEntity;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "payments")
public class Payments extends BaseEntity {

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
}