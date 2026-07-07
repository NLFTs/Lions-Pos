package com.fts.twin.model.procurement;

import java.math.BigDecimal;
import com.fts.twin.model.catalog.Product;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "purchase_receipt_items")
public class PurchaseReceiptItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_receipt_id", referencedColumnName = "id", nullable = false)
    private PurchaseReceipt purchaseReceipt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_item_id", referencedColumnName = "id", nullable = false)
    private PurchaseOrderItems purchaseOrderItem;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", referencedColumnName = "id", nullable = false)
    private Product product;

    @Column(precision = 15, scale = 2)
    private BigDecimal qtyReceived = BigDecimal.ZERO;

    @Column(precision = 15, scale = 2)
    private BigDecimal unitCost = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
