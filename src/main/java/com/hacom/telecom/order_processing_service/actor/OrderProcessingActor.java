package com.hacom.telecom.order_processing_service.actor;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.hacom.telecom.order_processing_service.grpc.CreateOrderResponse;
import com.hacom.telecom.order_processing_service.service.OrderService;

/**
 * Actor que procesa pedidos de forma asíncrona
 */
public class OrderProcessingActor extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final OrderService orderService;

    public OrderProcessingActor(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(OrderMessages.ProcessOrder.class, this::processOrder)
                .match(OrderMessages.OrderSaved.class, this::handleOrderSaved)
                .match(OrderMessages.OrderError.class, this::handleOrderError)
                .matchAny(this::unhandled)
                .build();
    }

    /**
     * Procesa un pedido nuevo
     */
    private void processOrder(OrderMessages.ProcessOrder message) {
        log.info("Processing order: {}", message.getOrderId());
        
        try {
            log.info("Saving order to database...");
            
            // Guardar el pedido en MongoDB de forma reactiva
            orderService.createOrder(
                message.getOrderId(),
                message.getCustomerId(),
                message.getCustomerPhone(),
                message.getItems()
            ).subscribe(
                order -> {
                    // Éxito: enviar mensaje de orden guardada
                    log.info("Order saved successfully: {}", order.getOrderId());
                    getSelf().tell(
                        new OrderMessages.OrderSaved(
                            order.getOrderId(), 
                            order.getStatus(),
                            message.getResponseObserver()
                        ), 
                        getSelf()
                    );
                },
                error -> {
                    // Error: enviar mensaje de error
                    log.error("Error saving order: {}", error.getMessage());
                    getSelf().tell(
                        new OrderMessages.OrderError(
                            message.getOrderId(),
                            error.getMessage(),
                            message.getResponseObserver()
                        ),
                        getSelf()
                    );
                }
            );
            
        } catch (Exception e) {
            log.error("Exception processing order: {}", e.getMessage());
            getSelf().tell(
                new OrderMessages.OrderError(
                    message.getOrderId(),
                    e.getMessage(),
                    message.getResponseObserver()
                ),
                getSelf()
            );
        }
    }

    /**
     * Maneja el evento de pedido guardado exitosamente
     */
    private void handleOrderSaved(OrderMessages.OrderSaved message) {
        log.info("Sending success response for order: {}", message.getOrderId());
        
        // Actualizar estado del pedido
        orderService.updateOrderStatus(message.getOrderId(), "PROCESSING")
            .subscribe(
                order -> log.info("Order status updated to PROCESSING: {}", order.getOrderId()),
                error -> log.error("Error updating order status: {}", error.getMessage())
            );
        
        // Enviar respuesta gRPC
        CreateOrderResponse response = CreateOrderResponse.newBuilder()
                .setOrderId(message.getOrderId())
                .setStatus("PROCESSING")
                .setMessage("Order received and is being processed")
                .build();
        
        message.getResponseObserver().onNext(response);
        message.getResponseObserver().onCompleted();
        
        log.info("Response sent successfully for order: {}", message.getOrderId());
    }

    /**
     * Maneja el evento de error al procesar pedido
     */
    private void handleOrderError(OrderMessages.OrderError message) {
        log.error("Sending error response for order: {}", message.getOrderId());
        
        // Enviar respuesta gRPC con error
        CreateOrderResponse response = CreateOrderResponse.newBuilder()
                .setOrderId(message.getOrderId())
                .setStatus("ERROR")
                .setMessage("Failed to process order: " + message.getErrorMessage())
                .build();
        
        message.getResponseObserver().onNext(response);
        message.getResponseObserver().onCompleted();
        
        log.info("Error response sent for order: {}", message.getOrderId());
    }

    @Override
    public void preStart() {
        log.info("OrderProcessingActor started");
    }

    @Override
    public void postStop() {
        log.info("OrderProcessingActor stopped");
    }
}
