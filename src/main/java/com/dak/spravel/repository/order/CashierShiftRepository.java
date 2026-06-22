package com.dak.spravel.repository.order;

import com.dak.spravel.model.order.CashierShift;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CashierShiftRepository extends JpaRepository<CashierShift, Long> {
        // Cek apakah kasir ini sudah punya shift OPEN di cabang tertentu
    Optional<CashierShift> findByCashierIdAndBranchIdAndStatus(
            Long cashierId, Long branchId, CashierShift.ShiftStatus status);

        //Semua shift di partner tertentu (super admin)
   @EntityGraph(attributePaths = {"cashier", "branch"})
    @Query("SELECT s FROM CashierShift s WHERE s.partner.id = :partnerId ORDER BY s.startedAt DESC")
    List<CashierShift> findAllByPartnerId(@Param("partnerId") Long partnerId);

    @EntityGraph(attributePaths = {"cashier", "branch"})
    @Query("SELECT s FROM CashierShift s WHERE s.cashier.id = :cashierId ORDER BY s.startedAt DESC")
    List<CashierShift> findAllByCashierId(@Param("cashierId") Long cashierId);

        // Semua shift di cabang tertentu (super admin)     
    @EntityGraph(attributePaths = {"cashier", "branch"})
    @Query("SELECT s FROM CashierShift s WHERE s.branch.id = :branchId ORDER BY s.startedAt DESC")
    List<CashierShift> findAllByBranchId(@Param("branchId") Long branchId);

        //Seluruh Shift
    @EntityGraph(attributePaths = {"cashier", "branch", "partner"})
    @Query("SELECT s FROM CashierShift s ORDER BY s.startedAt DESC")
    List<CashierShift> findAllWithDetails();

//     Hitung omzet 
    @Query("""
            SELECT COALESCE(SUM(o.total), 0)
            FROM Orders o
            WHERE o.cashier.id = :cashierId
              AND o.branch.id  = :branchId
              AND o.status     = com.dak.spravel.model.order.Orders$PaymentStatus.PAID
              AND o.createdAt >= :startedAt
              AND o.createdAt <= :endedAt
            """)
    BigDecimal calculateRevenue(
            @Param("cashierId") Long cashierId,
            @Param("branchId") Long branchId,
            @Param("startedAt") java.time.LocalDateTime startedAt,
            @Param("endedAt") java.time.LocalDateTime endedAt);

//    Hitung Jumlah Trnasaksi Saat Order close/Shift Close
    @Query("""
            SELECT COUNT(o)
            FROM Orders o
            WHERE o.cashier.id = :cashierId
              AND o.branch.id  = :branchId
              AND o.status     = com.dak.spravel.model.order.Orders$PaymentStatus.PAID
              AND o.createdAt >= :startedAt
              AND o.createdAt <= :endedAt
            """)
    Long countTransactions(
            @Param("cashierId") Long cashierId,
            @Param("branchId") Long branchId,
            @Param("startedAt") java.time.LocalDateTime startedAt,
            @Param("endedAt") java.time.LocalDateTime endedAt);

//    Omzet Pembayaran
    @Query("""
            SELECT COALESCE(SUM(o.total), 0)
            FROM Orders o
            JOIN o.payments p
            WHERE o.cashier.id = :cashierId
              AND o.branch.id  = :branchId
              AND o.status     = com.dak.spravel.model.order.Orders$PaymentStatus.PAID
              AND p.method     = :method
              AND p.status     = com.dak.spravel.model.order.Payments$Status.VERIFIED
              AND o.createdAt >= :startedAt
              AND o.createdAt <= :endedAt
            """)
    BigDecimal calculateRevenueByMethod(
            @Param("cashierId") Long cashierId,
            @Param("branchId") Long branchId,
            @Param("startedAt") java.time.LocalDateTime startedAt,
            @Param("endedAt") java.time.LocalDateTime endedAt,
            @Param("method") com.dak.spravel.model.order.Payments.Method method);

     // Jumlah transaksi PAID yang dibayar dengan metode tertentu.  
    @Query("""
            SELECT COUNT(DISTINCT o.id)
            FROM Orders o
            JOIN o.payments p
            WHERE o.cashier.id = :cashierId
              AND o.branch.id  = :branchId
              AND o.status     = com.dak.spravel.model.order.Orders$PaymentStatus.PAID
              AND p.method     = :method
              AND p.status     = com.dak.spravel.model.order.Payments$Status.VERIFIED
              AND o.createdAt >= :startedAt
              AND o.createdAt <= :endedAt
            """)
    Long countTransactionsByMethod(
            @Param("cashierId") Long cashierId,
            @Param("branchId") Long branchId,
            @Param("startedAt") java.time.LocalDateTime startedAt,
            @Param("endedAt") java.time.LocalDateTime endedAt,
            @Param("method") com.dak.spravel.model.order.Payments.Method method);
}
