package com.fts.twin.repository.order;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.fts.twin.model.order.Orders;

@Repository
public interface OrdersRepository extends JpaRepository <Orders, Long> {
    Optional<Orders> findByOrderNumber(String orderNumber);
    Optional<Orders> findByPartnerIdAndOrderNumber(Long partnerId, String orderNumber);

    @EntityGraph(attributePaths = {"items", "payments", "branch", "createdBy", "voucher"})
    // Untuk Get Data — diurutkan dari yang terbaru
    @Query("SELECT o FROM Orders o ORDER BY o.createdAt DESC")
    List<Orders> findAllWithDetails();
    
    // Untuk POST Data
    @EntityGraph(attributePaths = {"items", "payments", "branch", "cashier", "voucher"})
    @Query("SELECT o FROM Orders o WHERE o.id = :id")
    Optional<Orders> findByIdWithDetails(Long id);
}
