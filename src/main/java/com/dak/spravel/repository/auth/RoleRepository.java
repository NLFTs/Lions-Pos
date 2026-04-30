package com.dak.spravel.repository.auth;

import com.dak.spravel.model.auth.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data JPA repository for Role entity.
 */
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findBySlug(String slug);
    boolean existsBySlug(String slug);
}
