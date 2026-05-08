package com.dak.spravel.repository.inventory;

import com.dak.spravel.model.common.Partners;
import com.dak.spravel.model.inventory.Warehouses;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WarehousesRepository extends JpaRepository<Warehouses, Long> {

    List<Warehouses> findByPartners(Partners partner);
    List<Warehouses> findByIsActiveAndPartners(Boolean isActive, Partners partner);
}