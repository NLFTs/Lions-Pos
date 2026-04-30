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

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "stock_mutations")
public class StockMutation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Partners partner;

    // "sale_out" | "purchase_in" | "transfer" | "adjustment" | "return"

    @Column(name = "type", nullable = false, length = 50)
    private String type;

    // "branch" | "warehouse" | null
    @Column(name = "from_location_type", length = 50)
    private String fromLocationType;

    @Column(name = "from_location_id")
    private Long fromLocationId;

    // "branch" | "warehouse" | null
    @Column(name = "to_location_type", length = 50)
    private String toLocationType;

    @Column(name = "to_location_id")
    private Long toLocationId;

    @Column(name = "qty", nullable = false, precision = 19, scale = 4)
    private BigDecimal qty;

    // "order" | "transfer_request" | "stock_opname"

    @Column(name = "reference_type", length = 50)
    private String referenceType;

    // Polymorphic FK — points to orders.id, transfer_requests.id, or stock_opname.id

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