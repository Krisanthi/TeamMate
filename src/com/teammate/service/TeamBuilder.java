package com.teammate.service;

import com.teammate.model.*;
import com.teammate.util.Logger;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Forms balanced teams using concurrent processing
 * Implements matching criteria: game variety, role diversity, personality mix, skill balance
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
     * Three phases: leader distribution, role distribution, remaining distribution
     */
    public List<Team> formTeams() throws InterruptedException, ExecutionException {
        sortParticipantsForDistribution();

        int numTeams = participants.size() / teamSize;
        for (int i = 0; i < numTeams; i++) {
            teams.add(new Team("TEAM_" + (i + 1), teamSize));
        }

        // Concurrent processing with 3 threads
        ExecutorService executor = Executors.newFixedThreadPool(3);
        List<Future<Boolean>> futures = new ArrayList<>();

        futures.add(executor.submit(() -> distributeByPersonality(PersonalityType.LEADER)));
        futures.add(executor.submit(() -> distributeByRoleDiversity()));
        futures.add(executor.submit(() -> distributeRemaining()));

        for (Future<Boolean> future : futures) {
            future.get();
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        balanceTeamSkills();
        performFinalAdjustments();

        return teams;
    }

    private void sortParticipantsForDistribution() {
        participants.sort((p1, p2) -> {
            int personalityCompare = p1.getPersonalityType().compareTo(p2.getPersonalityType());
            if (personalityCompare != 0) return personalityCompare;
            return Integer.compare(p2.getSkillLevel(), p1.getSkillLevel());
        });
    }

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
     * Calculates team score based on matching criteria
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

        // Randomization
        score += new Random().nextInt(5);

        return score;
    }

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

    private boolean wouldSwapImproveBalance(Team team1, Team team2, Participant p1, Participant p2) {
        double currentDiff = Math.abs(team1.getAverageSkill() - team2.getAverageSkill());

        double team1NewAvg = (team1.getAverageSkill() * team1.getCurrentSize() -
                p1.getSkillLevel() + p2.getSkillLevel()) / team1.getCurrentSize();
        double team2NewAvg = (team2.getAverageSkill() * team2.getCurrentSize() -
                p2.getSkillLevel() + p1.getSkillLevel()) / team2.getCurrentSize();

        double newDiff = Math.abs(team1NewAvg - team2NewAvg);

        return newDiff < currentDiff;
    }

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

    private boolean isParticipantAssigned(Participant p) {
        for (Team team : teams) {
            if (team.getMembers().contains(p)) return true;
        }
        return false;
    }
}