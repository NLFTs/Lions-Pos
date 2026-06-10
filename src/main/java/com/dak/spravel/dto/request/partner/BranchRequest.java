package com.dak.spravel.dto.request.partner;

import java.util.List;
import lombok.Data;

@Data
public class BranchRequest {

    
    private String name;

    private String address;

    private String username;

    private String password;

    private List<Long> roleIds;
}
