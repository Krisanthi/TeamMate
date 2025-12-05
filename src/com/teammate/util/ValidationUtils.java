package com.teammate.util;

import java.util.regex.Pattern;

/**
 * ValidationUtils - Input Validation Utility Class
 *
 * Provides static methods for validating user inputs throughout the application.
 * Ensures data integrity and prevents invalid data from entering the system.
 *
 * @author Student Name
 * @version 1.0
 * @since 2025
 */
public class ValidationUtils {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    /**
     * Validates email address format
     * @param email The email address to validate
     * @return true if email is valid format, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validates participant ID format (P + 3 or more digits)
     * @param id The participant ID to validate
     * @return true if ID matches required format, false otherwise
     */
    public static boolean isValidParticipantId(String id) {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }
        return id.matches("P\\d{3,}");
    }

    /**
     * Validates skill level is within range 1-10
     * @param skill The skill level to validate
     * @return true if skill is between 1-10 inclusive, false otherwise
     */
    public static boolean isValidSkillLevel(int skill) {
        return skill >= 1 && skill <= 10;
    }

    /**
     * Validates team size (3-10 members, minimum 2 teams possible)
     * @param size The proposed team size
     * @param totalParticipants The total number of participants
     * @return true if team size is valid, false otherwise
     */
    public static boolean isValidTeamSize(int size, int totalParticipants) {
        if (size < 3 || size > 10) {
            return false;
        }
        return totalParticipants / size >= 2;
    }

    /**
     * Checks if a string is not empty or null
     * @param str The string to check
     * @return true if string contains non-whitespace characters
     */
    public static boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }

    /**
     * Sanitizes user input by removing potentially harmful characters
     * @param input The input string to sanitize
     * @return Sanitized string
     */
    public static String sanitizeInput(String input) {
        if (input == null) {
            return "";
        }
        return input.trim().replaceAll("[<>\"']", "");
    }
}