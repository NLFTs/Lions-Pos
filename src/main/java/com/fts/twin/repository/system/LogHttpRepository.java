package com.fts.twin.repository.system;

import com.fts.twin.model.system.LogHttp;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for LogHttp entity.
 */
public interface LogHttpRepository extends JpaRepository<LogHttp, Long> {
    Page<LogHttp> findByUserId(Long userId, Pageable pageable);
}
