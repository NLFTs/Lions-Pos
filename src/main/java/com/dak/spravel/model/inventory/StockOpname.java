package com.dak.spravel.model.inventory;

import java.time.LocalDateTime;
import java.util.UUID;
import com.dak.spravel.model.auth.User;
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
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "stock_opnames")
public class StockOpname extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Long id;

    @Column(name = "uid", updatable = false, unique = true)
    private UUID uid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id", referencedColumnName = "id", nullable = false)
    private Partners partner;

    // "branch" | "warehouse"
    @Column(name = "location_type", nullable = false)
    private String location;

    @Column(name = "location_id", nullable = false)
    private UUID locationId;

    private LocalDateTime date;

    private Status status = Status.DRAFT;
    public enum Status {
        DRAFT,
        COUNTING,
        RIEVIEWED,
        APPROVED,
        ADJUSTED
    }

    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by", referencedColumnName = "id")
    private User reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by", referencedColumnName = "id")
    private User approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
}
