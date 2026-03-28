package com.ujenzilink.ujenzilink_backend.projects.models;

import com.ujenzilink.ujenzilink_backend.auth.models.User;
import com.ujenzilink.ujenzilink_backend.projects.enums.PlanPurchaseStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "project_plan_purchases",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_plan_purchase_user_plan", columnNames = {"plan_id", "buyer_id"})
        }
)
public class ProjectPlanPurchase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private ProjectPlan plan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private User buyer;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amountPaid;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(length = 255)
    private String paymentReference;

    @Column(length = 50)
    private String paymentProvider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PlanPurchaseStatus status = PlanPurchaseStatus.PENDING;

    @Column(nullable = false)
    private boolean isManualGrant = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant purchasedAt;

    private Instant completedAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    public ProjectPlanPurchase() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public ProjectPlan getPlan() {
        return plan;
    }

    public void setPlan(ProjectPlan plan) {
        this.plan = plan;
    }

    public User getBuyer() {
        return buyer;
    }

    public void setBuyer(User buyer) {
        this.buyer = buyer;
    }

    public BigDecimal getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(BigDecimal amountPaid) {
        this.amountPaid = amountPaid;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    public String getPaymentProvider() {
        return paymentProvider;
    }

    public void setPaymentProvider(String paymentProvider) {
        this.paymentProvider = paymentProvider;
    }

    public PlanPurchaseStatus getStatus() {
        return status;
    }

    public void setStatus(PlanPurchaseStatus status) {
        this.status = status;
    }

    public boolean isManualGrant() {
        return isManualGrant;
    }

    public void setManualGrant(boolean manualGrant) {
        isManualGrant = manualGrant;
    }

    public Instant getPurchasedAt() {
        return purchasedAt;
    }

    public void setPurchasedAt(Instant purchasedAt) {
        this.purchasedAt = purchasedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
