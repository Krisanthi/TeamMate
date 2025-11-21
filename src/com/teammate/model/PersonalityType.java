package com.teammate.model;

/**
 * Enumeration of personality types based on survey scores
 */
public enum PersonalityType {
    LEADER(90, 100, "Leader", "Confident, decision-maker, naturally takes charge"),
    BALANCED(70, 89, "Balanced", "Adaptive, communicative, team-oriented"),
    THINKER(50, 69, "Thinker", "Observant, analytical, prefers planning before action");

    private final int minScore;
    private final int maxScore;
    private final String displayName;
    private final String description;

    PersonalityType(int minScore, int maxScore, String displayName, String description) {
        this.minScore = minScore;
        this.maxScore = maxScore;
        this.displayName = displayName;
        this.description = description;
    }

    public int getMinScore() { return minScore; }
    public int getMaxScore() { return maxScore; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }

    /**
     * Checks if a score falls within this personality type's range
     */
    public boolean isInRange(int score) {
        return score >= minScore && score <= maxScore;
    }

    /**
     * Parses string to PersonalityType enum
     */
    public static PersonalityType fromString(String type) {
        if (type == null) return null;

        for (PersonalityType pt : PersonalityType.values()) {
            if (pt.name().equalsIgnoreCase(type) ||
                    pt.displayName.equalsIgnoreCase(type)) {
                return pt;
            }
        }
        throw new IllegalArgumentException("Unknown personality type: " + type);
    }
}
