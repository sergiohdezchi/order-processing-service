package com.hacom.telecom.order_processing_service.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.OffsetDateTime;
import java.util.List;

@Document(collection = "orders")
public class Order {
    
    @Id
    private String id;
    private String orderId;
    private String customerId;
    private String customerPhoneNumber;
    private List<OrderItem> items;
    private String status;
    private OffsetDateTime ts;

    public Order() {
    }

    public Order(String orderId, String customerId, String customerPhoneNumber, List<OrderItem> items, String status) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.customerPhoneNumber = customerPhoneNumber;
        this.items = items;
        this.status = status;
        this.ts = OffsetDateTime.now();
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerPhoneNumber() {
        return customerPhoneNumber;
    }

    public void setCustomerPhoneNumber(String customerPhoneNumber) {
        this.customerPhoneNumber = customerPhoneNumber;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        this.ts = OffsetDateTime.now();
    }

    public OffsetDateTime getTs() {
        return ts;
    }

    public void setTs(OffsetDateTime ts) {
        this.ts = ts;
    }
}
