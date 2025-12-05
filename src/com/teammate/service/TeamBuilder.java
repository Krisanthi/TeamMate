package com.teammate.service;

import com.teammate.model.*;
import com.teammate.util.Logger;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * TeamBuilder - Intelligent Team Formation Algorithm
 *
 * Implements multi-phase algorithm to form balanced teams according to matching strategy.
 *
 * MATCHING CRITERIA:
 * 1. Game/Sport Variety: Max 3 players per game per team
 * 2. Role Diversity: At least 3 different roles per team
 * 3. Personality Mix: 1 Leader, 1-2 Thinkers, remaining Balanced
 * 4. Skill Balance: Avoid stacking high-skill players
 * 5. Randomization: Fair distribution within constraints
 *
 * ALGORITHM PHASES:
 * Phase 1: Shuffle participants for randomization
 * Phase 2: Distribute Leaders (exactly 1 per team)
 * Phase 3: Distribute Thinkers (1-2 per team)
 * Phase 4: Distribute by role diversity (concurrent)
 * Phase 5: Distribute Balanced types (concurrent)
 * Phase 6: Distribute remaining (concurrent)
 * Phase 7: Balance team skills
 * Phase 8: Final role diversity adjustments
 *
 * @author Student Name
 * @version 2.0
 * @since 2025
 */
public class TeamBuilder {

    private List<Participant> participants;
    private List<Team> teams;
    private int teamSize;

    private static final int MAX_SAME_GAME = 3;
    private static final int MIN_ROLE_DIVERSITY = 3;
    private static final int MAX_LEADERS_PER_TEAM = 1;
    private static final int MIN_THINKERS_PER_TEAM = 1;
    private static final int MAX_THINKERS_PER_TEAM = 2;

    private final Random random = new Random();

    public TeamBuilder(List<Participant> participants, int teamSize) {
        this.participants = new ArrayList<>(participants);
        this.teamSize = teamSize;
        this.teams = new ArrayList<>();
    }

    /**
     * Forms balanced teams using multi-phase concurrent algorithm
     * @return List of formed teams
     * @throws InterruptedException if thread execution is interrupted
     * @throws ExecutionException if concurrent execution fails
     */
    public List<Team> formTeams() throws InterruptedException, ExecutionException {
        // Phase 1: Shuffle for randomization
        Collections.shuffle(participants, random);
        Logger.logInfo("Shuffled participants for fair distribution");

        participants.sort((p1, p2) -> Integer.compare(p2.getSkillLevel(), p1.getSkillLevel()));

        // Initialize teams
        int numTeams = participants.size() / teamSize;
        for (int i = 0; i < numTeams; i++) {
            teams.add(new Team("TEAM_" + (i + 1), teamSize));
        }
        Logger.logInfo("Created " + numTeams + " teams");

        // Phase 2-3: Distribute personality types strategically
        distributeLeaders();
        distributeThinkers();

        // Phase 4-6: Concurrent distribution
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

        // Phase 7-8: Optimization
        balanceTeamSkills();
        performFinalAdjustments();

        Logger.logInfo("Team formation complete");
        return teams;
    }

    /**
     * Phase 2: Distributes Leaders (1 per team)
     * @return true when complete
     */
    private synchronized boolean distributeLeaders() {
        List<Participant> leaders = participants.stream()
                .filter(p -> p.getPersonalityType() == PersonalityType.LEADER)
                .sorted((p1, p2) -> Integer.compare(p2.getSkillLevel(), p1.getSkillLevel()))
                .collect(Collectors.toList());

        Logger.logInfo("Distributing " + leaders.size() + " leaders across " + teams.size() + " teams");

        int startTeam = random.nextInt(Math.max(1, teams.size()));
        int teamIndex = 0;

        for (Participant leader : leaders) {
            if (isParticipantAssigned(leader)) continue;

            for (int i = 0; i < teams.size(); i++) {
                int currentIndex = (startTeam + teamIndex + i) % teams.size();
                Team team = teams.get(currentIndex);

                if (!team.isFull() && team.getPersonalityCount(PersonalityType.LEADER) < MAX_LEADERS_PER_TEAM) {
                    team.addMember(leader);
                    teamIndex++;
                    break;
                }
            }
        }

        Logger.logInfo("Leaders distributed");
        return true;
    }

