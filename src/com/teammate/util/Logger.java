package com.teammate.util;

import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * Simple logging utility for application events
 * Logs to both console and file for production monitoring
 */
public class Logger {

    private static final String LOG_FILE = "teammate_application.log";
    private static final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void logInfo(String message) {
        log("INFO", message);
    }

    public static void logWarning(String message) {
        log("WARNING", message);
    }

    public static void logError(String message) {
        log("ERROR", message);
    }

    public static void logDebug(String message) {
        log("DEBUG", message);
    }

    private static void log(String level, String message) {
        String timestamp = LocalDateTime.now().format(formatter);
        String logMessage = String.format("[%s] [%s] %s", timestamp, level, message);

        // Write to file
        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(LOG_FILE, true))) {
            writer.write(logMessage);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Failed to write to log file: " + e.getMessage());
        }
    }

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
     */
    public static void logException(String message, Exception e) {
        logError(message + ": " + e.getMessage());

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