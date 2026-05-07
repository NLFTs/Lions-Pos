package com.dak.spravel.repository.catalog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.dak.spravel.model.catalog.Product;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsBySkuAndPartnerId(String sku, Long partnerId);
    List<Product> findAllByPartnerId(Long partnerId);
    Product findByIdAndPartnerId(Long id, Long partnerId);
}
