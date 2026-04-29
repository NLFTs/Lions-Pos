package com.dak.spravel.repository;

import com.dak.spravel.model.common.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for Post entity.
 */
public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findAll(Pageable pageable);
    Page<Post> findByStatus(String status, Pageable pageable);
    Page<Post> findByCreatedBy(String createdBy, Pageable pageable);
}
