package com.example.demo.cart.messaging;

import com.example.demo.cart.dto.CartDTOs.CartCookieItem;
import com.example.demo.cart.dto.CartDTOs.CartSyncMessage;
import com.example.demo.cart.entity.Cart;
import com.example.demo.cart.entity.CartItem;
import com.example.demo.cart.repo.CartRepository;
import com.example.demo.config.RabbitMQConfig;
import com.example.demo.product.entity.Product;
import com.example.demo.product.repo.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CartRabbitConsumer {

    private static final Logger log = LoggerFactory.getLogger(CartRabbitConsumer.class);

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    public CartRabbitConsumer(CartRepository cartRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
    }

    @RabbitListener(queues = RabbitMQConfig.CART_SYNC_QUEUE)
    @Transactional
    public void handleCartSync(CartSyncMessage message) {
        log.debug("Processing cart sync: userId={} businessId={}", message.userId(), message.businessId());

        Cart cart = cartRepository.findByUserIdAndBusinessIdAndActiveTrue(message.userId(), message.businessId())
                .orElseGet(() -> newCart(message.userId(), message.businessId()));

        // Skip stale messages — another sync already applied a newer state
        if (cart.getId() != null && cart.getUpdatedAt() != null
                && message.syncedAt().isBefore(cart.getUpdatedAt())) {
            log.debug("Skipping stale cart sync message for userId={}", message.userId());
            return;
        }

        // Fetch current prices — never trust prices from the message
        Set<Long> productIds = message.items().stream()
                .map(CartCookieItem::productId)
                .collect(Collectors.toSet());
        Map<Long, Product> products = productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        // Full replacement: clear existing items, re-insert from message
        cart.getItems().clear();

        for (CartCookieItem cookieItem : message.items()) {
            Product product = products.get(cookieItem.productId());
            if (product == null || !product.isActive()) continue;

            CartItem item = new CartItem();
            item.setCart(cart);
            item.setProductId(cookieItem.productId());
            item.setQuantity(cookieItem.quantity());
            item.setUnitPrice(product.getPrice());
            cart.getItems().add(item);
        }

        cartRepository.save(cart);
        log.debug("Cart sync applied: userId={} businessId={} items={}", message.userId(), message.businessId(), cart.getItems().size());
    }

    private Cart newCart(Long userId, Long businessId) {
        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setBusinessId(businessId);
        cart.setStatus("ACTIVE");
        cart.setActive(true);
        return cart;
    }
}
