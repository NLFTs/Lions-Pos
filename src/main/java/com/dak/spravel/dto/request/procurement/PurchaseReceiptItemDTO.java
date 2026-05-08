package com.dak.spravel.dto.request.procurement;

<<<<<<< HEAD
=======
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
>>>>>>> b0700c3517d5b13fa75f6b89ef296ac7ff417635
import lombok.Data;
import java.math.BigDecimal;

@Data
public class PurchaseReceiptItemDTO {
<<<<<<< HEAD
    private Long purchaseOrderItemId;
    private Long productId;
    private BigDecimal qtyReceived;
    private BigDecimal unitCost;
    private String notes;
=======

    @NotNull(message = "Purchase Order Item ID tidak boleh kosong")
    private Long purchaseOrderItemId;

    @NotNull(message = "Product ID tidak boleh kosong")
    private Long productId;

    @NotNull(message = "Qty received tidak boleh kosong")
    @Positive(message = "Qty received harus lebih dari 0")
    private BigDecimal qtyReceived;

    private BigDecimal unitCost; // opsional, diambil dari PO item di service
    private String notes;        // opsional
>>>>>>> b0700c3517d5b13fa75f6b89ef296ac7ff417635
}