package com.ujenzilink.ujenzilink_backend.projects.enums;

public enum PlanPurchaseStatus {
    /** Payment initiated but not yet confirmed */
    PENDING,

    /** Payment confirmed; user has full access */
    COMPLETED,

    /** Payment was reversed / refunded */
    REFUNDED
}
