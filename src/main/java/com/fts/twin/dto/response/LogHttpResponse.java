package com.fts.twin.dto.response;

import lombok.Data;

import java.util.Date;

/**
 * Response DTO for HTTP log entries with timing information.
 */
@Data
public class LogHttpResponse {
    private Long id;
    private String method;
    private String url;
    private String responseStatus;
    private String userAgent;
    private Long userId;
    private String userFullname;
    private Date requestAt;
    private Date responseAt;
    private Long durationMs;
}
