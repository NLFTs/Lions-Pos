package com.dak.spravel.dto.request.inventory;

<<<<<<< HEAD
=======
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
>>>>>>> b0700c3517d5b13fa75f6b89ef296ac7ff417635
import lombok.Data;
import java.math.BigDecimal;

@Data
public class StockMutationRequestDTO {
<<<<<<< HEAD
    private Long productId;
    private Long partnerId;
    private String type;
    private String fromLocationType;
    private Long fromLocationId;
    private String toLocationType;
    private Long toLocationId;
    private BigDecimal qty;
    private String referenceType;
    private Long referenceId;
    private String notes;
=======

    @NotNull(message = "Product ID tidak boleh kosong")
    private Long productId;

    @NotNull(message = "Partner ID tidak boleh kosong")
    private Long partnerId;

    @NotBlank(message = "Type tidak boleh kosong")
    @Pattern(regexp = "sale_out|purchase_in|transfer|adjustment|return",
            message = "Type harus 'sale_out', 'purchase_in', 'transfer', 'adjustment', atau 'return'")
    private String type;

    @Pattern(regexp = "branch|warehouse",
            message = "From location type harus 'branch' atau 'warehouse'")
    private String fromLocationType; // opsional

    private Long fromLocationId; // opsional

    @Pattern(regexp = "branch|warehouse",
            message = "To location type harus 'branch' atau 'warehouse'")
    private String toLocationType; // opsional

    private Long toLocationId; // opsional

    @NotNull(message = "Qty tidak boleh kosong")
    private BigDecimal qty;

    @Pattern(regexp = "order|transfer_request|stock_opname",
            message = "Reference type harus 'order', 'transfer_request', atau 'stock_opname'")
    private String referenceType; // opsional

    private Long referenceId; // opsional

    private String notes; // opsional
>>>>>>> b0700c3517d5b13fa75f6b89ef296ac7ff417635
}