package com.dak.spravel.util;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import com.dak.spravel.model.auth.User;

public class AuditHelper {
    public static void setCreated(Object entity) {
        User user = UserContext.getUser();
        setField(entity, "createdBy", user);
        setField(entity, "updatedBy", user);
    }

    public static void setUpdated(Object entity) {
        User user = UserContext.getUser();
        setField(entity, "updatedBy", user);
    }

    public static void setDeleted(Object entity) {
        User user = UserContext.getUser();
        if (user != null) {
            setField(entity, "updatedBy", user);
        }

        if (user == null) {
            throw new RuntimeException("User not found in context for audit");
        }
        setField(entity, "deletedBy", user);
        setField(entity, "deletedAt", LocalDateTime.now());
    }

    public static void setCounted(Object entity) {
        setField(entity, "countedBy", UserContext.getUser());
        setField(entity, "countedAt", LocalDateTime.now());
    }

    public static void setApproved(Object entity) {
        setField(entity, "approvedBy", UserContext.getUser());
        setField(entity, "approvedAt", LocalDateTime.now());
    }

    private static void setField(Object entity, String fieldName, Object value) {
        Class<?> clazz = entity.getClass();
        while (clazz != null) { // Loop ke atas untuk nyari field di parent class/original class
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(entity, value);
                return;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            } catch (IllegalAccessException e) {
                break;
            }
        }
    }
}
