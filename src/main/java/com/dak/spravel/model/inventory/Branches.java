package com.dak.spravel.model.inventory;

import java.util.UUID;
import java.sql.Timestamp;
import org.springframework.data.annotation.CreatedDate;
import com.dak.spravel.model.common.Partners;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "branches")
public class Branches {
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
    @JoinColumn(name = "partners_id")
    private Partners partners;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreatedDate
    @Column(updatable = false)
    private Timestamp createdAt;

    private Timestamp updatedAt;

    private Timestamp deletedAt;

    @Column(name = "created_by", updatable = false)
    private UUID createdBy;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @Column(name = "deleted_by")
    private UUID deletedBy;

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
