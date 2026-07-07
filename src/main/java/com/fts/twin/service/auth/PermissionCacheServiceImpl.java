package com.fts.twin.service.auth;

import com.fts.twin.repository.auth.UserRepository;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.Set;

/**
 * Concrete implementation of the PermissionCacheService interface using Caffeine.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PermissionCacheServiceImpl implements PermissionCacheService {

    private final Cache<String, Set<String>> permissionCache;
    private final UserRepository userRepository;

    @Override
    public Set<String> getPermissions(String username) {
        return permissionCache.get(username, this::loadFromDatabase);
    }

    @Override
    public void putPermissions(String username, Set<String> permissions) {
        permissionCache.put(username, Collections.unmodifiableSet(permissions));
        log.debug("[Cache] PUT permissions for '{}' -> {} entries", username, permissions.size());
    }

    @Override
    public void evict(String username) {
        permissionCache.invalidate(username);
        log.debug("[Cache] EVICT permissions for '{}'", username);
    }

    @Override
    public void evictAll() {
        permissionCache.invalidateAll();
        log.debug("[Cache] EVICT ALL permissions");
    }

    private Set<String> loadFromDatabase(String username) {
        log.debug("[Cache] MISS - loading permissions for '{}' from DB", username);
        return userRepository.findByUsernameWithRoles(username)
                .map(user -> {
                    Set<String> auths = new java.util.HashSet<>();
                    user.getRoles().forEach(role -> {
                        auths.add(role.getSlug());
                        role.getPermissions().forEach(perm -> auths.add(perm.getSlug()));
                    });
                    return Collections.unmodifiableSet(auths);
                })
                .orElse(Set.of());
    }
}
