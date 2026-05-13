package com.example.demo.cart.service;

import com.example.demo.cart.dto.CartDTOs.*;
import com.example.demo.cart.entity.Cart;
import com.example.demo.cart.entity.CartItem;
import com.example.demo.cart.messaging.CartRabbitProducer;
import com.example.demo.cart.repo.CartRepository;
import com.example.demo.common.exception.NotFoundException;
import com.example.demo.product.entity.Product;
import com.example.demo.product.repo.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserCartService {

    private static final int MAX_QUANTITY = 99;
    private static final Logger log = LoggerFactory.getLogger(UserCartService.class);

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final CartRabbitProducer producer;

    public UserCartService(CartRepository cartRepository, ProductRepository productRepository,
                           CartRabbitProducer producer) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.producer = producer;
    }

    @Transactional(readOnly = true)
    public CartDTO getCart(Long userId, Long businessId) {
        return cartRepository.findByUserIdAndBusinessIdAndActiveTrue(userId, businessId)
                .map(this::toCartDTO)
                .orElse(new CartDTO(businessId, List.of(), BigDecimal.ZERO));
    }

    @Transactional
    public CartDTO addItem(Long userId, Long businessId, Long productId, int quantity) {
        Product product = productRepository.findByBusinessIdAndId(businessId, productId)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        Cart cart = getOrLoadCart(userId, businessId);

        boolean found = false;
        for (CartItem existing : cart.getItems()) {
            if (existing.getProductId().equals(productId)) {
                existing.setQuantity(Math.min(existing.getQuantity() + quantity, MAX_QUANTITY));
                found = true;
                break;
            }
        }
        if (!found) {
            CartItem item = new CartItem();
            item.setCart(cart);
            item.setProductId(productId);
            item.setQuantity(Math.min(quantity, MAX_QUANTITY));
            item.setUnitPrice(product.getPrice());
            cart.getItems().add(item);
        }

        cartRepository.save(cart);
        List<CartCookieItem> cookieItems = currentItems(cart);
        publishAfterCommit(userId, businessId, cookieItems);
        return buildCartDTO(businessId, cookieItems);
    }

    @Transactional
    public CartDTO updateItem(Long userId, Long businessId, Long productId, int quantity) {
        Product product = productRepository.findByBusinessIdAndId(businessId, productId)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        Cart cart = getOrLoadCart(userId, businessId);

        boolean found = false;
        for (CartItem existing : cart.getItems()) {
            if (existing.getProductId().equals(productId)) {
                existing.setQuantity(Math.min(quantity, MAX_QUANTITY));
                found = true;
                break;
            }
        }
        if (!found) {
            CartItem item = new CartItem();
            item.setCart(cart);
            item.setProductId(productId);
            item.setQuantity(Math.min(quantity, MAX_QUANTITY));
            item.setUnitPrice(product.getPrice());
            cart.getItems().add(item);
        }

        cartRepository.save(cart);
        List<CartCookieItem> cookieItems = currentItems(cart);
        publishAfterCommit(userId, businessId, cookieItems);
        return buildCartDTO(businessId, cookieItems);
    }

    @Transactional
    public CartDTO removeItem(Long userId, Long businessId, Long productId) {
        Cart cart = getOrLoadCart(userId, businessId);
        cart.getItems().removeIf(i -> i.getProductId().equals(productId));
        cartRepository.save(cart);
        List<CartCookieItem> cookieItems = currentItems(cart);
        publishAfterCommit(userId, businessId, cookieItems);
        return buildCartDTO(businessId, cookieItems);
    }

    @Transactional
    public void clear(Long userId, Long businessId) {
        cartRepository.findByUserIdAndBusinessIdAndActiveTrue(userId, businessId)
                .ifPresent(cart -> {
                    cart.getItems().clear();
                    cartRepository.save(cart);
                });
        publishAfterCommit(userId, businessId, List.of());
    }

    // ── package-visible for CartService merge ─────────────────────────────────

    void publish(Long userId, Long businessId, List<CartCookieItem> items) {
        producer.publish(new CartSyncMessage(userId, businessId, items, LocalDateTime.now()));
    }

    // Schedules the RabbitMQ publish to run strictly after the DB transaction commits.
    // This prevents a broker failure from rolling back the already-saved cart state.
    private void publishAfterCommit(Long userId, Long businessId, List<CartCookieItem> items) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    publish(userId, businessId, items);
                } catch (Exception e) {
                    log.warn("Cart sync publish failed userId={} businessId={} — DB state is still saved", userId, businessId, e);
                }
            }
        });
    }

    Cart getOrLoadCart(Long userId, Long businessId) {
        return cartRepository.findByUserIdAndBusinessIdAndActiveTrue(userId, businessId)
                .orElseGet(() -> {
                    Cart empty = new Cart();
                    empty.setUserId(userId);
                    empty.setBusinessId(businessId);
                    empty.setStatus("ACTIVE");
                    empty.setActive(true);
                    return empty;
                });
    }

    // ── private helpers ───────────────────────────────────────────────────────

    private List<CartCookieItem> currentItems(Cart cart) {
        return cart.getItems().stream()
                .map(i -> new CartCookieItem(i.getProductId(), i.getQuantity()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private CartDTO toCartDTO(Cart cart) {
        if (cart.getItems().isEmpty()) {
            return new CartDTO(cart.getBusinessId(), List.of(), BigDecimal.ZERO);
        }
        Set<Long> productIds = cart.getItems().stream()
                .map(CartItem::getProductId)
                .collect(Collectors.toSet());
        Map<Long, Product> products = productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        List<CartItemDTO> items = cart.getItems().stream()
                .filter(ci -> products.containsKey(ci.getProductId()))
                .map(ci -> {
                    Product p = products.get(ci.getProductId());
                    BigDecimal unitPrice = ci.getUnitPrice();
                    BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(ci.getQuantity()));
                    return new CartItemDTO(p.getId(), p.getName(), p.getSku(),
                            ci.getQuantity(), unitPrice, lineTotal);
                })
                .toList();

        BigDecimal total = items.stream()
                .map(CartItemDTO::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CartDTO(cart.getBusinessId(), items, total);
    }

    private CartDTO buildCartDTO(Long businessId, List<CartCookieItem> cookieItems) {
        if (cookieItems.isEmpty()) {
            return new CartDTO(businessId, List.of(), BigDecimal.ZERO);
        }
        Set<Long> productIds = cookieItems.stream()
                .map(CartCookieItem::productId)
                .collect(Collectors.toSet());
        Map<Long, Product> products = productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        List<CartItemDTO> items = cookieItems.stream()
                .filter(ci -> products.containsKey(ci.productId()))
                .map(ci -> {
                    Product p = products.get(ci.productId());
                    BigDecimal lineTotal = p.getPrice().multiply(BigDecimal.valueOf(ci.quantity()));
                    return new CartItemDTO(p.getId(), p.getName(), p.getSku(),
                            ci.quantity(), p.getPrice(), lineTotal);
                })
                .toList();

        BigDecimal total = items.stream()
                .map(CartItemDTO::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CartDTO(businessId, items, total);
    }
}
