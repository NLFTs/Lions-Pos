package com.fts.twin.dto.request.partner;

import com.fts.twin.model.common.Partners;
import lombok.Data;

@Data
public class UpdatePartnerRequest {

    private String name;
    
    private Partners.Plan plan;

    private Boolean isActive;
}