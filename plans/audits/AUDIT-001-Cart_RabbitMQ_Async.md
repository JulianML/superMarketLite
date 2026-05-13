# AUDIT-001: Cart System with Guest Cookie and RabbitMQ Async Persistence

- **ID:** AUDIT-001
- **Date:** 2026-05-12
- **Auditor:** Senior Programmer
- **Plan:** [Plan_Cart_RabbitMQ_Async.md](../Plan_Cart_RabbitMQ_Async.md)
- **Verdict:** Approved with conditions

## Summary

The plan is well-structured and correctly identifies the dual-mode cart problem (guest vs. authenticated). The DB schema is already in place, and the phasing is logical. However, there are two security/correctness blockers that must be fixed before implementation begins: trusting `unitPrice` from the cookie (price tampering vector) and configuring RabbitMQ with `localhost` which will fail inside Docker Compose. Additionally, running both Kafka and RabbitMQ requires justification, and several implementation details are underspecified.

## Strengths

- Leverages the existing `carts` and `cart_items` DB schema — no new migration needed.
- Clean separation between `GuestCartService`, `UserCartService`, and the unified `CartService` facade.
- Correctly identifies async write pattern (fire-and-forget via RabbitMQ) with synchronous reads for the GET endpoint.
- Correctly routes guest vs. user at the service layer rather than polluting the controller.
- Good note that `SameSite=Strict` and `HttpOnly` should be set on the cookie.

## Findings

### Blockers

- **Price tampering via cookie**: `CartItemDTO` includes `unitPrice`, which would be serialized into the guest cookie. A malicious user can tamper with that value. Unit prices must **never** be trusted from the cookie — they must always be re-fetched from the `products` table when reading the guest cart. Store only `productId` and `quantity` in the cookie; compute `unitPrice` server-side.

- **RabbitMQ host set to `localhost`**: `application.properties` shows `spring.rabbitmq.host=localhost`, but the existing `application.properties` already uses `mysql-db` as the DB hostname (a Docker Compose service name). Using `localhost` will fail when the app runs in a container. The property must use the RabbitMQ service name (e.g. `rabbitmq`) and be overridable via an environment variable, consistent with how the DB is configured.

### Major concerns

- **Two message brokers (Kafka + RabbitMQ)**: The project already uses Kafka. Introducing RabbitMQ adds a second broker — double the infrastructure to operate, monitor, and understand. The justification ("queue model fits better") is weak: Kafka consumer groups with a `cart-sync` topic scoped to one partition-per-user-id would achieve the same ordering guarantees. Either add a stronger justification (team expertise, existing RabbitMQ infrastructure, etc.) or switch to Kafka for consistency.

- **JWT filter must still populate `SecurityContextHolder` for permitted endpoints**: The plan says `CartService` will check `SecurityContextHolder` to decide which cart mode to use, while `SecurityConfig` permits `/api/cart/**` without auth. By default, Spring Security's `JwtAuthFilter` still runs for permitted endpoints (the permit only skips the authorization check, not the filter). But this must be verified: if the filter is configured with `shouldNotFilter` logic, it may skip JWT parsing entirely for permitted paths. The plan should explicitly confirm this behavior.

- **`HttpServletRequest`/`HttpServletResponse` must be passed explicitly**: `GuestCartService` needs to read and write the HTTP cookie. The plan doesn't specify how this happens. Injecting `HttpServletRequest`/`HttpServletResponse` deep inside a service is an anti-pattern (makes unit testing hard). These should be received in the controller and passed as method arguments to the service.

- **Cart merge conflict strategy left unresolved**: The plan says "decide before implementing" whether to sum or replace quantities on merge. This must be decided now (it affects the consumer logic and has user-visible behavior). **Recommendation**: use *sum with a cap* — add guest quantities to existing DB quantities, capped at some sane maximum (e.g., 99). Silently replacing could cause cart loss; silently summing without a cap could cause absurd quantities.

- **Dead Letter Queue not included as a concrete step**: DLQ is mentioned as "consider" in the risks section but is not in any phase. A DLQ is essential for async reliability — failed messages will be silently dropped without it. Add a `cart.sync.dlq` queue and bind it in `RabbitMQConfig` (Phase 1).

