package com.dak.spravel.model.auth;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import com.dak.spravel.model.common.Partners;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@NoArgsConstructor
@Entity
@Table(name = "users")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password", "roles"})
public class User   {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    @JsonIgnore
    @Column(name = "full_name")
    private String fullname;

    @JsonIgnore
    private String password;
    
    @JsonIgnore
    @Column(unique = true)
    private String email;

    @Column(name = "avatar")
    private String avatar;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id", referencedColumnName = "id")
    private Partners partner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", referencedColumnName = "id")
    private com.dak.spravel.model.inventory.Branches branch;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id")
    )
    private Set<Role> roles = new HashSet<>();


    
    
    @Column(name = "created_at", updatable = false)
    @JsonIgnore
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    @JsonIgnore
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    @JsonIgnore
    private LocalDateTime deletedAt;
}
