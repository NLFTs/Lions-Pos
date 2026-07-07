package com.fts.twin.repository.auth;

import com.fts.twin.model.auth.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for Permission entity.
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findBySlug(String slug);
    boolean existsBySlug(String slug);
    List<Permission> findByModule_Slug(String moduleSlug);
}
