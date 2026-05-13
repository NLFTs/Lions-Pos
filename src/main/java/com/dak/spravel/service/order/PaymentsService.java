package com.dak.spravel.service.order;

import com.dak.spravel.dto.request.order.PaymentsRequest;
import com.dak.spravel.model.auth.User;
import com.dak.spravel.model.order.Orders;
import com.dak.spravel.model.order.Payments;
import com.dak.spravel.repository.auth.UserRepository;
import com.dak.spravel.repository.order.OrdersRepository;
import com.dak.spravel.repository.order.PaymentsRepository;
import com.dak.spravel.handler.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PaymentsService {

    private final PaymentsRepository paymentsRepository;
    private final OrdersRepository ordersRepository;
    private final UserRepository userRepository;


    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new RuntimeException("User tidak terautentikasi");
        }

        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
    }

    private boolean isAdmin(User user) {
        return user.getRoles().stream().anyMatch(role -> role.getSlug().equals("super_admin") ||
                role.getSlug().equals("admin"));
    }

    private boolean isAdminPartnerAndEmployee(User user) {
        return user.getRoles().stream().anyMatch(role -> role.getSlug().equals("employee") ||
                                role.getSlug().equals("admin-partners"));
    }

    private Payments getValidatedPayment(Long id, User currentUser) {

        if (isAdmin(getAuthenticatedUser())) {
            throw new RuntimeException("Admin tidak boleh akses payments");
        }

        return paymentsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", id));
    }

    public List<Payments> findAllPayments() {

        User currentUser = getAuthenticatedUser();

        if (isAdminPartnerAndEmployee(currentUser)) {
            throw new RuntimeException("Role ini tidak boleh akses semua payments");
        }

        return paymentsRepository.findAll();
    }

    public List<Payments> findAll() {

        User currentUser = getAuthenticatedUser();

        if (isAdmin(currentUser)) {
            throw new RuntimeException("Admin tidak boleh akses module ini");
        }

        return paymentsRepository.findAll();
    }

    public Page<Payments> findAll(int page, int size) {

        User currentUser = getAuthenticatedUser();

        if (isAdmin(currentUser)) {
            throw new RuntimeException("Admin tidak boleh akses module ini");
        }

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());

        return paymentsRepository.findAll(pageRequest);
    }

    @Transactional
    public Payments pay(PaymentsRequest request) {

        User currentUser = getAuthenticatedUser();

        Orders order = ordersRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        Payments payment = new Payments();

        payment.setOrders(Set.of(order));

        payment.setMethod(Payments.Method.valueOf(request.getMethod().toUpperCase()));
        payment.setAmount(request.getAmount());
        payment.setCreatedAt(LocalDateTime.now());

        if (request.getMethod().equalsIgnoreCase("cash")) {

            BigDecimal cash = request.getCashTendered();

            if (cash == null) {
                throw new RuntimeException("Cash wajib diisi");
            }

            if (cash.compareTo(order.getTotal()) < 0) {
                throw new RuntimeException("Cash tidak cukup");
            }

            payment.setCashTendered(cash);
            payment.setChangeDue(cash.subtract(order.getTotal()));
            payment.setStatus(Payments.Status.VERIFIED);

            order.setStatus(Orders.PaymentStatus.PAID);
        }

        else if (request.getMethod().equalsIgnoreCase("transfer")) {

            if (request.getBankName() == null || request.getReferenceNo() == null) {
                throw new RuntimeException("Bank & reference wajib diisi");
            }

            payment.setBankName(request.getBankName());
            payment.setReferenceNo(request.getReferenceNo());
            payment.setProofUrl(request.getProofUrl());
            payment.setStatus(Payments.Status.PENDING);

            order.setStatus(Orders.PaymentStatus.PAID);
        }

        else {
            throw new RuntimeException("Method tidak valid");
        }

        ordersRepository.save(order);

        return paymentsRepository.save(payment);
    }

    public void delete(Long id) {

        User currentUser = getAuthenticatedUser();

        if (isAdmin(currentUser)) {
            throw new RuntimeException("Admin tidak boleh delete payments");
        }

        Payments payment = getValidatedPayment(id, currentUser);

        paymentsRepository.delete(payment);
    }
}