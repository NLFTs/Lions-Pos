package com.dak.spravel.repository.catalog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.dak.spravel.model.catalog.Product;
<<<<<<< HEAD
=======
import com.dak.spravel.model.common.Partners;

>>>>>>> b0700c3517d5b13fa75f6b89ef296ac7ff417635
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsBySkuAndPartnerId(String sku, Long partnerId);
<<<<<<< HEAD
    List<Product> findAllByPartnerId(Long partnerId);
    Product findByIdAndPartnerId(Long id, Long partnerId);
=======
    
    List<Product> findAllByPartner(Partners partner);

    Product findByIdAndPartner(Long id, Partners partner);

    boolean existsBySkuAndPartner(String sku, Partners partner);
>>>>>>> b0700c3517d5b13fa75f6b89ef296ac7ff417635
}
