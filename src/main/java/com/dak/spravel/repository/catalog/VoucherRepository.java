package com.dak.spravel.repository.catalog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.dak.spravel.model.catalog.Voucher;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    
}
