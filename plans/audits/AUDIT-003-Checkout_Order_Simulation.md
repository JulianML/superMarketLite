# AUDIT-003: Checkout & Order Simulation

- **ID:** AUDIT-003
- **Date:** 2026-05-13
- **Auditor:** Senior Programmer
- **Plan:** [Plan_Checkout_Order_Simulation.md](../Plan_Checkout_Order_Simulation.md)
- **Verdict:** Approved with conditions

## Summary

The plan is well-structured and aligns sensibly with the existing stack. The phasing is logical and the technology choices are appropriate. However, several concrete details derived directly from the actual DB schema (`V1__init.sql`) are missing or incorrect, and one unaddressed security issue (IDOR on order detail) is a hard blocker. The plan must be corrected in at least three areas before implementation begins.

## Strengths

- Correctly identifies that the DB schema already exists and avoids proposing new migrations.
- Technology choices (RabbitMQ for async, `@Scheduled` for simulation, React Hook Form + Zod) are pragmatic and consistent with the existing stack.
- Each phase has a clear success criterion, making progress verifiable.
- The risk table covers the most common pitfalls (race condition, RabbitMQ availability, guest users).
- Explicitly scoping reviews out of this plan keeps the scope realistic.

## Findings

### Blockers

- **IDOR vulnerability on `GET /api/orders/{id}`.**
  The plan secures the endpoint with JWT (authentication) but does not mention authorization: the service must verify that `order.user_id == authenticatedUserId` before returning data. Without this check, any logged-in user can read any other user's order by guessing its ID. Must be listed as an explicit implementation requirement.

- **`order_number` generation strategy is unspecified.**
  The `orders` table declares `order_number VARCHAR(50) NOT NULL UNIQUE`. This field is required and must be unique, but the plan never defines how to produce it (UUID, business-prefixed sequence, timestamp-based, etc.). An implementation without this decision will fail on the first INSERT.

- **`delivery_address_json` is a JSON column, not relational fields.**
  The `orders` table stores the address as `delivery_address_json JSON NULL`. The plan doesn't mention this detail, which has direct consequences: (a) the backend needs a `@JdbcTypeCode(SqlTypes.JSON)` or a custom `@Convert` to serialize/deserialize the address DTO; (b) the frontend address form must produce a JSON object matching the expected structure (fields like `street`, `city`, `postalCode`, `country` must be agreed upon before building either side).

### Major concerns

- **`@Scheduled` task lacks filtering and idempotency logic.**
  The plan says the task "advances the delivery status every N minutes" but doesn't specify that it must query only orders in eligible states (`CONFIRMED`, `PREPARING`, `SHIPPED`) with sufficient time elapsed. Without this, either all orders advance on every tick (incorrect) or the task runs on every in-progress order regardless of when it entered that state. Also, on app restart the scheduler resumes immediately — orders that were already `SHIPPED` for 1 second could advance to `DELIVERED`. Add a `last_status_changed_at` check or use the existing `deliveries.eta_from/eta_to` fields for timing control.

- **`deliveries` has its own `status` column — one enum is not enough.**
  The plan defines a single `OrderStatus` enum, but the schema has two separate status columns: `orders.status` and `deliveries.status`. These can diverge (e.g., order is `CONFIRMED` while delivery is `PREPARING`). A `DeliveryStatus` enum is needed, and the `@Scheduled` task must update both tables consistently.

- **Inter-step state management in the checkout flow is unspecified.**
  The 3-step frontend flow (address → summary → confirmation) must pass the address from step 1 to the API call in step 3. The plan is silent on how this is handled. Options are React state lifted to a parent route, a `checkoutContext`, or sessionStorage. Without a decision here, the frontend implementation risks inconsistency or data loss on browser back/forward navigation.

- **`payments` stub must be a first-class implementation step, not a footnote.**
  The `payments` table has `provider VARCHAR(50) NOT NULL` and `status VARCHAR(30) NOT NULL` — both required fields with no DB default. A simulated payment record must be inserted at checkout time or the `placeOrder` transaction will succeed but leave the schema in an inconsistent state. Move this from "Additional Notes" into Phase 2.

- **`business_id` on orders is required but not sourced in the checkout flow.**
  `orders.business_id BIGINT NOT NULL`. The plan doesn't state how `business_id` is resolved at checkout time. Since the cart already has `business_id` (from `carts.business_id`), the service should read it from the active cart — but this must be made explicit.

