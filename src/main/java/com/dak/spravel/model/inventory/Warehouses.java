package com.dak.spravel.model.inventory;

import jakarta.persistence.*;

import java.time.LocalDateTime;

import com.dak.spravel.model.auth.User;
import com.dak.spravel.model.common.Partners;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "warehouses")
public class Warehouses   {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "partners_id", referencedColumnName = "id")
    private Partners partners;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @ToString.Exclude 
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "updated_by", referencedColumnName = "id")
    @JsonIgnoreProperties({"createdBy", "updatedBy", "deletedBy", "password", "roles"})
    private User updatedBy;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by", referencedColumnName = "id", updatable = false)
    @JsonIgnoreProperties({"createdBy", "updatedBy", "deletedBy", "password", "roles"})
    private User createdBy;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "deleted_by", referencedColumnName = "id")
    @JsonIgnoreProperties({"createdBy", "updatedBy", "deletedBy", "password", "roles"})
    private User deletedBy;
}
