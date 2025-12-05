package com.teammate.util;

/**
 * InvalidInputException - Custom Exception for Invalid User Input
 *
 * Thrown when user input fails validation checks
 *
 * @author Krisanthi Segar 2425596
 * @version 1.0
 * @since 2025
 */
public class InvalidInputException extends Exception {

    /**
     * Constructs exception with detail message
     * @param message The detail message
     */
    public InvalidInputException(String message) {
        super(message);
    }

    /**
     * Constructs exception with message and cause
     * @param message The detail message
     * @param cause The underlying exception
     */
    public InvalidInputException(String message, Throwable cause) {
        super(message, cause);
    }
}