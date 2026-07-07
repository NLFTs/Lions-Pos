package com.fts.twin.model.procurement;

import java.math.BigDecimal;
import com.fts.twin.model.catalog.Product;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table (name = "purchase_order_items")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class PurchaseOrderItems {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id", referencedColumnName = "id", nullable = false )
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "partner", "supplier", "createdBy", "updatedBy"})
    private PurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", referencedColumnName = "id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "partner", "category", "createdBy", "updatedBy"})
    private Product product;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(precision = 15, scale = 2)
    private BigDecimal qtyOrdered = BigDecimal.ZERO;

    @Column(precision = 15, scale = 2)
    private BigDecimal qtyReceived = BigDecimal.ZERO;

    @Column(precision = 15, scale = 2)
    private BigDecimal unitCost = BigDecimal.ZERO;

    @Column(precision = 15, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;
}
