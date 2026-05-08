package com.dak.spravel.repository.catalog;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.dak.spravel.model.catalog.CategoryProduct;
import com.dak.spravel.model.common.Partners;


@Repository
public interface CategoryProductRepository extends JpaRepository<CategoryProduct, Long> {
    boolean existsByName(String name);
    boolean existsByNameAndPartnerId(String name, Long partnerId);
    List<CategoryProduct> findAllByPartner(Partners partner, Sort sort);
    Page<CategoryProduct> findAllByPartner(Partners partner, Pageable pageable);
}
