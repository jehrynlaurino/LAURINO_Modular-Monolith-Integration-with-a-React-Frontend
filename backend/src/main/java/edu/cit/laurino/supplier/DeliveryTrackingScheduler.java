package edu.cit.laurino.supplier;

import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Polls LegacySupply for status on every order that isn't finished yet, and
 * maps LegacySupply's numeric StatusCode to our own SupplierOrderStatus -
 * Order and Inventory never see LegacySupply's codes. Publishes
 * SupplierOrderDelivered exactly once, on the transition into DELIVERED.
 */
@Component
class DeliveryTrackingScheduler {
    private static final Logger log = LoggerFactory.getLogger(DeliveryTrackingScheduler.class);
    private static final List<SupplierOrderStatus> OPEN_STATUSES =
            List.of(SupplierOrderStatus.SUBMITTED, SupplierOrderStatus.PICKING, SupplierOrderStatus.SHIPPED);

    private final SupplierOrderRepository supplierOrderRepository;
    private final LegacySupplyClient legacySupplyClient;
    private final ApplicationEventPublisher eventPublisher;

    DeliveryTrackingScheduler(
            SupplierOrderRepository supplierOrderRepository,
            LegacySupplyClient legacySupplyClient,
            ApplicationEventPublisher eventPublisher) {
        this.supplierOrderRepository = supplierOrderRepository;
        this.legacySupplyClient = legacySupplyClient;
        this.eventPublisher = eventPublisher;
    }

    @Scheduled(fixedDelayString = "${app.supplier.tracking-poll-interval-ms:45000}")
    @Transactional
    void pollOpenOrders() {
        List<SupplierOrderEntity> open = supplierOrderRepository.findByStatusIn(OPEN_STATUSES);
        for (SupplierOrderEntity order : open) {
            if (order.getPoNumber() == null) {
                continue;
            }
            legacySupplyClient.fetchStatusCode(order.getPoNumber()).ifPresent(code -> applyStatus(order, code));
        }
    }

    private void applyStatus(SupplierOrderEntity order, int legacyStatusCode) {
        SupplierOrderStatus mapped = mapStatusCode(legacyStatusCode);
        if (mapped == null) {
            // Undocumented/unexpected status code from LegacySupply: logged and
            // left unchanged rather than guessed at. Documented in INTEGRATION.md.
            log.warn("Unexpected LegacySupply status code {} for PO {}", legacyStatusCode, order.getPoNumber());
            return;
        }

        if (mapped == order.getStatus()) {
            return;
        }

        order.updateStatus(mapped);
        supplierOrderRepository.save(order);

        if (mapped == SupplierOrderStatus.DELIVERED) {
            eventPublisher.publishEvent(
                    new SupplierOrderDelivered(order.getProductId(), order.getUnits(), order.getPoNumber()));
        }
    }

    private SupplierOrderStatus mapStatusCode(int code) {
        return switch (code) {
            case 10 -> SupplierOrderStatus.SUBMITTED;
            case 20 -> SupplierOrderStatus.PICKING;
            case 30 -> SupplierOrderStatus.SHIPPED;
            case 40 -> SupplierOrderStatus.DELIVERED;
            default -> null;
        };
    }
}
