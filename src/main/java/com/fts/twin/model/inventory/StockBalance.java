package com.fts.twin.model.inventory;

import com.fts.twin.model.catalog.Product;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fts.twin.model.auth.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(
        name = "stock_balances",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"product_id", "location_type", "location_id"})
        }
)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class StockBalance {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "product_id", referencedColumnName = "id", nullable = false)
        private Product product;

        @Column(name = "location_type", nullable = true)
        private String locationType;

        @Column(name = "location_id", nullable = true)
        private Long locationId;

        @Column(name = "qty", nullable = false)
        private Long qty;

        @Column(name = "created_at", updatable = false)
        private LocalDateTime createdAt = LocalDateTime.now();

        @UpdateTimestamp
        @Column(name = "updated_at")
        private LocalDateTime updatedAt;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "created_by", referencedColumnName = "id", updatable = false)
        private User createdBy;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "updated_by", referencedColumnName = "id")
        private User updatedBy;
}