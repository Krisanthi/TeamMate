package com.teammate.util;

/**
 * Custom exception for invalid user inputs
 */
public class InvalidInputException extends Exception {

    /**
     * Constructor with message
     */
    public InvalidInputException(String message) {
        super(message);
    }

    /**
     * Constructor with message and cause
     */
    public InvalidInputException(String message, Throwable cause) {
        super(message, cause);
    }
}