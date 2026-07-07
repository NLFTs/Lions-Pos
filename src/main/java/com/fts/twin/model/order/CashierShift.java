package com.fts.twin.model.order;

import com.fts.twin.model.auth.User;
import com.fts.twin.model.common.Partners;
import com.fts.twin.model.inventory.Branches;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cashier_shifts")
public class CashierShift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id", referencedColumnName = "id", nullable = false)
    private Partners partner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", referencedColumnName = "id", nullable = false)
    private Branches branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cashier_id", referencedColumnName = "id", nullable = false)
    private User cashier;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "starting_cash", nullable = false, precision = 15, scale = 2)
    private BigDecimal startingCash = BigDecimal.ZERO;

    @Column(name = "total_revenue", precision = 15, scale = 2)
    private BigDecimal totalRevenue = BigDecimal.ZERO;

    @Column(name = "total_transactions")
    private Integer totalTransactions = 0;
    
    @Column(name = "cash_revenue", precision = 15, scale = 2)
    private BigDecimal cashRevenue = BigDecimal.ZERO;

    @Column(name = "transfer_revenue", precision = 15, scale = 2)
    private BigDecimal transferRevenue = BigDecimal.ZERO;

    @Column(name = "cash_transactions")
    private Integer cashTransactions = 0;

    @Column(name = "transfer_transactions")
    private Integer transferTransactions = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShiftStatus status;

    public enum ShiftStatus {
        OPEN, CLOSED, HOLD
    }

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "closing_notes", columnDefinition = "TEXT")
    private String closingNotes;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
