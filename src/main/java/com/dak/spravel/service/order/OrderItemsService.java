package com.dak.spravel.service.order;

import com.dak.spravel.dto.request.order.OrderItemsRequest;
import com.dak.spravel.model.catalog.Product;
import com.dak.spravel.model.order.OrderItems;
import com.dak.spravel.model.order.Orders;
import com.dak.spravel.repository.catalog.ProductRepository;
import com.dak.spravel.repository.order.OrderItemsRepository;
import com.dak.spravel.repository.order.OrdersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderItemsService {

    private final OrderItemsRepository orderItemsRepository;
    private final ProductRepository productRepository;
    private final OrdersRepository ordersRepository;


    public OrderItems create(OrderItemsRequest request) {

        Orders order = ordersRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        OrderItems item = new OrderItems();
        item.setOrder(order);
        item.setProduct(product);


        item.setProductName(product.getName());
        item.setUnitPrice(product.getBasePrice());

        item.setQty(request.getQty());
        item.setSubtotal(product.getBasePrice().multiply(request.getQty()));

        return orderItemsRepository.save(item);
    }


    public List<OrderItems> findAll() {
        return orderItemsRepository.findAll();
    }


    public OrderItems findById(Long id) {
        return orderItemsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order item not found"));
    }

    public OrderItems findByProductName(String productName) {
        return orderItemsRepository.findByProductName(productName)
                .orElseThrow(() -> new RuntimeException("Order item not found"));
    }


    public void delete(Long id) {
        OrderItems item = orderItemsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order item not found"));

        orderItemsRepository.delete(item);
    }
}