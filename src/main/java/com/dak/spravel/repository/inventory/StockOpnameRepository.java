package com.dak.spravel.repository.inventory;

import com.dak.spravel.model.inventory.StockOpname;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockOpnameRepository extends JpaRepository<StockOpname, Long> {

    List<StockOpname> findByPartnerId(Long partnerId);

    List<StockOpname> findByStatus(StockOpname.Status status);

    List<StockOpname> findByDeletedAtIsNull();

    List<StockOpname> findByPartnerIdAndDeletedAtIsNull(Long partnerId);

    Page<StockOpname> findByPartnerIdAndDeletedAtIsNull(Long partnerId, Pageable pageable);

    Page<StockOpname> findByDeletedAtIsNull(Pageable pageable);

}