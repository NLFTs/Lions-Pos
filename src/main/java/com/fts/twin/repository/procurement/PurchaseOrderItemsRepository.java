package com.fts.twin.repository.procurement;

import com.fts.twin.model.procurement.PurchaseOrderItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseOrderItemsRepository extends JpaRepository<PurchaseOrderItems, Long> {
    List<PurchaseOrderItems> findByPurchaseOrderId(Long purchaseOrderId);
}