- **Totals calculation (subtotal, VAT, shipping) is not specified.**
  The `orders` table stores `subtotal`, `tax_total`, `shipping_fee`, and `total` as separate computed fields. The plan never describes how these are calculated, particularly VAT (`order_items.vat_rate` is per-item). The checkout service must implement a calculation step, and it should be described.

### Minor observations

- Phase 2 mixes two concerns: `CheckoutService.placeOrder()` (write path) and `OrderService.getOrdersByUser()` / `getOrderById()` (read path). The read methods belong in Phase 3 alongside the controller, not in the checkout service phase.
- No mention of DTOs (`CheckoutRequestDTO`, `OrderSummaryDTO`, `OrderDetailDTO`). These should be listed as deliverables in Phases 2–3 to prevent leaking JPA entities through the REST layer.
- The success criterion "status updates refresh on page reload" implicitly rules out WebSocket/SSE but doesn't state this. Be explicit: polling via page reload is intentional for this prototype.
- Empty cart guard is missing: a user can navigate directly to `/checkout/address` with an empty cart. A route guard or redirect should be mentioned in Phase 5.

## Risk matrix

| Risk | Likelihood | Impact | Mitigation |
|------|-----------|--------|------------|
| IDOR on order detail endpoint | High | High | Service-layer ownership check: `order.userId != principal.id → 403` |
| Scheduler double-advances order status on restart | Medium | Medium | Filter by status AND minimum time in state; use `deliveries.eta_from` as the trigger threshold |
| `placeOrder` transaction partial failure (DB commit OK, RabbitMQ publish fails) | Medium | Medium | Implement outbox pattern or accept eventual delivery failure with DLQ retry |
| Inventory race condition (concurrent checkout of last item) | Medium | High | `@Version` on `Inventory` entity + catch `OptimisticLockException` and return HTTP 409 |
| Address JSON structure mismatch between frontend and backend | Medium | Medium | Define a shared `AddressDTO` record on the backend first; document its fields before building the frontend form |
| Missing `order_number` causing INSERT failure | High | High | Decide generation strategy before Phase 1 is complete |

## Recommendations

1. **Add an authorization check to `OrderService.getOrderById()`** — throw `403 Forbidden` if `order.userId != authenticatedUserId`. Document this explicitly in Phase 3.
2. **Define the `order_number` generation strategy in Phase 1** — a UUID prefixed with the current year (e.g., `ORD-2026-<UUID short>`) is simple, sortable, and collision-free.
3. **Define the `AddressDTO` JSON structure before building either side** — list the exact fields (e.g., `street`, `city`, `postalCode`, `country`) and add the JPA JSON converter to Phase 1.
4. **Add `DeliveryStatus` enum** alongside `OrderStatus` — both are needed since `orders.status` and `deliveries.status` are independent columns.
5. **Move the `payments` stub into Phase 2** as an explicit step inside `placeOrder()`: insert a `Payment` with `provider = "SIMULATED"`, `status = "SIMULATED"`, `paid_at = now()`.
6. **Specify `business_id` sourcing in Phase 2** — read it from `cart.businessId` during `placeOrder()`.
7. **Add totals calculation logic to Phase 2** — describe the formula: `subtotal = Σ(unit_price × qty)`, `tax_total = Σ(line_total × vat_rate)`, `total = subtotal + tax_total + shipping_fee`.
8. **Specify inter-step state strategy in Phase 5** — recommend a `CheckoutContext` (analogous to `CartContext`) to hold address and order draft across the three route steps.
9. **Add an empty cart guard to Phase 5** — redirect to `/cart` if cart is empty on entry to `/checkout/address`.
10. **Refine the `@Scheduled` task in Phase 4** — specify that it queries orders where `status IN ('CONFIRMED', 'PREPARING', 'SHIPPED')` and `delivery.created_at` is older than the minimum simulated delay for that state.

## Verdict rationale

The plan is approved with conditions because the architecture is sound and the scope is realistic, but three findings are hard blockers before coding starts: the IDOR vulnerability, the missing `order_number` strategy, and the unhandled `delivery_address_json` JSON column contract. The major concerns (totals calculation, DeliveryStatus enum, payments stub, inter-step state) must be addressed in the plan before the relevant phase begins — they will otherwise surface as implementation blockers mid-sprint. Resolving items 1–3 of the recommendations upgrades the verdict to Approved.
