package com.dak.spravel.model.order;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.dak.spravel.model.auth.User;
import com.dak.spravel.model.catalog.Voucher;
import com.dak.spravel.model.common.Partners;
import com.dak.spravel.model.inventory.Branches;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "orders",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"partner_id" ,"order_number"})
    }
)
public class Orders  {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id", referencedColumnName = "id", nullable = false)
    private Partners partner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", referencedColumnName = "id", nullable = false)
    private Branches branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cashier_id", referencedColumnName = "id", nullable = false)
    private User cashier;

    @Column(nullable = false)
    private String orderNumber;

    @Enumerated (EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    public enum PaymentStatus {
        DRAFT, RETURN, PAID, CANCELED
    }

    @Column(nullable = false)
    private BigDecimal subtotal;

    @Column(name = "discount_amount", nullable = false)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal total;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id", referencedColumnName = "id")
    private Voucher voucher;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "buyer_name", length = 255)
    private String buyerName;

    // Diskon manual
    @Column(name = "manual_discount_type", length = 10)
    private String manualDiscountType;

    @Column(name = "manual_discount_value", precision = 15, scale = 2)
    private BigDecimal manualDiscountValue;

    @Column(name = "manual_discount_note", length = 255)
    private String manualDiscountNote;

    // Field untuk retur
    @Column(name = "returned_at")
    private LocalDateTime returnedAt;

    @Column(name = "return_notes", columnDefinition = "TEXT")
    private String returnNotes;

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

    // Menambah Relasi Yang Belum Di Panggil Dari Repo
    @JsonManagedReference("orders-items")
    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    private List<OrderItems> items = new ArrayList<>();

    @JsonManagedReference("orders-payments")
    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    private Set<Payments> payments = new HashSet<>();
}