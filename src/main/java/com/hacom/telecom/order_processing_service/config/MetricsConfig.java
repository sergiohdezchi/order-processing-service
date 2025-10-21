package com.hacom.telecom.order_processing_service.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for defining application metrics using Micrometer.
 */
@Configuration
public class MetricsConfig {

    @Bean
    public Counter ordersCreatedCounter(MeterRegistry registry) {
        return Counter.builder("orders.created.total")
                .description("Total number of orders created successfully")
                .tag("service", "order-processing")
                .register(registry);
    }

    @Bean
    public Counter ordersDuplicateCounter(MeterRegistry registry) {
        return Counter.builder("orders.duplicate.total")
                .description("Total number of duplicate orders detected")
                .tag("service", "order-processing")
                .register(registry);
    }

    @Bean
    public Counter smsSentCounter(MeterRegistry registry) {
        return Counter.builder("sms.sent.total")
                .description("Total number of SMS notifications sent successfully")
                .tag("service", "order-processing")
                .register(registry);
    }

    @Bean
    public Counter smsFailedCounter(MeterRegistry registry) {
        return Counter.builder("sms.failed.total")
                .description("Total number of SMS notifications that failed to send")
                .tag("service", "order-processing")
                .register(registry);
    }
}
