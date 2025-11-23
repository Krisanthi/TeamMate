package com.teammate.util;

/**
 * Custom exception for file processing errors
 */
public class FileProcessingException extends Exception {

    public FileProcessingException(String message) {
        super(message);
    }

    public FileProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
