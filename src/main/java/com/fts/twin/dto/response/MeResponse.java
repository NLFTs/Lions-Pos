package com.fts.twin.dto.response;

import lombok.Data;

import java.util.List;

/**
 * Response DTO for current authenticated user profile.
 */
@Data
public class MeResponse {
    private Long id;
    private String username;
    private String fullname;
    private String avatar;
    private List<String> roles;
    private List<String> permissions;
    private Long branchId;
    private String branchName;
    private Long warehouseId;
    private String warehouseName;
    private Long partnerId;
    private String partnerName;
    private String plan;
}
