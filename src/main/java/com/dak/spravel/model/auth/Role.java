package com.dak.spravel.model.auth;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.util.HashSet;
import java.util.Set;
import com.dak.spravel.model.base.BaseEntitySimple;
/**
 * Role entity representing a role in the system.
 * Roles group permissions and can be assigned to users.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "roles", uniqueConstraints = @UniqueConstraint(columnNames = "slug"))
public class Role extends BaseEntitySimple {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Unique identifier for the role, e.g., "admin", "editor". */
    @Column(nullable = false, unique = true)
    private String slug;

    /** Human-readable name for the role, e.g., "Administrator". */
    @Column(nullable = false)
    private String name;

    /** Description of the role. */
    @Column
    private String description;

    /** Permissions associated with this role. */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "role_permissions",
        joinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id", referencedColumnName = "id")
    )
    private Set<Permission> permissions = new HashSet<>();

}
