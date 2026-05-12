package com.dak.spravel.dto.response.procurementresponse;

import com.dak.spravel.dto.response.components.PartnerSimpleDto;
import com.dak.spravel.dto.response.components.UserSimpleDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("partner")
    private PartnerSimpleDto partner;

    @JsonProperty("name")
    private String name;

    @JsonProperty("phone")
    private String phone;

    @JsonProperty("email")
    private String email;

    @JsonProperty("address")
    private String address;

    @JsonProperty("notes")
    private String notes;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @JsonProperty("deleted_at")
    private LocalDateTime deletedAt;

    @JsonProperty("created_by")
    private UserSimpleDto createdBy;

    @JsonProperty("updated_by")
    private UserSimpleDto updatedBy;

    @JsonProperty("deleted_by")
    private UserSimpleDto deletedBy;
}