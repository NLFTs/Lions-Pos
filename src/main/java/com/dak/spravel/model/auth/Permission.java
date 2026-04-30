package com.dak.spravel.model.auth;

import com.dak.spravel.model.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "permissions")
public class Permission extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** e.g. "View All Posts" */
    @Column(nullable = false)
    private String name;

    /** e.g. "post.index" */
    @Column(nullable = false, unique = true)
    private String slug;

    /** Parent module — groups this permission in the role matrix UI. */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "module_id", referencedColumnName = "id", nullable = false)
    private Module module;
}
