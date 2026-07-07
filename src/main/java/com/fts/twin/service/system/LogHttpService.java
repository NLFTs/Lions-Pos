package com.fts.twin.service.system;

import com.fts.twin.dto.response.LogHttpResponse;
import com.fts.twin.handler.ResourceNotFoundException;
import com.fts.twin.model.auth.User;
import com.fts.twin.model.system.LogHttp;
import com.fts.twin.repository.auth.UserRepository;
import com.fts.twin.repository.system.LogHttpRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Service;

/**
 * Business logic for HTTP audit logs with duration computation.
 */
@Service
@RequiredArgsConstructor
public class LogHttpService {

    private final LogHttpRepository logHttpRepository;
    private final UserRepository userRepository;

    // =========================
    // AUTH USER
    // =========================
    private User getAuthenticatedUser() {

        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        if (auth == null
                || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getName())) {

            throw new RuntimeException("User tidak terautentikasi");
        }

        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() ->
                        new RuntimeException("User tidak ditemukan"));
    }

    // =========================
    // CHECK EMPLOYEE
    // =========================
    private boolean isEmployeeOnly(User user) {
        return user.getRoles().stream()
                .allMatch(role ->
                        role.getSlug().equalsIgnoreCase("employee")
                                || role.getSlug().equalsIgnoreCase("employee-partners")
                );
    }

    // =========================
    // GET ALL LOGS
    // =========================
    public Page<LogHttpResponse> findAll(int page, int size) {

        User currentUser = getAuthenticatedUser();

        // Employee tidak boleh lihat log
        if (isEmployeeOnly(currentUser)) {
            throw new RuntimeException(
                    "Akses Ditolak: Employee tidak boleh melihat log aktivitas."
            );
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("requestAt").descending()
        );

        return logHttpRepository.findAll(pageable)
                .map(this::toResponse);
    }

    // =========================
    // GET LOGS BY USER
    // =========================
    public Page<LogHttpResponse> findByUserId(
            Long userId,
            int page,
            int size
    ) {

        User currentUser = getAuthenticatedUser();

        // Employee tidak boleh lihat log
        if (isEmployeeOnly(currentUser)) {
            throw new RuntimeException(
                    "Akses Ditolak: Employee tidak boleh melihat log aktivitas."
            );
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("requestAt").descending()
        );

        return logHttpRepository.findByUserId(userId, pageable)
                .map(this::toResponse);
    }

    // =========================
    // GET DETAIL LOG
    // =========================
    public LogHttpResponse findById(Long id) {

        User currentUser = getAuthenticatedUser();

        // Employee tidak boleh lihat log
        if (isEmployeeOnly(currentUser)) {
            throw new RuntimeException(
                    "Akses Ditolak: Employee tidak boleh melihat log aktivitas."
            );
        }

        LogHttp log = logHttpRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Log", id));

        return toResponse(log);
    }

    // =========================
    // MAPPING RESPONSE
    // =========================
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

        if (log.getRequestAt() != null
                && log.getResponseAt() != null) {

            long diff =
                    log.getResponseAt().getTime()
                            - log.getRequestAt().getTime();

            res.setDurationMs(diff);
        }

        return res;
    }
}