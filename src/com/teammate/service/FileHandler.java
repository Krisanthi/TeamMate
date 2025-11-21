package com.teammate.service;

import com.teammate.model.*;
import com.teammate.util.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Handles file I/O operations for CSV files
 * Loads participants and saves formed teams
 */
public class FileHandler {
    private String inputFilePath;
    private String outputFilePath;
    private static final String CSV_DELIMITER = ",";

    /**
     * Constructor with file paths
     */
    public FileHandler(String inputFilePath, String outputFilePath) {
        this.inputFilePath = inputFilePath;
        this.outputFilePath = outputFilePath;
    }

    /**
     * Loads participants from CSV file
     * @return List of participants
     * @throws FileProcessingException if file cannot be read
     */
    public List<Participant> loadParticipants() throws FileProcessingException {
        List<Participant> participants = new ArrayList<>();

        try {
            validateCSV();

            BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
            String line;
            boolean isFirstLine = true;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                // Skip header
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                try {
                    Participant participant = parseParticipantLine(line, lineNumber);
                    if (participant != null) {
                        participants.add(participant);
                    }
                } catch (InvalidInputException e) {
                    Logger.logWarning("Skipping invalid line " + lineNumber + ": " + e.getMessage());
                    // Continue processing other lines
                }
            }

            reader.close();
            Logger.logInfo("Successfully loaded " + participants.size() + " participants from " + inputFilePath);

        } catch (FileNotFoundException e) {
            throw new FileProcessingException("File not found: " + inputFilePath, e);
        } catch (IOException e) {
            throw new FileProcessingException("Error reading file: " + inputFilePath, e);
        }

        if (participants.isEmpty()) {
            throw new FileProcessingException("No valid participants found in file");
        }

        return participants;
    }

    /**
     * Parses a single CSV line into a Participant object
     */
    private Participant parseParticipantLine(String line, int lineNumber) throws InvalidInputException {
        String[] fields = line.split(CSV_DELIMITER);

        if (fields.length < 8) {
            throw new InvalidInputException("Insufficient fields in line " + lineNumber);
        }

        try {
            String id = fields[0].trim();
            String name = fields[1].trim();
            String email = fields[2].trim();
            String preferredGame = fields[3].trim();
            int skillLevel = Integer.parseInt(fields[4].trim());
            Role preferredRole = Role.fromString(fields[5].trim());
            int personalityScore = Integer.parseInt(fields[6].trim());

            // Validate inputs
            if (id.isEmpty() || name.isEmpty()) {
                throw new InvalidInputException("ID and Name cannot be empty");
            }

            if (skillLevel < 1 || skillLevel > 10) {
                throw new InvalidInputException("Skill level must be between 1 and 10");
            }

            if (!PersonalityClassifier.validateScore(personalityScore)) {
                throw new InvalidInputException("Invalid personality score: " + personalityScore);
            }

            return new Participant(id, name, email, preferredGame, skillLevel,
                    preferredRole, personalityScore);

        } catch (NumberFormatException e) {
            throw new InvalidInputException("Invalid number format in line " + lineNumber);
        } catch (IllegalArgumentException e) {
            throw new InvalidInputException("Invalid enum value in line " + lineNumber + ": " + e.getMessage());
        }
    }

    /**
     * Saves formed teams to CSV file
     */
    public void saveTeams(List<Team> teams) throws FileProcessingException {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath));

            // Write header
            writer.write("TeamID,ParticipantID,Name,Email,PreferredGame,SkillLevel,Role,PersonalityType,PersonalityScore");
            writer.newLine();

            // Write team data
            for (Team team : teams) {
                for (Participant p : team.getMembers()) {
                    String line = String.format("%s,%s,%s,%s,%s,%d,%s,%s,%d",
                            team.getTeamId(),
                            p.getId(),
                            p.getName(),
                            p.getEmail(),
                            p.getPreferredGame(),
                            p.getSkillLevel(),
                            p.getPreferredRole(),
                            p.getPersonalityType(),
                            p.getPersonalityScore()
                    );
                    writer.write(line);
                    writer.newLine();
                }
            }

            writer.close();
            Logger.logInfo("Teams successfully saved to " + outputFilePath);

        } catch (IOException e) {
            throw new FileProcessingException("Error writing to file: " + outputFilePath, e);
        }
    }

    /**
     * Validates CSV file exists and is readable
     */
    public boolean validateCSV() throws FileProcessingException {
        File file = new File(inputFilePath);

        if (!file.exists()) {
            throw new FileProcessingException("File does not exist: " + inputFilePath);
        }

        if (!file.canRead()) {
            throw new FileProcessingException("File is not readable: " + inputFilePath);
        }

        if (!inputFilePath.toLowerCase().endsWith(".csv")) {
            throw new FileProcessingException("File must be a CSV file: " + inputFilePath);
        }

        return true;
    }

    // Getters and Setters
    public String getInputFilePath() { return inputFilePath; }
    public void setInputFilePath(String inputFilePath) { this.inputFilePath = inputFilePath; }

    public String getOutputFilePath() { return outputFilePath; }
    public void setOutputFilePath(String outputFilePath) { this.outputFilePath = outputFilePath; }
}