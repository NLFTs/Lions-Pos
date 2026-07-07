package com.fts.twin.repository.inventory;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fts.twin.model.inventory.StockBalance;

@Repository
public interface StockBalanceRepository extends JpaRepository<StockBalance, Long> {
    Optional<StockBalance>findByProductIdAndLocationTypeAndLocationId(Long productId, String locationType, Long locationId);
    
    List<StockBalance> findByProductPartnerId(Long partnerId);

    Page<StockBalance> findByProductPartnerId(Long partnerId, Pageable pageable);

    List<StockBalance> findByProductId(Long productId);

    List<StockBalance> findByLocationTypeAndLocationId(String locationType, Long locationId);
    
    Page<StockBalance> findByProductPartnerIdAndLocationId(Long partnerId, Long locationId, Pageable pageable);
    
    Page<StockBalance> findByLocationTypeAndLocationId(String locationType, Long locationId, Pageable pageable);

    @Query("SELECT sb FROM StockBalance sb WHERE (UPPER(sb.locationType) = 'BRANCH' AND sb.locationId = :branchId) OR " +
           "(UPPER(sb.locationType) = 'WAREHOUSE' AND sb.locationId IN " +
           "(SELECT bw.warehouses.id FROM BranchWarehouses bw WHERE bw.branches.id = :branchId))")
    Page<StockBalance> findStockByBranchAndLinkedWarehouses(@Param("branchId") Long branchId, Pageable pageable);
}
