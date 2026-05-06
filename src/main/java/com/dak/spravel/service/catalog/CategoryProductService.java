package com.dak.spravel.service.catalog;

import com.dak.spravel.dto.request.catalog.CategoryProductCreate;
import com.dak.spravel.handler.ResourceNotFoundException;
import com.dak.spravel.model.catalog.CategoryProduct;
import com.dak.spravel.model.common.Partners;
import com.dak.spravel.repository.catalog.CategoryProductRepository;
import com.dak.spravel.repository.common.PartnerRepository;
import com.dak.spravel.util.AuditHelper;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryProductService {

    private final CategoryProductRepository categoryProductRepository;
    private final PartnerRepository partnersRepository;

    // GET ALL
    public List<CategoryProduct> findAll() {
        return categoryProductRepository.findAll(Sort.by("name").ascending());
    }

    // GET ALL PAGINATED
    public Page<CategoryProduct> findAll(int page, int size) {
        return categoryProductRepository
                .findAll(PageRequest.of(page, size, Sort.by("name").ascending()));
    }

    // GET BY ID
    public CategoryProduct findById(Long id) {
        return categoryProductRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CategoryProduct", id));
    }

    // CREATE
    public CategoryProduct create(CategoryProductCreate request) {
        Partners partner = partnersRepository.findById(request.getPartnerId())
                .orElseThrow(() -> new ResourceNotFoundException("Partner", request.getPartnerId()));

        CategoryProduct parent = null;
        if (request.getParentId() != null) {
            parent = categoryProductRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category", request.getParentId()));
        }

        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }

        if (categoryProductRepository.existsByNameAndPartnerId(request.getName(), request.getPartnerId())) {
            throw new IllegalArgumentException("Category with the name " + request.getName() + " already exists for this partner");
        }



        CategoryProduct category = new CategoryProduct();
        category.setPartner(partner);
        category.setParent(parent);
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setSortOrder(request.getSortOrder());

        AuditHelper.setCreated(category);

        return categoryProductRepository.save(category);
    }

    // UPDATE
    public CategoryProduct update(Long id, CategoryProductCreate request) {
        CategoryProduct category = categoryProductRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CategoryProduct", id));

        Partners partner = partnersRepository.findById(request.getPartnerId())
                .orElseThrow(() -> new ResourceNotFoundException("Partner", request.getPartnerId()));

        CategoryProduct parent = null;
        if (request.getParentId() != null) {
            parent = categoryProductRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category", request.getParentId()));
        }

        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }

        if (categoryProductRepository.existsByNameAndPartnerId(request.getName(), request.getPartnerId())
                && !category.getName().equals(request.getName())) {
            throw new IllegalArgumentException("Category with the name " + request.getName() + " already exists for this partner");
        }

        category.setPartner(partner);
        category.setParent(parent);
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setSortOrder(request.getSortOrder());

        AuditHelper.setUpdated(category);

        return categoryProductRepository.save(category);
    }

    // DELETE
    public void delete(Long id) {
        if (!categoryProductRepository.existsById(id)) {
            throw new ResourceNotFoundException("CategoryProduct", id);
        }
        categoryProductRepository.deleteById(id);
    }
}