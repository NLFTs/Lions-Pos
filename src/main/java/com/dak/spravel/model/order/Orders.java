package com.dak.spravel.model.order;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.UUID;
import com.dak.spravel.model.auth.User;
import com.dak.spravel.model.base.BaseEntity;
import com.dak.spravel.model.catalog.Voucher;
import com.dak.spravel.model.common.Partners;
import com.dak.spravel.model.inventory.Branches;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(
    name = "orders",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"partner_id" ,"order_number"})
    }
)
public class Orders extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uid", updatable = false, nullable = false, unique = true)
    private UUID uid;
 
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
}