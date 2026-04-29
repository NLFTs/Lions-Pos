package com.dak.spravel.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Caffeine in-process cache configuration.
 * <p>
 * Stores per-user permission sets server-side so JWT access tokens
 * no longer need to carry the full permissions list.
 * TTL is aligned with the access-token expiration to guarantee
 * cache freshness matches the token lifetime.
 */
@Configuration
public class CacheConfig {

    @Value("${app.jwt.access-token-expiration}")
    private long accessTokenExpirationMs;

    /**
     * Permission cache: username → Set&lt;permissionSlug&gt;.
     * <ul>
     *   <li>expireAfterWrite — entries auto-evict after the same
     *       duration as the access token, so stale permissions
     *       never outlive the token.</li>
     *   <li>maximumSize — caps memory at 10 000 users; LRU eviction
     *       handles overflow gracefully.</li>
     * </ul>
     */
    @Bean
    public Cache<String, Set<String>> permissionCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(accessTokenExpirationMs, TimeUnit.MILLISECONDS)
                .maximumSize(10_000)
                .recordStats()          // expose hit/miss metrics
                .build();
    }
}
