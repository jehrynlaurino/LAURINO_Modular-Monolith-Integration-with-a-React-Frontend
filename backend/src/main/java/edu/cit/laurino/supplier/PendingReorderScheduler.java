package edu.cit.laurino.supplier;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ensures a reorder is never lost: any order still PENDING (LegacySupply was
 * unavailable when first attempted) gets resent here, reusing the same
 * requestId/buyerRef persisted at creation time - so retries, and even app
 * restarts, never produce a duplicate purchase order.
 */
@Component
class PendingReorderScheduler {
    private static final Logger log = LoggerFactory.getLogger(PendingReorderScheduler.class);

    private final SupplierOrderRepository supplierOrderRepository;
    private final LegacySupplyClient legacySupplyClient;
    private final LegacySupplyProperties properties;

    PendingReorderScheduler(
            SupplierOrderRepository supplierOrderRepository,
            LegacySupplyClient legacySupplyClient,
            LegacySupplyProperties properties) {
        this.supplierOrderRepository = supplierOrderRepository;
        this.legacySupplyClient = legacySupplyClient;
        this.properties = properties;
    }

    @Scheduled(fixedDelayString = "${app.supplier.pending-retry-interval-ms:60000}")
    @Transactional
    void retryPendingOrders() {
        List<SupplierOrderEntity> pending = supplierOrderRepository.findByStatus(SupplierOrderStatus.PENDING);
        for (SupplierOrderEntity order : pending) {
            LegacySupplyProperties.ProductMapping mapping = properties.mappingFor(order.getProductId());
            if (mapping == null) {
                log.warn("Skipping retry for order {} - no mapping for product {}", order.getId(), order.getProductId());
                continue;
            }

            SubmitOutcome outcome = legacySupplyClient.submitPurchaseOrder(
                    mapping.getSku(), order.getCases(), order.getBuyerRef(), order.getRequestId());

            if (outcome.success()) {
                order.markSubmitted(outcome.poNumber(), outcome.statusCode());
                supplierOrderRepository.save(order);
                log.info("Retried reorder {} succeeded, PO {}", order.getId(), outcome.poNumber());
            } else if (!outcome.retryable()) {
                order.markFailed(outcome.failureReason());
                supplierOrderRepository.save(order);
                log.warn("Retried reorder {} permanently failed: {}", order.getId(), outcome.failureReason());
            }
            // else: still unavailable, left PENDING and tried again next cycle.
        }
    }
}
