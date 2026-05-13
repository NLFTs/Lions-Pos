package com.dak.spravel.service.order;

import com.dak.spravel.dto.request.order.OrdersRequest;
import com.dak.spravel.model.catalog.Voucher;
import com.dak.spravel.model.common.Partners;
import com.dak.spravel.model.inventory.Branches;
import com.dak.spravel.model.auth.User;
import com.dak.spravel.model.order.Orders;
import com.dak.spravel.repository.catalog.VoucherRepository;
import com.dak.spravel.repository.common.PartnerRepository;
import com.dak.spravel.repository.inventory.BranchesRepository;
import com.dak.spravel.repository.auth.UserRepository;
import com.dak.spravel.repository.order.OrdersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrdersService {

    private final OrdersRepository ordersRepository;
    private final PartnerRepository partnerRepository;
    private final BranchesRepository branchesRepository;
    private final UserRepository userRepository;
    private final VoucherRepository voucherRepository;

    // =========================
    // AUTH USER
    // =========================
    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new RuntimeException("User tidak terautentikasi");
        }

        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan di database"));
    }

    // =========================
    // SUPER ADMIN / ADMIN
    // =========================
    private boolean isAdmin(User user) {
        return user.getRoles().stream()
                .anyMatch(role ->
                        role.getSlug().equals("super_admin")
                                || role.getSlug().equals("admin"));
    }

    // =========================
    // ADMIN PARTNER / EMPLOYEE
    // =========================
    private boolean isAdminPartnerAndEmployee(User user) {
        return user.getRoles().stream()
                .anyMatch(role ->
                        role.getSlug().equals("employee")
                                || role.getSlug().equals("admin-partners"));
    }

    // =========================
    // VALIDASI ORDER (cross-partner)
    // =========================
    private Orders getValidatedOrder(Long id, User currentUser) {

        // VALIDASI 1: Admin tidak boleh akses
        if (isAdmin(currentUser)) {
            throw new RuntimeException(
                    "Akses Ditolak: Admin tidak diperbolehkan mengelola Orders.");
        }

        Orders order = ordersRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // VALIDASI 2: Cross-Partner Check
        if (currentUser.getPartner() == null
                || order.getPartner() == null
                || !order.getPartner().getId().equals(currentUser.getPartner().getId())) {
            throw new RuntimeException(
                    "Akses Ditolak: Order bukan milik partner Anda.");
        }

        return order;
    }

    // =========================
    // KHUSUS SUPER ADMIN
    // =========================
    public List<Orders> findAllOrders() {
        User currentUser = getAuthenticatedUser();

        if (isAdminPartnerAndEmployee(currentUser)) {
            throw new RuntimeException(
                    "Akses Ditolak: Employee dan Admin Partner tidak diperbolehkan melihat semua Orders.");
        }

        return ordersRepository.findAll();
    }

    // =========================
    // KHUSUS PARTNER / EMPLOYEE
    // =========================
    public List<Orders> findAll() {
        User currentUser = getAuthenticatedUser();

        if (isAdmin(currentUser)) {
            throw new RuntimeException(
                    "Akses Ditolak: Admin tidak diperbolehkan mengelola Orders.");
        }

        return ordersRepository.findAll()
                .stream()
                .filter(order ->
                        order.getPartner() != null
                                && order.getPartner().getId()
                                .equals(currentUser.getPartner().getId()))
                .toList();
    }

    // =========================
    // GET BY ID
    // =========================
    public Orders findById(Long id) {
        User currentUser = getAuthenticatedUser();

        return getValidatedOrder(id, currentUser);
    }

    // =========================
    // CREATE
    // =========================
    public Orders create(OrdersRequest request) {
        User currentUser = getAuthenticatedUser();

        // VALIDASI 1: Admin tidak boleh create
        if (isAdmin(currentUser)) {
            throw new RuntimeException(
                    "Akses Ditolak: Admin tidak diperbolehkan membuat Orders.");
        }

        // VALIDASI 2: User harus punya partner
        if (currentUser.getPartner() == null) {
            throw new RuntimeException("User ini tidak terasosiasi dengan Partner manapun.");
        }

        // Gunakan partner dari user login, bukan dari request
        Partners partner = currentUser.getPartner();

        Branches branch = branchesRepository.findById(request.getBranchId())
                .orElseThrow(() -> new RuntimeException("Branch not found"));

        // VALIDASI 3: Branch harus milik partner yang sama
        if (branch.getPartners() == null
                || !branch.getPartners().getId().equals(partner.getId())) {
            throw new RuntimeException(
                    "Akses Ditolak: Branch bukan milik partner Anda.");
        }

        User customer = userRepository.findById(request.getCashierId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Voucher voucher = null;
        if (request.getVoucherId() != null) {
            voucher = voucherRepository.findById(request.getVoucherId())
                    .orElseThrow(() -> new RuntimeException("Voucher not found"));

            // VALIDASI 4: Voucher harus milik partner yang sama
            if (voucher.getPartner() == null
                    || !voucher.getPartner().getId().equals(partner.getId())) {
                throw new RuntimeException(
                        "Akses Ditolak: Voucher bukan milik partner Anda.");
            }
        }

        Orders order = new Orders();
        order.setPartner(partner); // pakai partner dari user login
        order.setBranch(branch);
        order.setCustomer(customer);
        order.setOrderNumber(request.getOrderNumber());
        order.setVoucher(voucher);
        order.setNotes(request.getNotes());
        order.setStatus(Orders.PaymentStatus.DRAFT);
        order.setSubtotal(BigDecimal.ZERO);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setTotal(BigDecimal.ZERO);

        return ordersRepository.save(order);
    }

    // =========================
    // DELETE
    // =========================
    public void delete(Long id) {
        User currentUser = getAuthenticatedUser();

        if (isAdmin(currentUser)) {
            throw new RuntimeException(
                    "Akses Ditolak: Admin tidak diperbolehkan menghapus Orders.");
        }

        Orders order = getValidatedOrder(id, currentUser);
        ordersRepository.delete(order);
    }
}