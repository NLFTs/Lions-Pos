package com.dak.spravel.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "modules")
public class Module {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Lowercase identifier, e.g. "post". Used as grouping key in role matrix. */
    @Column(nullable = false, unique = true)
    private String slug;

    /** Human-readable label, e.g. "Post Management". */
    @Column(nullable = false)
    private String name;

    /** Optional description shown in the permission management UI. */
    @Column
    private String description;
}
