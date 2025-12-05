package com.teammate.service;

import com.teammate.model.*;
import com.teammate.util.*;
import java.util.*;

/**
 * UserService - Participant Management Service
 *
 * FIXED VERSION: Auto-loads existing participants from CSV on startup
 * This prevents new registrations from overwriting existing data.
 *
 * Handles CRUD operations for participants with auto-generated IDs.
 * Uses LinkedHashMap to maintain insertion order.
 *
 * @author Student Name
 * @version 1.1
 * @since 2025
 */
public class UserService {

    private Map<String, Participant> participants;
    private String csvFilePath;
    private int nextIdNumber;

    /**
     * Constructor - initializes UserService
     *
     * UPDATED APPROACH: Does NOT auto-load CSV on startup.
     * Instead, new participants are APPENDED to existing CSV file.
     * Organizer must manually load CSV to view all participants.
     *
     * @param csvFilePath Path to CSV file
     */
    public UserService(String csvFilePath) {
        this.participants = new LinkedHashMap<>();
        this.csvFilePath = csvFilePath;
        this.nextIdNumber = 101;

        // Scan CSV to determine next available ID (without loading all participants)
        try {
            java.io.File file = new java.io.File(csvFilePath);
            if (file.exists()) {
                updateNextIdFromCSV(csvFilePath);
                Logger.logInfo("UserService initialized. Next ID: P" +
                        String.format("%03d", nextIdNumber));
            } else {
                Logger.logInfo("UserService initialized - CSV not found, starting fresh");
            }
        } catch (Exception e) {
            Logger.logWarning("Could not scan CSV for next ID: " + e.getMessage());
        }
    }

