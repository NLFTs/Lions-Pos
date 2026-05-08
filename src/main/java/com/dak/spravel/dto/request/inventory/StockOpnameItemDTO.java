package com.dak.spravel.dto.request.inventory;

<<<<<<< HEAD
=======
import jakarta.validation.constraints.NotNull;
>>>>>>> b0700c3517d5b13fa75f6b89ef296ac7ff417635
import lombok.Data;
import java.math.BigDecimal;

@Data
public class StockOpnameItemDTO {
<<<<<<< HEAD
    private Long productId;
    private BigDecimal qtySystem;
    private BigDecimal qtyPhysical;
    private String notes;
=======

    @NotNull(message = "Product ID tidak boleh kosong")
    private Long productId;

    @NotNull(message = "Qty system tidak boleh kosong")
    private BigDecimal qtySystem;

    private BigDecimal qtyPhysical; // opsional, diisi saat counting

    private String notes; // opsional
>>>>>>> b0700c3517d5b13fa75f6b89ef296ac7ff417635
}