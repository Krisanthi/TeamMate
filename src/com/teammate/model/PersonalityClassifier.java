package com.teammate.model;

import com.teammate.util.InvalidInputException;

/**
 * Utility class for classifying personality types based on scores
 */
public class PersonalityClassifier {

    private static final int MIN_SCORE = 50;
    private static final int MAX_SCORE = 100;

    /**
     * Classifies a personality score into a PersonalityType
     * @param score The personality score (50-100)
     * @return The corresponding PersonalityType
     */
    public static PersonalityType classify(int score) {
        if (!validateScore(score)) {
            throw new IllegalArgumentException(
                    "Invalid personality score: " + score + ". Must be between " +
                            MIN_SCORE + " and " + MAX_SCORE);
        }

        for (PersonalityType type : PersonalityType.values()) {
            if (type.isInRange(score)) {
                return type;
            }
        }

        // Default fallback
        return PersonalityType.THINKER;
    }

    /**
     * Calculates personality score from survey responses (1-5 each)
     * @param responses Array of 5 responses (each 1-5)
     * @return Scaled score (50-100)
     */
    public static int calculateScore(int[] responses) throws InvalidInputException {
        if (responses == null || responses.length != 5) {
            throw new InvalidInputException("Survey must have exactly 5 responses");
        }

        int total = 0;
        for (int i = 0; i < responses.length; i++) {
            if (responses[i] < 1 || responses[i] > 5) {
                throw new InvalidInputException(
                        "Response " + (i+1) + " must be between 1 and 5");
            }
            total += responses[i];
        }

        // Total range: 5-25, scale to 50-100
        // Formula: ((total - 5) / 20) * 50 + 50
        return (int) Math.round(((total - 5) / 20.0) * 50 + 50);
    }

    /**
     * Validates if a score is within acceptable range
     */
    public static boolean validateScore(int score) {
        return score >= MIN_SCORE && score <= MAX_SCORE;
    }

    /**
     * Gets the personality type description for a score
     */
    public static String getDescriptionForScore(int score) {
        PersonalityType type = classify(score);
        return type.getDescription();
    }
}