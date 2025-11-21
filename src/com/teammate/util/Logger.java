package com.teammate.util;

import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * Simple logging utility for application events
 * Logs to both console and file
 */
public class Logger {

    private static final String LOG_FILE = "teammate_application.log";
    private static final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Logs an informational message
     */
    public static void logInfo(String message) {
        log("INFO", message);
    }

    /**
     * Logs a warning message
     */
    public static void logWarning(String message) {
        log("WARNING", message);
    }

    /**
     * Logs an error message
     */
    public static void logError(String message) {
        log("ERROR", message);
    }

    /**
     * Logs a debug message
     */
    public static void logDebug(String message) {
        log("DEBUG", message);
    }

    /**
     * Core logging method
     */
    private static void log(String level, String message) {
        String timestamp = LocalDateTime.now().format(formatter);
        String logMessage = String.format("[%s] [%s] %s", timestamp, level, message);

        // Write to file
        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(LOG_FILE, true))) {
            writer.write(logMessage);
            writer.newLine();
        } catch (IOException e) {
            // If logging fails, print to console only
            System.err.println("Failed to write to log file: " + e.getMessage());
        }

        // Also print to console for immediate feedback
        if (level.equals("ERROR")) {
            System.err.println(logMessage);
        } else {
            // Only log non-user-facing messages to avoid clutter
            // System.out.println(logMessage);
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
     * Logs exception with stack trace
     */
    public static void logException(String message, Exception e) {
        logError(message + ": " + e.getMessage());

        // Write stack trace to log file
        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(LOG_FILE, true))) {
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
