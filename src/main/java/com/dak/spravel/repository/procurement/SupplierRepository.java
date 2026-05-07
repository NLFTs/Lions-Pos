package com.dak.spravel.repository.procurement;

import com.dak.spravel.model.procurement.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    List<Supplier> findByPartnerId(Long partnerId);
    List<Supplier> findByDeletedAtIsNull();
    List<Supplier> findByPartnerIdAndDeletedAtIsNull(Long partnerId);
}