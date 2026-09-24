package edu.cit.laurino.supplier;

import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Binds the app.supplier.* configuration block - base URL, credentials (read
 * from env vars via application.yml, never hardcoded), and the productId ->
 * SupplierSku/PackSize mapping table from INTEGRATION.md.
 */
@Component
@ConfigurationProperties(prefix = "app.supplier")
class LegacySupplyProperties {
    private String baseUrl;
    private String clientId;
    private String apiKey;
    private Map<String, ProductMapping> productMap = new HashMap<>();

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public Map<String, ProductMapping> getProductMap() {
        return productMap;
    }

    public void setProductMap(Map<String, ProductMapping> productMap) {
        this.productMap = productMap;
    }

    ProductMapping mappingFor(String productId) {
        return productMap.get(productId);
    }

    static class ProductMapping {
        private String sku;
        private int packSize;

        public String getSku() {
            return sku;
        }

        public void setSku(String sku) {
            this.sku = sku;
        }

        public int getPackSize() {
            return packSize;
        }

        public void setPackSize(int packSize) {
            this.packSize = packSize;
        }
    }
}
