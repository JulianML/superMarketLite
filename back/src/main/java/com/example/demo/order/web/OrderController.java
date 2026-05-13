package com.example.demo.order.web;

import com.example.demo.common.exception.NotFoundException;
import com.example.demo.order.dto.OrderDTOs.*;
import com.example.demo.order.entity.*;
import com.example.demo.order.service.CheckoutService;
import com.example.demo.order.service.OrderService;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final Long BUSINESS_ID = 1L;

    private final CheckoutService checkoutService;
    private final OrderService orderService;
    private final UserRepository userRepository;

    public OrderController(CheckoutService checkoutService,
                           OrderService orderService,
                           UserRepository userRepository) {
        this.checkoutService = checkoutService;
        this.orderService = orderService;
        this.userRepository = userRepository;
    }

    @PostMapping("/checkout")
    public ResponseEntity<OrderSummaryDTO> checkout(@Valid @RequestBody CheckoutRequestDTO request) {
        Long userId = resolveUserId();
        AddressDTO address = new AddressDTO(
                request.address().street(),
                request.address().city(),
                request.address().postalCode(),
                request.address().country()
        );
        Order order = checkoutService.placeOrder(userId, BUSINESS_ID, address);
        return ResponseEntity.status(HttpStatus.CREATED).body(toSummary(order));
    }

    @GetMapping
    public List<OrderSummaryDTO> listOrders() {
        return orderService.getOrdersByUser(resolveUserId()).stream()
                .map(this::toSummary)
                .toList();
    }

    @GetMapping("/{id}")
    public OrderDetailDTO getOrder(@PathVariable Long id) {
        Long userId = resolveUserId();
        Order order = orderService.getOrderById(id, userId);

        List<StatusHistoryDTO> history = orderService.getHistory(id).stream()
                .map(h -> new StatusHistoryDTO(h.getFromStatus(), h.getToStatus(), h.getChangedAt(), h.getNote()))
                .toList();

        List<OrderItemDTO> items = order.getItems().stream()
                .map(i -> new OrderItemDTO(
                        i.getProductId(), i.getProductName(), i.getSku(),
                        i.getQuantity(), i.getUnitPrice(), i.getVatRate(), i.getLineTotal()))
                .toList();

        return new OrderDetailDTO(
                order.getId(), order.getOrderNumber(), order.getStatus().name(),
                order.getDeliveryAddress(), items,
                order.getSubtotal(), order.getTaxTotal(), order.getShippingFee(), order.getTotal(),
                order.getCurrency(), order.getPlacedAt(), history
        );
    }

    private OrderSummaryDTO toSummary(Order order) {
        return new OrderSummaryDTO(
                order.getId(), order.getOrderNumber(), order.getStatus().name(),
                order.getSubtotal(), order.getTaxTotal(), order.getShippingFee(), order.getTotal(),
                order.getCurrency(), order.getPlacedAt()
        );
    }

    private Long resolveUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .map(User::getId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }
}
