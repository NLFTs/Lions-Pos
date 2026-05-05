package com.dak.spravel.dto.request.partner;

import com.dak.spravel.model.common.Partners.Plan;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreatePartnerRequest {
    
    @NotBlank(message = "Name is required")
    private String name;
    private Plan plan; 
}
