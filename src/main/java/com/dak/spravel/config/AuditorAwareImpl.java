package com.dak.spravel.config;

import java.util.Optional;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import com.dak.spravel.model.auth.User;
import com.dak.spravel.repository.auth.UserRepository;

@Component("auditorAwareImpl")
public class AuditorAwareImpl implements AuditorAware<User> {

    private final UserRepository userRepository;

    public AuditorAwareImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @Override
    public Optional<User> getCurrentAuditor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return Optional.empty();
        }

        if (auth.getPrincipal() instanceof User user) {
            return Optional.of(user);
        }

        if (auth.getPrincipal() instanceof UserDetails userDetails) {
            return userRepository.findByUsername(userDetails.getUsername());
        }

        if (auth.getPrincipal() instanceof String username) {
            return userRepository.findByUsername(username);
        }

        return Optional.empty();
    }
}