package com.ujenzilink.ujenzilink_backend.auth.admin.enums;

public enum AdminActionType {
    // User Management
    VERIFY_USER,
    REVERT_USER_DELETION,
    
    // Resource Management
    BULK_DELETE_RESOURCES,
    
    // Legal Management
    UPDATE_TERMS_AND_CONDITIONS,
    UPDATE_PRIVACY_POLICY,
    
    // Auth related
    LOGIN_SUCCESS,
    LOGIN_FAILURE
}
