package com.example.demo.common.kafka;

import com.example.demo.product.dto.ProductEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);
    private static final String TOPIC_NAME = "product-events";

    private final KafkaTemplate<String, ProductEvent> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, ProductEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendProductEvent(ProductEvent productEvent) {
        try{
            kafkaTemplate.send(TOPIC_NAME, String.valueOf(productEvent.getProductId()), productEvent);
            logger.info("Product event sent to Kafka: {}", productEvent);
        }catch (Exception e){
            logger.error("Error sending product event to Kafka: {}", e.getMessage());
        }
    }

}
