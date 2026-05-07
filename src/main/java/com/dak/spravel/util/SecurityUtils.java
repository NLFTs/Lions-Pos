package com.dak.spravel.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.dak.spravel.model.auth.User;

public class SecurityUtils {
    public static User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails) {

            String username = auth.getName();
            throw new RuntimeException("Gunakan cara ambil di Service saja");
        }
        throw new RuntimeException("User tidak terautentikasi");
    }
}
