package com.ujenzilink.ujenzilink_backend.projects.enums;

public enum PlanVisibility {
    /** Visible to everyone, purchase required to download files */
    PUBLIC,

    /** Visible only to project members who have also purchased */
    MEMBERS,

    /** Completely hidden; only the owner and admins can see it */
    PRIVATE
}
