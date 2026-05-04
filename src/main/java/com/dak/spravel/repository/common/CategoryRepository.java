package com.dak.spravel.repository.common;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dak.spravel.model.common.Category;

import java.util.Optional;

/**
 * Spring Data JPA repository for Category entity.
 */
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String name);
    boolean existsByName(String name);
}
