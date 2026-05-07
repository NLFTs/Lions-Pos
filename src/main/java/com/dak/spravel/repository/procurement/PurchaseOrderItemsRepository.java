package com.dak.spravel.repository.procurement;

import com.dak.spravel.model.procurement.PurchaseOrderItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseOrderItemsRepository extends JpaRepository<PurchaseOrderItems, Long> {
    List<PurchaseOrderItems> findByPurchaseOrderId(Long purchaseOrderId);
}