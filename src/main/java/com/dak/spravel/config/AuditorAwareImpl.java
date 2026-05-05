package com.dak.spravel.config;

import java.util.Optional;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import com.dak.spravel.model.auth.User;

@Component("auditorAwareImpl")
public class AuditorAwareImpl implements AuditorAware<User> {

    @Override
    public Optional<User> getCurrentAuditor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth == null || !auth.isAuthenticated() ||  auth instanceof AnonymousAuthenticationToken ||
            auth.getPrincipal().equals("anonymousUser")) {
            System.out.println("DEBUG: Auth null atau anonymous");
            return Optional.empty();
        }

        System.out.println("DEBUG: principal type = " + auth.getPrincipal().getClass().getName());
        System.out.println("DEBUG: principal = " + auth.getPrincipal());
        
        if (auth.getPrincipal() instanceof User user) {
            System.out.println("DEBUG: user = " + user);
            return Optional.of(user);   
        }
        
        System.out.println("DEBUG: principal bukan instance User");
        return Optional.empty();
    }
}