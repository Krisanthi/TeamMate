package com.teammate.model;

/**
 * PersonalityType - Enumeration of Personality Classifications
 *
 * Based on personality survey scores (50-100), participants are classified into
 * three personality types that influence team dynamics and balance.
 *
 * Classification Ranges:
 * - LEADER: 90-100 (Natural leaders, decision-makers)
 * - BALANCED: 70-89 (Adaptive, communicative, team-oriented)
 * - THINKER: 50-69 (Analytical, observant, strategic planners)
 *
 * Team Formation Rules (Matching Strategy):
 * - Each team should have 1 Leader
 * - Each team should have 1-2 Thinkers
 * - Remaining members should be Balanced types
 *
 * @author Krisanthi Segar 2425596
 * @version 1.0
 * @since 2025
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
     * @param score The personality score to check
     * @return true if score is within range
     */
    public boolean isInRange(int score) {
        return score >= minScore && score <= maxScore;
    }

    /**
     * Converts string to PersonalityType
     * @param type The string representation
     * @return The matching PersonalityType
     */
    public static PersonalityType fromString(String type) {
        if (type == null) return null;

        for (PersonalityType pt : PersonalityType.values()) {
            if (pt.name().equalsIgnoreCase(type) || pt.displayName.equalsIgnoreCase(type)) {
                return pt;
            }
        }
        throw new IllegalArgumentException("Unknown personality type: " + type);
    }
}