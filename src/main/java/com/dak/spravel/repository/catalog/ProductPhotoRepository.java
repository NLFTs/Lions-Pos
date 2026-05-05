package com.dak.spravel.repository.catalog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.dak.spravel.model.catalog.ProductPhoto;

import java.util.List;

@Repository
public interface ProductPhotoRepository extends JpaRepository<ProductPhoto, Long> {
    List<ProductPhoto> findByProductId(Long productId);
}
