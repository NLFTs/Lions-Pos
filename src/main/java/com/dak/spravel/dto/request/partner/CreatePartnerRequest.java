package com.dak.spravel.dto.request.partner;

import java.util.List;

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

    private UserRequest admin;

    private List<UserRequest> employees;

    private List<BranchRequest> branches;
    private List<WarehouseRequest> warehouses;
    private List<BranchWarehouseMappingRequest> branchWarehouses;

    @Data
    public static class UserRequest {
        private String username;
        private String email;
        private String password;
    }
}
