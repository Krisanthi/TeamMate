package com.teammate.service;

import com.teammate.model.*;
import com.teammate.util.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Service for managing teams
 * CORRECTED VERSION: Auto-export removed - manual export only
 */
public class TeamService {

    private List<Team> teams;
    private Map<String, String> participantToTeam;
    private static final String TEAMS_CSV = "formed_teams.csv";

    public TeamService() {
        this.teams = new ArrayList<>();
        this.participantToTeam = new HashMap<>();
        Logger.logInfo("TeamService initialized");
    }

    /**
     * Generates teams using concurrent processing
     * CORRECTED: Auto-export removed - user must manually export
     */
    public List<Team> generateTeams(List<Participant> participants, int teamSize)
            throws InterruptedException, ExecutionException {

        if (participants == null || participants.isEmpty()) {
            throw new IllegalArgumentException("Participant list cannot be empty");
        }

        teams.clear();
        participantToTeam.clear();

        TeamBuilder teamBuilder = new TeamBuilder(participants, teamSize);

        // Create thread pool for concurrent processing
        ExecutorService executor = Executors.newFixedThreadPool(3);

        try {
            Future<List<Team>> futureTeams = executor.submit(() -> teamBuilder.formTeams());
            teams = futureTeams.get(30, TimeUnit.SECONDS);

            // Build participant-to-team mapping
            for (Team team : teams) {
                for (Participant p : team.getMembers()) {
                    participantToTeam.put(p.getId(), team.getTeamId());
                }
            }

            // REMOVED: Auto-export code (lines 52-57 deleted)
            // User must manually export using menu option

            Logger.logInfo("Generated " + teams.size() + " teams");

        } catch (TimeoutException e) {
            throw new RuntimeException("Team generation timeout", e);
        } finally {
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }

        return new ArrayList<>(teams);
    }

    public List<Team> getAllTeams() {
        return new ArrayList<>(teams);
    }

    public Team getTeamByParticipant(String participantId) {
        String teamId = participantToTeam.get(participantId);
        if (teamId == null) return null;

        for (Team team : teams) {
            if (team.getTeamId().equals(teamId)) return team;
        }
        return null;
    }

    public void exportToCSV(List<Team> teamsToExport, String filePath) throws FileProcessingException {
        if (teamsToExport == null || teamsToExport.isEmpty()) {
            // If no teams, create empty file (clears old data)
            FileHandler fileHandler = new FileHandler("", filePath);
            fileHandler.saveTeams(new ArrayList<>());
            Logger.logInfo("Cleared teams CSV file");
            return;
        }

        FileHandler fileHandler = new FileHandler("", filePath);
        fileHandler.saveTeams(teamsToExport);
        Logger.logInfo("Exported " + teamsToExport.size() + " teams to " + filePath);
    }

    /**
     * Clears all teams and updates formed_teams.csv
     */
    public void clearAllTeams() {
        teams.clear();
        participantToTeam.clear();

        // Clear formed_teams.csv when teams are cleared
        try {
            exportToCSV(new ArrayList<>(), TEAMS_CSV);
        } catch (FileProcessingException e) {
            Logger.logWarning("Failed to clear teams CSV: " + e.getMessage());
        }

        Logger.logInfo("All teams cleared");
    }
}