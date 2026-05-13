# Plan: Frontend Cart Integration

- **Date:** 2026-05-12
- **Status:** Draft
- **Author:** julian

## Objective

Connect the existing backend cart API to the frontend so users (both guests and authenticated) can add products to the cart, view it, modify quantities, and remove items — all without modifying the backend.

## Context

The backend already exposes a full REST cart API at `/api/cart/{businessId}` with support for both guest (HttpOnly cookie) and authenticated (JWT) users, including automatic guest→user merging on login. The frontend has a product catalog (`CatalogPage.tsx`), a product detail page (`ProductDetailPage.tsx`), a `Navbar.tsx`, and a shared HTTP client (`client.ts`). There is no cart API layer or UI yet. The `BUSINESS_ID` constant (`= 1`) already exists in `client.ts` and `AuthContext` exposes `businessId` from the JWT token.

## Steps / Phases

### Phase 1 — Cart API layer

1. Create `front/src/api/cart.ts` with typed interfaces and functions matching the backend DTOs:
   - Interfaces: `CartItemDTO`, `CartDTO`
   - `cartApi.get(businessId)` → `GET /api/cart/{businessId}`
   - `cartApi.addItem(businessId, productId, quantity)` → `POST /api/cart/{businessId}/items`
   - `cartApi.updateItem(businessId, productId, quantity)` → `PUT /api/cart/{businessId}/items/{productId}`
   - `cartApi.removeItem(businessId, productId)` → `DELETE /api/cart/{businessId}/items/{productId}`
   - `cartApi.clear(businessId)` → `DELETE /api/cart/{businessId}`
   - All calls go through the existing `apiRequest` in `client.ts` (auto-adds JWT when present; guest cookie is handled server-side automatically)

### Phase 2 — Cart context / state

2. Create `front/src/context/CartContext.tsx`:
   - State: `cart: CartDTO | null`, `loading: boolean`, `error: string | null`
   - Actions: `addItem`, `updateItem`, `removeItem`, `clearCart`, `refreshCart`
   - Derives `businessId` from `AuthContext` (falls back to `BUSINESS_ID = 1` for guests)
   - Fetches cart on mount and after login (`useEffect` depends on `user` from AuthContext)
   - Expose item count (sum of quantities) for the Navbar badge
   - Individual mutations (addItem, updateItem, removeItem) track their own in-flight state locally in the calling component, not in context — context `loading` is only for the initial fetch

3. Wrap `App.tsx` with `<CartProvider>` inside `<AuthProvider>`.

### Phase 3 — Navbar cart icon

4. Update `Navbar.tsx`:
   - Verify an icon library is available (e.g. `lucide-react`) — install if not present
   - Add a cart icon (e.g. shopping bag) that shows the total item count as a badge
   - Link it to `/cart`
   - Visible to all users (guests and authenticated)

### Phase 4 — "Add to cart" on product pages

5. Update `CatalogPage.tsx`:
   - Add an **Add to cart** button to each product card
   - Calls `CartContext.addItem(productId, 1)` and shows brief success feedback (e.g. button label changes to "Added ✓" for 1 second)
   - Disabled while the request is in flight

6. Update `ProductDetailPage.tsx`:
   - Add an **Add to cart** button with a quantity selector (default 1, min 1, max 99)
   - Same feedback pattern as above

### Phase 5 — Cart page

7. Create `front/src/pages/CartPage.tsx`:
   - Fetches and displays `CartDTO` from context
   - Lists each `CartItemDTO`: product name, unit price, quantity input (inline editable, `min={1}` `max={99}`), line total, remove button
   - Quantity input triggers `cartApi.updateItem` on blur or Enter key only (not on every keystroke) to avoid race conditions from rapid typing
   - Shows cart total at the bottom
   - Empty state: message + link back to `/catalog`
   - **Clear cart** button
   - Displays error message if a mutation fails (surfaces `CartContext.error`)
   - Quantity changes call `cartApi.updateItem` and refresh context; remove calls `cartApi.removeItem`
   - No checkout flow needed (out of scope)

8. Add the `/cart` route to `App.tsx` (public route, no `PrivateRoute` needed).

### Phase 6 — Post-login cart merge (no work needed)

The backend already calls `cartService.mergeGuestCart()` inside `AuthService.login()`. After a successful login the frontend just needs to call `refreshCart()` from `CartContext` — this can be triggered by the existing auth state change listener in Phase 2.

## Dependencies and risks

| Dependency / Risk | Notes |
|---|---|
| Backend must be running with RabbitMQ | GuestCartService and UserCartService both publish messages; consumer must be active for DB persistence |
| CORS configuration | Confirm `credentials: 'include'` is set in `apiRequest`. Also confirm `SecurityConfig` sets `allowCredentials(true)` and a specific (non-wildcard) `allowedOrigins` — without this the browser will reject the cookie on cross-origin requests even with `credentials: 'include'`. This may require a minor backend change; if so, update the success criterion accordingly. |
| `businessId` for guests | Falls back to hardcoded `BUSINESS_ID = 1`; multi-business guest carts would require a different approach |
| Price staleness | Not a frontend concern — backend always resolves prices server-side |
| Auth merge timing | `refreshCart` must be called *after* the login response is processed so the merged cart is returned |

## Success criteria

- A guest user can add items to the cart from the catalog and detail pages and see them reflected in the Navbar badge.
- An authenticated user's cart persists across page refreshes.
- After login, the guest cart items appear merged into the authenticated cart.
- Quantity updates and removals on the cart page are reflected immediately.
- The cart page shows an empty state when the cart has no items.
- No new backend changes are required.

## Additional notes

- Backend DTOs for reference: `CartDTO { businessId, items: CartItemDTO[], total }`, `CartItemDTO { productId, productName, sku, unitPrice, quantity, lineTotal }`.
- Backend cart endpoints: all under `/api/cart/{businessId}` (see `CartController.java`).
- **Known limitation:** if a product is deactivated after being added to the cart, the RabbitMQ consumer silently drops it from the persisted cart. The user may see an item disappear after a refresh with no explanation. No frontend mitigation is planned; this is a known edge case.
- Related plan: [Plan_Cart_RabbitMQ_Async](Plan_Cart_RabbitMQ_Async.md) — describes the backend cart architecture this plan integrates with.
- Audit: [AUDIT-002](audits/AUDIT-002-Cart_Frontend_Integration.md)
