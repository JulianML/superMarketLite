package com.example.demo.cart.service;

import com.example.demo.cart.dto.CartDTOs.*;
import com.example.demo.product.entity.Product;
import com.example.demo.product.repo.ProductRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GuestCartService {

    static final String COOKIE_NAME = "guest_cart";
    private static final int MAX_QUANTITY = 99;
    private static final int COOKIE_MAX_AGE_SECONDS = 2_592_000; // 30 days

    private static final Logger log = LoggerFactory.getLogger(GuestCartService.class);

    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper;

    public GuestCartService(ProductRepository productRepository, ObjectMapper objectMapper) {
        this.productRepository = productRepository;
        this.objectMapper = objectMapper;
    }

    public CartDTO getCart(Long businessId, HttpServletRequest request) {
        return buildCartDTO(businessId, readBusinessItems(businessId, request));
    }

    public CartDTO addItem(Long businessId, Long productId, int quantity,
                           HttpServletRequest request, HttpServletResponse response) {
        Map<String, List<CartCookieItem>> all = readAllFromCookie(request);
        String key = String.valueOf(businessId);
        List<CartCookieItem> items = new ArrayList<>(all.getOrDefault(key, List.of()));

        boolean found = false;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).productId().equals(productId)) {
                items.set(i, new CartCookieItem(productId,
                        Math.min(items.get(i).quantity() + quantity, MAX_QUANTITY)));
                found = true;
                break;
            }
        }
        if (!found) {
            items.add(new CartCookieItem(productId, Math.min(quantity, MAX_QUANTITY)));
        }

        all.put(key, items);
        writeCookie(all, response);
        return buildCartDTO(businessId, items);
    }

    public CartDTO updateItem(Long businessId, Long productId, int quantity,
                              HttpServletRequest request, HttpServletResponse response) {
        Map<String, List<CartCookieItem>> all = readAllFromCookie(request);
        String key = String.valueOf(businessId);
        List<CartCookieItem> items = new ArrayList<>(all.getOrDefault(key, List.of()));

        boolean found = false;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).productId().equals(productId)) {
                items.set(i, new CartCookieItem(productId, Math.min(quantity, MAX_QUANTITY)));
                found = true;
                break;
            }
        }
        if (!found) {
            items.add(new CartCookieItem(productId, Math.min(quantity, MAX_QUANTITY)));
        }

        all.put(key, items);
        writeCookie(all, response);
        return buildCartDTO(businessId, items);
    }

    public CartDTO removeItem(Long businessId, Long productId,
                              HttpServletRequest request, HttpServletResponse response) {
        Map<String, List<CartCookieItem>> all = readAllFromCookie(request);
        String key = String.valueOf(businessId);
        List<CartCookieItem> items = new ArrayList<>(all.getOrDefault(key, List.of()));
        items.removeIf(i -> i.productId().equals(productId));
        all.put(key, items);
        writeCookie(all, response);
        return buildCartDTO(businessId, items);
    }

    public void clear(Long businessId, HttpServletRequest request, HttpServletResponse response) {
        Map<String, List<CartCookieItem>> all = readAllFromCookie(request);
        all.remove(String.valueOf(businessId));
        writeCookie(all, response);
    }

    /** Returns all business carts stored in the cookie. */
    public Map<String, List<CartCookieItem>> readAllFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return new HashMap<>();
        return Arrays.stream(request.getCookies())
                .filter(c -> COOKIE_NAME.equals(c.getName()))
                .findFirst()
                .map(c -> {
                    try {
                        String decoded = URLDecoder.decode(c.getValue(), StandardCharsets.UTF_8);
                        return objectMapper.readValue(decoded,
                                new TypeReference<Map<String, List<CartCookieItem>>>() {});
                    } catch (Exception e) {
                        log.warn("Corrupt guest_cart cookie, resetting");
                        return new HashMap<String, List<CartCookieItem>>();
                    }
                })
                .orElse(new HashMap<>());
    }

    /** Clears the entire guest_cart cookie (used after login merge). */
    public void clearCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, "")
                .path("/")
                .httpOnly(true)
                .maxAge(0)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    // ── private helpers ───────────────────────────────────────────────────────

    private List<CartCookieItem> readBusinessItems(Long businessId, HttpServletRequest request) {
        return readAllFromCookie(request).getOrDefault(String.valueOf(businessId), List.of());
    }

    private void writeCookie(Map<String, List<CartCookieItem>> data, HttpServletResponse response) {
        try {
            String json = objectMapper.writeValueAsString(data);
            String encoded = URLEncoder.encode(json, StandardCharsets.UTF_8);
            ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, encoded)
                    .path("/")
                    .httpOnly(true)
                    .maxAge(COOKIE_MAX_AGE_SECONDS)
                    .sameSite("Lax")
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        } catch (Exception e) {
            log.error("Failed to write guest_cart cookie", e);
        }
    }

    CartDTO buildCartDTO(Long businessId, List<CartCookieItem> cookieItems) {
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
