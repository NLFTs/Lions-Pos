package com.fts.twin.dto.response.components;

import lombok.Data;

/**
 * Disini bisa digunakan untuk menampilkan data user yang memanage data tertentu 
 */
@Data
public class UserSimpleDto {
    private Long id;
    private String username;
}
