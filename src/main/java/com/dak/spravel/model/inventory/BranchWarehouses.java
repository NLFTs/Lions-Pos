package com.dak.spravel.model.inventory;


import com.dak.spravel.model.auth.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
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
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class BranchWarehouses {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
    @JoinColumn(name = "created_by", referencedColumnName = "id", updatable = false)
    private User createdBy;
}
