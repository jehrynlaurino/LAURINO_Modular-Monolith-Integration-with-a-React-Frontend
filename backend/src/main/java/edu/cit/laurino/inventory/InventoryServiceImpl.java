package edu.cit.laurino.inventory;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Package-private on purpose: Spring discovers the bean, while other modules
 * cannot name or inject this implementation directly.
 */
@Service
class InventoryServiceImpl implements InventoryService {
    private final InventoryRepository inventoryRepository;

    InventoryServiceImpl(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<InventoryItem> getItem(String productId) {
        return inventoryRepository.findById(productId).map(InventoryEntity::toItem);
    }

    @Override
    @Transactional
    public ReservationResult reserve(String productId, int quantity) {
        if (quantity <= 0) {
            return ReservationResult.rejected("Quantity must be at least 1.", null);
        }

        Optional<InventoryEntity> item = inventoryRepository.findById(productId);
        if (item.isEmpty()) {
            return ReservationResult.rejected("Product " + productId + " was not found.", null);
        }

        InventoryEntity inventory = item.get();
        if (quantity > inventory.getStock()) {
            return ReservationResult.rejected(
                    "Only " + inventory.getStock() + " unit(s) of " + inventory.getName() + " remain.",
                    inventory.toItem());
        }

        inventory.decreaseStock(quantity);
        InventoryItem remainingInventory = inventory.toItem();
        return new ReservationResult(true, "Inventory reserved.", remainingInventory);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryItem> listItems() {
        return inventoryRepository.findAll().stream()
                .map(InventoryEntity::toItem)
                .toList();
    }
}
