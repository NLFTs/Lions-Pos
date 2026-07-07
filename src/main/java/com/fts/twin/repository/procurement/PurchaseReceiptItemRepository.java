package com.fts.twin.repository.procurement;

import com.fts.twin.model.procurement.PurchaseReceiptItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseReceiptItemRepository extends JpaRepository<PurchaseReceiptItem, Long> {
    List<PurchaseReceiptItem> findByPurchaseReceiptId(Long purchaseReceiptId);
    List<PurchaseReceiptItem> findByPurchaseOrderItemId(Long purchaseOrderItemId);
}