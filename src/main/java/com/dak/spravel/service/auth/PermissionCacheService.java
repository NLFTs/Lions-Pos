package com.dak.spravel.service.auth;

import com.dak.spravel.repository.auth.UserRepository;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Server-side permission cache using Caffeine (cache-aside pattern).
 * <p>
 * On every authenticated request the JWT filter calls
 * {@link #getPermissions(String)} which:
 * <ol>
 *   <li>Returns the cached Set instantly on a <b>cache hit</b>.</li>
 *   <li>On a <b>cache miss</b> — loads from PostgreSQL via
 *       User → Roles → Permissions, stores the result, then returns it.</li>
 * </ol>
 * This keeps JWT tokens small (no "perms" claim) while avoiding
 * a DB round-trip on every request.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PermissionCacheService {

    private final Cache<String, Set<String>> permissionCache;
    private final UserRepository userRepository;

    // ─── Read ───────────────────────────────────────────────────────────

    /**
     * Resolve permissions for the given username.
     * Cache-aside: tries cache first, falls back to DB on miss.
     *
     * @param username the authenticated user's username (JWT subject)
     * @return unmodifiable set of permission slugs, never null
     */
    public Set<String> getPermissions(String username) {
        return permissionCache.get(username, this::loadFromDatabase);
    }

    // ─── Write / Warm ───────────────────────────────────────────────────

    /**
     * Explicitly populate the cache — called on login and token refresh
     * so the first subsequent API call is always a cache hit.
     */
    public void putPermissions(String username, Set<String> permissions) {
        permissionCache.put(username, Collections.unmodifiableSet(permissions));
        log.debug("[Cache] PUT permissions for '{}' → {} entries", username, permissions.size());
    }

    // ─── Invalidate ─────────────────────────────────────────────────────

    /**
     * Evict a single user's cached permissions.
     * Called when an admin updates user roles or logs the user out.
     */
    public void evict(String username) {
        permissionCache.invalidate(username);
        log.debug("[Cache] EVICT permissions for '{}'", username);
    }

    /**
     * Evict all cached permissions.
     * Called when role-level permission assignments change,
     * since any cached user could be affected.
     */
    public void evictAll() {
        permissionCache.invalidateAll();
        log.debug("[Cache] EVICT ALL permissions");
    }

    // ─── DB Loader ──────────────────────────────────────────────────────

    /**
     * Loads the user's permission slugs from the database.
     * This is the cache-miss fallback supplied to {@code Cache.get()}.
     */
    private Set<String> loadFromDatabase(String username) {
        log.debug("[Cache] MISS — loading permissions for '{}' from DB", username);
        return userRepository.findByUsername(username)
                .map(user -> user.getRoles().stream()
                        .flatMap(role -> role.getPermissions().stream())
                        .map(perm -> perm.getSlug())
                        .collect(Collectors.toUnmodifiableSet()))
                .orElse(Set.of());
    }
}
