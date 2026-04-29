package com.dak.spravel.model.procurement;

import java.math.BigDecimal;
import java.util.UUID;

import com.dak.spravel.model.catalog.Product;

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

    @Column(name = "uid", updatable = false, nullable = false, unique = true)
    private UUID uid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id", referencedColumnName = "id", nullable = false )
    private PurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", referencedColumnName = "id", nullable = false)
    private Product product;

    @Column(name = "product_name", nullable = false)
    private String productName;

    // Snapshot harga saat order dibuat dan disimpan di Service, untuk menghindari masalah jika harga produk berubah setelah order dibuat
    // orderItem.setProductName(product.getName()); // snapshot disini
    // orderItem.setUnitCost(product.getCost());  // snapshot disini

    @Column(precision = 10, scale = 2)
    private BigDecimal qtyOrdered = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal qtyReceived = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal unitCost = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    // Disimpan di Service, tidak dihitung otomatis disini, untuk menghindari masalah jika harga produk berubah setelah order dibuat
    // item.setUnitCost(request.getUnitCost());
    // item.setSubtotal(request.getQty().multiply(request.getUnitCost()));
}
