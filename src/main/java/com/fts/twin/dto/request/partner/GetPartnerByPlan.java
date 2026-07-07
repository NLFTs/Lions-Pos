package com.fts.twin.dto.request.partner;


import com.fts.twin.model.common.Partners.Plan;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class
GetPartnerByPlan {
    private Plan plan;
}
