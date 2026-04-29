package com.dak.spravel.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Table(
        name = "stock_balances",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"product_id", "location_type", "location_id"})
        }
)
public class StockBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "auto_id", updatable = false, nullable = false)
    private Long autoId;

    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false, unique = true)
    private UUID id;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    // "branch" | "warehouse"

    @Column(name = "location_type", nullable = false, length = 50)
    private String locationType;

    // FK to branches.id or warehouses.id depending on location_type

    @Column(name = "location_id", nullable = false)
    private UUID locationId;

    @Column(name = "qty", nullable = false, precision = 19, scale = 4)
    private BigDecimal qty = BigDecimal.ZERO;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private UUID updatedBy;

    // Relationships

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by", insertable = false, updatable = false)
    private User updatedByUser;
}