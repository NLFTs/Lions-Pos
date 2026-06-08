package com.dak.spravel.repository.auth;

import com.dak.spravel.model.auth.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for Role entity.
 */

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    Optional<Role> findBySlug(String slug);

    @Query("SELECT r FROM Role r WHERE r.type = com.dak.spravel.model.auth.Role.Type.EXTERNAL AND r.slug <> 'owner' AND r.slug <> 'admin-partner'")
    List<Role> findExternalRolesExceptOwner();

    boolean existsBySlug(String slug);
}
