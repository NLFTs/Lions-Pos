package com.fts.twin.repository.order;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fts.twin.model.order.OrderItems;
@Repository
public interface OrderItemsRepository extends JpaRepository <OrderItems, Long> {
    Optional <OrderItems> findByProductName(String productName);
}