    /**
     * Phase 3: Distributes Thinkers (1-2 per team)
     * @return true when complete
     */
    private synchronized boolean distributeThinkers() {
        List<Participant> thinkers = participants.stream()
                .filter(p -> p.getPersonalityType() == PersonalityType.THINKER)
                .sorted((p1, p2) -> Integer.compare(p2.getSkillLevel(), p1.getSkillLevel()))
                .collect(Collectors.toList());

        Logger.logInfo("Distributing " + thinkers.size() + " thinkers across " + teams.size() + " teams");

        int teamIndex = random.nextInt(Math.max(1, teams.size()));

        for (Participant thinker : thinkers) {
            if (isParticipantAssigned(thinker)) continue;

            Team bestTeam = null;
            for (int i = 0; i < teams.size(); i++) {
                int currentIndex = (teamIndex + i) % teams.size();
                Team team = teams.get(currentIndex);

                if (!team.isFull() && team.getPersonalityCount(PersonalityType.THINKER) < MIN_THINKERS_PER_TEAM) {
                    bestTeam = team;
                    teamIndex = (currentIndex + 1) % teams.size();
                    break;
                }
            }

            if (bestTeam == null) {
                for (int i = 0; i < teams.size(); i++) {
                    int currentIndex = (teamIndex + i) % teams.size();
                    Team team = teams.get(currentIndex);

                    if (!team.isFull() && team.getPersonalityCount(PersonalityType.THINKER) < MAX_THINKERS_PER_TEAM) {
                        bestTeam = team;
                        teamIndex = (currentIndex + 1) % teams.size();
                        break;
                    }
                }
            }

            if (bestTeam != null) {
                bestTeam.addMember(thinker);
            }
        }

        Logger.logInfo("Thinkers distributed");
        return true;
    }

    /**
     * Phase 4: Distributes Balanced types
     * @return true when complete
     */
    private synchronized boolean distributeBalancedTypes() {
        List<Participant> balancedParticipants = participants.stream()
                .filter(p -> p.getPersonalityType() == PersonalityType.BALANCED && !isParticipantAssigned(p))
                .collect(Collectors.toList());

        Logger.logInfo("Distributing " + balancedParticipants.size() + " balanced participants");

        for (Participant p : balancedParticipants) {
            Team bestTeam = findBestTeamForParticipant(p);
            if (bestTeam != null && !bestTeam.isFull()) {
                bestTeam.addMember(p);
            }
        }

        return true;
    }

    /**
     * Phase 5: Distributes by role diversity
     * @return true when complete
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

            for (Participant p : roleParticipants) {
                if (isParticipantAssigned(p)) continue;

                Team bestTeam = findBestTeamForParticipant(p);
                if (bestTeam != null && !bestTeam.isFull()) {
                    bestTeam.addMember(p);
                }
            }
        }
        return true;
    }

    /**
     * Phase 6: Distributes remaining unassigned participants
     * @return true when complete
     */
    private synchronized boolean distributeRemaining() {
        List<Participant> unassigned = participants.stream()
                .filter(p -> !isParticipantAssigned(p))
                .collect(Collectors.toList());

        if (!unassigned.isEmpty()) {
            Logger.logInfo("Distributing " + unassigned.size() + " remaining participants");
        }

        for (Participant p : unassigned) {
            Team bestTeam = findBestTeamForParticipant(p);
            if (bestTeam != null && !bestTeam.isFull()) {
                bestTeam.addMember(p);
            }
        }
        return true;
    }

    /**
     * Finds best team for participant using scoring algorithm
     * @param p The participant
     * @return Best team or null
     */
    private Team findBestTeamForParticipant(Participant p) {
        List<Team> availableTeams = teams.stream()
                .filter(t -> !t.isFull())
                .collect(Collectors.toList());

        if (availableTeams.isEmpty()) {
            return null;
        }

        double globalAvg = teams.stream()
                .filter(t -> t.getCurrentSize() > 0)
                .mapToDouble(Team::getAverageSkill)
                .average()
                .orElse(5.0);

        Team bestTeam = null;
        int bestScore = Integer.MIN_VALUE;

        Collections.shuffle(availableTeams, random);

        for (Team team : availableTeams) {
            int score = calculateTeamScore(team, p, globalAvg);

            if (score > bestScore || (score == bestScore && random.nextBoolean())) {
                bestScore = score;
                bestTeam = team;
            }
        }

        return bestTeam;
    }

    /**
     * Calculates team score for participant placement
     * @param team The team
     * @param p The participant
     * @param globalAvg Global average skill
     * @return Score (higher is better)
     */
    private int calculateTeamScore(Team team, Participant p, double globalAvg) {
        int score = 100;

        // Game variety
        if (team.getGameCount(p.getPreferredGame()) >= MAX_SAME_GAME) {
            score -= 40;
        }

        // Role diversity
        if (team.getRoleCount(p.getPreferredRole()) == 0) {
            score += 25;
        }

        // Personality mix
        PersonalityType pType = p.getPersonalityType();
        int currentCount = team.getPersonalityCount(pType);
        if (currentCount < team.getCurrentSize() / 3) {
            score += 15;
        }

        // Skill balance
        double teamAvg = team.getAverageSkill();
        if (teamAvg > 0) {
            double newAvg = (teamAvg * team.getCurrentSize() + p.getSkillLevel()) / (team.getCurrentSize() + 1);

            double currentDiff = Math.abs(teamAvg - globalAvg);
            double newDiff = Math.abs(newAvg - globalAvg);

            if (newDiff > currentDiff) {
                score -= (int)((newDiff - currentDiff) * 10);
            } else {
                score += (int)((currentDiff - newDiff) * 5);
            }
        }

        // Randomization
        score += random.nextInt(7) - 3;

        return score;
    }

