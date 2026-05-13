package com.dak.spravel.dto.request.inventory;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BranchesRequestDTO {
    
    @JsonProperty("name")
    private String name;

    @Column(columnDefinition = "TEXT")
    private String address; // opsional
}