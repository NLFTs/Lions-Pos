package com.dak.spravel.repository.inventory;

import com.dak.spravel.model.inventory.BranchWarehouses;
import com.dak.spravel.model.inventory.Branches;
import com.dak.spravel.model.inventory.Warehouses;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BranchWarehousesRepository extends JpaRepository<BranchWarehouses, Long> {

    List<BranchWarehouses> findByBranchesId(Long branchesId);

    List<BranchWarehouses> findByWarehousesId(Long warehousesId);

    boolean existsByBranchesIdAndWarehousesId(Branches branches, Warehouses warehouses);

    boolean existsByBranchesAndWarehouses(Branches branch, Warehouses warehouse);

    @Query("SELECT bw.branches.id FROM BranchWarehouses bw WHERE bw.warehouses.id = :warehouseId")
    List<Long> findBranchIdsByWarehouseId(@Param("warehouseId") Long warehouseId);
}