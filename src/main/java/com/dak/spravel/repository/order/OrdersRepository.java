package com.dak.spravel.repository.order;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.dak.spravel.model.order.Orders;

@Repository
public interface OrdersRepository extends JpaRepository <Orders, Long> {
    Optional<Orders> findByOrderNumber(String orderNumber);
}
