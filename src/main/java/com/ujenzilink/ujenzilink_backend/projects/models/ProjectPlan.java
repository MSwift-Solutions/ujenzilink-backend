package com.ujenzilink.ujenzilink_backend.projects.models;

import com.ujenzilink.ujenzilink_backend.auth.models.User;
import com.ujenzilink.ujenzilink_backend.projects.enums.PlanVisibility;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Represents a sellable/shareable plan package attached to a project.
 *
 * <p>Access rules:
 * <ul>
 *   <li>{@link PlanVisibility#PUBLIC}  – anyone can discover; only paid users can download files.</li>
 *   <li>{@link PlanVisibility#MEMBERS} – only project members who have also purchased can access.</li>
 *   <li>{@link PlanVisibility#PRIVATE} – hidden; only the owner and admins can see it.</li>
 * </ul>
 *
 * <p>Whether a user has paid is tracked via {@link ProjectPlanPurchase}.
 * The actual files are stored in {@link ProjectPlanFile}.
 */
@Entity
@Table(name = "project_plans")
public class ProjectPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // ─── Core identity ──────────────────────────────────────────────────────

    /** Human-readable title of this plan package (e.g. "Full Architectural Package"). */
    @Column(nullable = false, length = 255)
    private String name;

    /** Optional description / summary of what is included in this plan. */
    @Column(length = 2000)
    private String description;

    // ─── Pricing ────────────────────────────────────────────────────────────

    /**
     * Price a buyer must pay to gain access to the plan files.
     * A value of 0 means the plan is free but a purchase record is still created.
     */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    /** ISO-4217 currency code, e.g. "KES", "USD". */
    @Column(nullable = false, length = 3)
    private String currency = "KES";

    // ─── Visibility & access ─────────────────────────────────────────────────

    /**
     * Controls who can discover and access this plan.
     * Defaults to {@link PlanVisibility#PRIVATE} until explicitly published.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PlanVisibility visibility = PlanVisibility.PRIVATE;

    /**
     * When {@code true}, users must complete a purchase (or have a free-access grant)
     * before they can view or download any {@link ProjectPlanFile} attached to this plan.
     * Defaults to {@code true}; set to {@code false} for open/preview plans.
     */
    @Column(nullable = false)
    private boolean requiresPurchase = true;

    // ─── Relationships ───────────────────────────────────────────────────────

    /** The project this plan belongs to. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    /** The user who created / owns this plan. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;

    // ─── Lifecycle ───────────────────────────────────────────────────────────

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    /** Soft-delete flag; deleted plans are excluded from all queries by default. */
    @Column(nullable = false)
    private boolean isDeleted = false;

    private Instant deletedAt;

    // ─── Constructors ────────────────────────────────────────────────────────

    public ProjectPlan() {
    }

    // ─── Getters & Setters ───────────────────────────────────────────────────

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public PlanVisibility getVisibility() {
        return visibility;
    }

    public void setVisibility(PlanVisibility visibility) {
        this.visibility = visibility;
    }

    public boolean isRequiresPurchase() {
        return requiresPurchase;
    }

    public void setRequiresPurchase(boolean requiresPurchase) {
        this.requiresPurchase = requiresPurchase;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }
}
