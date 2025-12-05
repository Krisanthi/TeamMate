package com.teammate.model;

import com.teammate.util.InvalidInputException;

/**
 * PersonalityClassifier - Personality Type Classification System
 *
 * Classifies personality types based on survey scores.
 * Implements scoring algorithm: Total (5-25) × 4 = Score (20-100)
 * Valid range enforced: 50-100
 *
 * @author Student Name
 * @version 1.0
 * @since 2025
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

        return PersonalityType.THINKER;
    }

    /**
     * Calculates personality score from survey responses
     * Formula: (Q1 + Q2 + Q3 + Q4 + Q5) × 4 = Score (20-100)
     *
     * @param responses Array of 5 responses (each 1-5)
     * @return Scaled score (20-100, but enforced minimum is 50)
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

        // Total range: 5-25, multiply by 4 to scale to 20-100
        int score = total * 4;

        // Enforce minimum score of 50
        return Math.max(score, MIN_SCORE);
    }

    /**
     * Validates that a score is within acceptable range
     * @param score The score to validate
     * @return true if valid, false otherwise
     */
    public static boolean validateScore(int score) {
        return score >= MIN_SCORE && score <= MAX_SCORE;
    }

    /**
     * Gets description for a given score
     * @param score The personality score
     * @return Description of the personality type
     */
    public static String getDescriptionForScore(int score) {
        PersonalityType type = classify(score);
        return type.getDescription();
    }
}