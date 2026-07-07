package com.fts.twin.dto.request.partner;

import lombok.Data;

@Data
public class BranchWarehouseMappingRequest {
    private Integer branchIndex;
    private Integer warehouseIndex;
}
