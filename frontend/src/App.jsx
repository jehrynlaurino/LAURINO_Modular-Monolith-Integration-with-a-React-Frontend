import { useEffect, useState } from "react";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

function App() {
  const [products, setProducts] = useState([]);
  const [productId, setProductId] = useState("");
  const [quantity, setQuantity] = useState(1);
  const [result, setResult] = useState(null);
  const [error, setError] = useState("");
  const [loadingProducts, setLoadingProducts] = useState(true);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    async function loadProducts() {
      try {
        const response = await fetch(`${API_BASE_URL}/api/inventory`);
        if (!response.ok) {
          throw new Error("Could not load inventory.");
        }

        const items = await response.json();
        setProducts(items);
        setProductId(items[0]?.productId ?? "");
      } catch (loadError) {
        setError(loadError.message);
      } finally {
        setLoadingProducts(false);
      }
    }

    loadProducts();
  }, []);

  async function submitOrder(event) {
    event.preventDefault();
    setResult(null);
    setError("");
    setSubmitting(true);

    try {
      const response = await fetch(`${API_BASE_URL}/api/orders`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ productId, quantity: Number(quantity) }),
      });
      const payload = await response.json();

      if (!response.ok) {
        throw new Error(payload.message ?? "The API could not place the order.");
      }

      setResult(payload);
      if (payload.inventory) {
        setProducts((currentProducts) =>
          currentProducts.map((product) =>
            product.productId === payload.inventory.productId
              ? payload.inventory
              : product,
          ),
        );
      }
    } catch (submitError) {
      setError(submitError.message);
    } finally {
      setSubmitting(false);
    }
  }

  const selectedProduct = products.find((product) => product.productId === productId);
  const canSubmit = productId && Number(quantity) >= 1 && !submitting;

  return (
    <main className="page-shell">
      <section className="order-card" aria-labelledby="page-title">
        <p className="eyebrow">Modular Monolith Demo</p>
        <h1 id="page-title">Place an order</h1>
        <p className="intro">
          Orders call Inventory in-process, while this React client uses the REST API.
        </p>

        <form onSubmit={submitOrder}>
          <label htmlFor="product">Product</label>
          <select
            id="product"
            value={productId}
            onChange={(event) => setProductId(event.target.value)}
            disabled={loadingProducts || submitting}
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
            value={quantity}
            onChange={(event) => setQuantity(event.target.value)}
            disabled={submitting}
            required
          />

          {selectedProduct && (
            <p className="stock-note">
              Current stock: <strong>{selectedProduct.stock}</strong>
            </p>
          )}

          <button type="submit" disabled={!canSubmit}>
            {submitting ? "Submitting..." : "Submit order"}
          </button>
        </form>

        {error && <p className="message error" role="alert">{error}</p>}

        {result && (
          <section
            className={`result ${result.status === "CONFIRMED" ? "confirmed" : "rejected"}`}
            aria-live="polite"
          >
            <h2>{result.status}</h2>
            <p>{result.reason}</p>
            {result.inventory && (
              <p>
                Remaining stock for {result.inventory.name}:{" "}
                <strong>{result.inventory.stock}</strong>
              </p>
            )}
          </section>
        )}
      </section>
    </main>
  );
}

export default App;
