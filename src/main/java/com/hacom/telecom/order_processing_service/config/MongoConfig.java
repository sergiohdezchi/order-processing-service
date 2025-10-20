package com.hacom.telecom.order_processing_service.config;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Configuration
@EnableReactiveMongoRepositories(basePackages = "com.hacom.telecom.order_processing_service.repository")
public class MongoConfig extends AbstractReactiveMongoConfiguration {

    @Value("${mongodbDatabase}")
    private String database;

    @Value("${mongodbUri}")
    private String uri;

    @Override
    protected String getDatabaseName() {
        return database;
    }

    @Override
    @Bean
    public MongoClient reactiveMongoClient() {
        return MongoClients.create(uri);
    }

    @Bean
    public ReactiveMongoTemplate reactiveMongoTemplate() {
        return new ReactiveMongoTemplate(reactiveMongoClient(), getDatabaseName());
    }

    @Override
    public MongoCustomConversions customConversions() {
        List<Converter<?, ?>> converters = new ArrayList<>();
        converters.add(new OffsetDateTimeReadConverter());
        converters.add(new OffsetDateTimeWriteConverter());
        return new MongoCustomConversions(converters);
    }

    /**
     * Converter para leer OffsetDateTime desde MongoDB
     */
    static class OffsetDateTimeReadConverter implements Converter<Date, OffsetDateTime> {
        @Override
        public OffsetDateTime convert(Date date) {
            return date.toInstant().atOffset(ZoneOffset.UTC);
        }
    }

    /**
     * Converter para escribir OffsetDateTime en MongoDB
     */
    static class OffsetDateTimeWriteConverter implements Converter<OffsetDateTime, Date> {
        @Override
        public Date convert(OffsetDateTime offsetDateTime) {
            return Date.from(offsetDateTime.toInstant());
        }
    }
}
