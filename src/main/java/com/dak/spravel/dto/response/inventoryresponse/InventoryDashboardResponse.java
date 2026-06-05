package com.dak.spravel.dto.response.inventoryresponse;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InventoryDashboardResponse {
    @JsonProperty("total_products")
    private long totalProducts;

    @JsonProperty("damaged_products")
    private long damagedProducts;

    @JsonProperty("incoming_products")
    private long incomingProducts;

    @JsonProperty("outgoing_products")
    private long outgoingProducts;

    @JsonProperty("chart_data")
    private List<DailyMovementDto> chartData;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DailyMovementDto {
        private String date;
        private long incoming;
        private long outgoing;
    }
}
