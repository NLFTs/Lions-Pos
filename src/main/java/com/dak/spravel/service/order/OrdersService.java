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
    // KHUSUS SUPER ADMIN
    // =========================
    private User getAuthenticatedSuperAdmin() {
        User user = getAuthenticatedUser();
        boolean isSuperAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getSlug().equalsIgnoreCase("super_admin"));
        if (!isSuperAdmin) throw new RuntimeException("Akses ditolak: Anda bukan Super Admin");
        return user;
    }

    // =========================
    // KHUSUS ADMIN PARTNER / EMPLOYEE
    // =========================
    private User getAuthenticatedAdminPartnerOrEmployee() {
        User user = getAuthenticatedUser();
        boolean isAuthorized = user.getRoles().stream()
                .anyMatch(role -> role.getSlug().equalsIgnoreCase("admin-partners") ||
                        role.getSlug().equalsIgnoreCase("employee"));
        boolean isNotSuperAdmin = user.getRoles().stream()
                .noneMatch(role -> role.getSlug().equalsIgnoreCase("super_admin"));
        if (!isAuthorized || !isNotSuperAdmin) {
            throw new RuntimeException("Akses Ditolak: Hanya Admin Partner atau Employee yang diizinkan.");
        }
        return user;
    }

    // =========================
    // VALIDASI ORDER (cross-partner)
    // =========================
    private Orders getValidatedOrder(Long id, User currentUser) {
        Orders order = ordersRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (currentUser.getPartner() == null
                || order.getPartner() == null
                || !order.getPartner().getId().equals(currentUser.getPartner().getId())) {
            throw new RuntimeException("Akses Ditolak: Order bukan milik partner Anda.");
        }

        return order;
    }

    // =========================
    // KHUSUS SUPER ADMIN
    // =========================
    public List<Orders> findAllOrders() {
        getAuthenticatedSuperAdmin();
        return ordersRepository.findAll();
    }

    // =========================
    // KHUSUS PARTNER / EMPLOYEE
    // =========================
    public List<Orders> findAll() {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();

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
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();
        return getValidatedOrder(id, currentUser);
    }

    // =========================
    // CREATE
    // =========================
    public Orders create(OrdersRequest request) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();

        Partners partner = currentUser.getPartner();
        if (partner == null) {
            throw new RuntimeException("User tidak terasosiasi dengan Partner.");
        }

        Branches branch = branchesRepository.findById(request.getBranchId())
                .orElseThrow(() -> new RuntimeException("Branch not found"));

        if (branch.getPartners() == null
                || !branch.getPartners().getId().equals(partner.getId())) {
            throw new RuntimeException("Akses Ditolak: Branch bukan milik partner Anda.");
        }

        User customer = userRepository.findById(request.getCashierId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Voucher voucher = null;
        if (request.getVoucherId() != null) {
            voucher = voucherRepository.findById(request.getVoucherId())
                    .orElseThrow(() -> new RuntimeException("Voucher not found"));

            if (voucher.getPartner() == null
                    || !voucher.getPartner().getId().equals(partner.getId())) {
                throw new RuntimeException("Akses Ditolak: Voucher bukan milik partner Anda.");
            }
        }

        Orders order = new Orders();
        order.setPartner(partner);
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
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();
        Orders order = getValidatedOrder(id, currentUser);
        ordersRepository.delete(order);
    }
}