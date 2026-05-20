package com.dak.spravel.model.procurement;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDateTime;

import com.dak.spravel.model.auth.User;
import com.dak.spravel.model.common.Partners;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "purchase_orders",
    uniqueConstraints = @UniqueConstraint(columnNames = {"partner_id", "po_number"})
)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class PurchaseOrder  {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id", referencedColumnName = "id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "createdBy", "updatedBy", "deletedBy"})
    private Partners partner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", referencedColumnName = "id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "partner", "createdBy", "updatedBy", "deletedBy"})
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

    @Column(nullable = true)
    private String locationType;

    @Column(nullable = true)
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
    private Status status = Status.DRAFT;

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

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by", referencedColumnName = "id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "roles", "partner"})
    private User updatedBy;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", referencedColumnName = "id", updatable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "roles", "partner"})
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deleted_by", referencedColumnName = "id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "roles", "partner"})
    private User deletedBy;
}
