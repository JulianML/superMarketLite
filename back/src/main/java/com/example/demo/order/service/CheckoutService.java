package com.example.demo.order.service;

import com.example.demo.cart.entity.Cart;
import com.example.demo.cart.entity.CartItem;
import com.example.demo.cart.repo.CartRepository;
import com.example.demo.common.exception.BusinessRuleException;
import com.example.demo.common.exception.NotFoundException;
import com.example.demo.inventory.repo.InventoryRepository;
import com.example.demo.order.entity.*;
import com.example.demo.order.messaging.OrderPlacedEvent;
import com.example.demo.order.repo.*;
import com.example.demo.order.util.OrderNumberGenerator;
import com.example.demo.product.entity.Product;
import com.example.demo.product.repo.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.example.demo.config.RabbitMQConfig.*;

@Service
public class CheckoutService {

    private static final Logger log = LoggerFactory.getLogger(CheckoutService.class);

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final OrderStatusHistoryRepository historyRepository;
    private final RabbitTemplate rabbitTemplate;

    public CheckoutService(CartRepository cartRepository,
                           ProductRepository productRepository,
                           InventoryRepository inventoryRepository,
                           OrderRepository orderRepository,
                           PaymentRepository paymentRepository,
                           OrderStatusHistoryRepository historyRepository,
                           RabbitTemplate rabbitTemplate) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.historyRepository = historyRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Transactional
    public Order placeOrder(Long userId, Long businessId, AddressDTO address) {
        Cart cart = cartRepository.findByUserIdAndBusinessIdAndActiveTrue(userId, businessId)
                .orElseThrow(() -> new BusinessRuleException("No active cart found"));

        if (cart.getItems().isEmpty()) {
            throw new BusinessRuleException("Cannot place an order with an empty cart");
        }

        Set<Long> productIds = cart.getItems().stream()
                .map(CartItem::getProductId)
                .collect(Collectors.toSet());

        Map<Long, Product> products = productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal taxTotal = BigDecimal.ZERO;

        Order order = new Order();
        order.setOrderNumber(OrderNumberGenerator.generate());
        order.setUserId(userId);
        order.setBusinessId(businessId);
        order.setCartId(cart.getId());
        order.setStatus(OrderStatus.CONFIRMED);
        order.setDeliveryAddress(address);
        order.setShippingFee(BigDecimal.ZERO);
        order.setDiscountTotal(BigDecimal.ZERO);
        order.setCurrency("EUR");

        for (CartItem ci : cart.getItems()) {
            Product p = products.get(ci.getProductId());
            if (p == null) continue;

            // Decrement stock with pessimistic lock
            var inv = inventoryRepository
                    .findWithLockingByBusinessIdAndProductId(businessId, p.getId())
                    .orElseThrow(() -> new NotFoundException("Inventory not found for product " + p.getName()));

            if (inv.getStock() < ci.getQuantity()) {
                throw new BusinessRuleException("Not enough stock for product: " + p.getName());
            }
            inv.setStock(inv.getStock() - ci.getQuantity());
            inventoryRepository.save(inv);

            BigDecimal lineTotal = ci.getUnitPrice().multiply(BigDecimal.valueOf(ci.getQuantity()));
            BigDecimal vatRate = p.getVatRate() != null ? p.getVatRate() : BigDecimal.ZERO;
            BigDecimal lineTax = lineTotal.multiply(vatRate)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            subtotal = subtotal.add(lineTotal);
            taxTotal = taxTotal.add(lineTax);

            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setProductId(p.getId());
            oi.setProductName(p.getName());
            oi.setSku(p.getSku());
            oi.setQuantity(ci.getQuantity());
            oi.setUnitPrice(ci.getUnitPrice());
            oi.setVatRate(vatRate);
            oi.setLineTotal(lineTotal);
            order.getItems().add(oi);
        }

        order.setSubtotal(subtotal);
        order.setTaxTotal(taxTotal);
        order.setTotal(subtotal.add(taxTotal));

        Order saved = orderRepository.save(order);

        // Simulated payment stub
        Payment payment = new Payment();
        payment.setOrderId(saved.getId());
        payment.setProvider("SIMULATED");
        payment.setStatus("SIMULATED");
        payment.setAmount(saved.getTotal());
        payment.setCurrency("EUR");
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        // Initial status history row
        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrderId(saved.getId());
        history.setFromStatus(null);
        history.setToStatus(OrderStatus.CONFIRMED.name());
        history.setNote("Order placed");
        historyRepository.save(history);

        // Deactivate cart
        cart.setActive(false);
        cart.setStatus("CHECKED_OUT");
        cartRepository.save(cart);

        publishAfterCommit(saved.getId(), businessId, userId);
        return saved;
    }

    private void publishAfterCommit(Long orderId, Long businessId, Long userId) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    rabbitTemplate.convertAndSend(ORDER_EXCHANGE, ORDER_PLACED_KEY,
                            new OrderPlacedEvent(orderId, businessId, userId));
                } catch (Exception e) {
                    log.warn("OrderPlacedEvent publish failed orderId={} — delivery simulation will not start", orderId, e);
                }
            }
        });
    }
}
