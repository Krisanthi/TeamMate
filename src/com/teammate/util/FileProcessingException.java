package com.teammate.util;

/**
 * FileProcessingException - Custom Exception for File Operations
 *
 * Thrown when file operations fail (file not found, read/write errors, etc.)
 *
 * @author Student Name
 * @version 1.0
 * @since 2025
 */
public class FileProcessingException extends Exception {

    /**
     * Constructs exception with detail message
     * @param message The detail message
     */
    public FileProcessingException(String message) {
        super(message);
    }

    /**
     * Constructs exception with message and cause
     * @param message The detail message
     * @param cause The underlying exception
     */
    public FileProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}