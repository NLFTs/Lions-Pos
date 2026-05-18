package com.dak.spravel.model.inventory;

import com.dak.spravel.model.auth.User;
import com.dak.spravel.model.common.Partners;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.time.LocalDateTime;
import java.util.List;


@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "transfer_requests")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class TransferRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Partners partner;
    /**
     * "branch" | "warehouse"
     */
    public enum Location {
        BRANCH, WAREHOUSE
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "from_location_type", nullable = false)
    private Location fromLocationType;

    @Column(name = "from_location_id", nullable = false)
    private Long fromLocationId;

    /**
     * "branch" | "warehouse"
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "to_location_type", nullable = false)
    private Location toLocationType;
        
    @Column(name = "to_location_id", nullable = false)
    private Long toLocationId;

    /**
     * "pending" | "approved" | "in_transit" | "received" | "cancelled"
     */
    public enum Status {
        PENDING, APPROVED, IN_TRANSIT, RECEIVED, CANCELLED
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.PENDING;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "requested_at")
    private LocalDateTime requestedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "received_at")
    private LocalDateTime receivedAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @ToString.Exclude 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by", referencedColumnName = "id")
    private User updatedBy;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", referencedColumnName = "id", updatable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deleted_by", referencedColumnName = "id")
    private User deletedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by", referencedColumnName = "id", updatable = false)
    private User approvedByUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "received_by", referencedColumnName = "id", updatable = false)
    private User receivedByUser;

    @OneToMany(mappedBy = "transferRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TransferRequestItem> items;
}