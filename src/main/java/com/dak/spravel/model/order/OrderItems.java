package com.dak.spravel.model.order;

import java.math.BigDecimal;
import com.dak.spravel.model.catalog.Product;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "order_items")
public class OrderItems {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", referencedColumnName = "id", nullable = false)
    private Orders order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", referencedColumnName = "id", nullable = false)
    private Product product;

    @Column(name = "product_name", nullable = false)
    private String productName;

    // Snapshot harga saat order dibuat dan disimpan di Service, untuk menghindari masalah jika harga produk berubah setelah order dibuat
    // orderItem.setProductName(product.getName()); // snapshot disini
    // orderItem.setUnitPrice(product.getPrice());  // snapshot disini

    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private BigDecimal qty;

    @Column(nullable = false)
    private BigDecimal subtotal;
}