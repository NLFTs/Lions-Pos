package com.dak.spravel.model.procurement;

import java.math.BigDecimal;
import java.util.UUID;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table (name = "purchase_order_items")
public class PurchaseOrderItems {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID uid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id", nullable = false )
    private PurchaseOrder purchaseOrder;

    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "product_id", nullable = false)
    // private Product product;

    // @Column(name = "product_name", nullable = false)
    // private String productName;

    @Column(precision = 10, scale = 2)
    private BigDecimal qtyOrdered = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal qtyReceived = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal unitCost = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

}
