package com.dak.spravel.dto.response.inventoryresponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StockLocationSummaryResponse {

    // Rekap per product (total semua lokasi)
    private Long productId;
    private String productName;
    private String sku;
    private BigDecimal totalQty;

    private List<StockPerLocation> perLokasi;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StockPerLocation {
        private String locationType;   // "BRANCH" atau "WAREHOUSE"
        private Long locationId;
        private String locationName;
        private BigDecimal qty;
    }
}