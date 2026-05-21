package com.dak.spravel.dto.request.order;

import lombok.Data;
import java.util.List;

@Data
public class ReturnRequest {

    private List<ReturnItemRequest> items;

    @Data
    public static class ReturnItemRequest {
        private Long orderItemId;
        private Long qtyReturn;
        private String reason;
    }
}
