package com.dak.spravel.model.inventory;

import java.util.UUID;

import com.dak.spravel.model.base.BaseEntitySimple;
import com.dak.spravel.model.common.Partners;
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
@Table(name = "branches")
public class Branches extends BaseEntitySimple {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, updatable = false, nullable = false)
    private UUID uid;

    @PrePersist
    private void onCreateUid(){
        if (this.uid == null) {
            this.uid = UUID.randomUUID();
        }
    }

    @ManyToOne
    @JoinColumn(name = "partners_id", referencedColumnName = "id")
    private Partners partners;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(name = "is_active")
    private Boolean isActive = true;

}
