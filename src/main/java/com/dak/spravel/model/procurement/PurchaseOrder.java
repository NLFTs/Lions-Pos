package com.dak.spravel.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "purchase_orders"
    // ,uniqueConstraints = @UniqueConstraint(columnNames = {"partner_id", "po_number"})
)
public class PurchaseOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID uid;

    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "partner_id", nullable = false)
    // private UUID partner;

    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "supplier_id", nullable = false)
    // private UUID supplier;

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
    private LocationType locationType;

    public enum LocationType {
        WAREHOUSE,
        BRANCH
    }

    @Column(nullable = false)
    private UUID locationId;
                    
    @Column(nullable = false)
    private Status status;

    public enum Status {
        DRAFT,
        ORDERED,
        PARTIAL,
        RECEIVED,
        CANCELLED
    }

    @Column(nullable = false)
    private LocalDateTime orderDate;

    @Column(updatable = false)
    private LocalDateTime expectedDate;
    
    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(precision = 10, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;
}
