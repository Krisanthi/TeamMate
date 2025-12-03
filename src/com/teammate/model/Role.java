package com.teammate.model;

/**
 * Enumeration of gaming/sports roles
 */
public enum Role {
    STRATEGIST("Strategist", "Focuses on tactics and planning. Keeps the bigger picture in mind during gameplay."),
    ATTACKER("Attacker", "Frontline player. Good reflexes, offensive tactics, quick execution."),
    DEFENDER("Defender", "Protects and supports team stability. Good under pressure and team-focused."),
    SUPPORTER("Supporter", "Jack-of-all-trades. Adapts roles, ensures smooth coordination."),
    COORDINATOR("Coordinator", "Communication lead. Keeps the team informed and organized in real time.");

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
