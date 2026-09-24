# INTEGRATION.md — LegacySupply Integration

## Product mapping

| Inventory productId | Name | LegacySupply SupplierSku | PackSize |
|---|---|---|---|
| P100 | Wireless Mouse | YEU-4293 | 10 |
| P200 | Mechanical Keyboard | YEU-2184 | 20 |
| P300 | USB-C Hub | YEU-7363 | 24 |

Pulled directly from `GET /catalog` on 2026-09-24.

## Sessions

A session is obtained with `POST /auth/token` using our Client ID (our student ID) and API key, and returns a `SessionToken` that must be sent as the `X-LS-Session` header on every subsequent request.

**Measured lifetime:** we authenticated at 18:58:53 and made a successful authenticated call (`GET /catalog`) at that time. A repeat of the exact same call using the same token failed with `E-AUTH-07 Session not valid.` at 19:02:28. That puts the session's real lifetime at **under ~3 minutes 35 seconds** — much shorter than we assumed going in. The manual does not state a number; this was measured empirically.

Because of this short lifetime, our adapter re-authenticates lazily (on first use, or immediately after any 401) rather than trying to keep one long-lived session alive — see `LegacySupplySessionManager` in the `supplier` module.

## Errors encountered

| Code | HTTP | Message | What actually caused it |
|---|---|---|---|
| E-AUTH-07 | 401 | Session not valid. | Reused a session token after it had expired (~3.5 min old) |
| E-SYS-50 | 503 | Processing error. | Placed a completely valid order; LegacySupply's own backend returned a transient server error. Confirmed as transient — the identical request succeeded on immediate retry with the same `X-Request-Id`. |
| E-SKU-02 | 422 | Item not recognized. | Sent `SupplierSku=DOES-NOT-EXIST`, an item number not in our catalog |
| E-QTY-11 | 422 | Quantity invalid. | Sent `Qty=0` in a purchase order |
| E-IDEM-04 | 409 | Request id reused with different content. | Reused an `X-Request-Id` from an earlier successful order, but changed the `Qty` in the body |
| E-QRY-06 | 400 | Query parameter required. | Called `GET /purchase-orders` without the required `buyerRef` query parameter |

We treat `E-SYS-50` (like `E-SYS-99` and `429/E-RATE-03`, per the manual) as **retryable** — these are LegacySupply-side conditions unrelated to the content of our request. `E-SKU-02`, `E-QTY-11`, `E-REF-05`, and `E-FMT-*` are treated as **terminal** — retrying an invalid SKU or quantity will never succeed, so we mark the order `FAILED` immediately instead of endlessly retrying it. `E-AUTH-*` triggers exactly one automatic session refresh and retry before falling through to the normal retry/terminal logic.

## Qty and Uom, explained

`Qty` in a `PurchaseOrder` request is **not** the number of individual units we want — it's the number of **cases**, in whatever pack size LegacySupply uses for that item (`Uom: CS` in every response we received, confirming "cases" is the unit).

**Worked example:** our Low-Stock Auto-Reorder rule decided Wireless Mouse (`P100`) needed 20 more units. `P100`'s LegacySupply SupplierSku is `YEU-4293`, which has `PackSize: 10`. So we sent `Qty=2` (2 cases × 10 units/case = 20 units), and the acknowledgement confirmed:

```
PoNumber: PO-100005
StatusCode: 10
SupplierSku: YEU-4293
Qty: 2
Uom: CS
```

Our `SupplierGatewayImpl` always converts our own unit count into cases by rounding **up** (`Math.ceil(unitsNeeded / packSize)`), so we may occasionally order slightly more than strictly needed — never less — since LegacySupply has no concept of a partial case.

## Unexpected status codes

`GET /purchase-orders/{PoNumber}` can in principle return a `StatusCode` outside the four documented values (10/20/30/40). Our `DeliveryTrackingScheduler` maps only those four; any other code is logged as a warning and the order's stored status is **left unchanged** rather than guessed at or overwritten. This avoids silently corrupting our own status history based on an undocumented value, at the cost of that order not progressing in our system until a recognized code appears. We consider this the safer default for a system feeding real inventory restocking.
