package com.dak.spravel.repository.inventory;

import com.dak.spravel.model.common.Partners;
import com.dak.spravel.model.inventory.Warehouses;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WarehousesRepository extends JpaRepository<Warehouses, Long> {

<<<<<<< HEAD
    List<Warehouses> findByPartnerId(Long partnerId);

    List<Warehouses> findByIsActive(Boolean isActive);
=======
    List<Warehouses> findByPartners(Partners partner);
    List<Warehouses> findByIsActiveAndPartners(Boolean isActive, Partners partner);
>>>>>>> b0700c3517d5b13fa75f6b89ef296ac7ff417635
}