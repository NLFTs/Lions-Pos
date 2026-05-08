package com.dak.spravel.dto.request.inventory;

<<<<<<< HEAD
=======
import jakarta.validation.constraints.NotNull;
>>>>>>> b0700c3517d5b13fa75f6b89ef296ac7ff417635
import lombok.Data;
import java.math.BigDecimal;

@Data
public class TransferRequestItemDTO {
<<<<<<< HEAD
    private Long productId;
=======

    @NotNull(message = "Product ID tidak boleh kosong")
    private Long productId;

    @NotNull(message = "Qty requested tidak boleh kosong")
>>>>>>> b0700c3517d5b13fa75f6b89ef296ac7ff417635
    private BigDecimal qtyRequested;
}