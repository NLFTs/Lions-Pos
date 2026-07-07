package com.fts.twin.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationResponse {
    private Long id;
    private Long partnerId;
    private String partnerName;
    private String name;
    private String description;
    private String createdByName;
    private LocalDateTime createdAt;
    private Boolean isDraft;
    private Boolean isSeen;
    private Integer quantity;
}
