package com.fts.twin.model.procurement;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDateTime;

import com.fts.twin.model.auth.User;
import com.fts.twin.model.common.Partners;
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

    @Column(nullable = true)
    private String locationType;

    @Column(nullable = true)
    private Long locationId;
                    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
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

    @Column(precision = 15, scale = 2)
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
