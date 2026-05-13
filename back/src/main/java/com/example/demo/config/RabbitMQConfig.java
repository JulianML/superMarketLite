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

    public static final String ORDER_EXCHANGE   = "order.exchange";
    public static final String ORDER_PLACED_QUEUE = "order.placed.queue";
    public static final String ORDER_PLACED_KEY   = "order.placed";
    public static final String ORDER_DLX          = "order.dlx";
    public static final String ORDER_DLQ          = "order.placed.dlq";

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
    DirectExchange orderExchange() {
        return ExchangeBuilder.directExchange(ORDER_EXCHANGE).durable(true).build();
    }

    @Bean
    DirectExchange orderDlx() {
        return ExchangeBuilder.directExchange(ORDER_DLX).durable(true).build();
    }

    @Bean
    Queue orderPlacedQueue() {
        return QueueBuilder.durable(ORDER_PLACED_QUEUE)
                .withArgument("x-dead-letter-exchange", ORDER_DLX)
                .withArgument("x-dead-letter-routing-key", ORDER_DLQ)
                .build();
    }

    @Bean
    Queue orderDlq() {
        return QueueBuilder.durable(ORDER_DLQ).build();
    }

    @Bean
    Binding orderPlacedBinding(Queue orderPlacedQueue, DirectExchange orderExchange) {
        return BindingBuilder.bind(orderPlacedQueue).to(orderExchange).with(ORDER_PLACED_KEY);
    }

    @Bean
    Binding orderDlqBinding(Queue orderDlq, DirectExchange orderDlx) {
        return BindingBuilder.bind(orderDlq).to(orderDlx).with(ORDER_DLQ);
    }

    @Bean
    MessageConverter jacksonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
