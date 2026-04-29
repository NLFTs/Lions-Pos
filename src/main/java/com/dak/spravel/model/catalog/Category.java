package com.dak.spravel.model.catalog;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.util.UUID;

import com.dak.spravel.model.base.BaseEntity;
import com.dak.spravel.model.common.Partners;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "categories")
public class Category extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, updatable = false, nullable = false)
    private UUID uid; 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id", referencedColumnName = "id", nullable = false)
    private Partners partner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", referencedColumnName = "id")
    private Category parent;

    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;
}
