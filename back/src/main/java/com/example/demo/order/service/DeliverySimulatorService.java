package com.example.demo.order.service;

import com.example.demo.order.entity.*;
import com.example.demo.order.messaging.OrderPlacedEvent;
import com.example.demo.order.repo.DeliveryRepository;
import com.example.demo.order.repo.OrderRepository;
import com.example.demo.order.repo.OrderStatusHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.demo.config.RabbitMQConfig.ORDER_PLACED_QUEUE;

@Service
public class DeliverySimulatorService {

    private static final Logger log = LoggerFactory.getLogger(DeliverySimulatorService.class);

    // Simulated time-in-state thresholds (minutes)
    private static final int PREPARING_MINUTES = 2;
    private static final int SHIPPED_MINUTES   = 4;

    private final DeliveryRepository deliveryRepository;
    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository historyRepository;

    public DeliverySimulatorService(DeliveryRepository deliveryRepository,
                                    OrderRepository orderRepository,
                                    OrderStatusHistoryRepository historyRepository) {
        this.deliveryRepository = deliveryRepository;
        this.orderRepository = orderRepository;
        this.historyRepository = historyRepository;
    }

    @RabbitListener(queues = ORDER_PLACED_QUEUE)
    @Transactional
    public void onOrderPlaced(OrderPlacedEvent event) {
        log.info("Order placed event received: orderId={}", event.orderId());

        Delivery delivery = new Delivery();
        delivery.setOrderId(event.orderId());
        delivery.setBusinessId(event.businessId());
        delivery.setStatus(DeliveryStatus.PREPARING);
        delivery.setEtaFrom(LocalDateTime.now().plusMinutes(PREPARING_MINUTES));
        delivery.setEtaTo(LocalDateTime.now().plusMinutes(PREPARING_MINUTES + SHIPPED_MINUTES));
        deliveryRepository.save(delivery);

        orderRepository.findById(event.orderId()).ifPresent(order -> {
            order.setStatus(OrderStatus.PREPARING);
            orderRepository.save(order);
        });

        recordHistory(event.orderId(), OrderStatus.CONFIRMED.name(), OrderStatus.PREPARING.name(), "Preparing order");
    }

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void advanceDeliveries() {
        List<Delivery> active = deliveryRepository.findByStatusNotIn(
                List.of(DeliveryStatus.DELIVERED, DeliveryStatus.PICKED_UP));
        LocalDateTime now = LocalDateTime.now();

        for (Delivery delivery : active) {
            if (delivery.getEtaFrom() == null || now.isBefore(delivery.getEtaFrom())) continue;

            orderRepository.findById(delivery.getOrderId()).ifPresent(order -> {
                if (delivery.getStatus() == DeliveryStatus.PREPARING
                        && order.getStatus() == OrderStatus.PREPARING) {
                    advance(order, delivery, OrderStatus.SHIPPED, DeliveryStatus.SHIPPED,
                            "In transit", now.plusMinutes(SHIPPED_MINUTES));

                } else if (delivery.getStatus() == DeliveryStatus.SHIPPED
                        && order.getStatus() == OrderStatus.SHIPPED
                        && now.isAfter(delivery.getEtaTo())) {
                    delivery.setDeliveredAt(now);
                    advance(order, delivery, OrderStatus.DELIVERED, DeliveryStatus.DELIVERED,
                            "Delivered", null);
                }
            });
        }
    }

    private void advance(Order order, Delivery delivery,
                         OrderStatus nextOrder, DeliveryStatus nextDelivery,
                         String note, LocalDateTime newEtaTo) {
        String fromStatus = order.getStatus().name();
        order.setStatus(nextOrder);
        orderRepository.save(order);

        delivery.setStatus(nextDelivery);
        if (newEtaTo != null) delivery.setEtaTo(newEtaTo);
        deliveryRepository.save(delivery);

        recordHistory(order.getId(), fromStatus, nextOrder.name(), note);
        log.info("Order {} advanced: {} -> {}", order.getOrderNumber(), fromStatus, nextOrder);
    }

    private void recordHistory(Long orderId, String from, String to, String note) {
        OrderStatusHistory h = new OrderStatusHistory();
        h.setOrderId(orderId);
        h.setFromStatus(from);
        h.setToStatus(to);
        h.setNote(note);
        historyRepository.save(h);
    }
}
