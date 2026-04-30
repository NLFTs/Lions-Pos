package com.dak.spravel.repository.catalog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.dak.spravel.model.catalog.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
}
