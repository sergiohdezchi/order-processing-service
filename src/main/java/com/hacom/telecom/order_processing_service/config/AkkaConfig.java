package com.hacom.telecom.order_processing_service.config;

import akka.actor.ActorSystem;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AkkaConfig {

    @Bean
    public ActorSystem actorSystem() {
        // Configuración de Akka
        Config config = ConfigFactory.parseString(
            "akka {\n" +
            "  loglevel = \"INFO\"\n" +
            "  actor {\n" +
            "    default-dispatcher {\n" +
            "      type = Dispatcher\n" +
            "      executor = \"fork-join-executor\"\n" +
            "      fork-join-executor {\n" +
            "        parallelism-min = 2\n" +
            "        parallelism-factor = 2.0\n" +
            "        parallelism-max = 10\n" +
            "      }\n" +
            "      throughput = 100\n" +
            "    }\n" +
            "  }\n" +
            "}"
        );
        
        return ActorSystem.create("OrderProcessingSystem", config);
    }
}
