package com.teammate.service;

import com.teammate.model.*;
import com.teammate.util.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Manages team generation and storage using concurrent processing
 */
public class TeamService {

    private List<Team> teams;
    private Map<String, String> participantToTeam;

    public TeamService() {
        this.teams = new ArrayList<>();
        this.participantToTeam = new ConcurrentHashMap<>();
        Logger.logInfo("TeamService initialized");
    }

    /**
     * Generates balanced teams using thread pool (3 threads)
     * for concurrent processing of participants
     */
    public List<Team> generateTeams(List<Participant> participants, int teamSize)
            throws InterruptedException, ExecutionException {

        if (participants == null || participants.isEmpty()) {
            throw new IllegalArgumentException("No participants available");
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
            throw new FileProcessingException("No teams to export");
        }

        FileHandler fileHandler = new FileHandler("", filePath);
        fileHandler.saveTeams(teamsToExport);
        Logger.logInfo("Exported teams to " + filePath);
    }

    public void clearAllTeams() {
        teams.clear();
        participantToTeam.clear();
    }

    public int getTeamCount() {
        return teams.size();
    }
}