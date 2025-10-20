package com.hacom.telecom.order_processing_service.service;

import com.hacom.telecom.order_processing_service.model.Order;
import com.hacom.telecom.order_processing_service.model.OrderItem;
import com.hacom.telecom.order_processing_service.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrderRepository orderRepository;

    public Mono<Order> createOrder(String orderId, String customerId, String customerPhoneNumber, List<OrderItem> items) {
        return orderRepository.findByOrderId(orderId)
                .doOnNext(existingOrder -> {
                    log.info("Order with orderId '{}' already exists. Returning existing order with status: {}", 
                            orderId, existingOrder.getStatus());
                })
                .switchIfEmpty(
                    Mono.defer(() -> {
                        log.info("Creating new order with orderId: {}", orderId);
                        Order newOrder = new Order(orderId, customerId, customerPhoneNumber, items, "PENDING");
                        return orderRepository.save(newOrder)
                                .doOnSuccess(savedOrder -> 
                                    log.info("New order successfully created with orderId: {}, status: {}", 
                                            orderId, savedOrder.getStatus())
                                );
                    })
                );
    }

    public Mono<Order> updateOrderStatus(String orderId, String status) {
        return orderRepository.findByOrderId(orderId)
                .flatMap(order -> {
                    order.setStatus(status);
                    return orderRepository.save(order);
                });
    }

    public Mono<Order> findOrderByOrderId(String orderId) {
        return orderRepository.findByOrderId(orderId);
    }
    
    public Mono<Order> findOrderById(String id) {
        return orderRepository.findById(id);
    }
    
    public Mono<Long> countOrdersByDateRange(OffsetDateTime startDate, OffsetDateTime endDate) {
        log.info("Counting orders between {} and {}", startDate, endDate);
        return orderRepository.countByTsBetween(startDate, endDate)
                .doOnSuccess(count -> 
                    log.info("Found {} orders in the specified date range", count)
                );
    }
}
