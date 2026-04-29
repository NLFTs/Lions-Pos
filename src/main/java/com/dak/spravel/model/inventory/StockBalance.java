package com.dak.spravel.model.inventory;

import com.dak.spravel.model.catalog.Product;
import com.dak.spravel.model.auth.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

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
public class StockBalance {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "uid", updatable = false, nullable = false, unique = true)
        private UUID uid;

        public void setUid(UUID uid) {
            if (this.uid == null) {
                this.uid = uid;
            }
        }

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "product_id", referencedColumnName = "id", nullable = false)
        private Product product;

        @Column(name = "location_type", nullable = false)
        private String locationType;

        // FK to branches.id or warehouses.id depending on location_type
        @Column(name = "location_id", nullable = false)
        private UUID locationId;

// @Service
// @RequiredArgsConstructor
// public class StockService {

//     private final StockBalanceRepository stockBalanceRepository;
//     private final WarehouseRepository warehouseRepository;
//     private final BranchRepository branchRepository;

//     // Saat transaksi dari warehouse
//         public StockBalance createFromWarehouse(UUID warehouseId, UUID productId, BigDecimal qty) {
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
//         public StockBalance createFromBranch(UUID branchId, UUID productId, BigDecimal qty) {
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

        @Column(name = "qty", nullable = false, precision = 19, scale = 4)
        private BigDecimal qty = BigDecimal.ZERO;

        @UpdateTimestamp
        @Column(name = "updated_at")
        private LocalDateTime updatedAt;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "updated_by", referencedColumnName = "id", insertable = false, updatable = false)
        private User updatedBy;
}