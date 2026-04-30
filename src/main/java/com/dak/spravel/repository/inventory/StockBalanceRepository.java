package com.dak.spravel.repository.inventory;

import com.dak.spravel.model.inventory.StockBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockBalanceRepository extends JpaRepository<StockBalance, Long> {
    Optional<StockBalance>findByProductIdAndLocationTypeAndLocationId(Long productId, String locationType, Long locationId);

    List<StockBalance> findByProductId(Long productId);

    List<StockBalance> findByLocationTypeAndLocationId(String locationType, Long locationId);
}
