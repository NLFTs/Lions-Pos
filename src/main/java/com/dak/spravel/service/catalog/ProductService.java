package com.dak.spravel.service.catalog;

import com.dak.spravel.dto.request.product.ProductRequest;
import com.dak.spravel.handler.ResourceNotFoundException;
import com.dak.spravel.model.auth.User;
import com.dak.spravel.model.catalog.CategoryProduct;
import com.dak.spravel.model.catalog.Product;
import com.dak.spravel.model.common.Partners;
import com.dak.spravel.repository.auth.UserRepository;
import com.dak.spravel.repository.catalog.CategoryProductRepository;
import com.dak.spravel.repository.catalog.ProductRepository;
import com.dak.spravel.util.AuditHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryProductRepository categoryProductRepository;
    private final UserRepository userRepository;

    private User getAuthenticatedUser() {

        Authentication auth = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (auth == null
                || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getName())) {

            throw new RuntimeException("User tidak terautentikasi");
        }

        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() ->
                        new RuntimeException("User tidak ditemukan"));

        // VALIDASI SUPER ADMIN & ADMIN
        if (isAdmin(user)) {
            throw new RuntimeException(
                    "Akses Ditolak: Admin tidak diperbolehkan mengelola Product."
            );
        }

        return user;
    }


    private boolean isAdmin(User user) {

        return user.getRoles().stream()
                .anyMatch(role ->
                        role.getSlug().equals("super_admin")
                                || role.getSlug().equals("admin")
                );
    }

    private Product getValidatedProduct(Long id, User currentUser) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product", id));

        if (currentUser.getPartner() == null
                || !product.getPartner().getId()
                .equals(currentUser.getPartner().getId())) {

            throw new RuntimeException(
                    "Akses Ditolak: Product beda partner."
            );
        }

        return product;
    }

    private CategoryProduct getValidatedCategory(Long id, User currentUser) {

        CategoryProduct category = categoryProductRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("CategoryProduct", id));

        if (currentUser.getPartner() == null
                || !category.getPartner().getId()
                .equals(currentUser.getPartner().getId())) {

            throw new RuntimeException(
                    "Akses Ditolak: Category beda partner."
            );
        }

        return category;
    }

    public List<Product> findAll(Long partnerId) {

        User currentUser = getAuthenticatedUser();

        return productRepository.findAllByPartnerId(
                currentUser.getPartner().getId()
        );
    }

    public Product findById(Long id, Long partnerId) {

        User currentUser = getAuthenticatedUser();

        return getValidatedProduct(id, currentUser);
    }

    @Transactional
    public Product create(ProductRequest request, Long partnerId) {

        User currentUser = getAuthenticatedUser();

        Partners partner = currentUser.getPartner();

        if (partner == null) {
            throw new RuntimeException(
                    "User tidak punya partner."
            );
        }

        CategoryProduct category = getValidatedCategory(
                request.getCategoryId(),
                currentUser
        );

        if (request.getSku() != null
                && productRepository.existsBySkuAndPartnerId(
                request.getSku(),
                partner.getId()
        )) {

            throw new RuntimeException(
                    "SKU sudah digunakan."
            );
        }

        Product product = new Product();

        product.setPartner(partner);
        product.setCategory(category);
        product.setName(request.getName());
        product.setSku(request.getSku());
        product.setBasePrice(request.getBasePrice());

        if (request.getTrackStock() != null) {
            product.setTrackStock(request.getTrackStock());
        } else {
            product.setTrackStock(true);
        }

        product.setIsActive(true);

        AuditHelper.setCreated(product);

        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(Long id,
                                 ProductRequest request,
                                 Long partnerId) {

        User currentUser = getAuthenticatedUser();

        Product product = getValidatedProduct(id, currentUser);

        CategoryProduct category = getValidatedCategory(
                request.getCategoryId(),
                currentUser
        );

        product.setCategory(category);
        product.setName(request.getName());
        product.setSku(request.getSku());
        product.setBasePrice(request.getBasePrice());

        if (request.getTrackStock() != null) {
            product.setTrackStock(request.getTrackStock());
        }

        AuditHelper.setUpdated(product);

        return productRepository.save(product);
    }

    @Transactional
    public void delete(Long id, Long partnerId) {

        User currentUser = getAuthenticatedUser();

        Product product = getValidatedProduct(id, currentUser);

        productRepository.delete(product);
    }

    @Transactional
    public Product softDeleteProduct(Long id, Long partnerId) {

        User currentUser = getAuthenticatedUser();

        Product product = getValidatedProduct(id, currentUser);

        product.setDeletedAt(LocalDateTime.now());
        product.setDeletedBy(currentUser);
        product.setIsActive(false);

        AuditHelper.setUpdated(product);

        return productRepository.save(product);
    }

    @Transactional
    public Product restoreProduct(Long id, Long partnerId) {

        User currentUser = getAuthenticatedUser();

        Product product = getValidatedProduct(id, currentUser);

        product.setDeletedAt(null);
        product.setDeletedBy(null);
        product.setIsActive(true);

        AuditHelper.setUpdated(product);

        return productRepository.save(product);
    }


    @Transactional
    public Product setTrueTrackStock(Long id, Long partnerId) {

        User currentUser = getAuthenticatedUser();

        Product product = getValidatedProduct(id, currentUser);

        product.setTrackStock(true);

        AuditHelper.setUpdated(product);

        return productRepository.save(product);
    }

    @Transactional
    public Product setFalseTrackStock(Long id, Long partnerId) {

        User currentUser = getAuthenticatedUser();

        Product product = getValidatedProduct(id, currentUser);

        product.setTrackStock(false);

        AuditHelper.setUpdated(product);

        return productRepository.save(product);
    }
}