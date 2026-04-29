package com.dak.spravel.model.inventory;

import com.dak.spravel.model.catalog.Product;
import com.dak.spravel.model.auth.User;
import com.dak.spravel.model.common.Partners;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "stock_mutations")
public class StockMutation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "auto_id", updatable = false, nullable = false)
    private Long autoId;

    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false, unique = true)
    private UUID id;

    @Column(name = "partner_id", nullable = false)
    private UUID partnerId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    // "sale_out" | "purchase_in" | "transfer" | "adjustment" | "return"

    @Column(name = "type", nullable = false, length = 50)
    private String type;

    // "branch" | "warehouse" | null
    @Column(name = "from_location_type", length = 50)
    private String fromLocationType;

    @Column(name = "from_location_id")
    private UUID fromLocationId;

    // "branch" | "warehouse" | null
    @Column(name = "to_location_type", length = 50)
    private String toLocationType;

    @Column(name = "to_location_id")
    private UUID toLocationId;

    @Column(name = "qty", nullable = false, precision = 19, scale = 4)
    private BigDecimal qty;

    // "order" | "transfer_request" | "stock_opname"

    @Column(name = "reference_type", length = 50)
    private String referenceType;

    // Polymorphic FK — points to orders.id, transfer_requests.id, or stock_opname.id

    @Column(name = "reference_id")
    private UUID referenceId;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private UUID createdBy;

    //  Relationships

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id", insertable = false, updatable = false)
    private Partners partner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", insertable = false, updatable = false)
    private User createdByUser;
}