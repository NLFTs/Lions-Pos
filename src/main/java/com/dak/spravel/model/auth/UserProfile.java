package com.dak.spravel.model.auth;

import com.dak.spravel.model.base.BaseEntitySimple;
import com.dak.spravel.model.common.Partners;
import com.dak.spravel.model.inventory.Branches;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@Entity
@Table(name = "user_profiles")
public class UserProfile extends BaseEntitySimple {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false, unique = true)
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id", referencedColumnName = "id", nullable = false, unique = true)
    private Partners partner;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", referencedColumnName = "id", nullable = false, unique = true)
    private Branches branch;
    
    private String phone;

    private String avatarUrl;
}
