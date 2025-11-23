package com.teammate.model;

/**
 * Enumeration of gaming/sports roles
 */
public enum Role {
    STRATEGIST("Strategist", "Focuses on tactics and planning"),
    ATTACKER("Attacker", "Frontline player with offensive tactics"),
    DEFENDER("Defender", "Protects and supports team stability"),
    SUPPORTER("Supporter", "Jack-of-all-trades, adapts roles"),
    COORDINATOR("Coordinator", "Communication lead, keeps team organized");

    private final String displayName;
    private final String description;

    Role(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }

    public static Role fromString(String role) {
        if (role == null) return null;

        for (Role r : Role.values()) {
            if (r.name().equalsIgnoreCase(role) ||
                    r.displayName.equalsIgnoreCase(role)) {
                return r;
            }
        }
        throw new IllegalArgumentException("Unknown role: " + role);
    }
}