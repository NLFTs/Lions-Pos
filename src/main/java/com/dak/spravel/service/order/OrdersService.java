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

    public Orders create(OrdersRequest request) {

        Partners partner = partnerRepository.findById(request.getPartnerId())
                .orElseThrow(() -> new RuntimeException("Partner not found"));

        Branches branch = branchesRepository.findById(request.getBranchId())
                .orElseThrow(() -> new RuntimeException("Branch not found"));

        User customer = userRepository.findById(request.getCashierId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Voucher voucher = null;
        if (request.getPartnerId() != null) {
            voucher = voucherRepository.findById(request.getPartnerId())
                    .orElseThrow(() -> new RuntimeException("Voucher not found"));
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


    public List<Orders> findAll() {
        return ordersRepository.findAll();
    }


    public Orders findById(Long id) {
        return ordersRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }


    public void delete(Long id) {
        ordersRepository.deleteById(id);
    }
}