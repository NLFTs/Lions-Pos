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
        setField(entity, "deletedBy", user);
        setField(entity, "deletedAt", LocalDateTime.now());
    }

    private static void setField(Object entity, String fieldName, Object value) {
        try {
            Field field = entity.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(entity, value);
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
            // field ga ada di entity, skip aja
        }
    }
}
