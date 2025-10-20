package com.hacom.telecom.order_processing_service.service;

import com.hacom.telecom.order_processing_service.model.Order;
import com.hacom.telecom.order_processing_service.model.OrderItem;
import com.hacom.telecom.order_processing_service.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrderRepository orderRepository;

    /**
     * Crea un pedido. Si ya existe un pedido con ese orderId, devuelve el existente.
     * No crea duplicados basándose en el orderId del negocio (no confundir con el _id de MongoDB).
     */
    public Mono<Order> createOrder(String orderId, String customerId, String customerPhoneNumber, List<OrderItem> items) {
        return orderRepository.findByOrderId(orderId)
                .doOnNext(existingOrder -> {
                    // Log cuando encuentra un pedido existente
                    log.info("Order with orderId '{}' already exists. Returning existing order with status: {}", 
                            orderId, existingOrder.getStatus());
                })
                .switchIfEmpty(
                    // Si no existe, crear uno nuevo
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

    /**
     * Actualiza el estado de un pedido buscando por orderId
     */
    public Mono<Order> updateOrderStatus(String orderId, String status) {
        return orderRepository.findByOrderId(orderId)
                .flatMap(order -> {
                    order.setStatus(status);
                    return orderRepository.save(order);
                });
    }

    /**
     * Busca un pedido por orderId (no por el _id de MongoDB)
     */
    public Mono<Order> findOrderByOrderId(String orderId) {
        return orderRepository.findByOrderId(orderId);
    }
    
    /**
     * Busca un pedido por su _id de MongoDB
     */
    public Mono<Order> findOrderById(String id) {
        return orderRepository.findById(id);
    }
}