### Minor observations

- `SameSite=Strict` on the cookie will break cross-origin requests (e.g. the front-end and API on different ports/subdomains). Use `SameSite=Lax` instead, which still protects against CSRF but allows same-site top-level navigations.
- "DB cart is updated asynchronously via RabbitMQ within milliseconds" is not a measurable criterion. Replace with "within a reasonable time under normal load (target: <1 s end-to-end latency from mutation to DB write)".
- Phase 3, Step 9 leaves the cookie scope ambiguous: "a map keyed by businessId, or one cookie per cart." Decide now — a single JSON cookie with a map keyed by `businessId` is the simpler approach and avoids proliferating cookies.
- The `CartSyncMessage` is referenced but never fully defined. Add its field list to Phase 4 (userId, businessId, items `List<{productId, quantity}>`; notably, no price — see blocker above).
- The plan doesn't address cart expiration: guest cookies expire with the browser session by default; abandoned user carts have no TTL. A simple `max-age` on the cookie (e.g., 30 days) would improve UX.

## Risk matrix

| Risk | Likelihood | Impact | Mitigation |
|------|-----------|--------|------------|
| Price tampering via cookie | High | High | Never store or trust `unitPrice` in cookie; fetch from `products` table on every read |
| RabbitMQ down, async writes silently lost | Medium | High | Add DLQ; log and alert on consumer failures |
| Docker Compose `localhost` misconfiguration | High | High | Use service name as host; make it configurable via env var |
| Out-of-order message consumption | Medium | Medium | Include `updatedAt` timestamp in `CartSyncMessage`; consumer skips stale messages |
| Cookie size exceeds 4 KB | Low | Medium | Store only `productId` + `quantity` in cookie (blocker fix also addresses this) |
| Two brokers double operational overhead | Medium | Medium | Evaluate using Kafka instead; document the decision in an ADR |
| SameSite=Strict breaks cross-origin frontend | Medium | Low | Switch to `SameSite=Lax` |
| Merge creates quantity overflow | Low | Low | Cap merged quantity per item (e.g., max 99) |

## Recommendations

1. **Remove `unitPrice` from the cookie DTO** — store only `productId` + `quantity`. Re-fetch prices from the `products` table in `GuestCartService.getCart()`.
2. **Fix RabbitMQ host config** — set `spring.rabbitmq.host=${RABBITMQ_HOST:rabbitmq}` (matching the Docker Compose service name), consistent with how DB host is handled.
3. **Add DLQ to Phase 1** — declare `cart.sync.dlq` queue and bind it in `RabbitMQConfig` alongside the main queue.
4. **Decide merge strategy explicitly** — document "sum quantities, cap at 99" (or chosen policy) in the plan and in an ADR.
5. **Confirm JWT filter behavior for permitted endpoints** — add a note to Phase 6 / SecurityConfig step verifying that `JwtAuthFilter` still runs (and populates `SecurityContextHolder`) for `/api/cart/**` even when the endpoint is permitted.
6. **Pass HTTP request/response via controller args** — update Phase 3 and Phase 5 to pass `HttpServletRequest` and `HttpServletResponse` from the controller down to `GuestCartService` methods.
7. **Resolve cookie scope ambiguity** — commit to a single `guest_cart` cookie containing a JSON map keyed by `businessId`.
8. **Evaluate using Kafka instead of RabbitMQ** — if the team is already operating Kafka, adding a `cart-sync` topic there avoids introducing a second broker. Document the final technology choice as an ADR.
9. **Change `SameSite=Strict` to `SameSite=Lax`** in the cookie configuration note.
10. **Define `CartSyncMessage` fields explicitly** in Phase 4 (userId, businessId, `List<{productId, quantity}>`, `syncedAt` timestamp).

## Verdict rationale

Approved with conditions: the plan's overall structure and phasing are sound, but the two blockers (price tampering and Docker host misconfiguration) are correctness/security issues that would ship a broken or insecure feature if not addressed. The major concerns around DLQ, merge strategy, and JWT filter behavior are not hard blockers but carry significant risk if left unresolved. Move to Approved once recommendations 1–6 are applied to the plan.
