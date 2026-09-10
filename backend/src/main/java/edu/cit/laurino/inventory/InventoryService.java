package edu.cit.laurino.inventory;

import java.util.List;
import java.util.Optional;

/**
 * The inventory module's public contract. Other modules may only use this type
 * and its public value objects, never inventory persistence classes.
 */
public interface InventoryService {
    Optional<InventoryItem> getItem(String productId);

    ReservationResult reserve(String productId, int quantity);

    /**
     * Supports the client product picker without exposing the repository.
     */
    List<InventoryItem> listItems();
}
