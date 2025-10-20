package com.hacom.telecom.order_processing_service.grpc;

import com.hacom.telecom.order_processing_service.model.OrderItem;
import com.hacom.telecom.order_processing_service.service.OrderService;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

@GrpcService
public class OrderGrpcService extends OrderServiceGrpc.OrderServiceImplBase {

    @Autowired
    private OrderService orderService;

    @Override
    public void createOrder(CreateOrderRequest request, StreamObserver<CreateOrderResponse> responseObserver) {
        try {
            // Convertir items de gRPC a modelo de dominio
            List<OrderItem> items = request.getItemsList().stream()
                    .map(grpcItem -> new OrderItem(
                            grpcItem.getItemId(),
                            grpcItem.getProductName(),
                            grpcItem.getQuantity(),
                            grpcItem.getPrice()
                    ))
                    .collect(Collectors.toList());

            // Crear el pedido
            orderService.createOrder(
                    request.getOrderId(),
                    request.getCustomerId(),
                    request.getCustomerPhone(),
                    items
            ).subscribe(
                    order -> {
                        // Enviar respuesta exitosa
                        CreateOrderResponse response = CreateOrderResponse.newBuilder()
                                .setOrderId(order.getOrderId())
                                .setStatus(order.getStatus())
                                .setMessage("Order created successfully")
                                .build();
                        
                        responseObserver.onNext(response);
                        responseObserver.onCompleted();
                    },
                    error -> {
                        // Enviar respuesta de error
                        CreateOrderResponse response = CreateOrderResponse.newBuilder()
                                .setOrderId(request.getOrderId())
                                .setStatus("ERROR")
                                .setMessage("Failed to create order: " + error.getMessage())
                                .build();
                        
                        responseObserver.onNext(response);
                        responseObserver.onCompleted();
                    }
            );
        } catch (Exception e) {
            // Manejo de excepciones
            CreateOrderResponse response = CreateOrderResponse.newBuilder()
                    .setOrderId(request.getOrderId())
                    .setStatus("ERROR")
                    .setMessage("Exception: " + e.getMessage())
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
}
