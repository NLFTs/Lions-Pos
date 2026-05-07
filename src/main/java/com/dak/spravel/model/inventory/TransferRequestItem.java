package com.dak.spravel.model.inventory;

import jakarta.persistence.*;
import com.dak.spravel.model.catalog.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Product product;

    @Column(name = "qty_requested", nullable = false, precision = 19, scale = 4)
    private BigDecimal qtyRequested;

    @Column(name = "qty_received", precision = 19, scale = 4)
    private BigDecimal qtyReceived;
}