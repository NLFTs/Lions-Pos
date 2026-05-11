package com.dak.spravel.dto.response.inventoryresponse;

import com.dak.spravel.dto.response.components.PartnerSimpleDto;
import com.dak.spravel.dto.response.components.UserSimpleDto;
import com.dak.spravel.model.inventory.TransferRequest;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransferRequestResponse {
    private Long id;
    private PartnerSimpleDto partner;

    @JsonProperty("from_location_type")
    private String fromLocationType;

    @JsonProperty("from_location_id")
    private Long fromLocationId;

    @JsonProperty("to_location_type")
    private String toLocationType;

    @JsonProperty("to_location_id")
    private Long toLocationId;

    private TransferRequest.Status status;
    private String notes;

    @JsonProperty("requested_at")
    private LocalDateTime requestedAt;

    @JsonProperty("approved_at")
    private LocalDateTime approvedAt;

    @JsonProperty("received_at")
    private LocalDateTime receivedAt;

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

    @JsonProperty("approved_by")
    private UserSimpleDto approvedBy;

    @JsonProperty("received_by")
    private UserSimpleDto receivedBy;

    private List<TransferRequestItemResponse> items;

    @Data
    public static class TransferRequestItemResponse {
        private Long id;
        private ProductSimpleDto product;

        @JsonProperty("qty_requested")
        private BigDecimal qtyRequested;

        @JsonProperty("qty_received")
        private BigDecimal qtyReceived;
    }

    @Data
    public static class ProductSimpleDto {
        private Long id;
        private String name;
        private String sku;
    }
}