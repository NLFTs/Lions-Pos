package com.dak.spravel.repository.auth;

import com.dak.spravel.model.auth.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("SELECT r FROM Role r WHERE r.partner.id = :partnerId OR (r.partner IS NULL AND r.slug NOT IN ('admin', 'admin-partners', 'owner'))")
    List<Role> findAllByPartnerIdOrPartnerIsNull(@Param("partnerId") Long partnerId);    

    boolean existsBySlugAndPartnerIsNull(String slug);
}
