package com.dak.spravel.dto.request.partner;


import com.dak.spravel.model.common.Partners.Plan;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetPartnerByPlan {
    private Plan plan;
}
