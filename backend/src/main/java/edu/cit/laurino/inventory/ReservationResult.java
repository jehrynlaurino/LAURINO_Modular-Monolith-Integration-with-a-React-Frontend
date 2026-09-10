package edu.cit.laurino.inventory;

/**
 * Explicit result at the inventory module boundary. A rejected reservation does
 * not modify stock.
 */
public record ReservationResult(boolean reserved, String reason, InventoryItem inventory) {
    public static ReservationResult rejected(String reason, InventoryItem inventory) {
        return new ReservationResult(false, reason, inventory);
    }
}
