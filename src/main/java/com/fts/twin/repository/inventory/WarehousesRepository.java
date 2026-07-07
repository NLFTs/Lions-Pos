package com.fts.twin.repository.inventory;

import com.fts.twin.model.inventory.Warehouses;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WarehousesRepository extends JpaRepository<Warehouses, Long> {

    List<Warehouses> findByPartnersIdAndDeletedAtIsNull(Long partnersId);

    Page<Warehouses> findByPartnersIdAndDeletedAtIsNull(Long partnersId, Pageable pageable);

    boolean existsByNameAndPartnersIdAndDeletedAtIsNull(String name, Long partnersId);

    @Query("SELECT COUNT(bw) > 0 FROM BranchWarehouses bw " + "WHERE bw.warehouses.id = :warehouseId AND bw.branches.id = :branchId")
    boolean isWarehouseLinkedToBranch(@Param("warehouseId") Long warehouseId, @Param("branchId") Long branchId);
}