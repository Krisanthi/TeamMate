package com.teammate.service;

import com.teammate.model.*;
import com.teammate.util.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * TeamService - Team Management Service
 *
 * Manages team generation and export operations.
 * Manual export only (no auto-export).
 *
 * @author Student Name
 * @version 1.0
 * @since 2025
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
     * User must manually export teams after generation
     *
     * @param participants List of participants
     * @param teamSize Desired team size
     * @return List of formed teams
     * @throws InterruptedException if thread interrupted
     * @throws ExecutionException if execution fails
     */
    public List<Team> generateTeams(List<Participant> participants, int teamSize)
            throws InterruptedException, ExecutionException {

        if (participants == null || participants.isEmpty()) {
            throw new IllegalArgumentException("Participant list cannot be empty");
        }

        teams.clear();
        participantToTeam.clear();

        TeamBuilder teamBuilder = new TeamBuilder(participants, teamSize);

        ExecutorService executor = Executors.newFixedThreadPool(3);

        try {
            Future<List<Team>> futureTeams = executor.submit(() -> teamBuilder.formTeams());
            teams = futureTeams.get(30, TimeUnit.SECONDS);

            for (Team team : teams) {
                for (Participant p : team.getMembers()) {
                    participantToTeam.put(p.getId(), team.getTeamId());
                }
            }

            Logger.logInfo("Generated " + teams.size() + " teams");

        } catch (TimeoutException e) {
            throw new RuntimeException("Team generation timeout", e);
        } finally {
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }

        return new ArrayList<>(teams);
    }

    /**
     * Gets all formed teams
     * @return List of teams
     */
    public List<Team> getAllTeams() {
        return new ArrayList<>(teams);
    }

    /**
     * Gets team that contains a specific participant
     * @param participantId Participant ID
     * @return Team or null if not found
     */
    public Team getTeamByParticipant(String participantId) {
        String teamId = participantToTeam.get(participantId);
        if (teamId == null) return null;

        for (Team team : teams) {
            if (team.getTeamId().equals(teamId)) return team;
        }
        return null;
    }

    /**
     * Exports teams to CSV file
     * @param teamsToExport Teams to export
     * @param filePath Output file path
     * @throws FileProcessingException if export fails
     */
    public void exportToCSV(List<Team> teamsToExport, String filePath) throws FileProcessingException {
        if (teamsToExport == null || teamsToExport.isEmpty()) {
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
     * Clears all teams and updates CSV
     */
    public void clearAllTeams() {
        teams.clear();
        participantToTeam.clear();

        try {
            exportToCSV(new ArrayList<>(), TEAMS_CSV);
        } catch (FileProcessingException e) {
            Logger.logWarning("Failed to clear teams CSV: " + e.getMessage());
        }

        Logger.logInfo("All teams cleared");
    }
}