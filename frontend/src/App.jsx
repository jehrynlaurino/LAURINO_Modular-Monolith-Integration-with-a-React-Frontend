import { useCallback, useEffect, useState } from "react";

const API_BASE_URL = "http://localhost:8080";
const LOW_STOCK_THRESHOLD = 5;

function App() {
  const [products, setProducts] = useState([]);
  const [orders, setOrders] = useState([]);
  const [notifications, setNotifications] = useState([]);

  const [selectedProductId, setSelectedProductId] = useState("");
  const [selectedQuantity, setSelectedQuantity] = useState(1);
  const [cart, setCart] = useState([]);

  const [result, setResult] = useState(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [cancellingOrderId, setCancellingOrderId] = useState(null);

  const refreshAll = useCallback(async () => {
    try {
      const [productsRes, ordersRes, notificationsRes] = await Promise.all([
        fetch(`${API_BASE_URL}/api/inventory`),
        fetch(`${API_BASE_URL}/api/orders`),
        fetch(`${API_BASE_URL}/api/notifications`),
      ]);

      if (!productsRes.ok || !ordersRes.ok || !notificationsRes.ok) {
        throw new Error("Could not load the latest data from the server.");
      }

      const [productsData, ordersData, notificationsData] = await Promise.all([
        productsRes.json(),
        ordersRes.json(),
        notificationsRes.json(),
      ]);

      setProducts(productsData);
      setOrders(ordersData);
      setNotifications(notificationsData);

      setSelectedProductId((current) => current || productsData[0]?.productId || "");
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    refreshAll();
  }, [refreshAll]);

  function addToCart() {
    if (!selectedProductId || Number(selectedQuantity) < 1) {
      return;
    }

    setCart((currentCart) => {
      const existing = currentCart.find((line) => line.productId === selectedProductId);
      if (existing) {
        return currentCart.map((line) =>
          line.productId === selectedProductId
            ? { ...line, quantity: line.quantity + Number(selectedQuantity) }
            : line
        );
      }
      return [...currentCart, { productId: selectedProductId, quantity: Number(selectedQuantity) }];
    });
    setSelectedQuantity(1);
  }

  function removeFromCart(productId) {
    setCart((currentCart) => currentCart.filter((line) => line.productId !== productId));
  }

  function updateCartQuantity(productId, quantity) {
    setCart((currentCart) =>
      currentCart.map((line) =>
        line.productId === productId ? { ...line, quantity: Number(quantity) } : line
      )
    );
  }

  function productName(productId) {
    return products.find((product) => product.productId === productId)?.name ?? productId;
  }

  async function submitOrder(event) {
    event.preventDefault();
    setResult(null);
    setError("");

    if (cart.length === 0) {
      setError("Add at least one product to the cart first.");
      return;
    }

    setSubmitting(true);
    try {
      const response = await fetch(`${API_BASE_URL}/api/orders`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ items: cart }),
      });

      const payload = await response.json();

      if (!response.ok && !payload.status) {
        throw new Error(payload.message || "The API could not place the order.");
      }

      setResult(payload);
      setCart([]);
      await refreshAll();
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  }

  async function cancelOrder(orderId) {
    setError("");
    setCancellingOrderId(orderId);
    try {
      const response = await fetch(`${API_BASE_URL}/api/orders/${orderId}/cancel`, {
        method: "POST",
      });
      const payload = await response.json();

      if (!response.ok) {
        throw new Error(payload.message || "Could not cancel the order.");
      }

      await refreshAll();
    } catch (err) {
      setError(err.message);
    } finally {
      setCancellingOrderId(null);
    }
  }

  const canAddToCart = selectedProductId && Number(selectedQuantity) >= 1;
  const canSubmit = cart.length > 0 && !submitting;

  return (
    <main className="page-shell">
      <div className="layout">
        <section className="order-card" aria-labelledby="page-title">
          <p className="eyebrow">Modular Monolith Demo</p>
          <h1 id="page-title">Place an order</h1>
          <p className="intro">
            Add one or more products to your cart, then submit a single multi-item order.
            If any line item exceeds available stock, the whole order is rejected.
          </p>

          <div className="cart-builder">
            <label htmlFor="product">Product</label>
            <select
              id="product"
              value={selectedProductId}
              onChange={(event) => setSelectedProductId(event.target.value)}
              disabled={loading}
            >
              {products.map((product) => (
                <option key={product.productId} value={product.productId}>
                  {product.productId} - {product.name} ({product.stock} in stock)
                </option>
              ))}
            </select>

            <label htmlFor="quantity">Quantity</label>
            <input
              id="quantity"
              type="number"
              min="1"
              step="1"
              value={selectedQuantity}
              onChange={(event) => setSelectedQuantity(event.target.value)}
            />

            <button type="button" onClick={addToCart} disabled={!canAddToCart}>
              Add to cart
            </button>
          </div>

          {cart.length > 0 && (
            <ul className="cart-list">
              {cart.map((line) => (
                <li key={line.productId} className="cart-line">
                  <span>{productName(line.productId)}</span>
                  <input
                    type="number"
                    min="1"
                    step="1"
                    value={line.quantity}
                    onChange={(event) => updateCartQuantity(line.productId, event.target.value)}
                  />
                  <button
                    type="button"
                    className="link-button"
                    onClick={() => removeFromCart(line.productId)}
                  >
                    Remove
                  </button>
                </li>
              ))}
            </ul>
          )}

          <form onSubmit={submitOrder}>
            <button type="submit" disabled={!canSubmit}>
              {submitting ? "Submitting..." : "Submit order"}
            </button>
          </form>

          {error && (
            <p className="message error" role="alert">
              {error}
            </p>
          )}

          {result && (
            <section
              className={`result ${result.status === "CONFIRMED" ? "confirmed" : "rejected"}`}
              aria-live="polite"
            >
              <h2>{result.status}</h2>
              <p>{result.reason}</p>
              {result.items && result.items.length > 0 && (
                <ul className="outcome-list">
                  {result.items.map((item, index) => (
                    <li key={`${item.productId}-${index}`}>
                      {item.productId}: {item.outcome}
                    </li>
                  ))}
                </ul>
              )}
            </section>
          )}
        </section>

        <section className="side-panel">
          <div className="panel-block">
            <h2>Inventory</h2>
            <table className="inventory-table">
              <thead>
                <tr>
                  <th>Product</th>
                  <th>Stock</th>
                </tr>
              </thead>
              <tbody>
                {products.map((product) => (
                  <tr
                    key={product.productId}
                    className={product.stock < LOW_STOCK_THRESHOLD ? "low-stock" : ""}
                  >
                    <td>
                      {product.productId} - {product.name}
                    </td>
                    <td>{product.stock}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <div className="panel-block">
            <h2>Order history</h2>
            {orders.length === 0 && <p className="empty-note">No orders yet.</p>}
            <ul className="order-history">
              {orders.map((order) => (
                <li key={order.orderId} className={`order-row status-${order.status.toLowerCase()}`}>
                  <div className="order-row-header">
                    <span>
                      Order #{order.orderId} — {order.status}
                    </span>
                    {order.status === "CONFIRMED" && (
                      <button
                        type="button"
                        className="link-button"
                        onClick={() => cancelOrder(order.orderId)}
                        disabled={cancellingOrderId === order.orderId}
                      >
                        {cancellingOrderId === order.orderId ? "Cancelling..." : "Cancel"}
                      </button>
                    )}
                  </div>
                  <ul className="order-items">
                    {order.items.map((item, index) => (
                      <li key={`${item.productId}-${index}`}>
                        {item.productId} x{item.quantity}
                      </li>
                    ))}
                  </ul>
                  {order.reason && <p className="order-reason">{order.reason}</p>}
                </li>
              ))}
            </ul>
          </div>

          <div className="panel-block">
            <h2>Activity feed</h2>
            {notifications.length === 0 && <p className="empty-note">No activity yet.</p>}
            <ul className="notification-feed">
              {notifications.map((notification) => (
                <li
                  key={notification.notificationId}
                  className={notification.message.startsWith("Reorder needed") ? "reorder" : ""}
                >
                  {notification.message}
                </li>
              ))}
            </ul>
          </div>
        </section>
      </div>
    </main>
  );
}

export default App;
