package com.example.demo.cart.messaging;

import com.example.demo.cart.dto.CartDTOs.CartSyncMessage;
import com.example.demo.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class CartRabbitProducer {

    private static final Logger log = LoggerFactory.getLogger(CartRabbitProducer.class);

    private final RabbitTemplate rabbitTemplate;

    public CartRabbitProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(CartSyncMessage message) {
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.CART_EXCHANGE, RabbitMQConfig.CART_SYNC_KEY, message);
            log.debug("Cart sync published: userId={} businessId={}", message.userId(), message.businessId());
        } catch (Exception e) {
            log.error("Failed to publish cart sync message: userId={} businessId={}", message.userId(), message.businessId(), e);
        }
    }
}
