package com.example.demo.order.service;

import com.example.demo.common.exception.NotFoundException;
import com.example.demo.order.entity.Order;
import com.example.demo.order.entity.OrderStatusHistory;
import com.example.demo.order.repo.DeliveryRepository;
import com.example.demo.order.repo.OrderRepository;
import com.example.demo.order.repo.OrderStatusHistoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository historyRepository;
    private final DeliveryRepository deliveryRepository;

    public OrderService(OrderRepository orderRepository,
                        OrderStatusHistoryRepository historyRepository,
                        DeliveryRepository deliveryRepository) {
        this.orderRepository = orderRepository;
        this.historyRepository = historyRepository;
        this.deliveryRepository = deliveryRepository;
    }

    @Transactional(readOnly = true)
    public List<Order> getOrdersByUser(Long userId) {
        return orderRepository.findByUserIdOrderByPlacedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public Order getOrderById(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));
        if (!order.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        return order;
    }

    @Transactional(readOnly = true)
    public List<OrderStatusHistory> getHistory(Long orderId) {
        return historyRepository.findByOrderIdOrderByChangedAtAsc(orderId);
    }
}
