package com.teammate.service;

import com.teammate.model.*;
import com.teammate.util.*;
import java.io.*;
import java.util.*;

/**
 * FileHandler - CSV File Operations
 *
 * Handles reading and writing CSV files for participants and teams.
 *
 * @author Student Name
 * @version 1.0
 * @since 2025
 */
public class FileHandler {

    private String inputFilePath;
    private String outputFilePath;
    private static final String CSV_DELIMITER = ",";

    public FileHandler(String inputFilePath, String outputFilePath) {
        this.inputFilePath = inputFilePath;
        this.outputFilePath = outputFilePath;
    }

    /**
     * Loads participants from CSV file
     * @return List of participants
     * @throws FileProcessingException if file operation fails
     */
    public List<Participant> loadParticipants() throws FileProcessingException {
        List<Participant> participants = new ArrayList<>();
        BufferedReader reader = null;

        try {
            validateCSV();
            reader = new BufferedReader(new FileReader(inputFilePath));
            String line;
            boolean isFirstLine = true;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                if (line.trim().isEmpty()) continue;

                try {
                    Participant p = parseParticipantLine(line, lineNumber);
                    if (p != null) participants.add(p);
                } catch (InvalidInputException e) {
                    Logger.logWarning("Skipping line " + lineNumber);
                }
            }

        } catch (FileNotFoundException e) {
            throw new FileProcessingException("File not found: " + inputFilePath, e);
        } catch (IOException e) {
            throw new FileProcessingException("Error reading file", e);
        } finally {
            if (reader != null) {
                try { reader.close(); } catch (IOException e) {}
            }
        }

        if (participants.isEmpty()) {
            throw new FileProcessingException("No valid participants found");
        }

        return participants;
    }

    /**
     * Parses a CSV line into a Participant object
     * @param line The CSV line
     * @param lineNumber The line number
     * @return Participant object
     * @throws InvalidInputException if line is invalid
     */
    private Participant parseParticipantLine(String line, int lineNumber) throws InvalidInputException {
        String[] fields = line.split(CSV_DELIMITER, -1);

        if (fields.length < 8) {
            throw new InvalidInputException("Insufficient fields");
        }

        try {
            String id = fields[0].trim();
            String name = fields[1].trim();
            String email = fields[2].trim();
            String preferredGame = fields[3].trim();
            int skillLevel = Integer.parseInt(fields[4].trim());
            Role preferredRole = Role.fromString(fields[5].trim());
            int personalityScore = Integer.parseInt(fields[6].trim());

            if (!ValidationUtils.isValidParticipantId(id) ||
                    !ValidationUtils.isValidSkillLevel(skillLevel) ||
                    !PersonalityClassifier.validateScore(personalityScore)) {
                throw new InvalidInputException("Invalid data");
            }

            return new Participant(id, name, email, preferredGame, skillLevel,
                    preferredRole, personalityScore);

        } catch (NumberFormatException e) {
            throw new InvalidInputException("Invalid number format");
        }
    }

    /**
     * Saves participants to CSV file
     * @param participants List of participants
     * @param filePath Output file path
     * @throws FileProcessingException if write fails
     */
    public void saveParticipants(List<Participant> participants, String filePath)
            throws FileProcessingException {
        BufferedWriter writer = null;

        try {
            writer = new BufferedWriter(new FileWriter(filePath));

            writer.write("ID,Name,Email,PreferredGame,SkillLevel,PreferredRole,PersonalityScore,PersonalityType");
            writer.newLine();

            for (Participant p : participants) {
                String line = String.format("%s,%s,%s,%s,%d,%s,%d,%s",
                        p.getId(), p.getName(), p.getEmail(), p.getPreferredGame(),
                        p.getSkillLevel(), p.getPreferredRole(),
                        p.getPersonalityScore(), p.getPersonalityType());
                writer.write(line);
                writer.newLine();
            }

            Logger.logInfo("Saved " + participants.size() + " participants");

        } catch (IOException e) {
            throw new FileProcessingException("Error writing file", e);
        } finally {
            if (writer != null) {
                try { writer.close(); } catch (IOException e) {}
            }
        }
    }

    /**
     * Saves teams to CSV file (overwrites existing)
     * @param teams List of teams
     * @throws FileProcessingException if write fails
     */
    public void saveTeams(List<Team> teams) throws FileProcessingException {
        BufferedWriter writer = null;

        try {
            writer = new BufferedWriter(new FileWriter(outputFilePath, false));

            writer.write("TeamID,ParticipantID,Name,Email,PreferredGame,SkillLevel,Role,PersonalityType,PersonalityScore");
            writer.newLine();

            for (Team team : teams) {
                for (Participant p : team.getMembers()) {
                    String line = String.format("%s,%s,%s,%s,%s,%d,%s,%s,%d",
                            team.getTeamId(), p.getId(), p.getName(), p.getEmail(),
                            p.getPreferredGame(), p.getSkillLevel(), p.getPreferredRole(),
                            p.getPersonalityType(), p.getPersonalityScore());
                    writer.write(line);
                    writer.newLine();
                }
            }

            Logger.logInfo("Saved " + teams.size() + " teams");

        } catch (IOException e) {
            throw new FileProcessingException("Error writing teams", e);
        } finally {
            if (writer != null) {
                try { writer.close(); } catch (IOException e) {}
            }
        }
    }

    /**
     * Validates CSV file exists and is readable
     * @return true if valid
     * @throws FileProcessingException if validation fails
     */
    public boolean validateCSV() throws FileProcessingException {
        if (inputFilePath == null || inputFilePath.trim().isEmpty()) {
            throw new FileProcessingException("File path is empty");
        }

        File file = new File(inputFilePath);

        if (!file.exists()) {
            throw new FileProcessingException("File does not exist: " + inputFilePath);
        }
        if (!file.canRead()) {
            throw new FileProcessingException("File is not readable");
        }
        if (!inputFilePath.toLowerCase().endsWith(".csv")) {
            throw new FileProcessingException("File must be CSV");
        }
        if (file.length() == 0) {
            throw new FileProcessingException("File is empty");
        }

        return true;
    }
}