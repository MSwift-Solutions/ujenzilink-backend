package com.ujenzilink.ujenzilink_backend.notifications.enums;

public enum NotificationType {
    // Posts
    POST_LIKE,
    POST_COMMENT,
    POST_COMMENT_REPLY,

    // Projects
    PROJECT_LIKE,
    PROJECT_COMMENT,
    PROJECT_COMMENT_REPLY,
    PROJECT_FOLLOW,
    PROJECT_MEMBER_ADDED,
    PROJECT_STAGE_UPDATE,

    // Chats
    DIRECT_MESSAGE,
    GROUP_MESSAGE,
    CHAT_MENTION,

    // Authentication
    VERIFICATION_CODE,
    PASSWORD_RESET,
    SIGNUP_SUCCESS,
    SIGNIN_SUCCESS,

    // System
    SYSTEM_ANNOUNCEMENT,
    ACCOUNT_SECURITY
}
