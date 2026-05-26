package com.dak.spravel.model.inventory;

import jakarta.persistence.*;
import com.dak.spravel.model.catalog.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "transfer_request_items")
public class TransferRequestItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transfer_request_id", referencedColumnName = "id", updatable = false)
    private TransferRequest transferRequest;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Product product;

    @Column(name = "qty_requested", nullable = false)
    private Long qtyRequested;

    @Column(name = "qty_received")
    private Long qtyReceived;
}