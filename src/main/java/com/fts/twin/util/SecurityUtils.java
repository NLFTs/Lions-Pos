package com.fts.twin.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import com.fts.twin.model.auth.User;

public class SecurityUtils {
    public static User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetails) {

            String username = auth.getName();
            if (username != null) {
                return (User) auth.getPrincipal();
            }
            throw new RuntimeException("Gunakan cara ambil di Service saja");
        }
        throw new RuntimeException("User tidak terautentikasi");
    }
}
