package com.hacom.telecom.order_processing_service.actor;

import com.hacom.telecom.order_processing_service.model.OrderItem;
import io.grpc.stub.StreamObserver;
import com.hacom.telecom.order_processing_service.grpc.CreateOrderResponse;

import java.io.Serializable;
import java.util.List;

/**
 * Mensajes que puede recibir el OrderProcessingActor
 */
public class OrderMessages {

    /**
     * Mensaje para procesar un pedido
     */
    public static class ProcessOrder implements Serializable {
        private final String orderId;
        private final String customerId;
        private final String customerPhone;
        private final List<OrderItem> items;
        private final StreamObserver<CreateOrderResponse> responseObserver;

        public ProcessOrder(String orderId, String customerId, String customerPhone, 
                          List<OrderItem> items, StreamObserver<CreateOrderResponse> responseObserver) {
            this.orderId = orderId;
            this.customerId = customerId;
            this.customerPhone = customerPhone;
            this.items = items;
            this.responseObserver = responseObserver;
        }

        public String getOrderId() {
            return orderId;
        }

        public String getCustomerId() {
            return customerId;
        }

        public String getCustomerPhone() {
            return customerPhone;
        }

        public List<OrderItem> getItems() {
            return items;
        }

        public StreamObserver<CreateOrderResponse> getResponseObserver() {
            return responseObserver;
        }
    }

    /**
     * Mensaje interno: pedido guardado exitosamente
     */
    public static class OrderSaved implements Serializable {
        private final String orderId;
        private final String status;
        private final String customerPhone;
        private final StreamObserver<CreateOrderResponse> responseObserver;

        public OrderSaved(String orderId, String status, String customerPhone, StreamObserver<CreateOrderResponse> responseObserver) {
            this.orderId = orderId;
            this.status = status;
            this.customerPhone = customerPhone;
            this.responseObserver = responseObserver;
        }

        public String getOrderId() {
            return orderId;
        }

        public String getStatus() {
            return status;
        }

        public String getCustomerPhone() {
            return customerPhone;
        }

        public StreamObserver<CreateOrderResponse> getResponseObserver() {
            return responseObserver;
        }
    }

    /**
     * Mensaje interno: error al procesar pedido
     */
    public static class OrderError implements Serializable {
        private final String orderId;
        private final String errorMessage;
        private final StreamObserver<CreateOrderResponse> responseObserver;

        public OrderError(String orderId, String errorMessage, StreamObserver<CreateOrderResponse> responseObserver) {
            this.orderId = orderId;
            this.errorMessage = errorMessage;
            this.responseObserver = responseObserver;
        }

        public String getOrderId() {
            return orderId;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public StreamObserver<CreateOrderResponse> getResponseObserver() {
            return responseObserver;
        }
    }
}
