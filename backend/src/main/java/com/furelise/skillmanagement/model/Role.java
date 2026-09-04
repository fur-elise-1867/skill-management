package com.furelise.skillmanagement.model;

/**
 * Enum representing user roles in the system.
 */
public enum Role {
    USER,
    ADMIN;

    /**
     * Returns the Spring Security authority string with ROLE_ prefix.
     */
    public String toAuthority() {
        return "ROLE_" + this.name();
    }
}
