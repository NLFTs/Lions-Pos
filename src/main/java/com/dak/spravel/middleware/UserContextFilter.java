// UserContextFilter.java
package com.dak.spravel.middleware;

import com.dak.spravel.model.auth.User;
import com.dak.spravel.repository.auth.UserRepository;
import com.dak.spravel.util.UserContext;
import jakarta.servlet.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class UserContextFilter implements Filter {

    private final UserRepository userRepository; // inject repo buat ambil User

    public UserContextFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                // ambil User object dari DB
                User user = userRepository.findByUsername(auth.getName()).orElse(null);
                UserContext.setUser(user);
            }
            chain.doFilter(request, response);
        } finally {
            UserContext.clear();
        }
    }
}