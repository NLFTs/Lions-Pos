package com.dak.spravel.repository.auth;

import com.dak.spravel.model.auth.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for Token entity.
 */
public interface TokenRepository extends JpaRepository<Token, Long> {
    Optional<Token> findByRefreshToken(String refreshToken);
    Optional<Token> findByAccessToken(String accessToken);
    List<Token> findAllByUsername(String username);

    @Modifying
    @Transactional
    void deleteAllByUsername(String username);
}
