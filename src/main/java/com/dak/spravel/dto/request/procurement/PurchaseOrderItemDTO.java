package com.dak.spravel.dto.request.procurement;

<<<<<<< HEAD
=======
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
>>>>>>> b0700c3517d5b13fa75f6b89ef296ac7ff417635
import lombok.Data;
import java.math.BigDecimal;

@Data
public class PurchaseOrderItemDTO {
<<<<<<< HEAD
    private Long productId;
    private BigDecimal qtyOrdered;
=======

    @NotNull(message = "Product ID tidak boleh kosong")
    private Long productId;

    @NotNull(message = "Qty ordered tidak boleh kosong")
    @Positive(message = "Qty ordered harus lebih dari 0")
    private BigDecimal qtyOrdered;

    @NotNull(message = "Unit cost tidak boleh kosong")
    @Positive(message = "Unit cost harus lebih dari 0")
>>>>>>> b0700c3517d5b13fa75f6b89ef296ac7ff417635
    private BigDecimal unitCost;
}