    /**
     * Scans CSV file to determine next available participant ID
     * Does NOT load participants into memory - only checks IDs
     */
    private void updateNextIdFromCSV(String filePath) {
        try {
            java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.FileReader(filePath));

            String line;
            reader.readLine(); // Skip header

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length > 0) {
                    String id = parts[0].trim();
                    try {
                        String numStr = id.substring(1); // Remove 'P'
                        int num = Integer.parseInt(numStr);
                        if (num >= nextIdNumber) {
                            nextIdNumber = num + 1;
                        }
                    } catch (Exception e) {
                        // Ignore malformed IDs
                    }
                }
            }
            reader.close();
        } catch (Exception e) {
            Logger.logWarning("Error scanning CSV: " + e.getMessage());
        }
    }

    /**
     * Registers new participant with auto-generated ID
     *
     * UPDATED: Now APPENDS new participant to CSV file instead of overwriting.
     * Existing CSV data is preserved, new participant is added at the end.
     *
     * @param name Participant name
     * @param email Email address
     * @param preferredGame Preferred game
     * @param skillLevel Skill level (1-10)
     * @param preferredRole Preferred role
     * @param personalityScore Personality score (50-100)
     * @return The registered participant
     * @throws InvalidInputException if validation fails
     */
    public Participant registerParticipant(String name, String email, String preferredGame,
                                           int skillLevel, Role preferredRole, int personalityScore)
            throws InvalidInputException {
        String newId = generateNextId();

        Participant participant = new Participant(newId, name, email, preferredGame,
                skillLevel, preferredRole, personalityScore);

        participants.put(newId, participant);

        try {
            // APPEND to CSV instead of overwriting
            appendToCSV(participant);
            Logger.logInfo("Participant registered and appended to CSV: " + newId);
        } catch (FileProcessingException e) {
            Logger.logError("Failed to append participant to CSV: " + e.getMessage());
        }

        return participant;
    }

    /**
     * Appends a single participant to the CSV file
     * Does NOT overwrite existing data - adds new row at the end
     *
     * @param participant Participant to append
     * @throws FileProcessingException if append fails
     */
    private void appendToCSV(Participant participant) throws FileProcessingException {
        try {
            java.io.File file = new java.io.File(csvFilePath);
            boolean fileExists = file.exists() && file.length() > 0;

            java.io.FileWriter fw = new java.io.FileWriter(csvFilePath, true); // true = append mode
            java.io.BufferedWriter bw = new java.io.BufferedWriter(fw);

            // If file doesn't exist or is empty, write header first
            if (!fileExists) {
                bw.write("ParticipantID,Name,Email,PreferredGame,SkillLevel,PreferredRole," +
                        "PersonalityScore,PersonalityType");
                bw.newLine();
            }

            // Append participant data
            bw.write(String.format("%s,%s,%s,%s,%d,%s,%d,%s",
                    participant.getId(),
                    participant.getName(),
                    participant.getEmail(),
                    participant.getPreferredGame(),
                    participant.getSkillLevel(),
                    participant.getPreferredRole(),
                    participant.getPersonalityScore(),
                    participant.getPersonalityType()));
            bw.newLine();

            bw.close();
            fw.close();

            Logger.logInfo("Appended participant " + participant.getId() + " to CSV");

        } catch (Exception e) {
            throw new FileProcessingException("Failed to append to CSV: " + e.getMessage());
        }
    }

    /**
     * Generates next available participant ID
     *
     * Scans existing IDs to ensure no duplicates.
     * If P101-P200 exist, will generate P201.
     *
     * @return Next ID (e.g., P101, P102)
     */
    private String generateNextId() {
        while (participants.containsKey("P" + String.format("%03d", nextIdNumber))) {
            nextIdNumber++;
        }
        return "P" + String.format("%03d", nextIdNumber++);
    }

    /**
     * Gets participant by ID
     * @param id Participant ID
     * @return Participant or null if not found
     */
    public Participant getParticipant(String id) {
        return participants.get(id);
    }

    /**
     * Updates participant information
     *
     * Automatically saves all participants to CSV after update.
     *
     * @param participant Updated participant
     * @throws InvalidInputException if validation fails
     */
    public void updateParticipant(Participant participant) throws InvalidInputException {
        if (participant == null) {
            throw new InvalidInputException("Participant cannot be null");
        }

        if (!participants.containsKey(participant.getId())) {
            throw new InvalidInputException("Participant not found");
        }

        participants.put(participant.getId(), participant);

        try {
            saveAllToCSV();
        } catch (FileProcessingException e) {
            Logger.logError("Failed to update CSV: " + e.getMessage());
        }

        Logger.logInfo("Participant updated: " + participant.getId());
    }

    /**
     * Deletes participant
     *
     * Automatically saves remaining participants to CSV after deletion.
     *
     * @param id Participant ID
     * @return true if deleted, false if not found
     */
    public boolean deleteParticipant(String id) {
        Participant removed = participants.remove(id);

        if (removed != null) {
            try {
                saveAllToCSV();
            } catch (FileProcessingException e) {
                Logger.logError("Failed to update CSV after deletion: " + e.getMessage());
            }

            Logger.logInfo("Participant deleted: " + id + " (Total: " + participants.size() + ")");
            return true;
        }
        return false;
    }

    /**
     * Gets all participants in insertion order
     * @return List of all participants
     */
    public List<Participant> getAllParticipants() {
        return new ArrayList<>(participants.values());
    }

    /**
     * Loads participants from CSV file
     *
     * MERGES loaded participants with existing ones (does not replace).
     * Updates nextIdNumber to prevent ID conflicts.
     *
     * @param filePath CSV file path
     * @return Number of participants loaded
     * @throws FileProcessingException if load fails
     */
    public int loadFromCSV(String filePath) throws FileProcessingException {
        FileHandler fileHandler = new FileHandler(filePath, "");
        List<Participant> loaded = fileHandler.loadParticipants();

        for (Participant p : loaded) {
            participants.put(p.getId(), p);

            // Update nextIdNumber to prevent conflicts
            try {
                String numStr = p.getId().substring(1);
                int num = Integer.parseInt(numStr);
                if (num >= nextIdNumber) {
                    nextIdNumber = num + 1;
                }
            } catch (Exception e) {
                // Ignore malformed IDs
            }
        }

        Logger.logInfo("Loaded " + loaded.size() + " participants from CSV. Next ID: P" +
                String.format("%03d", nextIdNumber) + ". Total in memory: " + participants.size());
        return loaded.size();
    }

    /**
     * Saves all participants to CSV
     *
     * Overwrites CSV with current in-memory state.
     * Called automatically after register/update/delete operations.
     *
     * @throws FileProcessingException if save fails
     */
    private void saveAllToCSV() throws FileProcessingException {
        FileHandler fileHandler = new FileHandler("", csvFilePath);
        List<Participant> allParticipants = new ArrayList<>(participants.values());

        fileHandler.saveParticipants(allParticipants, csvFilePath);
        Logger.logInfo("Saved " + allParticipants.size() + " participants to CSV: " + csvFilePath);
    }
}