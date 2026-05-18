package com.dak.spravel.model.inventory;

import com.dak.spravel.model.catalog.Product;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.dak.spravel.model.auth.User;
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

        // FK to branches.id or warehouses.id depending on location_type
        @Column(name = "location_id", nullable = true)
        private Long locationId;

// @Service
// @RequiredArgsConstructor
// public class StockService {

//     private final StockBalanceRepository stockBalanceRepository;
//     private final WarehouseRepository warehouseRepository;
//     private final BranchRepository branchRepository;

//     // Saat transaksi dari warehouse
//         public StockBalance createFromWarehouse(String warehouseId, String productId, BigDecimal qty) {
//                 Warehouse warehouse = warehouseRepository.findByUid(warehouseId)
//                 .orElseThrow(() -> new RuntimeException("Warehouse tidak ditemukan"));

//                 StockBalance stock = new StockBalance();
//                 stock.setProduct(productRepository.findByUid(productId).orElseThrow());
//                 stock.setQty(qty);

//                 // Otomatis set dari warehouse
//                 stock.setLocationType(LocationType.WAREHOUSE);
//                 stock.setLocationId(warehouse.getUid());

//                 return stockBalanceRepository.save(stock);
//         }

//         // Saat transaksi dari branch
//         public StockBalance createFromBranch(String branchId, String productId, BigDecimal qty) {
//                 Branch branch = branchRepository.findByUid(branchId)
//                 .orElseThrow(() -> new RuntimeException("Branch tidak ditemukan"));

//                 StockBalance stock = new StockBalance();
//                 stock.setProduct(productRepository.findByUid(productId).orElseThrow());
//                 stock.setQty(qty);

//                 // Otomatis set dari branch
//                 stock.setLocationType(LocationType.BRANCH);
//                 stock.setLocationId(branch.getUid());

//                 return stockBalanceRepository.save(stock);
//         }
// }

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