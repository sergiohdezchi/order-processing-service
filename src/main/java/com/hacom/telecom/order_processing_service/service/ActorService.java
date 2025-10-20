package com.hacom.telecom.order_processing_service.service;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.hacom.telecom.order_processing_service.actor.OrderMessages;
import com.hacom.telecom.order_processing_service.actor.OrderProcessingActor;
import com.hacom.telecom.order_processing_service.grpc.CreateOrderResponse;
import com.hacom.telecom.order_processing_service.model.OrderItem;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;

@Service
public class ActorService {

    @Autowired
    private ActorSystem actorSystem;

    @Autowired
    private OrderService orderService;

    private ActorRef orderProcessingActor;

    @PostConstruct
    public void init() {
        // Crear el actor de procesamiento de pedidos
        orderProcessingActor = actorSystem.actorOf(
            Props.create(OrderProcessingActor.class, orderService),
            "orderProcessingActor"
        );
    }

    /**
     * Envía un pedido al actor para que lo procese
     */
    public void processOrder(String orderId, String customerId, String customerPhone, 
                           List<OrderItem> items, StreamObserver<CreateOrderResponse> responseObserver) {
        OrderMessages.ProcessOrder message = new OrderMessages.ProcessOrder(
            orderId, customerId, customerPhone, items, responseObserver
        );
        orderProcessingActor.tell(message, ActorRef.noSender());
    }

    @PreDestroy
    public void shutdown() {
        // Detener el sistema de actores al cerrar la aplicación
        actorSystem.terminate();
    }
}
