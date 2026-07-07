package com.fts.twin.dto.request.inventory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class StockMutationRequestDTO {

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
    private String fromLocationType; 

    private Long fromLocationId;

    @Pattern(regexp = "branch|warehouse",
            message = "To location type harus 'branch' atau 'warehouse'")
    private String toLocationType; 

    private Long toLocationId; 

    @NotNull(message = "Qty tidak boleh kosong")
    private BigDecimal qty;

    @Pattern(regexp = "order|transfer_request|stock_opname",
            message = "Reference type harus 'order', 'transfer_request', atau 'stock_opname'")
    private String referenceType; 

    private Long referenceId; 
    
    private String notes; 
}