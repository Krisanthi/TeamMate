package com.teammate.service;

import com.teammate.model.*;
import com.teammate.util.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages participant data with auto-ID generation and CSV persistence
 */
public class UserService {

    private Map<String, Participant> participants;
    private String csvFilePath;
    private int nextIdNumber;

    public UserService(String csvFilePath) {
        this.participants = new ConcurrentHashMap<>();
        this.csvFilePath = csvFilePath;
        this.nextIdNumber = 101;
        Logger.logInfo("UserService initialized");
    }

    /**
     * Registers participant with auto-generated ID (P101, P102, etc.)
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
            Logger.logError("Failed to save to CSV: " + e.getMessage());
        }

        Logger.logInfo("Registered: " + newId);
        return participant;
    }

    private String generateNextId() {
        while (participants.containsKey("P" + String.format("%03d", nextIdNumber))) {
            nextIdNumber++;
        }
        return "P" + String.format("%03d", nextIdNumber++);
    }

    public boolean participantExists(String id) {
        return participants.containsKey(id);
    }

    public Participant getParticipant(String id) {
        return participants.get(id);
    }

    public void updateParticipant(Participant participant) throws InvalidInputException {
        if (participant == null || !participants.containsKey(participant.getId())) {
            throw new InvalidInputException("Participant not found");
        }

        participants.put(participant.getId(), participant);

        try {
            saveAllToCSV();
        } catch (FileProcessingException e) {
            Logger.logError("Failed to update CSV: " + e.getMessage());
        }

        Logger.logInfo("Updated: " + participant.getId());
    }

    public boolean deleteParticipant(String id) {
        Participant removed = participants.remove(id);

        if (removed != null) {
            try {
                saveAllToCSV();
            } catch (FileProcessingException e) {
                Logger.logError("Failed to update CSV: " + e.getMessage());
            }
            Logger.logInfo("Deleted: " + id);
            return true;
        }
        return false;
    }

    public List<Participant> getAllParticipants() {
        return new ArrayList<>(participants.values());
    }

    public int loadFromCSV(String filePath) throws FileProcessingException {
        FileHandler fileHandler = new FileHandler(filePath, "");
        List<Participant> loaded = fileHandler.loadParticipants();

        for (Participant p : loaded) {
            participants.put(p.getId(), p);

            // Update nextIdNumber based on existing IDs
            try {
                int num = Integer.parseInt(p.getId().substring(1));
                if (num >= nextIdNumber) {
                    nextIdNumber = num + 1;
                }
            } catch (Exception e) {
                // Ignore malformed IDs
            }
        }

        Logger.logInfo("Loaded " + loaded.size() + " participants");
        return loaded.size();
    }

    private void saveAllToCSV() throws FileProcessingException {
        FileHandler fileHandler = new FileHandler("", csvFilePath);
        List<Participant> allParticipants = new ArrayList<>(participants.values());
        allParticipants.sort(Comparator.comparing(Participant::getId));
        fileHandler.saveParticipants(allParticipants, csvFilePath);
    }

    public void clearAll() {
        participants.clear();
    }

    public int getParticipantCount() {
        return participants.size();
    }
}