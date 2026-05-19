package com.dak.spravel.model.inventory;

import com.dak.spravel.model.auth.User;
import com.dak.spravel.model.catalog.Product;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "stock_opname_items")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class StockOpnameItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_opname_id", referencedColumnName = "id", nullable = false)
    private StockOpname stockOpname;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", referencedColumnName = "id", nullable = false)
    private Product product;

    private Long qtySystem ;

    private Long qtyPhysical;

    private Long qtyDifference;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counted_by", referencedColumnName = "id")
    private User countedBy;

    @Column(name = "counted_at")
    private LocalDateTime countedAt;
}
