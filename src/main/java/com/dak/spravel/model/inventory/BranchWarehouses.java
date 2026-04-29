package com.dak.spravel.model.inventory;
import java.sql.Timestamp;
import com.dak.spravel.model.common.Partners;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import lombok.Data;

@Entity
@Data
public class BranchWarehouses {
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

    @ManyToOne
    @JoinColumn(name = "branches_id")
    private Branches branches;

    @ManyToOne
    @JoinColumn(name = "warehouses_id")
    private Warehouses warehouses;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Timestamp createdAt;

    @Column(name = "created_by", updatable = false)
    private UUID createdBy;

    @PrePersist
    private void onCreate(){
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }
}
