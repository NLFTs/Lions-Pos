package com.dak.spravel.service.catalog;

import com.dak.spravel.dto.request.product.ProductRequest;
import com.dak.spravel.model.catalog.CategoryProduct;
import com.dak.spravel.model.catalog.Product;
import com.dak.spravel.model.common.Partners;
import com.dak.spravel.repository.catalog.CategoryProductRepository;
import com.dak.spravel.repository.catalog.ProductRepository;
import com.dak.spravel.repository.common.PartnerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final PartnerRepository partnerRepository;
    private final CategoryProductRepository categoryRepository;

    public Product create(ProductRequest request) {

        Partners partner = partnerRepository.findById(request.getPartnerId())
                .orElseThrow(() -> new RuntimeException("Partner not found"));

        CategoryProduct category = null;

        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
        }

        Product product = new Product();
        product.setPartner(partner);
        product.setCategory(category);
        product.setName(request.getName());
        product.setSku(request.getSku());
        product.setBasePrice(request.getBasePrice());

        return productRepository.save(product);
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    public void delete(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        productRepository.delete(product);
    }
}