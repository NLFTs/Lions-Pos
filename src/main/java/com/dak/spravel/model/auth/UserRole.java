package com.dak.spravel.model.auth;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "user_roles",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "role_id"})
)
public class UserRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", referencedColumnName = "id", nullable = false)
    private Role role;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by", referencedColumnName = "id", updatable = false)
    @JsonIgnoreProperties({"createdBy", "updatedBy", "deletedBy", "password", "roles"})
    private User createdBy;

    // Disimpan di service saat membuat UserRole baru
    // private void onCreate() {
    //     this.createdAt = LocalDateTime.now();

    //     Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    //     if (auth != null && auth.getPrincipal() instanceof User currentUser) {
    //         this.createdBy = currentUser;
    //     }
    // }
}
