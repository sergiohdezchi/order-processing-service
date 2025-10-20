package com.hacom.telecom.order_processing_service.controller;

import com.hacom.telecom.order_processing_service.model.Order;
import com.hacom.telecom.order_processing_service.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;

    @GetMapping("/{orderId}/status")
    public Mono<ResponseEntity<Map<String, Object>>> getOrderStatus(@PathVariable String orderId) {
        log.info("GET /api/v1/orders/{}/status - Querying order status", orderId);
        
        return orderService.findOrderByOrderId(orderId)
                .map(order -> {
                    log.info("Order found: {} with status: {}", orderId, order.getStatus());
                    
                    Map<String, Object> response = Map.of(
                        "orderId", order.getOrderId(),
                        "status", order.getStatus(),
                        "customerId", order.getCustomerId(),
                        "customerPhoneNumber", order.getCustomerPhoneNumber(),
                        "timestamp", order.getTs().toString()
                    );
                    
                    return ResponseEntity.ok(response);
                })
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                            "error", "Order not found",
                            "orderId", orderId
                        )));
    }

    @GetMapping("/count")
    public Mono<ResponseEntity<Map<String, Object>>> getOrderCountByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate) {
        
        log.info("GET /api/v1/orders/count - Querying orders between {} and {}", startDate, endDate);
        
        if (startDate.isAfter(endDate)) {
            log.warn("Invalid date range: startDate {} is after endDate {}", startDate, endDate);
            return Mono.just(ResponseEntity.badRequest()
                    .body(Map.of(
                        "error", "Invalid date range",
                        "message", "startDate must be before endDate"
                    )));
        }
        
        return orderService.countOrdersByDateRange(startDate, endDate)
                .map(count -> {
                    log.info("Found {} orders between {} and {}", count, startDate, endDate);
                    
                    Map<String, Object> response = Map.of(
                        "totalOrders", count,
                        "startDate", startDate.toString(),
                        "endDate", endDate.toString()
                    );
                    
                    return ResponseEntity.ok(response);
                })
                .defaultIfEmpty(ResponseEntity.ok(
                        Map.of(
                            "totalOrders", 0L,
                            "startDate", startDate.toString(),
                            "endDate", endDate.toString()
                        )));
    }

    @GetMapping("/{orderId}")
    public Mono<ResponseEntity<Order>> getOrderDetails(@PathVariable String orderId) {
        log.info("GET /api/v1/orders/{} - Querying order details", orderId);
        
        return orderService.findOrderByOrderId(orderId)
                .map(order -> {
                    log.info("Order found: {}", orderId);
                    return ResponseEntity.ok(order);
                })
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
