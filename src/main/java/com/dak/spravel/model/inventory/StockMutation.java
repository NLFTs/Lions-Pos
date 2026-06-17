package com.dak.spravel.model.inventory;

import com.dak.spravel.model.catalog.Product;
import com.dak.spravel.model.auth.User;
import com.dak.spravel.model.common.Partners;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "stock_mutations")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class StockMutation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", referencedColumnName = "id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id", referencedColumnName = "id", nullable = false)
    private Partners partner;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private Type type;

    public enum Type {
        SALE_OUT, PURCHASE_IN, TRANSFER, ADJUSTMENT, RETURN, QUARANTINE_IN, QUARANTINE_DISPOSE
    }

    public enum Location {
        BRANCH, WAREHOUSE, QUARANTINE
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "from_location_type", nullable = true)
    private Location fromLocationType;

    @Column(name = "from_location_id", nullable = true)
    private Long fromLocationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_location_type", length = 50)
    private Location toLocationType;

    @Column(name = "to_location_id")
    private Long toLocationId;

    @Column(name = "qty", nullable = false)
    private Long qty;

    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type")
    private ReferenceType referenceType;

    public enum ReferenceType {
        ORDER, TRANSFER_REQUEST, STOCK_OPNAME, PURCHASE_RECEIPT
    }
    
    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", updatable = false)
    private User createdBy;
}