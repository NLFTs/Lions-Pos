package com.dak.spravel.dto.request.partner;

import lombok.Data;

@Data
public class BranchWarehouseMappingRequest {
    private Integer branchIndex;
    private Integer warehouseIndex;
}
