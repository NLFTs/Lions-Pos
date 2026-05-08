package com.dak.spravel.dto.request.procurement;

import lombok.Data;

@Data
public class SupplierRequestDTO {
    private Long partnerId;
    private String name;
    private String phone;
    private String address;
    private String notes;
}