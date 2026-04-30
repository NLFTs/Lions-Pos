package com.dak.spravel.model.auth;

import com.dak.spravel.model.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Entity
@Table(name = "modules")
public class Module extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Human-readable label, e.g. "Post Management". */
    @Column(nullable = false)
    private String name;

    /** Lowercase identifier, e.g. "post". Used as grouping key in role matrix. */
    @Column(nullable = false, unique = true)
    private String slug;

    /** Optional description shown in the permission management UI. */
    @Column
    private String description;
}
