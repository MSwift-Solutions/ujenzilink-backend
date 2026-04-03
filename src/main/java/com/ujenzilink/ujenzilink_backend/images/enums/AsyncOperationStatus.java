package com.ujenzilink.ujenzilink_backend.images.enums;

public enum AsyncOperationStatus {
    /** Initial state when the async task throws an exception. */
    FAILED,

    /** Admin has triggered at least one retry but it has not succeeded yet. */
    RETRIED,

    /** Operation completed successfully after admin retry. */
    RESOLVED
}
