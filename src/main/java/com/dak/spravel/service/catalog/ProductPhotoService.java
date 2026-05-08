package com.dak.spravel.service.catalog;

import com.dak.spravel.dto.request.catalog.ProductPhotoRequestDTO;
import com.dak.spravel.handler.ResourceNotFoundException;
import com.dak.spravel.model.auth.User;
import com.dak.spravel.model.catalog.Product;
import com.dak.spravel.model.catalog.ProductPhoto;
import com.dak.spravel.repository.auth.UserRepository;
import com.dak.spravel.repository.catalog.ProductPhotoRepository;
import com.dak.spravel.repository.catalog.ProductRepository;
import com.dak.spravel.util.AuditHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductPhotoService {

    private final ProductPhotoRepository productPhotoRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getName())) {
            throw new RuntimeException("User tidak terautentikasi");
        }

        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

        if (isAdmin(user)) {
            throw new RuntimeException(
                    "Akses Ditolak: Admin tidak diperbolehkan mengelola Product Photo."
            );
        }

        return user;
    }

    private boolean isAdmin(User user) {
        return user.getRoles().stream()
                .anyMatch(role ->
                        role.getSlug().equals("super_admin")
                                || role.getSlug().equals("admin"));
    }

    private Product validateProduct(Long productId, User currentUser) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product", productId));

        if (currentUser.getPartner() == null
                || !product.getPartner().getId()
                .equals(currentUser.getPartner().getId())) {

            throw new RuntimeException(
                    "Akses Ditolak: Product berasal dari partner lain."
            );
        }

        return product;
    }

    private ProductPhoto validateProductPhoto(Long id, User currentUser) {

        ProductPhoto productPhoto = productPhotoRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("ProductPhoto", id));

        if (currentUser.getPartner() == null
                || !productPhoto.getProduct().getPartner().getId()
                .equals(currentUser.getPartner().getId())) {

            throw new RuntimeException(
                    "Akses Ditolak: Product photo berasal dari partner lain."
            );
        }

        return productPhoto;
    }

    public List<ProductPhoto> findByProductId(Long productId) {

        User currentUser = getAuthenticatedUser();

        Product product = validateProduct(productId, currentUser);

        return productPhotoRepository.findByProductId(product.getId());
    }

    public ProductPhoto findById(Long id) {

        User currentUser = getAuthenticatedUser();

        return validateProductPhoto(id, currentUser);
    }

    @Transactional
    public ProductPhoto create(ProductPhotoRequestDTO request) {

        User currentUser = getAuthenticatedUser();

        Product product = validateProduct(
                request.getProductId(),
                currentUser
        );

        ProductPhoto productPhoto = new ProductPhoto();

        productPhoto.setProduct(product);
        productPhoto.setUrl(request.getUrl());
        productPhoto.setIsPrimary(request.getIsPrimary());
        productPhoto.setSortOrder(request.getSortOrder());

        AuditHelper.setCreated(productPhoto);

        return productPhotoRepository.save(productPhoto);
    }

    @Transactional
    public ProductPhoto update(Long id, ProductPhotoRequestDTO request) {

        User currentUser = getAuthenticatedUser();

        ProductPhoto productPhoto = validateProductPhoto(id, currentUser);

        Product product = validateProduct(
                request.getProductId(),
                currentUser
        );

        productPhoto.setProduct(product);
        productPhoto.setUrl(request.getUrl());
        productPhoto.setIsPrimary(request.getIsPrimary());
        productPhoto.setSortOrder(request.getSortOrder());

        AuditHelper.setUpdated(productPhoto);

        return productPhotoRepository.save(productPhoto);
    }

    @Transactional
    public void delete(Long id) {

        User currentUser = getAuthenticatedUser();

        ProductPhoto productPhoto = validateProductPhoto(id, currentUser);

        productPhotoRepository.delete(productPhoto);
    }
}