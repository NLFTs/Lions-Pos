package com.fts.twin.service.order;

import com.fts.twin.dto.request.order.ShiftRequest;
import com.fts.twin.dto.response.order.ShiftResponse;
import com.fts.twin.model.auth.User;
import com.fts.twin.model.inventory.Branches;
import com.fts.twin.model.order.CashierShift;
import com.fts.twin.repository.auth.UserRepository;
import com.fts.twin.repository.inventory.BranchesRepository;
import com.fts.twin.repository.order.CashierShiftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CashierShiftService {

    private final CashierShiftRepository shiftRepository;
    private final BranchesRepository branchesRepository;
    private final UserRepository userRepository;

    //Auth Helper

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new RuntimeException("User tidak terautentikasi");
        }
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan di database"));
    }

    private void checkPermission(User user, String permissionSlug) {
        if (user.getPartner() == null) return;
        boolean hasPerm = user.getRoles().stream()
                .filter(role -> role.getPermissions() != null)
                .flatMap(role -> role.getPermissions().stream())
                .anyMatch(perm -> perm.getSlug().equalsIgnoreCase(permissionSlug));
        if (!hasPerm) {
            throw new RuntimeException("Akses Ditolak: Anda tidak memiliki hak akses '" + permissionSlug + "'!");
        }
    }

    // Open Shift 

    @Transactional
    public ShiftResponse openShift(ShiftRequest request) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "shift.open");

        if (currentUser.getPartner() == null) {
            throw new RuntimeException("Super Admin Global tidak diperbolehkan membuka shift.");
        }

        // Validasi branch
        Branches branch = branchesRepository.findById(request.getBranchId())
                .orElseThrow(() -> new RuntimeException("Cabang tidak ditemukan dengan ID: " + request.getBranchId()));

        if (branch.getPartners() == null || !branch.getPartners().getId().equals(currentUser.getPartner().getId())) {
            throw new RuntimeException("Akses Ditolak: Cabang bukan milik partner Anda.");
        }

        // Branch isolation guard
        if (currentUser.getBranch() != null && !currentUser.getBranch().getId().equals(branch.getId())) {
            throw new RuntimeException("Akses Ditolak: Anda hanya boleh membuka shift di cabang penugasan Anda.");
        }

        // Cegah duplikat shift OPEN di branch yang sama
        Optional<CashierShift> existing = shiftRepository.findByCashierIdAndBranchIdAndStatus(
                currentUser.getId(), branch.getId(), CashierShift.ShiftStatus.OPEN);
        if (existing.isPresent()) {
            throw new RuntimeException("Anda masih memiliki shift yang sedang aktif di cabang ini. Tutup shift terlebih dahulu.");
        }

        CashierShift shift = new CashierShift();
        shift.setPartner(currentUser.getPartner());
        shift.setBranch(branch);
        shift.setCashier(currentUser);
        shift.setStartedAt(LocalDateTime.now());
        shift.setStartingCash(request.getStartingCash() != null ? request.getStartingCash() : BigDecimal.ZERO);
        shift.setNotes(request.getNotes());
        shift.setStatus(CashierShift.ShiftStatus.OPEN);
        shift.setTotalRevenue(BigDecimal.ZERO);
        shift.setTotalTransactions(0);
        shift.setCashRevenue(BigDecimal.ZERO);
        shift.setCashTransactions(0);
        shift.setTransferRevenue(BigDecimal.ZERO);
        shift.setTransferTransactions(0);
        shift.setCreatedAt(LocalDateTime.now());

        return ShiftResponse.from(shiftRepository.save(shift));
    }

    // Close Shift
    @Transactional
    public ShiftResponse closeShift(Long shiftId, ShiftRequest request) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "shift.close");

        CashierShift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new RuntimeException("Shift tidak ditemukan dengan ID: " + shiftId));

        // Hanya kasir pemilik shift atau partner admin yang boleh tutup
        if (currentUser.getPartner() != null) {
            boolean isOwnerCashier = shift.getCashier().getId().equals(currentUser.getId());
            boolean isSamePartner = shift.getPartner().getId().equals(currentUser.getPartner().getId());
            if (!isOwnerCashier && !isSamePartner) {
                throw new RuntimeException("Akses Ditolak: Anda tidak bisa menutup shift milik kasir lain.");
            }
        }

        if (shift.getStatus() == CashierShift.ShiftStatus.CLOSED) {
            throw new RuntimeException("Shift ini sudah ditutup sebelumnya.");
        }

        LocalDateTime endedAt = LocalDateTime.now();
        shift.setEndedAt(endedAt);
        shift.setStatus(CashierShift.ShiftStatus.CLOSED);
        shift.setClosingNotes(request != null ? request.getClosingNotes() : null);
        shift.setUpdatedAt(LocalDateTime.now());

        // Hitung omzet dari orders yang dibuat kasir ini di branch ini selama shift berlangsung
        BigDecimal revenue = shiftRepository.calculateRevenue(
                shift.getCashier().getId(),
                shift.getBranch().getId(),
                shift.getStartedAt(),
                endedAt);
        Long txCount = shiftRepository.countTransactions(
                shift.getCashier().getId(),
                shift.getBranch().getId(),
                shift.getStartedAt(),
                endedAt);

        shift.setTotalRevenue(revenue != null ? revenue : BigDecimal.ZERO);
        shift.setTotalTransactions(txCount != null ? txCount.intValue() : 0);

        // Breakdown per metode pembayaran
        BigDecimal cashRev = shiftRepository.calculateRevenueByMethod(
                shift.getCashier().getId(), shift.getBranch().getId(),
                shift.getStartedAt(), endedAt,
                com.fts.twin.model.order.Payments.Method.CASH);
        Long cashTx = shiftRepository.countTransactionsByMethod(
                shift.getCashier().getId(), shift.getBranch().getId(),
                shift.getStartedAt(), endedAt,
                com.fts.twin.model.order.Payments.Method.CASH);
        BigDecimal transferRev = shiftRepository.calculateRevenueByMethod(
                shift.getCashier().getId(), shift.getBranch().getId(),
                shift.getStartedAt(), endedAt,
                com.fts.twin.model.order.Payments.Method.TRANSFER);
        Long transferTx = shiftRepository.countTransactionsByMethod(
                shift.getCashier().getId(), shift.getBranch().getId(),
                shift.getStartedAt(), endedAt,
                com.fts.twin.model.order.Payments.Method.TRANSFER);

        shift.setCashRevenue(cashRev != null ? cashRev : BigDecimal.ZERO);
        shift.setCashTransactions(cashTx != null ? cashTx.intValue() : 0);
        shift.setTransferRevenue(transferRev != null ? transferRev : BigDecimal.ZERO);
        shift.setTransferTransactions(transferTx != null ? transferTx.intValue() : 0);

        return ShiftResponse.from(shiftRepository.save(shift));
    }

    //  Get Active Shift (kasir saat ini)
    public ShiftResponse getActiveShift(Long branchId) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "shift.open");

        Optional<CashierShift> active = shiftRepository.findByCashierIdAndBranchIdAndStatus(
                currentUser.getId(), branchId, CashierShift.ShiftStatus.OPEN);
        return active.map(ShiftResponse::from).orElse(null);
    }

    //  Get Shift By Id 
    public ShiftResponse findById(Long id) {
        User currentUser = getAuthenticatedUser();
        checkPermission(currentUser, "shift.index");

        CashierShift shift = shiftRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shift tidak ditemukan dengan ID: " + id));

        // Multi-tenant guard
        if (currentUser.getPartner() != null &&
                (shift.getPartner() == null || !shift.getPartner().getId().equals(currentUser.getPartner().getId()))) {
            throw new RuntimeException("Akses Ditolak: Shift bukan milik partner Anda.");
        }

        return ShiftResponse.from(shift);
    }

    //  List Shifts
    public List<ShiftResponse> findAll() {
        User currentUser = getAuthenticatedUser();

        // Super Admin — lihat semua
        if (currentUser.getPartner() == null) {
            return shiftRepository.findAllWithDetails().stream()
                    .map(ShiftResponse::from)
                    .toList();
        }

        checkPermission(currentUser, "shift.index");

        List<CashierShift> shifts;

        // Kasir yang dikunci ke satu cabang hanya bisa lihat riwayat shiftnya sendiri
        if (currentUser.getBranch() != null) {
            shifts = shiftRepository.findAllByCashierId(currentUser.getId());
        } else {
            shifts = shiftRepository.findAllByPartnerId(currentUser.getPartner().getId());
        }

        return shifts.stream().map(ShiftResponse::from).toList();
    }
}
