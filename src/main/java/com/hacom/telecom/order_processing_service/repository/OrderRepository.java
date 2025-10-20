package com.hacom.telecom.order_processing_service.repository;

import com.hacom.telecom.order_processing_service.model.Order;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

@Repository
public interface OrderRepository extends ReactiveMongoRepository<Order, String> {
    
    Mono<Order> findByOrderId(String orderId);
    
    Mono<Long> countByTsBetween(OffsetDateTime startDate, OffsetDateTime endDate);
}
