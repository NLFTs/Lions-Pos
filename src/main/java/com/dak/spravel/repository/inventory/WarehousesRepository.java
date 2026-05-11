package com.dak.spravel.repository.inventory;

import com.dak.spravel.model.inventory.Warehouses;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WarehousesRepository extends JpaRepository<Warehouses, Long> {

    List<Warehouses> findByPartnersId(long partnersId);

    List<Warehouses> findByIsActive(Boolean isActive);
}