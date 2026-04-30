package com.dak.spravel.service.system;

import com.dak.spravel.dto.response.LogHttpResponse;
import com.dak.spravel.handler.ResourceNotFoundException;
import com.dak.spravel.model.system.LogHttp;
import com.dak.spravel.repository.system.LogHttpRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

/**
 * Business logic for HTTP audit logs with duration computation.
 */
@Service
@RequiredArgsConstructor
public class LogHttpService {
    private final LogHttpRepository logHttpRepository;

    public Page<LogHttpResponse> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("requestAt").descending());
        return logHttpRepository.findAll(pageable).map(this::toResponse);
    }

    public Page<LogHttpResponse> findByUserId(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("requestAt").descending());
        return logHttpRepository.findByUserId(userId, pageable).map(this::toResponse);
    }

    public LogHttpResponse findById(Long id) {
        LogHttp log = logHttpRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Log", id));
        return toResponse(log);
    }

    private LogHttpResponse toResponse(LogHttp log) {
        LogHttpResponse res = new LogHttpResponse();
        res.setId(log.getId());
        res.setMethod(log.getMethod());
        res.setUrl(log.getUrl());
        res.setResponseStatus(log.getResponseStatus());
        res.setUserAgent(log.getUserAgent());
        res.setUserId(log.getUserId());
        res.setUserFullname(log.getUserFullname());
        res.setRequestAt(log.getRequestAt());
        res.setResponseAt(log.getResponseAt());

        if (log.getRequestAt() != null && log.getResponseAt() != null) {
            long diff = log.getResponseAt().getTime() - log.getRequestAt().getTime();
            res.setDurationMs(diff);
        }

        return res;
    }
}
