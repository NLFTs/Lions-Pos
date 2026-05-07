package com.dak.spravel.dto.request.procurement;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PurchaseReceiptRequestDTO {
    private Long purchaseOrderId;
    private LocalDateTime receivedDate;
    private String notes;
    private List<PurchaseReceiptItemDTO> items;
}