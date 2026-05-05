package com.dak.spravel.model.order;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import com.dak.spravel.model.auth.User;
import com.dak.spravel.model.catalog.Voucher;
import com.dak.spravel.model.common.Partners;
import com.dak.spravel.model.inventory.Branches;
import com.fasterxml.jackson.annotation.JsonIgnore;

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
public class Orders {

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
    @JoinColumn(name = "customer_id", referencedColumnName = "id", nullable = false)
    private User customer;

    @Column(nullable = false)
    private String orderNumber;

    // // Repository untuk status order bisa dibuat terpisah, tapi untuk sementara kita buat enum saja
    // long countByOrderNumberStartingWith(String prefix);

    // Services untuk generate order number bisa dibuat di service, tapi untuk sementara kita buat method di entity saja
    // private String generateOrderNumber() {
    // String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    // String prefix = "ORD-" + date + "-";
    
    // long count = orderRepository.countByOrderNumberStartingWith(prefix);
    // String sequence = String.format("%04d", count + 1);
    
    // return prefix + sequence;
    // // Hasil: ORD-20260429-0001
    // }

    // public Order createOrder(OrderRequest request) {
    //     Order order = new Order();
    //     order.setOrderNumber(generateOrderNumber());
    //     // set field lainnya...
    //     return orderRepository.save(order);
    // }

    @Column(nullable = false)
    private PaymentStatus status;

    public enum PaymentStatus {
        DRAFT, PAID, CANCELED
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