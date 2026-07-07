package com.fts.twin.service.auth;

import java.util.Set;

/**
 * Server-side permission cache interface.
 */
public interface PermissionCacheService {
    Set<String> getPermissions(String username);
    void putPermissions(String username, Set<String> permissions);
    void evict(String username);
    void evictAll();
}
