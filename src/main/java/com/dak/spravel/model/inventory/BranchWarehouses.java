package com.dak.spravel.model.inventory;


import com.dak.spravel.model.auth.User;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.annotation.CreatedDate;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "branch_warehouses")
public class BranchWarehouses {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, updatable = false, nullable = false)
    private UUID uid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branches_id", referencedColumnName = "id", nullable = false)
    private Branches branches;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouses_id", referencedColumnName = "id", nullable = false)
    private Warehouses warehouses;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", updatable = false)
    private User createdBy;

    // Disimpan di service saat membuat relasi baru
    // @PrePersist
    // private void onCreate(){
    //     this.createdAt = LocalDateTime.now();
    // }
}
