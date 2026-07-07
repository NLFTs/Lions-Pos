package com.fts.twin.dto.response.procurementresponse;

import com.fts.twin.dto.response.components.UserSimpleDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseReceiptResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("purchase_order")
    private PurchaseOrderDto purchaseOrder;

    @JsonProperty("receipt_number")
    private String receiptNumber;

    @JsonProperty("received_date")
    private LocalDate receivedDate;

    @JsonProperty("notes")
    private String notes;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @JsonProperty("created_by")
    private UserSimpleDto createdBy;

    @JsonProperty("updated_by")
    private UserSimpleDto updatedBy;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PurchaseOrderDto {

        @JsonProperty("id")
        private Long id;

        @JsonProperty("po_number")
        private String poNumber;

        @JsonProperty("status")
        private String status;
    }

}