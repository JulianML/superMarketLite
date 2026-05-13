package com.example.demo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String CART_EXCHANGE    = "cart.exchange";
    public static final String CART_SYNC_QUEUE  = "cart.sync.queue";
    public static final String CART_SYNC_KEY    = "cart.sync";
    public static final String CART_DLX         = "cart.dlx";
    public static final String CART_DLQ         = "cart.sync.dlq";
    public static final String CART_DLQ_KEY     = "cart.sync.dlq";

    @Bean
    DirectExchange cartExchange() {
        return ExchangeBuilder.directExchange(CART_EXCHANGE).durable(true).build();
    }

    @Bean
    DirectExchange cartDlx() {
        return ExchangeBuilder.directExchange(CART_DLX).durable(true).build();
    }

    @Bean
    Queue cartSyncQueue() {
        return QueueBuilder.durable(CART_SYNC_QUEUE)
                .withArgument("x-dead-letter-exchange", CART_DLX)
                .withArgument("x-dead-letter-routing-key", CART_DLQ_KEY)
                .build();
    }

    @Bean
    Queue cartDlq() {
        return QueueBuilder.durable(CART_DLQ).build();
    }

    @Bean
    Binding cartSyncBinding(Queue cartSyncQueue, DirectExchange cartExchange) {
        return BindingBuilder.bind(cartSyncQueue).to(cartExchange).with(CART_SYNC_KEY);
    }

    @Bean
    Binding cartDlqBinding(Queue cartDlq, DirectExchange cartDlx) {
        return BindingBuilder.bind(cartDlq).to(cartDlx).with(CART_DLQ_KEY);
    }

    @Bean
    MessageConverter jacksonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
