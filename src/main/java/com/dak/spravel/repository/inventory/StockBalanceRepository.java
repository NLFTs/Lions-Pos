package com.dak.spravel.repository.inventory;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dak.spravel.model.inventory.StockBalance;

@Repository
public interface StockBalanceRepository extends JpaRepository<StockBalance, Long> {
    Optional<StockBalance>findByProductIdAndLocationTypeAndLocationId(Long productId, String locationType, Long locationId);

    List<StockBalance> findByProductPartnerId(Long partnerId);

    Page<StockBalance> findByProductPartnerId(Long partnerId, Pageable pageable);

    List<StockBalance> findByProductId(Long productId);

    List<StockBalance> findByLocationTypeAndLocationId(String locationType, Long locationId);
    Page<StockBalance> findByProductPartnerIdAndLocationId(Long partnerId, Long locationId, Pageable pageable);
    Page<StockBalance> findByLocationTypeAndLocationId(String locationType, Long locationId, org.springframework.data.domain.Pageable pageable);
}
