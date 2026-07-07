package com.fts.twin.repository.procurement;

import com.fts.twin.model.procurement.PurchaseReceipt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseReceiptRepository extends JpaRepository<PurchaseReceipt, Long> {

    

    List<PurchaseReceipt> findByPurchaseOrderId(Long purchaseOrderId);

    // Untuk pagination di findAll(page, size) — filter by partner
    Page<PurchaseReceipt> findAllByPurchaseOrderPartnerId(Long partnerId, Pageable pageable);
}