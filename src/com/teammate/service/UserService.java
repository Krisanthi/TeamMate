package com.teammate.service;

import com.teammate.model.*;
import com.teammate.util.*;
import java.util.*;

/**
 * Service for managing participants
 * Uses LinkedHashMap to maintain insertion order automatically
 */
public class UserService {

    private Map<String, Participant> participants;
    private String csvFilePath;
    private int nextIdNumber;

    public UserService(String csvFilePath) {
        this.participants = new LinkedHashMap<>();
        this.csvFilePath = csvFilePath;
        this.nextIdNumber = 101;
        Logger.logInfo("UserService initialized");
    }

    /**
     * Registers new participant with auto-generated ID
     */
    public Participant registerParticipant(String name, String email, String preferredGame,
                                           int skillLevel, Role preferredRole, int personalityScore)
            throws InvalidInputException {
        String newId = generateNextId();

        Participant participant = new Participant(newId, name, email, preferredGame,
                skillLevel, preferredRole, personalityScore);

        participants.put(newId, participant);

        try {
            saveAllToCSV();
        } catch (FileProcessingException e) {
            Logger.logError("Failed to save participant to CSV: " + e.getMessage());
        }

        Logger.logInfo("Participant registered: " + newId);
        return participant;
    }

    private String generateNextId() {
        while (participants.containsKey("P" + String.format("%03d", nextIdNumber))) {
            nextIdNumber++;
        }
        return "P" + String.format("%03d", nextIdNumber++);
    }

    public Participant getParticipant(String id) {
        return participants.get(id);
    }

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

    public boolean deleteParticipant(String id) {
        Participant removed = participants.remove(id);

        if (removed != null) {
            // Update participants CSV
            try {
                saveAllToCSV();
            } catch (FileProcessingException e) {
                Logger.logError("Failed to update CSV after deletion: " + e.getMessage());
            }

            Logger.logInfo("Participant deleted: " + id);
            return true;
        }
        return false;
    }

    /**
     * Gets all participants in insertion order (LinkedHashMap maintains order)
     */
    public List<Participant> getAllParticipants() {
        return new ArrayList<>(participants.values());
    }

    public int loadFromCSV(String filePath) throws FileProcessingException {
        FileHandler fileHandler = new FileHandler(filePath, "");
        List<Participant> loaded = fileHandler.loadParticipants();

        for (Participant p : loaded) {
            participants.put(p.getId(), p);

            // Update nextIdNumber
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

        Logger.logInfo("Loaded " + loaded.size() + " participants. Next ID: P" +
                String.format("%03d", nextIdNumber));
        return loaded.size();
    }

    private void saveAllToCSV() throws FileProcessingException {
        FileHandler fileHandler = new FileHandler("", csvFilePath);
        List<Participant> allParticipants = new ArrayList<>(participants.values());

        fileHandler.saveParticipants(allParticipants, csvFilePath);
        Logger.logInfo("Saved " + allParticipants.size() + " participants to CSV");
    }
}