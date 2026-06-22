package com.dak.spravel.dto.response.order;

import com.dak.spravel.model.order.CashierShift;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ShiftResponse {

    private Long id;
    private Long cashierId;
    private String cashierUsername;
    private String cashierFullname;
    private Long branchId;
    private String branchName;
    private Long partnerId;
    private String partnerName;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private BigDecimal startingCash;
    private BigDecimal totalRevenue;
    private Integer totalTransactions;

    // Breakdown per metode pembayaran
    private BigDecimal cashRevenue;
    private BigDecimal transferRevenue;
    private Integer cashTransactions;
    private Integer transferTransactions;
    private String status;
    private String notes;
    private String closingNotes;
    private LocalDateTime createdAt;

    public static ShiftResponse from(CashierShift shift) {
        return ShiftResponse.builder()
                .id(shift.getId())
                .cashierId(shift.getCashier() != null ? shift.getCashier().getId() : null)
                .cashierUsername(shift.getCashier() != null ? shift.getCashier().getUsername() : null)
                .cashierFullname(shift.getCashier() != null ? shift.getCashier().getFullname() : null)
                .branchId(shift.getBranch() != null ? shift.getBranch().getId() : null)
                .branchName(shift.getBranch() != null ? shift.getBranch().getName() : null)
                .partnerId(shift.getPartner() != null ? shift.getPartner().getId() : null)
                .partnerName(shift.getPartner() != null ? shift.getPartner().getName() : null)
                .startedAt(shift.getStartedAt())
                .endedAt(shift.getEndedAt())
                .startingCash(shift.getStartingCash())
                .totalRevenue(shift.getTotalRevenue())
                .totalTransactions(shift.getTotalTransactions())
                .cashRevenue(shift.getCashRevenue())
                .transferRevenue(shift.getTransferRevenue())
                .cashTransactions(shift.getCashTransactions())
                .transferTransactions(shift.getTransferTransactions())
                .status(shift.getStatus() != null ? shift.getStatus().name() : null)
                .notes(shift.getNotes())
                .closingNotes(shift.getClosingNotes())
                .createdAt(shift.getCreatedAt())
                .build();
    }
}
