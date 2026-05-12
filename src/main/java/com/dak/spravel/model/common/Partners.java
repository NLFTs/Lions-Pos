package com.dak.spravel.model.common;

import java.time.LocalDateTime;
import org.hibernate.annotations.SQLDelete;
import com.dak.spravel.model.auth.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "partners")
@SQLDelete(sql = "UPDATE partners SET deleted_at = NOW(), is_active = false WHERE id = ?")
public class Partners {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String slug;

    public enum Plan {
        BASIC, PRO, ENTERPRISE;
    }

    @Enumerated(EnumType.STRING)
    private Plan plan;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @JsonIgnore
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @JsonIgnore
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @JsonIgnore
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @ToString.Exclude 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by", referencedColumnName = "id")
    private User updatedBy;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", referencedColumnName = "id", updatable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY  )
    @JoinColumn(name = "deleted_by", referencedColumnName = "id")
    private User deletedBy;
}

