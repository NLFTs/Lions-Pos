package com.fts.twin.controller.order;

import com.fts.twin.dto.request.order.ShiftRequest;
import com.fts.twin.dto.response.ResData;
import com.fts.twin.dto.response.order.ShiftResponse;
import com.fts.twin.service.order.CashierShiftService;
import com.fts.twin.util.ResponseBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/shifts")
@RequiredArgsConstructor
public class CashierShiftController {

    private final CashierShiftService shiftService;

    // Daftar Semua Shitf
    @GetMapping
    public ResponseEntity<ResData<List<ShiftResponse>>> findAll() {
        log.info("[GET] /api/v1/shifts");
        return ResponseBuilder.ok(shiftService.findAll());
    }

    // Detail Satu Shift
    @GetMapping("/{id}")
    public ResponseEntity<ResData<ShiftResponse>> findById(@PathVariable Long id) {
        log.info("[GET] /api/v1/shifts/{}", id);
        return ResponseBuilder.ok(shiftService.findById(id));
    }

    // Melihat Status Kasir
    @GetMapping("/active")
    public ResponseEntity<ResData<ShiftResponse>> getActiveShift(@RequestParam Long branchId) {
        log.info("[GET] /api/v1/shifts/active?branchId={}", branchId);
        return ResponseBuilder.ok(shiftService.getActiveShift(branchId));
    }

    // Buka Shift Baru
    @PostMapping("/open")
    public ResponseEntity<ResData<ShiftResponse>> openShift(@RequestBody ShiftRequest request) {
        log.info("[POST] /api/v1/shifts/open");
        return ResponseBuilder.ok(shiftService.openShift(request));
    }
    
    // Tutup Shift
    @PatchMapping("/{id}/close")
    public ResponseEntity<ResData<ShiftResponse>> closeShift(
            @PathVariable Long id,
            @RequestBody(required = false) ShiftRequest request) {
        log.info("[PATCH] /api/v1/shifts/{}/close", id);
        return ResponseBuilder.ok(shiftService.closeShift(id, request));
    }
}
