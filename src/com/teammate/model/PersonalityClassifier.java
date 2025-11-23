package com.teammate.model;

import com.teammate.util.InvalidInputException;

/**
 * Utility class for classifying personality types based on scores
 * Implements scoring algorithm: Total (5-25) * 4 = Score (50-100)
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

        return PersonalityType.THINKER; // Default fallback
    }

    /**
     * Calculates personality score from survey responses (1-5 each)
     * Formula: Total (5-25) * 4 = Score (50-100)
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

        // Total range: 5-25, multiply by 4 to scale to 50-100
        return total * 4;
    }

    public static boolean validateScore(int score) {
        return score >= MIN_SCORE && score <= MAX_SCORE;
    }

    public static String getDescriptionForScore(int score) {
        PersonalityType type = classify(score);
        return type.getDescription();
    }
}
