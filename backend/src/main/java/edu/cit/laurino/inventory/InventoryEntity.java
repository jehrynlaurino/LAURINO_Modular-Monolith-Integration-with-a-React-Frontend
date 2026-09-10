package edu.cit.laurino.inventory;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "inventory")
class InventoryEntity {
    @Id
    @Column(name = "product_id", length = 50)
    private String productId;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false)
    private int stock;

    protected InventoryEntity() {
        // Required by JPA.
    }

    InventoryEntity(String productId, String name, int stock) {
        this.productId = productId;
        this.name = name;
        this.stock = stock;
    }

    String getProductId() {
        return productId;
    }

    String getName() {
        return name;
    }

    int getStock() {
        return stock;
    }

    void decreaseStock(int quantity) {
        if (quantity <= 0 || quantity > stock) {
            throw new IllegalArgumentException("Cannot reserve the requested quantity.");
        }
        stock -= quantity;
    }

    InventoryItem toItem() {
        return new InventoryItem(productId, name, stock);
    }
}
