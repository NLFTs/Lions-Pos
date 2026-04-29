package com.dak.spravel.model.inventory;

import jakarta.persistence.*;
import com.dak.spravel.model.catalog.Product;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Data
@Table(name = "transfer_request_items")
public class TransferRequestItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "auto_id", updatable = false, nullable = false)
    private Long autoId;

    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false, unique = true)
    private UUID id;

    @Column(name = "transfer_request_id", nullable = false)
    private UUID transferRequestId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "qty_requested", nullable = false, precision = 19, scale = 4)
    private BigDecimal qtyRequested;

    /**
     * Filled on receive; may differ from qty_requested (partial fulfillment)
     */
    @Column(name = "qty_received", precision = 19, scale = 4)
    private BigDecimal qtyReceived;

    // --- Relationships ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transfer_request_id", insertable = false, updatable = false)
    private TransferRequest transferRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private Product product;
}