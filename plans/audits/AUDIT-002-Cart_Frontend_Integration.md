# AUDIT-002: Frontend Cart Integration

- **ID:** AUDIT-002
- **Date:** 2026-05-12
- **Auditor:** Senior Programmer
- **Plan:** [Plan_Cart_Frontend_Integration.md](../Plan_Cart_Frontend_Integration.md)
- **Verdict:** Approved with conditions

## Summary

The plan is well-structured and the phase ordering is logical. The separation between API layer, context, UI components, and routing is correct. However, there are three concrete gaps that will cause bugs if unaddressed before implementation: the CORS/credentials requirement for guest cookies is deferred as "verify" but directly contradicts the "no backend changes" constraint; the cart page's inline quantity input has no debounce strategy, which will fire a request per keystroke; and CartContext exposes no error state, leaving API failures silent to the user.

## Strengths

- Clean 6-phase breakdown with clear file targets for each deliverable.
- Correctly identifies that guest cookie handling is transparent to the frontend — no special branching needed in `cart.ts`.
- Phase 6 (post-login merge) correctly requires zero frontend work beyond `refreshCart()`, matching the actual backend behaviour.
- `businessId` fallback strategy (`AuthContext` → `BUSINESS_ID = 1`) is pragmatic and covers the guest case without extra complexity.
- Success criteria map 1:1 to the actual user flows described.

## Findings

### Blockers

- **CORS + `credentials: 'include'` may require a backend change.** The plan says "verify `credentials: 'include'` is set in `apiRequest`" but lists "no backend changes required" as a success criterion. The guest cookie is `SameSite=Lax`; for cross-origin fetches (frontend dev server on a different port than the Spring backend), the browser only sends cookies when `credentials: 'include'` is present in the request AND the backend responds with `Access-Control-Allow-Credentials: true` plus a specific (non-wildcard) `Access-Control-Allow-Origin`. If `SecurityConfig` currently uses `allowedOrigins("*")` or omits `allowCredentials(true)`, the guest cart will silently break. This must be verified and fixed before implementation, even if it requires a backend tweak.

### Major concerns

- **No debounce on the cart page quantity input.** Phase 5 says the quantity field is "inline editable" and each change calls `cartApi.updateItem`. If the user types `15`, this fires a request for `1` and then for `15`, creating a race condition where the `1` response could arrive after the `15` response and leave the UI in a stale state. The plan must specify either: (a) a debounce of ~400 ms on the input, or (b) only triggering on blur/Enter rather than on every keystroke.

- **CartContext has no error state.** The plan defines `{ cart, loading }` but no `error`. If `refreshCart()` or `addItem()` fails (e.g., backend down, 500, network error), the UI has no way to surface the failure. At minimum the context should expose a per-operation error or a global `error: string | null` field, and the product pages and cart page should show an error message or toast.

- **Per-operation loading state is missing.** A single `loading: boolean` on the context is too coarse. While the cart is being refreshed, should all "Add to cart" buttons on the catalog be disabled? Likely not — each button should track its own in-flight state independently. The plan should specify that individual mutations track their own loading state locally in the component, separate from the context-level `loading` used for the initial fetch.

### Minor observations

- **Quantity cap on the cart page is unspecified.** Phase 4 mentions `max 99` for the detail page quantity selector, but the cart page's inline editor has no such constraint stated. The backend enforces a cap of 99 when summing quantities, so the frontend should also set `max={99}` on the input to avoid a confusing server rejection.
- **Step numbering spans phases.** Steps are numbered 1–8 globally across phases 1–6. If steps are ever reordered this becomes confusing. Consider restarting numbering within each phase (Phase 1 step 1, Phase 2 step 1, etc.) or drop global numbering.
- **Icon library not specified.** Phase 3 adds a cart icon to the Navbar but doesn't confirm that an icon library (e.g. `lucide-react`, `react-icons`) is already in the project. If none is installed, the phase needs a dependency installation step.
- **Inactive product edge case unacknowledged.** If a product is deactivated after being added to the cart, the RabbitMQ consumer silently drops it from the DB cart. The user could add an item, refresh, and find it missing with no explanation. The plan should at least acknowledge this as a known limitation.

## Risk matrix

| Risk | Likelihood | Impact | Mitigation |
|------|-----------|--------|------------|
| Guest cookie not sent cross-origin (CORS/credentials gap) | High | High | Confirm `credentials: 'include'` in `apiRequest` and `allowCredentials(true)` + specific origin in `SecurityConfig`; accept this as a minor backend change |
| Race condition from rapid quantity edits | Medium | Medium | Debounce input ~400 ms or trigger only on blur/Enter |
| Silent API failure with no user feedback | Medium | Medium | Add `error` field to CartContext; show toast or inline error on mutation failure |
| Double-click "Add to cart" sends duplicate requests | Medium | Low | Disable button while request is in-flight (already planned) |
| Stale cart in second browser tab | Low | Low | Acceptable limitation; acknowledge in notes |
| Icon library not installed | Low | Low | Verify in Phase 3 before implementing |

## Recommendations

1. **Resolve the CORS/credentials question first** (before writing any code): check `SecurityConfig` for `allowCredentials` and `allowedOrigins`. If a backend change is needed, update the "no backend changes" success criterion.
2. **Decide on quantity input UX** and add it to Phase 5: debounce (400 ms) or blur/Enter trigger. Document the chosen approach in the plan.
3. **Expand CartContext state** to `{ cart, loading, error }` and add per-component in-flight state for individual mutations.
4. **Add `max={99}` constraint** to the cart page quantity input, consistent with Phase 4's detail page.
5. **Verify icon library availability** as a prerequisite in Phase 3.
6. **Add a note** about the inactive-product silent-drop behaviour so the team is not surprised when testing.

## Verdict rationale

The plan is approved with conditions. The phase structure, file targets, and architectural decisions are sound and ready to implement. The three conditions that must be addressed before starting are: (1) CORS/credentials verification and fix, (2) debounce/blur strategy for the quantity input, and (3) error state in CartContext. Items 4–6 are lower priority but should be incorporated into the plan before it moves to "In Progress" status. Once conditions 1–3 are resolved the plan can move to Approved.
