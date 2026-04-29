package com.dak.spravel.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "stock_opname_items")
public class StockOpnameItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private UUID uid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_opname_id", nullable = false)
    private StockOpname stockOpname;

    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "product_id", nullable = false)
    // private Product product;

    @Column(precision = 10, scale = 2)
    private BigDecimal qtySystem = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal qtyPhysical = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal qtyDifference = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counted_by")
    private User countedBy;

    @Column(name = "counted_at")
    private LocalDateTime countedAt;
}
