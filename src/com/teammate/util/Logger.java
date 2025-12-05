package com.teammate.util;

import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * Logger - Application Event Logging Utility
 *
 * Logs application events to file with timestamps and severity levels.
 * Supports INFO, WARNING, and ERROR levels for categorizing events.
 *
 * @author Krisanthi Segar 2425596
 * @version 1.0
 * @since 2025
 */
public class Logger {

    private static final String LOG_FILE = "teammate_application.log";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Logs an informational message
     * @param message The message to log
     */
    public static void logInfo(String message) {
        log("INFO", message);
    }

    /**
     * Logs a warning message
     * @param message The warning message
     */
    public static void logWarning(String message) {
        log("WARNING", message);
    }

    /**
     * Logs an error message
     * @param message The error message
     */
    public static void logError(String message) {
        log("ERROR", message);
    }

    /**
     * Core logging method that writes to file
     * @param level The log level
     * @param message The message to log
     */
    private static void log(String level, String message) {
        String timestamp = LocalDateTime.now().format(formatter);
        String logMessage = String.format("[%s] [%s] %s", timestamp, level, message);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE, true))) {
            writer.write(logMessage);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Failed to write to log file: " + e.getMessage());
        }
    }

    /**
     * Clears the log file
     */
    public static void clearLog() {
        try {
            new FileWriter(LOG_FILE, false).close();
            logInfo("Log file cleared");
        } catch (IOException e) {
            System.err.println("Failed to clear log file: " + e.getMessage());
        }
    }

    /**
     * Logs exception with full stack trace
     * @param message Contextual message
     * @param e The exception
     */
    public static void logException(String message, Exception e) {
        logError(message + ": " + e.getMessage());

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE, true))) {
            writer.write("Stack Trace:");
            writer.newLine();

            for (StackTraceElement element : e.getStackTrace()) {
                writer.write("  at " + element.toString());
                writer.newLine();
            }
            writer.newLine();
        } catch (IOException ioException) {
            System.err.println("Failed to write stack trace to log file");
        }
    }
}