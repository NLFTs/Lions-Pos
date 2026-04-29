package com.dak.spravel.service;

import com.dak.spravel.dto.request.CreatePostRequest;
import com.dak.spravel.dto.request.UpdatePostRequest;
import com.dak.spravel.dto.response.PostResponse;
import com.dak.spravel.handler.ResourceNotFoundException;
import com.dak.spravel.model.Category;
import com.dak.spravel.model.Post;
import com.dak.spravel.repository.CategoryRepository;
import com.dak.spravel.repository.PostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

/**
 * Business logic for posts with author assignment and status management.
 */
@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;

    public Page<PostResponse> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return postRepository.findAll(pageable).map(this::toResponse);
    }

    public PostResponse findById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post", id));
        return toResponse(post);
    }

    public PostResponse create(CreatePostRequest request, String username) {
        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setStatus(request.getStatus() != null ? request.getStatus() : "DRAFT");
        post.setCreatedBy(username);
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", request.getCategoryId()));
            post.setCategory(category);
        }
        return toResponse(postRepository.save(post));
    }

    public PostResponse update(Long id, UpdatePostRequest request, String username) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post", id));
        if (request.getTitle() != null) post.setTitle(request.getTitle());
        if (request.getContent() != null) post.setContent(request.getContent());
        if (request.getStatus() != null) post.setStatus(request.getStatus());
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", request.getCategoryId()));
            post.setCategory(category);
        }
        return toResponse(postRepository.save(post));
    }

    public void delete(Long id) {
        if (!postRepository.existsById(id)) {
            throw new ResourceNotFoundException("Post", id);
        }
        postRepository.deleteById(id);
    }

    private PostResponse toResponse(Post post) {
        PostResponse res = new PostResponse();
        res.setId(post.getId());
        res.setTitle(post.getTitle());
        res.setContent(post.getContent());
        res.setStatus(post.getStatus());
        res.setCreatedBy(post.getCreatedBy());
        res.setCreatedAt(post.getCreatedAt());
        res.setUpdatedAt(post.getUpdatedAt());
        if (post.getCategory() != null) {
            res.setCategoryId(post.getCategory().getId());
            res.setCategoryName(post.getCategory().getName());
        }
        return res;
    }
}
