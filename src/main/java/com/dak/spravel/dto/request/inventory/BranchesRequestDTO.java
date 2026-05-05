package com.dak.spravel.dto.request.inventory;

import lombok.Data;

@Data
public class BranchesRequestDTO {
    private Long partnersId;
    private String name;
    private String address;
    private Boolean isActive;
}