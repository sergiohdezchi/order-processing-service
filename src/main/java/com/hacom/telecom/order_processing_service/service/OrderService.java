package com.hacom.telecom.order_processing_service.service;

import com.hacom.telecom.order_processing_service.model.Order;
import com.hacom.telecom.order_processing_service.model.OrderItem;
import com.hacom.telecom.order_processing_service.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    public Mono<Order> createOrder(String orderId, String customerId, String customerPhoneNumber, List<OrderItem> items) {
        Order order = new Order(orderId, customerId, customerPhoneNumber, items, "PENDING");
        return orderRepository.save(order);
    }

    public Mono<Order> updateOrderStatus(String orderId, String status) {
        return orderRepository.findById(orderId)
                .flatMap(order -> {
                    order.setStatus(status);
                    return orderRepository.save(order);
                });
    }

    public Mono<Order> findOrderById(String orderId) {
        return orderRepository.findById(orderId);
    }
}
