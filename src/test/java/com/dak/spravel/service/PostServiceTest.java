package com.dak.spravel.service;

import com.dak.spravel.dto.request.user.CreatePostRequest;
import com.dak.spravel.dto.request.user.UpdatePostRequest;
import com.dak.spravel.dto.response.PostResponse;
import com.dak.spravel.handler.ResourceNotFoundException;
import com.dak.spravel.model.common.Category;
import com.dak.spravel.model.common.Post;
import com.dak.spravel.repository.common.CategoryRepository;
import com.dak.spravel.repository.common.PostRepository;
import com.dak.spravel.service.common.PostService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private PostService postService;

    private Post createSamplePost() {
        Post post = new Post();
        post.setId(1L);
        post.setTitle("Test Post");
        post.setContent("Test content");
        post.setStatus("PUBLISHED");
        post.setCreatedBy("testuser");
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        return post;
    }

    // ── findAll ──────────────────────────────────────────────────────────────

    @Test
    void findAll_returnsPaginatedResultsSortedByCreatedAtDesc() {
        Post post = createSamplePost();
        Page<Post> page = new PageImpl<>(List.of(post));
        when(postRepository.findAll(any(PageRequest.class))).thenReturn(page);

        Page<PostResponse> result = postService.findAll(0, 10);

        assertEquals(1, result.getTotalElements());
        assertEquals("Test Post", result.getContent().getFirst().getTitle());
        verify(postRepository).findAll(PageRequest.of(0, 10, Sort.by("createdAt").descending()));
    }

    // ── findById ─────────────────────────────────────────────────────────────

    @Test
    void findById_returnsPostResponse_whenFound() {
        Post post = createSamplePost();
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        PostResponse result = postService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Post", result.getTitle());
    }

    @Test
    void findById_throwsResourceNotFoundException_whenNotFound() {
        when(postRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> postService.findById(99L));
    }

    // ── create ───────────────────────────────────────────────────────────────

    @Test
    void create_createsPostWithCategory_whenCategoryIdProvided() {
        CreatePostRequest request = new CreatePostRequest();
        request.setTitle("New Post");
        request.setContent("New content");
        request.setStatus("DRAFT");
        request.setCategoryId(5L);

        Category category = new Category();
        category.setId(5L);
        category.setName("Tech");

        when(categoryRepository.findById(5L)).thenReturn(Optional.of(category));
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
            Post p = invocation.getArgument(0);
            p.setId(10L);
            return p;
        });

        PostResponse result = postService.create(request, "author1");

        assertNotNull(result);
        assertEquals("New Post", result.getTitle());
        assertEquals("DRAFT", result.getStatus());
        assertEquals("author1", result.getCreatedBy());
        assertEquals(5L, result.getCategoryId());
        assertEquals("Tech", result.getCategoryName());
    }

    @Test
    void create_createsPostWithoutCategory_whenCategoryIdIsNull() {
        CreatePostRequest request = new CreatePostRequest();
        request.setTitle("No Category Post");
        request.setContent("Content");

        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
            Post p = invocation.getArgument(0);
            p.setId(11L);
            return p;
        });

        PostResponse result = postService.create(request, "author2");

        assertNotNull(result);
        assertEquals("No Category Post", result.getTitle());
        assertEquals("author2", result.getCreatedBy());
        assertNull(result.getCategoryId());
        assertNull(result.getCategoryName());
    }

    @Test
    void create_throwsResourceNotFoundException_whenCategoryNotFound() {
        CreatePostRequest request = new CreatePostRequest();
        request.setTitle("Post");
        request.setContent("Content");
        request.setCategoryId(999L);

        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> postService.create(request, "user"));
    }

    // ── update ───────────────────────────────────────────────────────────────

    @Test
    void update_updatesFieldsAndSaves() {
        Post existing = createSamplePost();
        when(postRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdatePostRequest request = new UpdatePostRequest();
        request.setTitle("Updated Title");
        request.setContent("Updated content");
        request.setStatus("PUBLISHED");

        PostResponse result = postService.update(1L, request, "editor");

        assertEquals("Updated Title", result.getTitle());
        assertEquals("Updated content", result.getContent());
    }

    @Test
    void update_throwsResourceNotFoundException_whenPostNotFound() {
        when(postRepository.findById(99L)).thenReturn(Optional.empty());

        UpdatePostRequest request = new UpdatePostRequest();
        request.setTitle("Updated");

        assertThrows(ResourceNotFoundException.class, () -> postService.update(99L, request, "user"));
    }

    // ── delete ───────────────────────────────────────────────────────────────

    @Test
    void delete_deletesWhenExists() {
        when(postRepository.existsById(1L)).thenReturn(true);

        postService.delete(1L);

        verify(postRepository).deleteById(1L);
    }

    @Test
    void delete_throwsResourceNotFoundException_whenNotExists() {
        when(postRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> postService.delete(99L));
    }
}
