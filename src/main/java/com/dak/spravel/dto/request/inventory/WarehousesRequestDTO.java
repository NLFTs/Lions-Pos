package com.dak.spravel.dto.request.inventory;

import lombok.Data;
import java.util.List;

@Data
public class WarehousesRequestDTO {

    private String name;

    private String address;

    private String username;

    private String password;
    
    private List<Long> roleIds;
}