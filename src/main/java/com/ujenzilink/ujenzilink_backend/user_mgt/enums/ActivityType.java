package com.ujenzilink.ujenzilink_backend.user_mgt.enums;

public enum ActivityType {
    LOGIN,
    CREATE_PROJECT,
    UPDATE_PROJECT,
    DELETE_PROJECT,

    // Project posts (ProjectStage posts)
    CREATE_PROJECT_POST,
    UPDATE_PROJECT_POST,
    DELETE_PROJECT_POST,
    LIKE_PROJECT_POST,
    UNLIKE_PROJECT_POST,

    // Normal/standalone posts
    CREATE_POST,
    UPDATE_POST,
    DELETE_POST,
    LIKE_POST,
    UNLIKE_POST,

    // Comments
    CREATE_COMMENT,
    UPDATE_COMMENT,
    DELETE_COMMENT,
    LIKE_COMMENT,
    UNLIKE_COMMENT,

    // Project follows
    LIKE_PROJECT,
    UNLIKE_PROJECT,
    FOLLOW_PROJECT,
    UNFOLLOW_PROJECT
}
