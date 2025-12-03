package com.teammate.service;

import com.teammate.model.*;
import com.teammate.util.Logger;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * TeamBuilder - Intelligent Team Formation Algorithm
 *
 * This class implements a multi-phase concurrent algorithm to form balanced teams
 * according to the following matching criteria:
 *
 * MATCHING CRITERIA:
 * 1. Game/Sport Variety: Max 2 players per game per team
 * 2. Role Diversity: At least 3 different roles per team
 * 3. Personality Mix: 1 Leader, 1-2 Thinkers, remaining Balanced per team
 * 4. Skill Balance: Team averages close to global average
 *
 * ALGORITHM PHASES:
 * Phase 1: Distribute Leaders (1 per team, round-robin)
 * Phase 2: Distribute Thinkers (1-2 per team, round-robin)
 * Phase 3: Distribute roles for diversity (concurrent)
 * Phase 4: Distribute Balanced types to fill teams (concurrent)
 * Phase 5: Distribute any remaining participants (concurrent)
 * Phase 6: Balance team skills through strategic swaps
 * Phase 7: Final adjustments for role diversity if needed
 *
 * CONCURRENCY:
 * - Uses ExecutorService with 3 threads for parallel distribution
 * - Synchronized methods prevent race conditions
 * - Thread-safe assignment checking
 *
 * @author Student Name
 * @version 2.0
 * @since 2025
 */
public class TeamBuilder {

    private List<Participant> participants;
    private List<Team> teams;
    private int teamSize;
    private static final int MAX_SAME_GAME = 2;
    private static final int MIN_ROLE_DIVERSITY = 3;

    public TeamBuilder(List<Participant> participants, int teamSize) {
        this.participants = new ArrayList<>(participants);
        this.teamSize = teamSize;
        this.teams = new ArrayList<>();
    }

