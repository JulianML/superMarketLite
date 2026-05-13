package com.example.demo.cart.service;

import com.example.demo.cart.dto.CartDTOs.*;
import com.example.demo.cart.entity.Cart;
import com.example.demo.cart.entity.CartItem;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CartService {

    private static final int MAX_QUANTITY = 99;

    private final GuestCartService guestCartService;
    private final UserCartService userCartService;
    private final UserRepository userRepository;

    public CartService(GuestCartService guestCartService, UserCartService userCartService,
                       UserRepository userRepository) {
        this.guestCartService = guestCartService;
        this.userCartService = userCartService;
        this.userRepository = userRepository;
    }

    public CartDTO getCart(Long businessId, HttpServletRequest req, HttpServletResponse resp) {
        return resolveUserId()
                .map(uid -> userCartService.getCart(uid, businessId))
                .orElseGet(() -> guestCartService.getCart(businessId, req));
    }

    public CartDTO addItem(Long businessId, Long productId, int quantity,
                           HttpServletRequest req, HttpServletResponse resp) {
        return resolveUserId()
                .map(uid -> userCartService.addItem(uid, businessId, productId, quantity))
                .orElseGet(() -> guestCartService.addItem(businessId, productId, quantity, req, resp));
    }

    public CartDTO updateItem(Long businessId, Long productId, int quantity,
                              HttpServletRequest req, HttpServletResponse resp) {
        return resolveUserId()
                .map(uid -> userCartService.updateItem(uid, businessId, productId, quantity))
                .orElseGet(() -> guestCartService.updateItem(businessId, productId, quantity, req, resp));
    }

    public CartDTO removeItem(Long businessId, Long productId,
                              HttpServletRequest req, HttpServletResponse resp) {
        return resolveUserId()
                .map(uid -> userCartService.removeItem(uid, businessId, productId))
                .orElseGet(() -> guestCartService.removeItem(businessId, productId, req, resp));
    }

    public void clearCart(Long businessId, HttpServletRequest req, HttpServletResponse resp) {
        resolveUserId()
                .ifPresentOrElse(
                        uid -> userCartService.clear(uid, businessId),
                        () -> guestCartService.clear(businessId, req, resp)
                );
    }

    /**
     * Called after successful login. Merges all business carts found in the guest cookie
     * into the user's DB carts, then clears the cookie.
     * Sum-with-cap strategy: guest quantity + DB quantity, capped at 99.
     */
    public void mergeGuestCart(Long userId, HttpServletRequest req, HttpServletResponse resp) {
        Map<String, List<CartCookieItem>> allGuestCarts = guestCartService.readAllFromCookie(req);
        if (allGuestCarts.isEmpty()) return;

        for (Map.Entry<String, List<CartCookieItem>> entry : allGuestCarts.entrySet()) {
            Long businessId = Long.parseLong(entry.getKey());
            List<CartCookieItem> guestItems = entry.getValue();
            if (guestItems.isEmpty()) continue;

            Cart dbCart = userCartService.getOrLoadCart(userId, businessId);
            List<CartCookieItem> merged = mergeItems(dbCart.getItems(), guestItems);
            userCartService.publish(userId, businessId, merged);
        }

        guestCartService.clearCookie(resp);
    }

    // ── private helpers ───────────────────────────────────────────────────────

    private Optional<Long> resolveUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getPrincipal())) {
            return Optional.empty();
        }
        String email = auth.getName();
        return userRepository.findByEmail(email).map(User::getId);
    }

    /** Sum quantities; guest wins for new items; cap at MAX_QUANTITY. */
    private List<CartCookieItem> mergeItems(List<CartItem> dbItems, List<CartCookieItem> guestItems) {
        List<CartCookieItem> result = new ArrayList<>();

        for (CartItem dbItem : dbItems) {
            int guestQty = guestItems.stream()
                    .filter(g -> g.productId().equals(dbItem.getProductId()))
                    .mapToInt(CartCookieItem::quantity)
                    .findFirst()
                    .orElse(0);
            result.add(new CartCookieItem(dbItem.getProductId(),
                    Math.min(dbItem.getQuantity() + guestQty, MAX_QUANTITY)));
        }

        for (CartCookieItem guestItem : guestItems) {
            boolean alreadyMerged = result.stream()
                    .anyMatch(r -> r.productId().equals(guestItem.productId()));
            if (!alreadyMerged) {
                result.add(new CartCookieItem(guestItem.productId(),
                        Math.min(guestItem.quantity(), MAX_QUANTITY)));
            }
        }

        return result;
    }
}
