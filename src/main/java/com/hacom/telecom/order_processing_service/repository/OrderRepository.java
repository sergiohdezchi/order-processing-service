package com.hacom.telecom.order_processing_service.repository;

import com.hacom.telecom.order_processing_service.model.Order;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface OrderRepository extends ReactiveMongoRepository<Order, String> {
    
    /**
     * Busca un pedido por su orderId (no por el _id de MongoDB)
     */
    Mono<Order> findByOrderId(String orderId);
}
