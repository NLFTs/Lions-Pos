package com.dak.spravel.model.inventory;

import java.sql.Timestamp;
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
@Table(name = "stock_opnames")
public class StockOpname {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Long id;

    private UUID uid;

    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "partner_id", nullable = false)
    // private UUID partner;

    private String location;

    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "location_id", nullable = false)
    // private Location location;

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

    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "reviewed_by")
    // private User reviewedBy;

    @Column(name = "reviewed_at")
    private Timestamp reviewedAt;

    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "approved_by")
    // private User approvedBy;

    @Column(name = "approved_at")
    private Timestamp approvedAt;
}
