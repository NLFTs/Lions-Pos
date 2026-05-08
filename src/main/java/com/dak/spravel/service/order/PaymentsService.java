// =====================================
// PAYMENTS SERVICE
// =====================================

package com.dak.spravel.service.order;

import com.dak.spravel.dto.request.order.PaymentsRequest;
import com.dak.spravel.model.order.Orders;
import com.dak.spravel.model.order.Payments;
import com.dak.spravel.repository.order.OrdersRepository;
import com.dak.spravel.repository.order.PaymentsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PaymentsService {

    private final PaymentsRepository paymentsRepository;
    private final OrdersRepository ordersRepository;

    // CREATE PAYMENT
    public Payments pay(PaymentsRequest request) {

        Orders order = ordersRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        Payments payment = new Payments();

        payment.setOrders((Set<Orders>) order);

        payment.setMethod(
                Payments.Method.valueOf(request.getMethod().toUpperCase())
        );

        payment.setAmount(request.getAmount());

        payment.setCreatedAt(LocalDateTime.now());

        // CASH
        if (request.getMethod().equalsIgnoreCase("cash")) {

            BigDecimal cash = request.getCashTendered();

            if (cash == null) {
                throw new RuntimeException("Cash tendered is required");
            }

            if (cash.compareTo(order.getTotal()) < 0) {
                throw new RuntimeException("Cash is not enough");
            }

            BigDecimal change = cash.subtract(order.getTotal());

            payment.setCashTendered(cash);
            payment.setChangeDue(change);

            payment.setStatus(Payments.Status.VERIFIED);

            order.setStatus(Orders.PaymentStatus.PAID);

            ordersRepository.save(order);
        }

        // TRANSFER
        else if (request.getMethod().equalsIgnoreCase("transfer")) {

            if (request.getBankName() == null ||
                    request.getReferenceNo() == null) {

                throw new RuntimeException(
                        "Bank name and reference number required"
                );
            }

            payment.setBankName(request.getBankName());
            payment.setReferenceNo(request.getReferenceNo());
            payment.setProofUrl(request.getProofUrl());

            payment.setStatus(Payments.Status.PENDING);
        }

        else {
            throw new RuntimeException("Invalid payment method");
        }

        return paymentsRepository.save(payment);
    }

    // GET ALL
    public List<Payments> findAll() {
        return paymentsRepository.findAll();
    }

    // GET BY ID
    public Payments findById(Long id) {
        return paymentsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
    }

    // DELETE
    public void delete(Long id) {

        Payments payment = paymentsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        paymentsRepository.delete(payment);
    }
}