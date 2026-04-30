package com.dak.spravel.repository.procurement;

import com.dak.spravel.model.procurement.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, UUID> {

    List<PurchaseOrder>findByPartnerId(UUID partnerId);

    List<PurchaseOrder>findByStatus(String Status);

}
