package com.dak.spravel.repository.procurement;

import com.dak.spravel.model.procurement.PurchaseReceipt;

import java.util.List;
import java.util.UUID;

public interface PurchaseReceiptRepository {

    List<PurchaseReceipt> findByPartnerId(UUID partnerId);

    List<PurchaseReceipt> findByStatus(String status);
}
