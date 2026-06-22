package com.dak.spravel.model.order;

import java.math.BigDecimal;
import com.dak.spravel.model.catalog.Product;
import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "order_items")
public class OrderItems {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JsonBackReference("orders-items")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", referencedColumnName = "id", nullable = false)
    private Orders order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", referencedColumnName = "id", nullable = false)
    private Product product;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private Long qty;

    @Column(nullable = false)
    private BigDecimal subtotal;

    @Column(name = "item_discount_type")
    private String itemDiscountType;

    @Column(name = "item_discount_value")
    private BigDecimal itemDiscountValue;

    @Column(name = "item_discount_amount")
    private BigDecimal itemDiscountAmount;

    @Column(name = "return_qty")
    private Long returnQty;

    @Column(name = "return_reason", columnDefinition = "TEXT")
    private String returnReason;

    @Column(name = "item_note", columnDefinition = "TEXT")
    private String itemNote;
}