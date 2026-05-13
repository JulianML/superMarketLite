# Plan: Cart System with Guest Cookie and RabbitMQ Async Persistence

- **Date:** 2026-05-11
- **Status:** Draft (revised 2026-05-12 — see AUDIT-001)

## Objective

Implement a dual-mode cart system that works for both guest users (cart stored in a cookie) and authenticated users (cart persisted in the DB). When a logged-in user modifies their cart, changes are published to RabbitMQ and consumed asynchronously to keep the DB up-to-date. On login, the guest cookie cart is merged into the user's DB cart.

## Context

The DB schema already defines `carts` and `cart_items` tables (V1__init.sql). The project already uses Kafka for product events and JWT for authentication. RabbitMQ is introduced here specifically for cart sync because it fits the reliable, queue-based async pattern better than Kafka for this use case. The Spring Security config needs to allow unauthenticated access to cart endpoints while still reading the JWT if present to decide which cart strategy to use.

## Steps / Phases

### Phase 1 — Dependencies & Infrastructure

1. Add `spring-boot-starter-amqp` dependency to `pom.xml`.
2. Add RabbitMQ service to `compose.yaml` (or existing docker-compose file).
3. Add RabbitMQ connection properties to `application.properties`:
   ```
   spring.rabbitmq.host=${RABBITMQ_HOST:rabbitmq}
   spring.rabbitmq.port=5672
   spring.rabbitmq.username=guest
   spring.rabbitmq.password=guest
   ```
   Use the Docker Compose service name (`rabbitmq`) as the default, consistent with how `mysql-db` is used for the DB host. Never hardcode `localhost`.
4. Create `RabbitMQConfig.java` under `config/` declaring:
   - Exchange: `cart.exchange` (DirectExchange)
   - Queue: `cart.sync.queue`
   - Dead Letter Queue: `cart.sync.dlq` (bound to `cart.exchange` with routing key `cart.sync.dlq`)
   - Binding with routing key: `cart.sync`
   - `MessageConverter` bean using `Jackson2JsonMessageConverter`

### Phase 2 — Domain Layer

5. Create `Cart` entity (`cart/entity/Cart.java`) mapping to the existing `carts` table.
6. Create `CartItem` entity (`cart/entity/CartItem.java`) mapping to the existing `cart_items` table.
7. Create `CartRepository` and `CartItemRepository` (Spring Data JPA).
8. Create DTOs in `cart/dto/CartDTOs.java`:
   - `CartCookieItem` (productId, quantity) — **cookie-only**, no price (prices are never trusted from the cookie)
   - `CartItemDTO` (productId, quantity, unitPrice, productName) — API response only, always populated server-side
   - `CartDTO` (businessId, items list of `CartItemDTO`) — API response
   - `AddItemRequest` / `UpdateQuantityRequest`
   - `CartSyncMessage` (userId, businessId, `List<CartCookieItem>`, syncedAt) — RabbitMQ payload

### Phase 3 — Guest Cart (Cookie)

9. Create `GuestCartService.java` responsible for:
   - Reading the guest cart from a single JSON-encoded HTTP cookie named `guest_cart`. The cookie contains a JSON map keyed by `businessId` → `List<CartCookieItem>` (productId + quantity only — **no price**).
   - On read (`getCart`): deserialize the cookie, then fetch current prices from `ProductRepository` to build the `CartDTO` response. Prices are **always** fetched server-side and never stored in the cookie.
   - Writing/updating the cookie after each cart mutation.
   - `HttpServletRequest` and `HttpServletResponse` are passed as **method arguments** from the controller — they are not injected into the service (keeps the service unit-testable).
   - Cookie attributes: `HttpOnly`, `SameSite=Lax` (not Strict — Strict blocks cross-origin top-level navigations), `Max-Age=2592000` (30 days), `Path=/`.

### Phase 4 — Authenticated User Cart

10. Create `CartRabbitProducer.java` that publishes a `CartSyncMessage` (userId, businessId, `List<CartCookieItem>` with productId + quantity, `syncedAt` timestamp) to `cart.exchange` with routing key `cart.sync`.
11. Create `CartRabbitConsumer.java` annotated with `@RabbitListener(queues = "cart.sync.queue")` that:
    - Receives the `CartSyncMessage`.
    - Skips the message if its `syncedAt` is older than the `cart.updated_at` already in the DB (last-write-wins, prevents out-of-order processing).
    - Fetches current prices from `ProductRepository` to populate `unit_price` on `cart_items`.
    - Upserts the `Cart` record in the DB.
    - Upserts each `CartItem` (insert or update quantity/unit_price).
    - Removes items not present in the message (full cart replacement strategy).
    - On failure, lets the message go to `cart.sync.dlq` after exhausting retries (configure via `SimpleRabbitListenerContainerFactory` with 3 retries).
12. Create `UserCartService.java` that:
    - Loads the current cart from DB (synchronous read for GET requests).
    - After each mutation, publishes the updated cart to RabbitMQ (fire-and-forget async write).

