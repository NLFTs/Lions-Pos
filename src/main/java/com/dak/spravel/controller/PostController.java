package com.dak.spravel.controller;

import com.dak.spravel.dto.request.CreatePostRequest;
import com.dak.spravel.dto.request.UpdatePostRequest;
import com.dak.spravel.dto.response.ResData;
import com.dak.spravel.dto.response.PostResponse;
import com.dak.spravel.service.PostService;
import com.dak.spravel.util.ResponseBuilder;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

/**
 * Provides full CRUD operations for blog posts with pagination
 * and DRAFT/PUBLISHED status management.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    /**
     * GET /api/v1/posts
     * List all posts with pagination.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('post.index')")
    public ResponseEntity<ResData<Page<PostResponse>>> index(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("[GET] /api/v1/posts page={} size={}", page, size);
        return ResponseBuilder.ok(postService.findAll(page, size));
    }

    /**
     * GET /api/v1/posts/{id}
     * Get a single post by ID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('post.show')")
    public ResponseEntity<ResData<PostResponse>> show(@PathVariable Long id) {
        log.info("[GET] /api/v1/posts/{}", id);
        return ResponseBuilder.ok(postService.findById(id));
    }

    /**
     * POST /api/v1/posts
     * Create a new post. The current authenticated user is set as the author.
     */
    @PostMapping
@PreAuthorize("hasAuthority('post.store')")
    public ResponseEntity<ResData<PostResponse>> store(
            @Valid @RequestBody CreatePostRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("[POST] /api/v1/posts by {}", userDetails.getUsername());
        return ResponseBuilder.created(postService.create(request, userDetails.getUsername()));
    }

    /**
     * PUT /api/v1/posts/{id}
     * Update an existing post by ID.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('post.update')")
    public ResponseEntity<ResData<PostResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePostRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("[PUT] /api/v1/posts/{} by {}", id, userDetails.getUsername());
        return ResponseBuilder.ok(postService.update(id, request, userDetails.getUsername()));
    }

    /**
     * DELETE /api/v1/posts/{id}
     * Delete a post by ID.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('post.delete')")
    public ResponseEntity<ResData<Void>> destroy(@PathVariable Long id) {
        log.info("[DELETE] /api/v1/posts/{}", id);
        postService.delete(id);
        return ResponseBuilder.ok();
    }
}
