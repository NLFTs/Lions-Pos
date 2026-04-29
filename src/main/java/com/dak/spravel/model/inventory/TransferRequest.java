package com.dak.spravel.model.inventory;

import com.dak.spravel.model.auth.User;
import com.dak.spravel.model.common.Partners;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "transfer_requests")
public class TransferRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID uid;
    public void generateUid() {
        if (this.uid == null) {
            this.uid = UUID.randomUUID();
        }
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Partners partner;
    /**
     * "branch" | "warehouse"
     */
    @Column(name = "from_location_type", nullable = false, length = 50)
    private String fromLocationType;

    @Column(name = "from_location_id", nullable = false)
    private UUID fromLocationId;

    /**
     * "branch" | "warehouse"
     */
    @Column(name = "to_location_type", nullable = false, length = 50)
    private String toLocationType;

    @Column(name = "to_location_id", nullable = false)
    private UUID toLocationId;

    /**
     * "pending" | "approved" | "in_transit" | "received" | "cancelled"
     */
    public enum Status {
        PENDING, APPROVED, IN_TRANSIT, RECEIVED, CANCELLED
    }

    @Column(name = "status", nullable = false, length = 50)
    private Status status = Status.PENDING;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "requested_at")
    private LocalDateTime requestedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "received_at")
    private LocalDateTime receivedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // --- Relationships ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", referencedColumnName = "id", updatable = false)
    private User createdByUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by", referencedColumnName = "id", updatable = false)
    private User updatedByUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deleted_by", referencedColumnName = "id", updatable = false)
    private User deletedByUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by", referencedColumnName = "id", updatable = false)
    private User approvedByUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "received_by", referencedColumnName = "id", updatable = false)
    private User receivedByUser;

    @OneToMany(mappedBy = "transferRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TransferRequestItem> items;
}