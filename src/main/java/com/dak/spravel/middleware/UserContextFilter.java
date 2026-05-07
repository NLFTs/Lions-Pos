// UserContextFilter.java
package com.dak.spravel.middleware;

import com.dak.spravel.model.auth.User;
import com.dak.spravel.repository.auth.UserRepository;
import com.dak.spravel.util.UserContext;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class UserContextFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    public UserContextFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                User user = userRepository.findByUsername(auth.getName()).orElse(null);
                if (user != null) {
                    UserContext.setUser(user);
                    // System.out.println("DEBUG: Set user " + user.getUsername());
                }
            }
            filterChain.doFilter(request, response);
        } finally {
            UserContext.clear();
        }
    }
}