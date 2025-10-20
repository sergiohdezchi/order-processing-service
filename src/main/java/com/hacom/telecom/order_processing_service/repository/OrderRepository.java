package com.hacom.telecom.order_processing_service.repository;

import com.hacom.telecom.order_processing_service.model.Order;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends ReactiveMongoRepository<Order, String> {
}
