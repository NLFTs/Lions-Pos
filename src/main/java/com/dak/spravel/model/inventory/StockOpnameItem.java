package com.dak.spravel.model.inventory;

import com.dak.spravel.model.auth.User;
import com.dak.spravel.model.catalog.Product;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
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

    @Column(precision = 10, scale = 2)
    private BigDecimal qtySystem = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal qtyPhysical = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal qtyDifference = BigDecimal.ZERO;

        // // Service akan menghitung qtyDifference = qtyPhysical - qtySystem, dan menyimpan snapshot unit cost dari stock balance saat ini
        // public void inputPhysicalCount(String opnameItemId, BigDecimal qtyPhysical) {
        //     OpnameItem item = opnameItemRepository.findByUid(opnameItemId)
        //         .orElseThrow(() -> new RuntimeException("Item tidak ditemukan"));

        //     item.setQtyPhysical(qtyPhysical);
            
        //     // Otomatis hitung selisih
        //     // positif = surplus, negatif = loss
        //     item.setQtyDifference(qtyPhysical.subtract(item.getQtySystem()));

        //     opnameItemRepository.save(item);
        // }   

    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "counted_by", referencedColumnName = "id")
    @JsonIgnoreProperties({"createdBy", "updatedBy", "deletedBy", "password", "roles"})
    private User countedBy;

    @Column(name = "counted_at")
    private LocalDateTime countedAt;
}
