package com.dak.spravel.repository.inventory;

import com.dak.spravel.model.inventory.BranchWarehouses;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BranchWarehousesRepository extends JpaRepository<BranchWarehouses, Long> {

    List<BranchWarehouses> findByBranchesId(Long branchesId);

    List<BranchWarehouses> findByWarehousesId(Long warehousesId);

    boolean existsByBranchesIdAndWarehousesId(Long branchesId, Long warehousesId);
}