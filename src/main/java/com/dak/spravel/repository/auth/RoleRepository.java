package com.dak.spravel.repository.auth;

import com.dak.spravel.model.auth.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for Role entity.
 */

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findBySlugAndPartnerId(String slug, Long partnerId);
    boolean existsBySlugAndPartnerId(String slug, Long partnerId);

    List<Role> findAllByPartnerIdOrPartnerIsNull(Long partnerId);
    
    boolean existsBySlugAndPartnerIsNull(String slug);
}
