package com.dak.spravel.service.catalog;

import com.dak.spravel.dto.request.catalog.ProductPhotoRequestDTO;
import com.dak.spravel.handler.ResourceNotFoundException;
import com.dak.spravel.model.catalog.Product;
import com.dak.spravel.model.catalog.ProductPhoto;
import com.dak.spravel.repository.catalog.ProductPhotoRepository;
import com.dak.spravel.repository.catalog.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductPhotoService {

    private final ProductPhotoRepository productPhotoRepository;
    private final ProductRepository productRepository;

    // GET ALL BY PRODUCT
    public List<ProductPhoto> findByProductId(Long productId) {
        return productPhotoRepository.findByProductId(productId);
    }

    // GET BY ID
    public ProductPhoto findById(Long id) {
        return productPhotoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductPhoto", id));
    }

    // CREATE
    public ProductPhoto create(ProductPhotoRequestDTO request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", request.getProductId()));

        ProductPhoto photo = new ProductPhoto();
        photo.setProduct(product);
        photo.setUrl(request.getUrl());
        photo.setIsPrimary(request.getIsPrimary() != null ? request.getIsPrimary() : false);
        photo.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);

        return productPhotoRepository.save(photo);
    }

    // UPDATE
    public ProductPhoto update(Long id, ProductPhotoRequestDTO request) {
        ProductPhoto photo = productPhotoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductPhoto", id));

        if (request.getUrl() != null) photo.setUrl(request.getUrl());
        if (request.getIsPrimary() != null) photo.setIsPrimary(request.getIsPrimary());
        if (request.getSortOrder() != null) photo.setSortOrder(request.getSortOrder());

        return productPhotoRepository.save(photo);
    }

    // DELETE
    public void delete(Long id) {
        if (!productPhotoRepository.existsById(id)) {
            throw new ResourceNotFoundException("ProductPhoto", id);
        }
        productPhotoRepository.deleteById(id);
    }
}