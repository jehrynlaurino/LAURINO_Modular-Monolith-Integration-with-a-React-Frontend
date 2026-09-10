package edu.cit.laurino.inventory;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

interface InventoryRepository extends JpaRepository<InventoryEntity, String> {
    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    java.util.Optional<InventoryEntity> findById(String productId);
}
