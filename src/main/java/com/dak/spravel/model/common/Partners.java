package com.dak.spravel.model;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import org.springframework.data.annotation.CreatedDate;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Data;

@Entity
@Data
public class Partners {
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

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String slug;

    public enum Plan {
        FREE, PRO, ENTERPRISE
    }

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar default 'free'")
    private Plan plan = Plan.FREE;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Timestamp createdAt;

    private Timestamp updatedAt;

    private Timestamp deletedAt;

    @Column(name = "created_by", updatable = false)
    private UUID createdBy;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @Column(name = "deleted_by")
    private UUID deletedBy;

    @OneToMany(mappedBy = "partners", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Branches> branches;

    @OneToMany(mappedBy = "partners", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Warehouses> warehouses;

    @OneToMany(mappedBy = "partners", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BranchWarehouses> branchWarehouses;

    @PrePersist
    private void onCreate(){
        this.createdAt = new Timestamp(System.currentTimeMillis());
        this.updatedAt = new Timestamp(System.currentTimeMillis());
    }

    @PreUpdate
    private void onUpdate(){
        this.updatedAt = new Timestamp(System.currentTimeMillis());
    }
}
