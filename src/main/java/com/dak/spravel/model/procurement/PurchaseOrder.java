package com.dak.spravel.model.procurement;

import java.math.BigDecimal;
import java.sql.Date;
import com.dak.spravel.model.base.BaseEntity;
import com.dak.spravel.model.common.Partners;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false, of = {"id", "poNumber"}) // Exclude poNumber
@Entity
@Table(
    name = "purchase_orders",
    uniqueConstraints = @UniqueConstraint(columnNames = {"partner_id", "po_number"})
)
public class PurchaseOrder extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id", referencedColumnName = "id", nullable = false)
    private Partners partner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", referencedColumnName = "id", nullable = false)
    private Supplier supplier;

    @Column(nullable = false, updatable = false)
    private String poNumber;

    // Disimpan di service, karena harus generate nomor unik dengan format tertentu
    // private String generatePoNumber() {
        // String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        // String prefix = "PO-" + date + "-";
        
        // long count = purchaseOrderRepository.countByPoNumberStartingWith(prefix);
        // String sequence = String.format("%04d", count + 1);
        
        // return prefix + sequence;
        // // Hasil: PO-20260429-0001, PO-20260429-0002, dst
    // }

    @Column(nullable = false)
    private String locationType;

    @Column(nullable = false)
    private Long locationId;
    // @Service
    // @RequiredArgsConstructor
    // public class PurchaseOrderService {

    //     private final PurchaseOrderRepository purchaseOrderRepository;
    //     private final WarehouseRepository warehouseRepository;
    //     private final BranchRepository branchRepository;
    //     private final ProductRepository productRepository;

    //     // Saat transaksi dari warehouse
    //     public PurchaseOrder createFromWarehouse(String warehouseId,  productId, BigDecimal qty) {
    //         Warehouse warehouse = warehouseRepository.findByUid(warehouseId)
    //             .orElseThrow(() -> new RuntimeException("Warehouse tidak ditemukan"));

    //         PurchaseOrder po = new PurchaseOrder();
    //         po.setPoNumber(generatePoNumber());
    //         po.setLocationType(LocationType.WAREHOUSE);
    //         po.setLocationId(warehouse.getUid());

    //         return purchaseOrderRepository.save(po);
    //     }

    //     // Saat transaksi dari branch
    //     public PurchaseOrder createFromBranch(String branchId,  productId, BigDecimal qty) {
    //         Branch branch = branchRepository.findByUid(branchId)
    //             .orElseThrow(() -> new RuntimeException("Branch tidak ditemukan"));

    //         PurchaseOrder po = new PurchaseOrder();
    //         po.setPoNumber(generatePoNumber());
    //         po.setLocationType(LocationType.BRANCH);
    //         po.setLocationId(branch.getUid());

    //         return purchaseOrderRepository.save(po);
    //     }

    //     private String generatePoNumber() {
    //         String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    //         String prefix = "PO-" + date + "-";
    //         long count = purchaseOrderRepository.countByPoNumberStartingWith(prefix);
    //         return prefix + String.format("%04d", count + 1);
    //     }
    // }
// }
                    
    @Column(nullable = false)
    private Status status;

    public enum Status {
        DRAFT,
        ORDERED,
        PARTIAL,
        RECEIVED,
        CANCELLED
    }

    private Date orderDate;

    private Date expectedDate;
    
    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(precision = 10, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;
}
