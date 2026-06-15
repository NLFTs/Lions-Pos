package com.dak.spravel.dto.request.order;

import lombok.Data;
import java.util.List;

@Data
public class ReturnRequest {

    private List<ReturnItemRequest> items;
    
    private String returnLocationType;
    
    private Long returnLocationId;

    @Data
    public static class ReturnItemRequest {
        private Long orderItemId;
        private Long qtyReturn;
        private String reason;
    }
}
