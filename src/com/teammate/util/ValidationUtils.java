package com.teammate.util;

import java.util.regex.Pattern;

/**
 * Utility class for input validation
 */
public class ValidationUtils {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    /**
     * Validates email format
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validates participant ID format
     */
    public static boolean isValidParticipantId(String id) {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }
        // ID should start with P and followed by digits
        return id.matches("P\\d{3,}");
    }

    /**
     * Validates skill level (1-10)
     */
    public static boolean isValidSkillLevel(int skill) {
        return skill >= 1 && skill <= 10;
    }

    /**
     * Validates team size (3-10)
     */
    public static boolean isValidTeamSize(int size, int totalParticipants) {
        if (size < 3 || size > 10) {
            return false;
        }
        // Must be able to form at least 2 teams
        return totalParticipants / size >= 2;
    }

    /**
     * Validates that a string is not null or empty
     */
    public static boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }

    /**
     * Sanitizes string input by trimming and removing special characters
     */
    public static String sanitizeInput(String input) {
        if (input == null) {
            return "";
        }
        return input.trim().replaceAll("[<>\"']", "");
    }
}