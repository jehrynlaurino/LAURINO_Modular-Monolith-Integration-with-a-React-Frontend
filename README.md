# Modular Monolith Shop Lab

A Spring Boot modular monolith with an in-process **Order -> Inventory** integration, a shared Supabase Postgres database, and a React/Vite browser client.

> Package assumption: this project uses `edu.cit.laurino`, based on the workspace name. Replace `laurino` everywhere if the required surname is different.

## Architecture

```text
React (http://localhost:5173)
       | HTTP / JSON
       v
Spring Boot: edu.cit.laurino
  shop module -- InventoryService interface --> inventory module
       |                                        |
       +-------------- shared transaction ------+
                            |
                            v
                    Supabase Postgres
                    inventory + orders
```

The key boundary is in [InventoryService.java](backend/src/main/java/edu/cit/laurino/inventory/InventoryService.java). The order module constructor-injects that interface only. [InventoryServiceImpl.java](backend/src/main/java/edu/cit/laurino/inventory/InventoryServiceImpl.java) has no `public` modifier, so it cannot be directly imported from the `shop` package. Inventory reservation uses a pessimistic database lock to avoid overselling during concurrent orders.

## Project layout

- `backend/` - Spring Boot API and backend integration tests
- `frontend/` - Vite React application
- `sql/schema.sql` - Supabase table creation and seed data
- `evidence/` - destination for the two browser Network-tab screenshots

## Publish to GitHub

This workspace is initialized as a local Git repository. After you create an empty GitHub repository in your own account, publish it with:

```powershell
git add .
git commit -m "Complete modular monolith shop lab"
git remote add origin https://github.com/<your-username>/modular-monolith-shop.git
git push -u origin main
```

## Supabase setup

1. Create a free Supabase project and wait until its database is available.
2. In **SQL Editor**, run [schema.sql](sql/schema.sql). It creates `inventory` and `orders`, then seeds P100 Wireless Mouse (25), P200 Mechanical Keyboard (10), and P300 USB-C Hub (0).
3. In the Supabase **Connect** panel, copy the Postgres connection values. In PowerShell, set them for the current terminal session. Do not put the real password in a file.

   ```powershell
   $env:SUPABASE_DB_URL = "jdbc:postgresql://db.<project-ref>.supabase.co:5432/postgres?sslmode=require"
   $env:SUPABASE_DB_USERNAME = "postgres"
   $env:SUPABASE_DB_PASSWORD = "<your-supabase-database-password>"
   $env:CORS_ALLOWED_ORIGIN = "http://localhost:5173"
   ```

   If Supabase gives a pooler host/port instead, use that exact host and port in the JDBC URL and retain `sslmode=require`. [backend/.env.example](backend/.env.example) documents the variable names only; it contains no usable secret. The root [.gitignore](.gitignore) excludes local env files.

## Run it

Requirements: Java 17+, Maven 3.9+, Node.js 20+ and an accessible Supabase project.

```powershell
cd backend
mvn test
mvn spring-boot:run
```

In a second terminal:

```powershell
cd frontend
npm install
npm run dev
```

Open the URL Vite prints (normally http://localhost:5173). The frontend calls `http://localhost:8080/api/inventory` to populate its product dropdown and `POST http://localhost:8080/api/orders` to place an order. CORS is restricted by default to the Vite development origin.

## API

```http
POST /api/orders
Content-Type: application/json

{ "productId": "P100", "quantity": 3 }
```

A successful result is:

```json
{
  "status": "CONFIRMED",
  "reason": "Inventory reserved.",
  "inventory": { "productId": "P100", "name": "Wireless Mouse", "stock": 22 }
}
```

Choosing P300 with quantity 1 returns `REJECTED` and leaves stock at 0. Every attempt, including a rejected one, is written to `orders`.

## Network-tab evidence

The automated backend test covers both responses in [OrderApiIntegrationTest.java](backend/src/test/java/edu/cit/laurino/shop/OrderApiIntegrationTest.java). The lab also requires browser evidence. With the app running against **your** Supabase database:

1. Open browser DevTools -> **Network** and filter to Fetch/XHR.
2. Submit P100 quantity 1 (or another quantity within currently available stock). Select the `orders` POST, then capture a screenshot showing its **Payload** and a `CONFIRMED` JSON **Response**.
3. Submit P300 quantity 1. Capture the `orders` POST showing its Payload and the `REJECTED` JSON Response with stock 0.
4. Save the screenshots as `evidence/confirmed-network.png` and `evidence/rejected-network.png`. The expected captures and a checklist are in [evidence/README.md](evidence/README.md).

Screenshots are intentionally not fabricated in this repository: they must show the student's own Supabase-backed browser session, timestamp, and Network request.

## Reflection (451 words)

**1. In-process versus network integration.** Inside this monolith, Order invokes an ordinary Java interface implemented by Inventory. The compiler checks the method signatures, Spring wires both modules in one process, and a method call has no HTTP serialization, DNS lookup, socket failure, remote timeout, or independently deployed version to manage. Both modules can also join the same local Spring transaction, so the stock decrement and order insert succeed or roll back together. This is convenient and fast, but it only works because the modules share one runtime and one database. If Inventory became a remote service, the call would need an HTTP or messaging client, stable request and response contracts, authentication, service discovery or fixed routing, timeouts, retries, circuit breakers, tracing, metrics, and compatible API versioning. A cross-service transaction would no longer be automatic; the design would usually need idempotency keys, an outbox/event flow, compensation, and eventual-consistency handling. Network failure must become a normal business case rather than an unexpected exception.

**2. Why package-private matters.** `InventoryServiceImpl` is deliberately package-private while `InventoryService`, `InventoryItem`, and `ReservationResult` form the inventory module's public API. Code in the Order package cannot import the implementation, repository, or entity and therefore must request an abstraction through constructor injection. That keeps inventory rules, locking, and persistence choices owned by the Inventory module. Tests can replace the interface with a fake without depending on JPA details. If the implementation were public, Order could inject it directly, cast to it, or start using implementation-only methods. It could also grow dependencies on the repository or entity. Those shortcuts compile, but they quietly turn a module boundary into a convention. A later refactor of Inventory would then require changes throughout Order, and extracting Inventory would be much harder because callers would know internal classes rather than a stable contract.

**3. When to extract Inventory.** I would extract Inventory when it needs independent scaling or deployment, has a separate team and release cadence, serves several applications, requires different availability or security controls, or has become a domain with enough complexity to justify operational overhead. The code would keep a public inventory contract but replace the in-process Spring bean with an HTTP or messaging adapter. Order would call a versioned remote API and would persist a pending request or idempotency key before sending it. Instead of one database transaction, Inventory would own its database and publish a reservation result; Order would consume that result and move from PENDING to CONFIRMED or REJECTED. Retries, duplicate messages, timeouts, reconciliation, and observability would be added. The current interface boundary makes that change focused: only the adapter and order workflow need to change, rather than every caller.
