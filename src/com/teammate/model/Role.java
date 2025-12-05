package com.teammate.model;

/**
 * Role - Enumeration of Gaming/Sports Roles
 *
 * Defines the five playing roles available in the TeamMate system.
 * Each role represents a different gameplay style and team function.
 *
 * Team Formation Rules:
 * - Each team should have at least 3 different roles
 *
 * @author Krisanthi Segar 2425596
 * @version 1.0
 * @since 2025
 */
public enum Role {

    STRATEGIST("Strategist",
            "Focuses on tactics and planning. Keeps the bigger picture in mind during gameplay."),

    ATTACKER("Attacker",
            "Frontline player. Good reflexes, offensive tactics, quick execution."),

    DEFENDER("Defender",
            "Protects and supports team stability. Good under pressure and team-focused."),

    SUPPORTER("Supporter",
            "Jack-of-all-trades. Adapts roles, ensures smooth coordination."),

    COORDINATOR("Coordinator",
            "Communication lead. Keeps the team informed and organized in real time.");

    private final String displayName;
    private final String description;

    Role(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }

    /**
     * Converts string to Role
     * @param role The string representation
     * @return The matching Role
     */
    public static Role fromString(String role) {
        if (role == null) return null;

        for (Role r : Role.values()) {
            if (r.name().equalsIgnoreCase(role) || r.displayName.equalsIgnoreCase(role)) {
                return r;
            }
        }
        throw new IllegalArgumentException("Unknown role: " + role);
    }
}