# Browser Network-tab evidence checklist

This directory is deliberately committed without made-up screenshots. Add the two genuine captures below before submitting.

## confirmed-network.png

1. Start the backend with your Supabase environment variables and start Vite.
2. Open http://localhost:5173 and DevTools -> Network -> Fetch/XHR.
3. Select P100 and a quantity within the shown stock, then submit.
4. Click the POST request to `http://localhost:8080/api/orders`.
5. Capture one image that visibly includes:
   - request method `POST` and URL `/api/orders`;
   - request payload with the chosen productId and quantity;
   - a response body whose status is `CONFIRMED`;
   - the returned inventory object and remaining stock.

## rejected-network.png

1. Keep Network open and choose P300, quantity 1.
2. Submit and select the new POST `/api/orders` request.
3. Capture the same fields, with a `REJECTED` response and inventory stock `0`.

Do not expose the Supabase database password, connection string, project URL with private credentials, or browser cookies in either image.
