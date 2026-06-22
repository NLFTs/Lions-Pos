package com.dak.spravel.dto.request.order;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ShiftRequest {

    private Long branchId;
    private BigDecimal startingCash = BigDecimal.ZERO;
    // Catatan opsonal untuk shift ini, misal: "Kasir A buka shift pagi, kasir B buka shift sore"
    private String notes;
    private String closingNotes;
}
