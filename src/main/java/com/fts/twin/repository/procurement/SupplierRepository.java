package com.fts.twin.repository.procurement;

import com.fts.twin.model.procurement.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    // Untuk Admin: List & Page (Sudah ada findAll bawaan JpaRepository)

    // Untuk Partner: Filter berdasarkan partner_id dan data yang belum di-soft delete
    List<Supplier> findByPartnerIdAndDeletedAtIsNull(Long partnerId);

    // Untuk Partner: Pagination data supplier
    Page<Supplier> findByPartnerIdAndDeletedAtIsNull(Long partnerId, Pageable pageable);

    // Validasi duplikasi nama supplier dalam satu partner yang sama
    boolean existsByNameAndPartnerIdAndDeletedAtIsNull(String name, Long partnerId);
}