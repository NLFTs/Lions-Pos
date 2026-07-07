package com.fts.twin.repository.catalog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.fts.twin.model.catalog.ProductPhoto;

import java.util.List;

@Repository
public interface ProductPhotoRepository extends JpaRepository<ProductPhoto, Long> {
    List<ProductPhoto> findByProductId(Long productId);
    long countByUrl(String url);
}
