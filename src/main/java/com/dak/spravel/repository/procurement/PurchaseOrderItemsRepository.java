package com.dak.spravel.repository.procurement;

import com.dak.spravel.model.procurement.PurchaseOrderItems;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PurchaseOrderItemsRepository extends JpaRepository<PurchaseOrderItems, UUID> {
    List<PurchaseOrderItems> findByPurchaseOrderId(UUID purchaseOrderId);

}