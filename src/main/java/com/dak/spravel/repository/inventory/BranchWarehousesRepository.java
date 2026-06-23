package com.dak.spravel.repository.inventory;

import com.dak.spravel.model.inventory.BranchWarehouses;
import com.dak.spravel.model.inventory.Branches;
import com.dak.spravel.model.inventory.Warehouses;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BranchWarehousesRepository extends JpaRepository<BranchWarehouses, Long> {

    List<BranchWarehouses> findByBranchesId(Long branchesId);

    List<BranchWarehouses> findByWarehousesId(Long warehousesId);

    boolean existsByBranchesIdAndWarehousesId(Branches branches, Warehouses warehouses);

    boolean existsByBranchesAndWarehouses(Branches branch, Warehouses warehouse);

    @Query("SELECT bw.warehouses FROM BranchWarehouses bw " +
            "WHERE bw.branches.id = :branchId " +
            "AND bw.warehouses.deletedAt IS NULL " +
            "AND (:partnerId IS NULL OR bw.warehouses.partners.id = :partnerId)")
    List<Warehouses> findWarehousesByBranchIdAndPartner(
            @Param("branchId") Long branchId, 
            @Param("partnerId") Long partnerId);

    @Query("SELECT bw.warehouses FROM BranchWarehouses bw " +
            "WHERE bw.branches.id = :branchId " +
            "AND bw.warehouses.deletedAt IS NULL " +
            "AND (:partnerId IS NULL OR bw.warehouses.partners.id = :partnerId)")
    Page<Warehouses> findWarehousesByBranchIdAndPartner(
            @Param("branchId") Long branchId,
            @Param("partnerId") Long partnerId,
            Pageable pageable);
}