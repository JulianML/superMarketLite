package com.example.demo.common.kafka;

import com.example.demo.product.dto.ProductEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaCostumerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaCostumerService.class);

    @KafkaListener(topics = "product-events", groupId ="products-group")
    public void consumeProductEvent(ProductEvent event) {
        logger.info("Received product event: {} - {} - {}",
                event.getProductId(),
                event.getProductName(),
                event.getPrice());
    }
}
