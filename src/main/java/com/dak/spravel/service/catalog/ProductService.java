package com.dak.spravel.service.catalog;

import com.dak.spravel.dto.request.product.ProductRequest;
import com.dak.spravel.model.catalog.CategoryProduct;
import com.dak.spravel.model.catalog.Product;
import com.dak.spravel.model.common.Partners;
import com.dak.spravel.repository.catalog.CategoryProductRepository;
import com.dak.spravel.repository.catalog.ProductRepository;
import com.dak.spravel.repository.common.PartnerRepository;
import com.dak.spravel.util.AuditHelper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final PartnerRepository partnerRepository;
    private final CategoryProductRepository categoryRepository;

    public Product create(ProductRequest request, Long partnerId) {

        Partners partner = partnerRepository.findById(partnerId)
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

        if (request.getSku() != null && !request.getSku().trim().isEmpty()) {
            // Validasi manual SKU: Cek apakah SKU manual sudah ada di DB
            if (productRepository.existsBySkuAndPartnerId(request.getSku().trim().toUpperCase(), partnerId)) {
                throw new RuntimeException("SKU " + request.getSku() + " sudah terdaftar!");
            }
        }

        if (product.getSku() == null || product.getSku().trim().isEmpty()) {
            // Jika tidak diinput, generate otomatis
            String generatedSku = generateUniqueSku(product.getName(), partnerId);
            product.setSku(generatedSku);
        } else {
            // Jika diinput manual, pastikan dalam format Uppercase (Opsional tapi disarankan)
            product.setSku(product.getSku().trim().toUpperCase());
            
            // 2. Validasi manual SKU: Cek apakah SKU manual sudah ada di DB
            if (productRepository.existsBySkuAndPartnerId(product.getSku(), partnerId)) {
                throw new RuntimeException("SKU " + product.getSku() + " sudah terdaftar!");
            }
        }
        product.setBasePrice(request.getBasePrice());
        AuditHelper.setCreated(product);

        return productRepository.save(product);
    }

    private String generateUniqueSku(String name, Long partnerId) {
        String newSku;
        do {
            String cleanName = name.replaceAll("[^a-zA-Z]", "").toUpperCase();
            String prefix;
            if (cleanName.length() >= 3) {
                prefix = "" + cleanName.charAt(0) + cleanName.charAt(cleanName.length() / 2) + cleanName.charAt(cleanName.length() - 1);
            } else {
                prefix = (cleanName + "XXX").substring(0, 3);
            }

            int randomDigits = (int) (Math.random() * 900) + 100;
            newSku = prefix + "-" + randomDigits;
        } while (productRepository.existsBySkuAndPartnerId(newSku, partnerId));

        return newSku;
    }

    public List<Product> findAll(Long partnerId) {
        return productRepository.findAllByPartnerId(partnerId);
    }

    public Product findById(Long id, Long partnerId) {
        return productRepository.findByIdAndPartnerId(id, partnerId);
    }

    public void delete(Long id, Long partnerId) {
        Product product = productRepository.findByIdAndPartnerId(id, partnerId);
        if (product == null) {
            throw new RuntimeException("Product not found");
        }
        productRepository.delete(product);
    }

    public Product updateProduct(Long id, ProductRequest request, Long partnerId) {
        Product product = productRepository.findByIdAndPartnerId(id, partnerId);
        if (product == null) {
            throw new RuntimeException("Product not found");
        }

        if (request.getPartnerId() != null && !request.getPartnerId().equals(partnerId)) {
            throw new RuntimeException("Cannot change partner");
        }

        if (request.getCategoryId() != null) {
            CategoryProduct category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            product.setCategory(category);
        }

        if (request.getName() != null) product.setName(request.getName());
        if (request.getSku() != null) product.setSku(request.getSku());
        if (request.getBasePrice() != null) product.setBasePrice(request.getBasePrice());

        AuditHelper.setUpdated(product);
        return productRepository.save(product);
    }

    public Product softDeleteProduct(Long id, Long partnerId) {
        Product product = productRepository.findByIdAndPartnerId(id, partnerId);
        if (product == null) {
            throw new RuntimeException("Product not found");
        }
        product.setIsActive(false);

        AuditHelper.setDeleted(product);
        return productRepository.save(product);
    }

    public Product restoreProduct(Long id, Long partnerId) {
        Product product = productRepository.findByIdAndPartnerId(id, partnerId);
        if (product == null) {
            throw new RuntimeException("Product not found");
        }
        product.setIsActive(true);

        AuditHelper.setUpdated(product);
        return productRepository.save(product);
    }

    public Product setFalseTrackStock(Long id, Long partnerId) {
        Product product = productRepository.findByIdAndPartnerId(id, partnerId);
        if (product == null) {
            throw new RuntimeException("Product not found");
        }
        product.setTrackStock(false);

        AuditHelper.setUpdated(product);
        return productRepository.save(product);
    }

    public Product setTrueTrackStock(Long id, Long partnerId) {
        Product product = productRepository.findByIdAndPartnerId(id, partnerId);
        if (product == null) {
            throw new RuntimeException("Product not found");
        }
        product.setTrackStock(true);

        AuditHelper.setUpdated(product);
        return productRepository.save(product);
    }
}