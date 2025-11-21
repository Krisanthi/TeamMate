package com.teammate.service;

import com.teammate.model.*;
import com.teammate.util.Logger;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Core service class for building balanced teams
 * Uses concurrent processing and sophisticated matching algorithms
 */
public class TeamBuilder {
    private List<Participant> participants;
    private List<Team> teams;
    private int teamSize;
    private static final int MAX_SAME_GAME = 2;
    private static final int MIN_ROLE_DIVERSITY = 3;

    /**
     * Constructor
     */
    public TeamBuilder(List<Participant> participants, int teamSize) {
        this.participants = new ArrayList<>(participants);
        this.teamSize = teamSize;
        this.teams = new ArrayList<>();
    }

    /**
     * Main method to form balanced teams using concurrent processing
     * @return List of formed teams
     */
    public List<Team> formTeams() throws InterruptedException, ExecutionException {
        Logger.logInfo("Starting team formation for " + participants.size() +
                " participants with team size " + teamSize);

        // Step 1: Sort participants for optimal distribution
        sortParticipantsForDistribution();

        // Step 2: Calculate number of teams
        int numTeams = participants.size() / teamSize;

        // Step 3: Initialize teams
        for (int i = 0; i < numTeams; i++) {
            teams.add(new Team("TEAM_" + (i + 1), teamSize));
        }

        // Step 4: Distribute participants using concurrent threads
        ExecutorService executor = Executors.newFixedThreadPool(3);
        List<Future<Boolean>> futures = new ArrayList<>();

        // Phase 1: Distribute leaders
        Future<Boolean> leaderDistribution = executor.submit(() ->
                distributeByPersonality(PersonalityType.LEADER));
        futures.add(leaderDistribution);

        // Phase 2: Distribute by roles
        Future<Boolean> roleDistribution = executor.submit(() ->
                distributeByRoleDiversity());
        futures.add(roleDistribution);

        // Phase 3: Distribute remaining participants
        Future<Boolean> remainingDistribution = executor.submit(() ->
                distributeRemaining());
        futures.add(remainingDistribution);

        // Wait for all phases to complete
        for (Future<Boolean> future : futures) {
            future.get();
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        // Step 5: Balance teams
        balanceTeamSkills();

        // Step 6: Final adjustments
        performFinalAdjustments();

        Logger.logInfo("Team formation completed. Formed " + teams.size() + " teams");
        return teams;
    }

    /**
     * Sorts participants for optimal distribution
     */
    private void sortParticipantsForDistribution() {
        // Sort by personality type (Leaders first), then by skill level
        participants.sort((p1, p2) -> {
            int personalityCompare = p1.getPersonalityType().compareTo(p2.getPersonalityType());
            if (personalityCompare != 0) {
                return personalityCompare;
            }
            return Integer.compare(p2.getSkillLevel(), p1.getSkillLevel());
        });
    }

    /**
     * Distributes participants by personality type
     */
    private synchronized boolean distributeByPersonality(PersonalityType targetType) {
        List<Participant> typeParticipants = participants.stream()
                .filter(p -> p.getPersonalityType() == targetType)
                .collect(Collectors.toList());

        int teamIndex = 0;
        for (Participant p : typeParticipants) {
            if (isParticipantAssigned(p)) continue;

            Team team = teams.get(teamIndex % teams.size());
            if (!team.isFull() && team.getPersonalityCount(targetType) == 0) {
                team.addMember(p);
                teamIndex++;
            }
        }
        return true;
    }

    /**
     * Distributes participants ensuring role diversity
     */
    private synchronized boolean distributeByRoleDiversity() {
        Map<Role, List<Participant>> roleMap = new HashMap<>();

        for (Role role : Role.values()) {
            roleMap.put(role, participants.stream()
                    .filter(p -> p.getPreferredRole() == role && !isParticipantAssigned(p))
                    .collect(Collectors.toList()));
        }

        // Distribute each role across teams
        for (Role role : Role.values()) {
            List<Participant> roleParticipants = roleMap.get(role);
            int teamIndex = 0;

            for (Participant p : roleParticipants) {
                if (isParticipantAssigned(p)) continue;

                Team team = findBestTeamForParticipant(p, teamIndex);
                if (team != null && !team.isFull()) {
                    team.addMember(p);
                    teamIndex = (teamIndex + 1) % teams.size();
                }
            }
        }
        return true;
    }

    /**
     * Distributes remaining unassigned participants
     */
    private synchronized boolean distributeRemaining() {
        List<Participant> unassigned = participants.stream()
                .filter(p -> !isParticipantAssigned(p))
                .collect(Collectors.toList());

        for (Participant p : unassigned) {
            Team bestTeam = findBestTeamForParticipant(p, 0);
            if (bestTeam != null && !bestTeam.isFull()) {
                bestTeam.addMember(p);
            }
        }
        return true;
    }

    /**
     * Finds the best team for a participant based on balance criteria
     */
    private Team findBestTeamForParticipant(Participant p, int startIndex) {
        Team bestTeam = null;
        int bestScore = -1;

        for (int i = 0; i < teams.size(); i++) {
            int index = (startIndex + i) % teams.size();
            Team team = teams.get(index);

            if (team.isFull()) continue;

            int score = calculateTeamScore(team, p);
            if (score > bestScore) {
                bestScore = score;
                bestTeam = team;
            }
        }

        return bestTeam;
    }

    /**
     * Calculates a score for adding a participant to a team
     * Higher score = better fit
     */
    private int calculateTeamScore(Team team, Participant p) {
        int score = 100;

        // Penalty for too many same games
        if (team.getGameCount(p.getPreferredGame()) >= MAX_SAME_GAME) {
            score -= 50;
        }

        // Bonus for role diversity
        if (team.getRoleCount(p.getPreferredRole()) == 0) {
            score += 30;
        }

        // Bonus for personality balance
        PersonalityType pType = p.getPersonalityType();
        if (pType == PersonalityType.LEADER && team.getPersonalityCount(pType) == 0) {
            score += 40;
        } else if (team.getPersonalityCount(pType) < team.getCurrentSize() / 2) {
            score += 20;
        }

        // Penalty for skill imbalance
        double teamAvg = team.getAverageSkill();
        if (teamAvg > 0) {
            int skillDiff = Math.abs(p.getSkillLevel() - (int) teamAvg);
            score -= skillDiff * 2;
        }

        return score;
    }

    /**
     * Balances team skills across all teams
     */
    private void balanceTeamSkills() {
        // Calculate global average skill
        double globalAvg = teams.stream()
                .mapToDouble(Team::getAverageSkill)
                .average()
                .orElse(0.0);

        // Identify teams that need balancing
        List<Team> highSkillTeams = teams.stream()
                .filter(t -> t.getAverageSkill() > globalAvg + 1.5)
                .collect(Collectors.toList());

        List<Team> lowSkillTeams = teams.stream()
                .filter(t -> t.getAverageSkill() < globalAvg - 1.5)
                .collect(Collectors.toList());

        // Swap members to balance
        for (Team highTeam : highSkillTeams) {
            for (Team lowTeam : lowSkillTeams) {
                swapMembersForBalance(highTeam, lowTeam);
            }
        }
    }

    /**
     * Swaps members between two teams to improve balance
     */
    private void swapMembersForBalance(Team team1, Team team2) {
        List<Participant> team1Members = team1.getMembers();
        List<Participant> team2Members = team2.getMembers();

        for (Participant p1 : team1Members) {
            for (Participant p2 : team2Members) {
                // Check if swap improves balance
                if (wouldSwapImproveBalance(team1, team2, p1, p2)) {
                    team1.removeMember(p1);
                    team2.removeMember(p2);
                    team1.addMember(p2);
                    team2.addMember(p1);
                    return; // One swap at a time
                }
            }
        }
    }

    /**
     * Checks if swapping two members would improve overall balance
     */
    private boolean wouldSwapImproveBalance(Team team1, Team team2,
                                            Participant p1, Participant p2) {
        double currentDiff = Math.abs(team1.getAverageSkill() - team2.getAverageSkill());

        // Calculate hypothetical averages after swap
        double team1NewAvg = (team1.getAverageSkill() * team1.getCurrentSize() -
                p1.getSkillLevel() + p2.getSkillLevel()) / team1.getCurrentSize();
        double team2NewAvg = (team2.getAverageSkill() * team2.getCurrentSize() -
                p2.getSkillLevel() + p1.getSkillLevel()) / team2.getCurrentSize();

        double newDiff = Math.abs(team1NewAvg - team2NewAvg);

        return newDiff < currentDiff;
    }

    /**
     * Performs final adjustments to ensure all criteria are met
     */
    private void performFinalAdjustments() {
        for (Team team : teams) {
            // Ensure minimum role diversity
            Set<Role> roles = new HashSet<>();
            for (Participant p : team.getMembers()) {
                roles.add(p.getPreferredRole());
            }

            if (roles.size() < MIN_ROLE_DIVERSITY && teams.size() > 1) {
                // Try to swap with another team
                for (Team otherTeam : teams) {
                    if (otherTeam == team) continue;
                    swapForRoleDiversity(team, otherTeam);
                    break;
                }
            }
        }
    }

    /**
     * Swaps members to improve role diversity
     */
    private void swapForRoleDiversity(Team team1, Team team2) {
        Set<Role> team1Roles = team1.getMembers().stream()
                .map(Participant::getPreferredRole)
                .collect(Collectors.toSet());

        for (Participant p2 : team2.getMembers()) {
            if (!team1Roles.contains(p2.getPreferredRole())) {
                for (Participant p1 : team1.getMembers()) {
                    team1.removeMember(p1);
                    team2.removeMember(p2);
                    team1.addMember(p2);
                    team2.addMember(p1);
                    return;
                }
            }
        }
    }

    /**
     * Checks if a participant is already assigned to a team
     */
    private boolean isParticipantAssigned(Participant p) {
        for (Team team : teams) {
            if (team.getMembers().contains(p)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Validates if all teams meet balance criteria
     */
    public boolean balanceByRole(Team team) {
        Set<Role> roles = new HashSet<>();
        for (Participant p : team.getMembers()) {
            roles.add(p.getPreferredRole());
        }
        return roles.size() >= MIN_ROLE_DIVERSITY;
    }

    /**
     * Validates personality balance in a team
     */
    public boolean balanceByPersonality(Team team) {
        return team.getPersonalityCount(PersonalityType.LEADER) >= 1 &&
                team.getPersonalityCount(PersonalityType.THINKER) >= 1;
    }

    /**
     * Validates game diversity in a team
     */
    public boolean balanceByGame(Team team) {
        for (Participant p : team.getMembers()) {
            if (team.getGameCount(p.getPreferredGame()) > MAX_SAME_GAME) {
                return false;
            }
        }
        return true;
    }

    // Getters
    public List<Team> getTeams() { return new ArrayList<>(teams); }
    public int getTeamSize() { return teamSize; }
    public void setTeamSize(int teamSize) { this.teamSize = teamSize; }
}
