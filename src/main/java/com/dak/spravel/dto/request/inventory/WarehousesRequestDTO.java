package com.dak.spravel.dto.request.inventory;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;

@Data
public class WarehousesRequestDTO {

    private Long partnersId;

    @NotBlank(message = "Nama warehouse tidak boleh kosong")
    private String name;

    private String address; // opsional

    // ── Akun User Pengelola Gudang (opsional, hanya saat create) ──────────────
    private String username;
    private String password;
    private List<Long> roleIds;
}