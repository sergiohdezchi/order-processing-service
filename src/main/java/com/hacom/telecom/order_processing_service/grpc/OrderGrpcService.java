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
            List<OrderItem> items = request.getItemsList().stream()
                    .map(grpcItem -> new OrderItem(
                            grpcItem.getItemId(),
                            grpcItem.getProductName(),
                            grpcItem.getQuantity(),
                            grpcItem.getPrice()
                    ))
                    .collect(Collectors.toList());

            actorService.processOrder(
                    request.getOrderId(),
                    request.getCustomerId(),
                    request.getCustomerPhone(),
                    items,
                    responseObserver
            );
            
        } catch (Exception e) {
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
