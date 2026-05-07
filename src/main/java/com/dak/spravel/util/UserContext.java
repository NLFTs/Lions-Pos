package com.dak.spravel.util;

import com.dak.spravel.model.auth.User;

public class UserContext {
    private static final ThreadLocal<User> currentUser = new InheritableThreadLocal<>();

    public static void setUser(User user) {
        currentUser.set(user);
    }

    public static User getUser() {
        return currentUser.get();
    }

    public static void clear() {
        currentUser.remove();
    }
}