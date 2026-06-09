package com.dak.spravel.repository.procurement;

import com.dak.spravel.model.procurement.PurchaseOrder;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    List<PurchaseOrder> findByPartnerId(Long partnerId);
    List<PurchaseOrder> findByStatus(PurchaseOrder.Status status);
    List<PurchaseOrder> findByDeletedAtIsNull();
    List<PurchaseOrder> findByPartnerIdAndDeletedAtIsNull(Long partnerId, Sort sort);
    Page<PurchaseOrder> findByPartnerIdAndDeletedAtIsNull(Long partnerId, Pageable pageable); // tambahan
    long countByPoNumberStartingWith(String prefix);
}