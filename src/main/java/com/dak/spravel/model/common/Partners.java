package com.dak.spravel.model.common;

import com.dak.spravel.model.base.BaseEntitySimple;
import com.dak.spravel.model.inventory.Warehouses;
import com.dak.spravel.model.inventory.Branches;
import java.util.List;
import java.util.UUID;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "partners")
public class Partners extends BaseEntitySimple{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, updatable = false, nullable = false)
    private UUID uid;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String slug;

    public enum Plan {
        BASIC, PRO, ENTERPRISE
    }

    @Enumerated(EnumType.STRING)
    private Plan plan;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @OneToMany(mappedBy = "partners", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Branches> branches;

    @OneToMany(mappedBy = "partners", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Warehouses> warehouses;
}
