package com.dak.spravel.repository;

import com.dak.spravel.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for Permission entity.
 */
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findBySlug(String slug);
    boolean existsBySlug(String slug);
    List<Permission> findByModule_Slug(String moduleSlug);
}
