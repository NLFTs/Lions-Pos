package com.dak.spravel.repository.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dak.spravel.model.auth.UserRole;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
}
