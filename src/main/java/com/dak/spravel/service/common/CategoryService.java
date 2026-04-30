package com.dak.spravel.service.common;

import com.dak.spravel.dto.request.user.CreateCategoryRequest;
import com.dak.spravel.dto.request.user.UpdateCategoryRequest;
import com.dak.spravel.dto.response.CategoryResponse;
import com.dak.spravel.handler.ResourceNotFoundException;
import com.dak.spravel.model.common.Category;
import com.dak.spravel.repository.common.CategoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import lombok.RequiredArgsConstructor;

/**
 * Business logic for categories with uniqueness validation on name.
 */
@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public List<CategoryResponse> findAll() {
        return categoryRepository.findAll(Sort.by("name").ascending())
                .stream().map(this::toResponse).toList();
    }

    public Page<CategoryResponse> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        return categoryRepository.findAll(pageable).map(this::toResponse);
    }

    public CategoryResponse findById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
        return toResponse(category);
    }

    public CategoryResponse create(CreateCategoryRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Category name '" + request.getName() + "' already exists");
        }
        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        return toResponse(categoryRepository.save(category));
    }

    public CategoryResponse update(Long id, UpdateCategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
        if (request.getName() != null) {
            if (!request.getName().equals(category.getName()) && categoryRepository.existsByName(request.getName())) {
                throw new IllegalArgumentException("Category name '" + request.getName() + "' already exists");
            }
            category.setName(request.getName());
        }
        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }
        return toResponse(categoryRepository.save(category));
    }

    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category", id);
        }
        categoryRepository.deleteById(id);
    }

    private CategoryResponse toResponse(Category category) {
        CategoryResponse res = new CategoryResponse();
        res.setId(category.getId());
        res.setName(category.getName());
        res.setDescription(category.getDescription());
        res.setCreatedAt(category.getCreatedAt());
        res.setUpdatedAt(category.getUpdatedAt());
        return res;
    }
}
