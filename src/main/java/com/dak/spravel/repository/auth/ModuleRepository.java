package com.dak.spravel.repository.auth;

import com.dak.spravel.model.auth.Module;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data JPA repository for Module entity.
 */
public interface ModuleRepository extends JpaRepository<Module, Long> {
    Optional<Module> findBySlug(String slug);
    boolean existsBySlug(String slug);
}