    /**
     * Phase 7: Balances team skills through swaps
     */
    private void balanceTeamSkills() {
        double globalAvg = teams.stream()
                .mapToDouble(Team::getAverageSkill)
                .average()
                .orElse(0.0);

        Logger.logInfo("Global average skill: " + String.format("%.2f", globalAvg));

        List<Team> highSkillTeams = teams.stream()
                .filter(t -> t.getAverageSkill() > globalAvg + 1.0)
                .collect(Collectors.toList());

        List<Team> lowSkillTeams = teams.stream()
                .filter(t -> t.getAverageSkill() < globalAvg - 1.0)
                .collect(Collectors.toList());

        for (Team highTeam : highSkillTeams) {
            for (Team lowTeam : lowSkillTeams) {
                if (swapMembersForBalance(highTeam, lowTeam, globalAvg)) {
                    Logger.logInfo("Swapped members between " + highTeam.getTeamId() +
                            " and " + lowTeam.getTeamId() + " for skill balance");
                }
            }
        }
    }

    /**
     * Attempts to swap members for better balance
     * @param team1 First team
     * @param team2 Second team
     * @param globalAvg Global average
     * @return true if swap made
     */
    private boolean swapMembersForBalance(Team team1, Team team2, double globalAvg) {
        List<Participant> team1Members = team1.getMembers();
        List<Participant> team2Members = team2.getMembers();

        for (Participant p1 : team1Members) {
            for (Participant p2 : team2Members) {
                if (p1.getPersonalityType() == PersonalityType.LEADER ||
                        p2.getPersonalityType() == PersonalityType.LEADER) {
                    continue;
                }

                if (wouldSwapImproveBalance(team1, team2, p1, p2, globalAvg)) {
                    team1.removeMember(p1);
                    team2.removeMember(p2);
                    team1.addMember(p2);
                    team2.addMember(p1);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if swap would improve balance
     * @param team1 First team
     * @param team2 Second team
     * @param p1 Participant from team1
     * @param p2 Participant from team2
     * @param globalAvg Global average
     * @return true if swap improves balance
     */
    private boolean wouldSwapImproveBalance(Team team1, Team team2, Participant p1, Participant p2, double globalAvg) {
        double currentDev1 = Math.abs(team1.getAverageSkill() - globalAvg);
        double currentDev2 = Math.abs(team2.getAverageSkill() - globalAvg);
        double currentTotalDev = currentDev1 + currentDev2;

        double team1NewAvg = (team1.getAverageSkill() * team1.getCurrentSize() -
                p1.getSkillLevel() + p2.getSkillLevel()) / team1.getCurrentSize();
        double team2NewAvg = (team2.getAverageSkill() * team2.getCurrentSize() -
                p2.getSkillLevel() + p1.getSkillLevel()) / team2.getCurrentSize();

        double newDev1 = Math.abs(team1NewAvg - globalAvg);
        double newDev2 = Math.abs(team2NewAvg - globalAvg);
        double newTotalDev = newDev1 + newDev2;

        return newTotalDev < currentTotalDev;
    }

    /**
     * Phase 8: Final role diversity adjustments
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
                    if (swapForRoleDiversity(team, otherTeam)) {
                        Logger.logInfo("Improved role diversity for " + team.getTeamId());
                        break;
                    }
                }
            }
        }
    }

    /**
     * Swaps members to improve role diversity
     * @param team1 Team needing diversity
     * @param team2 Team to swap with
     * @return true if swap made
     */
    private boolean swapForRoleDiversity(Team team1, Team team2) {
        Set<Role> team1Roles = team1.getMembers().stream()
                .map(Participant::getPreferredRole)
                .collect(Collectors.toSet());

        for (Participant p2 : team2.getMembers()) {
            if (!team1Roles.contains(p2.getPreferredRole())) {
                for (Participant p1 : team1.getMembers()) {
                    if (p1.getPersonalityType() != PersonalityType.LEADER &&
                            p2.getPersonalityType() != PersonalityType.LEADER) {
                        team1.removeMember(p1);
                        team2.removeMember(p2);
                        team1.addMember(p2);
                        team2.addMember(p1);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Checks if participant is already assigned
     * @param p The participant
     * @return true if assigned
     */
    private boolean isParticipantAssigned(Participant p) {
        for (Team team : teams) {
            if (team.getMembers().contains(p)) {
                return true;
            }
        }
        return false;
    }
}