package com.dak.spravel.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * JPA entity for HTTP audit log entries with request/response tracking.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "log_http")
public class LogHttp implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "service_name", nullable = false)
    private String serviceName;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "method")
    private String method;

    @Column(name = "url")
    private String url;

    @Column(name = "headers", columnDefinition = "TEXT")
    private String header;

    @Column(name = "request", columnDefinition = "TEXT")
    private String request;

    @Column(name = "request_at")
    private Date requestAt;

    @Column(name = "response_status")
    private String responseStatus;

    @Column(name = "response", columnDefinition = "TEXT")
    private String response;

    @Column(name = "response_at")
    private Date responseAt;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "user_full_name")
    private String userFullname;
}
