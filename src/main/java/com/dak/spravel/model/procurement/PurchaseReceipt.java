package com.dak.spravel.model.procurement;

import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.annotation.CreatedDate;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "purchase_receipts"
    ,uniqueConstraints = @UniqueConstraint(columnNames = {"purchase_order_id", "receipt_number"})
)
public class PurchaseReceipt {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID uid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @Column(nullable = false)
    private String receiptNumber;

    // Disimpan di service, karena harus generate nomor unik dengan format tertentu
    // private void generateReceiptNumber() {
    //     if (this.receiptNumber == null) {
    //         // Format tanggal hari ini
    //         String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            
    //         // Random 4 digit
    //         String random = String.format("%04d", new Random().nextInt(9999) + 1);
            
    //         this.receiptNumber = "GR-" + date + "-" + random;
    //         // Hasil: GR-20260429-0421
    //     }
    // }

    @Column(nullable = false)
    private LocalDateTime receivedDate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    @Column(name = "created_by", updatable = false)
    private UUID createdBy;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @Column(name = "deleted_by")
    private UUID deletedBy;

    @PrePersist
    private void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    private void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
