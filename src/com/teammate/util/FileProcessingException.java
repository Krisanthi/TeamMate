package com.teammate.util;

/**
 * Custom exception for file processing errors
 */
public class FileProcessingException extends Exception {

    /**
     * Constructor with message
     */
    public FileProcessingException(String message) {
        super(message);
    }

    /**
     * Constructor with message and cause
     */
    public FileProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}