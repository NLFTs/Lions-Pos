package com.dak.spravel.dto.response.procurementresponse;

import com.dak.spravel.dto.response.components.PartnerSimpleDto;
import com.dak.spravel.dto.response.components.UserSimpleDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("partner")
    private PartnerSimpleDto partner;

    @JsonProperty("supplier")
    private SupplierDto supplier;

    @JsonProperty("po_number")
    private String poNumber;

    @JsonProperty("location_type")
    private String locationType;

    @JsonProperty("location_id")
    private Long locationId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("order_date")
    private LocalDate orderDate;

    @JsonProperty("expected_date")
    private LocalDate expectedDate;

    @JsonProperty("notes")
    private String notes;

    @JsonProperty("total")
    private BigDecimal total;

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

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SupplierDto {

        @JsonProperty("id")
        private Long id;

        @JsonProperty("name")
        private String name;

        @JsonProperty("phone")
        private String phone;
    }
}