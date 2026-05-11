package com.dak.spravel.dto.request.partner;

import java.util.List;

import com.dak.spravel.model.common.Partners.Plan;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @Valid
    @NotNull(message = "Admin data is required")
    private UserRequest admin;

    @Valid
    private List<UserRequest> employees;

    @Valid
    @NotNull(message = "Branch data is required")
    private BranchRequest branch;

    @Data
    public static class UserRequest {
        @NotBlank(message = "Username is required")
        private String username;

        @NotBlank(message = "Email is required")
        private String email;

        @NotBlank(message = "Password is required")
        private String password;
    }

    @Data
    public static class BranchRequest {

        @NotBlank(message = "Branch name is required")
        private String name;

        private String address;
    }
}