### Phase 5 — Unified Cart Service & Merge

13. Create `CartService.java` as the single entry point for the controller:
    - Checks `SecurityContextHolder.getContext().getAuthentication()` — the `JwtAuthFilter` still runs for permitted endpoints (it only skips the authorization check, not the filter itself), so the context will be populated if a valid JWT is present.
    - Routes to `UserCartService` if authenticated, to `GuestCartService` otherwise.
    - `HttpServletRequest` and `HttpServletResponse` are passed from the controller as method arguments.
    - `mergeGuestCart(userId, businessId, HttpServletRequest, HttpServletResponse)`: reads the `guest_cart` cookie, merges items into the user's DB cart using **sum-with-cap strategy** (add guest quantity to existing DB quantity, cap at 99 per item), publishes a `CartSyncMessage` to RabbitMQ, then clears the cookie.
14. Hook `mergeGuestCart` into `AuthService.login(...)` — after issuing the JWT, call `CartService.mergeGuestCart(...)` passing the `HttpServletRequest`/`HttpServletResponse` from the login endpoint. If the cookie is absent or empty, skip silently.

### Phase 6 — REST API

15. Create `CartController.java` under `cart/web/` with endpoints:

   | Method | Path | Description |
   |--------|------|-------------|
   | GET | `/api/cart/{businessId}` | Get current cart (guest or user) |
   | POST | `/api/cart/{businessId}/items` | Add item |
   | PUT | `/api/cart/{businessId}/items/{productId}` | Update item quantity |
   | DELETE | `/api/cart/{businessId}/items/{productId}` | Remove item |
   | DELETE | `/api/cart/{businessId}` | Clear cart |

16. Update `SecurityConfig.java` to permit `/api/cart/**` without authentication. Verify that `JwtAuthFilter` is **not** excluded from running on these paths (i.e., no `shouldNotFilter` override for `/api/cart/**`) — the filter must still parse the JWT and populate `SecurityContextHolder` when a valid token is present, even though no auth is required.

### Phase 7 — Validation & Error Handling

17. Validate that the `productId` exists and belongs to `businessId` before adding to cart (delegate to `ProductRepository`).
18. Check `Inventory` stock before adding (optional soft check — hard check happens at order placement).
19. Return `404` if product not found, `422` if quantity ≤ 0.

## Dependencies and risks

- **RabbitMQ must be running**: if the broker is down, async writes will fail. The DLQ (`cart.sync.dlq`) catches failed messages after 3 retries. Monitor the DLQ and alert on non-empty queue.
- **Cart cookie size**: mitigated by storing only `productId` + `quantity` in the cookie (no price). For extreme cases (50+ items), the cookie may still approach 4 KB — log a warning if exceeded.
- **Race conditions on async writes**: mitigated by including `syncedAt` in `CartSyncMessage`; the consumer skips messages older than the DB's `updated_at` (last-write-wins).
- **Cart merge conflicts**: resolved — **sum-with-cap strategy**: guest quantity + DB quantity, capped at 99 per item.
- **Two message brokers (Kafka + RabbitMQ)**: acknowledged operational overhead. RabbitMQ is chosen here because the team already plans to use it for other future features (orders, notifications). If that changes, revisit using a Kafka `cart-sync` topic instead. Document this choice in an ADR.
- **Price integrity**: prices are never stored in the cookie nor in the RabbitMQ message. The consumer always fetches prices from the `products` table before writing to `cart_items`.

## Success criteria

- Guest user can add/update/remove items; the cart survives page refresh (cookie persists for 30 days).
- Prices on the guest cart response always reflect the current catalog prices, regardless of what is in the cookie.
- Logged-in user cart is readable from DB immediately (GET is synchronous).
- After login, guest cart items are merged into the user DB cart using sum-with-cap; the guest cookie is cleared.
- DB cart is updated asynchronously via RabbitMQ with a target end-to-end latency of <1 s under normal load.
- Failed RabbitMQ messages land in `cart.sync.dlq` and are not silently dropped.
- Cart endpoints return `200` for both guest and authenticated requests without requiring a JWT.
- An empty cart returns `200` with an empty items list, not `404`.

## Additional notes

- The existing `carts` table has a `UNIQUE INDEX uk_cart_active` on `(user_id, business_id, is_active)` — only one active cart per user per business is allowed.
- The `carts.status` column uses `'ACTIVE'` / `'CHECKED_OUT'` / `'ABANDONED'` — set to `'ACTIVE'` on creation, `'CHECKED_OUT'` when converted to an order.
- RabbitMQ is chosen over Kafka here because cart events are per-user point-to-point messages, not broadcast events — a queue model is a better fit.
- Cookie: name `guest_cart`, `HttpOnly`, `SameSite=Lax` (not Strict — Strict blocks cross-origin navigations which would break the frontend), `Max-Age=2592000` (30 days), `Path=/`.
- See [AUDIT-001](audits/AUDIT-001-Cart_RabbitMQ_Async.md) for the full audit report.
