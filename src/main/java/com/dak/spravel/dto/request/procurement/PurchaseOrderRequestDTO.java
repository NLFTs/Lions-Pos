package com.dak.spravel.dto.request.procurement;

import lombok.Data;
import java.sql.Date;
import java.util.List;

@Data
public class PurchaseOrderRequestDTO {
    private Long partnerId;
    private Long supplierId;
    private String locationType;
    private Long locationId;
    private Date orderDate;
    private Date expectedDate;
    private String notes;
    private List<PurchaseOrderItemDTO> items;
}