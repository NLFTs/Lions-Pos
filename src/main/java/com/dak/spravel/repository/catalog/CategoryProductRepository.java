package com.dak.spravel.repository.catalog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dak.spravel.model.catalog.CategoryProduct;

@Repository
public interface CategoryProductRepository extends JpaRepository<CategoryProduct, Long> {
    boolean existsByName(String name);
    boolean existsByNameAndPartnerId(String name, Long partnerId);
}
