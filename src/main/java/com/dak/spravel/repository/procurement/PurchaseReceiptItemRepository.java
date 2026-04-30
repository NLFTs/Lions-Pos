package com.dak.spravel.repository.procurement;

import com.dak.spravel.model.procurement.PurchaseReceiptItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PurchaseReceiptItemRepository  extends JpaRepository<PurchaseReceiptItem, UUID> {
    List<PurchaseReceiptItem> findByPurchaseReceiptId(UUID purchaseReceiptId);

    List<PurchaseReceiptItem> findByProductId(UUID productId);

    List<PurchaseReceiptItem> findByPurchaseReceiptIdAndProductId(UUID purchaseReceiptId, UUID productId);
}
