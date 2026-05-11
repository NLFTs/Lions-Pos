package com.dak.spravel.model.inventory;

import java.time.LocalDateTime;
import com.dak.spravel.model.auth.User;
import com.dak.spravel.model.common.Partners;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "stock_opnames")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class StockOpname  {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id", referencedColumnName = "id", nullable = false)
    private Partners partner;

    // "branch" | "warehouse"
    @Column(name = "location_type", nullable = true)
    private String location;

    @Column(name = "location_id", nullable = true)
    private Long locationId;

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

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @ToString.Exclude 
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "updated_by", referencedColumnName = "id")
    private User updatedBy;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by", referencedColumnName = "id", updatable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "deleted_by", referencedColumnName = "id")
    private User deletedBy;
}
