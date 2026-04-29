package com.dak.spravel.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "permissions")
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** e.g. "post.index" */
    @Column(nullable = false, unique = true)
    private String slug;

    /** e.g. "View All Posts" */
    @Column(nullable = false)
    private String name;

    /** Parent module — groups this permission in the role matrix UI. */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "module_id", nullable = false)
    private Module module;
}
