package com.dak.spravel.repository.catalog;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.dak.spravel.model.catalog.Product;
import com.dak.spravel.model.common.Partners;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT p FROM Product p")
    List<Product> findAllProduct();

    Page<Product> findAllByPartner(Partners partner, Pageable pageable);

    List<Product> findAllByPartner(Partners partner);
    Product findByIdAndPartner(Long id, Partners partner);
    boolean existsBySkuAndPartnerId(String sku, Long partnerId);
    boolean existsBySkuAndPartner(String sku, Partners partner);
    long countByCategoryId(Long categoryId);

}
