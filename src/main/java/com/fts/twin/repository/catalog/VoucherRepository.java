package com.fts.twin.repository.catalog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.fts.twin.model.catalog.Voucher;
import com.fts.twin.model.common.Partners;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    Optional<Voucher> findByCode(String code);
    List<Voucher> findAllByPartner(Partners partner);
    boolean existsByCode(String code);
}
