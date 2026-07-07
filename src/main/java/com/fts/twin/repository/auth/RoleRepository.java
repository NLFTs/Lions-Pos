package com.fts.twin.repository.auth;

import com.fts.twin.model.auth.Role;
import com.fts.twin.model.auth.Role.Type;
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

    public final Type type = Type.EXTERNAL; 
    
    Optional<Role> findBySlug(String slug);

    @Query("SELECT r FROM Role r WHERE r.type = Type.EXTERNAL AND r.slug <> 'owner' AND r.slug <> 'admin-partner'")
    List<Role> findExternalRolesExceptOwner();

    boolean existsBySlug(String slug);
}
