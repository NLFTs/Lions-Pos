package com.dak.spravel.repository.catalog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.dak.spravel.model.catalog.Product;
import com.dak.spravel.model.common.Partners;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsBySkuAndPartnerId(String sku, Long partnerId);
    
    List<Product> findAllByPartner(Partners partner);

    Product findByIdAndPartner(Long id, Partners partner);

    boolean existsBySkuAndPartner(String sku, Partners partner);
}
