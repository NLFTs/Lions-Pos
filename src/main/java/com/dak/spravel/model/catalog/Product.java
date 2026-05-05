package com.dak.spravel.model.catalog;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import com.dak.spravel.model.auth.User;
import com.dak.spravel.model.common.Partners;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
    name = "products"

)
public class Product{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "partner_id", referencedColumnName = "id", nullable = false)
    private Partners partner;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", referencedColumnName = "id")
    private CategoryProduct category;

    @Column(nullable = false)
    private String name;

    @Column(name = "sku")
    private String sku;

    @Column(name = "base_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal basePrice;

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
