package edu.cit.laurino.supplier;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "supplier_orders")
class SupplierOrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false, length = 50)
    private String productId;

    @Column(name = "buyer_ref", nullable = false, unique = true, length = 40)
    private String buyerRef;

    @Column(name = "request_id", nullable = false, length = 80)
    private String requestId;

    @Column(name = "po_number", length = 50)
    private String poNumber;

    @Column(nullable = false)
    private int cases;

    @Column(nullable = false)
    private int units;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SupplierOrderStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected SupplierOrderEntity() {
        // Required by JPA.
    }

    SupplierOrderEntity(String productId, String buyerRef, String requestId, int cases, int units) {
        this.productId = productId;
        this.buyerRef = buyerRef;
        this.requestId = requestId;
        this.cases = cases;
        this.units = units;
        this.status = SupplierOrderStatus.PENDING;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    void markSubmitted(String poNumber, int legacyStatusCode) {
        this.poNumber = poNumber;
        this.status = SupplierOrderStatus.SUBMITTED;
    }

    void markFailed(String reason) {
        this.status = SupplierOrderStatus.FAILED;
    }

    void updateStatus(SupplierOrderStatus newStatus) {
        this.status = newStatus;
    }

    Long getId() {
        return id;
    }

    String getProductId() {
        return productId;
    }

    String getBuyerRef() {
        return buyerRef;
    }

    String getRequestId() {
        return requestId;
    }

    String getPoNumber() {
        return poNumber;
    }

    int getCases() {
        return cases;
    }

    int getUnits() {
        return units;
    }

    SupplierOrderStatus getStatus() {
        return status;
    }
}
