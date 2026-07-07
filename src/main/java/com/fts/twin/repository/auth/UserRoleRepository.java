package com.fts.twin.repository.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fts.twin.model.auth.UserRole;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
}
