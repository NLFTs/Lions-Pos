package com.dak.spravel.repository.inventory;

import com.dak.spravel.model.inventory.Warehouses;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WarehousesRepository extends JpaRepository<Warehouses, Long> {

    // Untuk Partner: Ambil yang aktif & belum dihapus
    List<Warehouses> findByPartnersIdAndDeletedAtIsNull(Long partnersId);

    // Untuk Partner: Pagination data warehouse
    Page<Warehouses> findByPartnersIdAndDeletedAtIsNull(Long partnersId, Pageable pageable);

    // Validasi duplikasi nama warehouse dalam satu partner
    boolean existsByNameAndPartnersIdAndDeletedAtIsNull(String name, Long partnersId);
}