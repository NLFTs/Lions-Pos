package com.dak.spravel.dto.request.partner;

import com.dak.spravel.model.common.Partners;
import lombok.Data;

@Data
public class UpdatePartnerRequest {

    private String name;
    
    private Partners.Plan plan;

    private Boolean isActive;
}