    /**
     * Forms teams using concurrent processing with thread pool
     *
     * Strategy:
     * Phase 1: Distribute 1 Leader per team (round-robin)
     * Phase 2: Distribute 1-2 Thinkers per team (round-robin)
     * Phase 3-5: Concurrent distribution of roles and remaining participants
     * Phase 6: Balance skills across teams
     * Phase 7: Final adjustments for role diversity
     */
    public List<Team> formTeams() throws InterruptedException, ExecutionException {
        sortParticipantsForDistribution();

        int numTeams = participants.size() / teamSize;
        for (int i = 0; i < numTeams; i++) {
            teams.add(new Team("TEAM_" + (i + 1), teamSize));
        }

        // Phase 1-2: Distribute personalities strategically (FIXED)
        distributeByPersonality(PersonalityType.LEADER, 1);      // 1 Leader per team
        distributeByPersonality(PersonalityType.THINKER, 2);     // 1-2 Thinkers per team

        // Phase 3-5: Concurrent role distribution
        ExecutorService executor = Executors.newFixedThreadPool(3);
        List<Future<Boolean>> futures = new ArrayList<>();

        futures.add(executor.submit(() -> distributeByRoleDiversity()));
        futures.add(executor.submit(() -> distributeBalancedTypes()));
        futures.add(executor.submit(() -> distributeRemaining()));

        for (Future<Boolean> future : futures) {
            future.get();
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        // Phase 6-7: Optimization
        balanceTeamSkills();
        performFinalAdjustments();

        return teams;
    }

    /**
     * Sorts participants for optimal distribution
     * Priority: Personality type first, then skill level (high to low)
     */
    private void sortParticipantsForDistribution() {
        participants.sort((p1, p2) -> {
            int personalityCompare = p1.getPersonalityType().compareTo(p2.getPersonalityType());
            if (personalityCompare != 0) return personalityCompare;
            return Integer.compare(p2.getSkillLevel(), p1.getSkillLevel());
        });
    }

    /**
     * Distributes participants of a specific personality type across teams
     * FIXED: Now accepts maxPerTeam parameter to enforce personality mix rules
     *
     * @param targetType The personality type to distribute
     * @param maxPerTeam Maximum number of this type per team (1 for Leader, 2 for Thinker)
     * @return true when distribution is complete
     */
    private synchronized boolean distributeByPersonality(PersonalityType targetType, int maxPerTeam) {
        List<Participant> typeParticipants = participants.stream()
                .filter(p -> p.getPersonalityType() == targetType)
                .collect(Collectors.toList());

        int teamIndex = 0;
        for (Participant p : typeParticipants) {
            if (isParticipantAssigned(p)) continue;

            Team team = teams.get(teamIndex % teams.size());
            if (!team.isFull() && team.getPersonalityCount(targetType) < maxPerTeam) {
                team.addMember(p);
                teamIndex++;
            }
        }
        return true;
    }

    /**
     * NEW METHOD: Distributes Balanced personality types to fill remaining team slots
     * Called after Leaders and Thinkers are assigned
     *
     * @return true when distribution is complete
     */
    private synchronized boolean distributeBalancedTypes() {
        List<Participant> balancedParticipants = participants.stream()
                .filter(p -> p.getPersonalityType() == PersonalityType.BALANCED && !isParticipantAssigned(p))
                .collect(Collectors.toList());

        int teamIndex = 0;
        for (Participant p : balancedParticipants) {
            if (isParticipantAssigned(p)) continue;

            Team team = findBestTeamForParticipant(p, teamIndex);
            if (team != null && !team.isFull()) {
                team.addMember(p);
                teamIndex = (teamIndex + 1) % teams.size();
            }
        }
        return true;
    }

    /**
     * Distributes participants by role to ensure role diversity
     * Groups participants by role and distributes each role across teams
     *
     * @return true when distribution is complete
     */
    private synchronized boolean distributeByRoleDiversity() {
        Map<Role, List<Participant>> roleMap = new HashMap<>();

        for (Role role : Role.values()) {
            roleMap.put(role, participants.stream()
                    .filter(p -> p.getPreferredRole() == role && !isParticipantAssigned(p))
                    .collect(Collectors.toList()));
        }

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
     * Distributes any remaining unassigned participants
     * Uses scoring algorithm to find best team fit
     *
     * @return true when distribution is complete
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
     * Finds the best team for a participant using scoring algorithm
     * Considers: game variety, role diversity, personality mix, skill balance
     *
     * @param p The participant to assign
     * @param startIndex Starting team index for rotation
     * @return Best team for this participant, or null if none available
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
     * Calculates team score based on matching criteria
     * Higher score = better fit for the participant
     *
     * Scoring:
     * - Game variety: -50 if exceeds max same game
     * - Role diversity: +30 if role is new to team
     * - Personality mix: +40 for needed Leader, +20 for needed type
     * - Skill balance: -2 per point difference from team average
     *
     * @param team The team to score
     * @param p The participant being evaluated
     * @return Score (higher is better fit)
     */
    private int calculateTeamScore(Team team, Participant p) {
        int score = 100;

        // Game variety: penalty for too many same game
        if (team.getGameCount(p.getPreferredGame()) >= MAX_SAME_GAME) {
            score -= 50;
        }

        // Role diversity: bonus for new roles
        if (team.getRoleCount(p.getPreferredRole()) == 0) {
            score += 30;
        }

        // Personality mix
        PersonalityType pType = p.getPersonalityType();
        if (pType == PersonalityType.LEADER && team.getPersonalityCount(pType) == 0) {
            score += 40;
        } else if (team.getPersonalityCount(pType) < team.getCurrentSize() / 2) {
            score += 20;
        }

        // Skill balance
        double teamAvg = team.getAverageSkill();
        if (teamAvg > 0) {
            int skillDiff = Math.abs(p.getSkillLevel() - (int) teamAvg);
            score -= skillDiff * 2;
        }

        // Note: Removed random component for deterministic results

        return score;
    }

    /**
     * Balances team skills by swapping members between high and low skill teams
     * Goal: Bring all team averages closer to global average
     */
    private void balanceTeamSkills() {
        double globalAvg = teams.stream()
                .mapToDouble(Team::getAverageSkill)
                .average()
                .orElse(0.0);

        List<Team> highSkillTeams = teams.stream()
                .filter(t -> t.getAverageSkill() > globalAvg + 1.5)
                .collect(Collectors.toList());

        List<Team> lowSkillTeams = teams.stream()
                .filter(t -> t.getAverageSkill() < globalAvg - 1.5)
                .collect(Collectors.toList());

        for (Team highTeam : highSkillTeams) {
            for (Team lowTeam : lowSkillTeams) {
                swapMembersForBalance(highTeam, lowTeam);
            }
        }
    }

    /**
     * Swaps members between two teams to improve skill balance
     * Only swaps if it reduces the skill difference between teams
     *
     * @param team1 First team
     * @param team2 Second team
     */
    private void swapMembersForBalance(Team team1, Team team2) {
        List<Participant> team1Members = team1.getMembers();
        List<Participant> team2Members = team2.getMembers();

        for (Participant p1 : team1Members) {
            for (Participant p2 : team2Members) {
                if (wouldSwapImproveBalance(team1, team2, p1, p2)) {
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
     * Evaluates if swapping two participants would improve team balance
     *
     * @param team1 First team
     * @param team2 Second team
     * @param p1 Participant from team1
     * @param p2 Participant from team2
     * @return true if swap reduces skill difference between teams
     */
    private boolean wouldSwapImproveBalance(Team team1, Team team2, Participant p1, Participant p2) {
        double currentDiff = Math.abs(team1.getAverageSkill() - team2.getAverageSkill());

        double team1NewAvg = (team1.getAverageSkill() * team1.getCurrentSize() -
                p1.getSkillLevel() + p2.getSkillLevel()) / team1.getCurrentSize();
        double team2NewAvg = (team2.getAverageSkill() * team2.getCurrentSize() -
                p2.getSkillLevel() + p1.getSkillLevel()) / team2.getCurrentSize();

        double newDiff = Math.abs(team1NewAvg - team2NewAvg);

        return newDiff < currentDiff;
    }

    /**
     * Performs final adjustments to ensure minimum role diversity
     * If a team has fewer than MIN_ROLE_DIVERSITY roles, swaps members with other teams
     */
    private void performFinalAdjustments() {
        for (Team team : teams) {
            Set<Role> roles = new HashSet<>();
            for (Participant p : team.getMembers()) {
                roles.add(p.getPreferredRole());
            }

            if (roles.size() < MIN_ROLE_DIVERSITY && teams.size() > 1) {
                for (Team otherTeam : teams) {
                    if (otherTeam == team) continue;
                    swapForRoleDiversity(team, otherTeam);
                    break;
                }
            }
        }
    }

    /**
     * Swaps members between teams to improve role diversity
     * Finds a role missing from team1 and swaps for it
     *
     * @param team1 Team needing more role diversity
     * @param team2 Team to swap with
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
     * Checks if a participant is already assigned to any team
     * Thread-safe check to prevent double-assignment in concurrent execution
     *
     * @param p The participant to check
     * @return true if participant is assigned to a team, false otherwise
     */
    private boolean isParticipantAssigned(Participant p) {
        for (Team team : teams) {
            if (team.getMembers().contains(p)) return true;
        }
        return false;
    }
}