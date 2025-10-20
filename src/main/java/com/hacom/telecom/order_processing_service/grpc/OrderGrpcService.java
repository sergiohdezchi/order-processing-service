package com.hacom.telecom.order_processing_service.grpc;

import com.hacom.telecom.order_processing_service.model.OrderItem;
import com.hacom.telecom.order_processing_service.service.ActorService;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

@GrpcService
public class OrderGrpcService extends OrderServiceGrpc.OrderServiceImplBase {

    @Autowired
    private ActorService actorService;

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

            // Delegar el procesamiento al Actor (procesamiento asíncrono)
            // El Actor se encargará de guardar el pedido y enviar la respuesta gRPC
            actorService.processOrder(
                    request.getOrderId(),
                    request.getCustomerId(),
                    request.getCustomerPhone(),
                    items,
                    responseObserver
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
