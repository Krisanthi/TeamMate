package com.teammate.model;

import java.util.*;

/**
 * Team - Represents a formed team with multiple participants
 *
 * Provides methods to check team balance and composition according to matching strategy.
 *
 * @author Krisanthi Segar 2425596
 * @version 1.0
 * @since 2025
 */
public class Team {
    private String teamId;
    private List<Participant> members;
    private int teamSize;
    private double averageSkill;

    public Team(String teamId, int teamSize) {
        this.teamId = teamId;
        this.teamSize = teamSize;
        this.members = new ArrayList<>();
        this.averageSkill = 0.0;
    }

    /**
     * Adds a member to the team
     * @param participant The participant to add
     * @return true if added successfully, false if team is full
     */
    public boolean addMember(Participant participant) {
        if (members.size() >= teamSize) {
            return false;
        }
        members.add(participant);
        calculateAverageSkill();
        return true;
    }

    /**
     * Removes a member from the team
     * @param participant The participant to remove
     * @return true if removed successfully
     */
    public boolean removeMember(Participant participant) {
        boolean removed = members.remove(participant);
        if (removed) {
            calculateAverageSkill();
        }
        return removed;
    }

    /**
     * Calculates average skill level of team members
     * @return The average skill level
     */
    public double calculateAverageSkill() {
        if (members.isEmpty()) {
            averageSkill = 0.0;
            return 0.0;
        }

        int totalSkill = 0;
        for (Participant p : members) {
            totalSkill += p.getSkillLevel();
        }
        averageSkill = (double) totalSkill / members.size();
        return averageSkill;
    }

    /**
     * Checks if team is balanced according to all matching criteria:
     * 1. Team is full
     * 2. At least 3 different roles
     * 3. No single game dominates (max half the team)
     * 4. At least 2 different personality types
     *
     * @return true if team meets all balance criteria
     */
    public boolean isBalanced() {
        if (members.size() < teamSize) {
            return false;
        }

        // Check role diversity
        Set<Role> uniqueRoles = new HashSet<>();
        for (Participant p : members) {
            uniqueRoles.add(p.getPreferredRole());
        }

        // Check personality mix
        Map<PersonalityType, Integer> personalityCount = new HashMap<>();
        for (Participant p : members) {
            personalityCount.merge(p.getPersonalityType(), 1, Integer::sum);
        }

        // Check game diversity
        Map<String, Integer> gameCount = new HashMap<>();
        for (Participant p : members) {
            gameCount.merge(p.getPreferredGame(), 1, Integer::sum);
        }

        // Balance criteria
        boolean rolesDiverse = uniqueRoles.size() >= Math.min(3, teamSize);
        boolean noGameDomination = gameCount.values().stream().noneMatch(count -> count > teamSize / 2);
        boolean hasPersonalityMix = personalityCount.size() >= 2;

        return rolesDiverse && noGameDomination && hasPersonalityMix;
    }

    /**
     * Checks if team is full
     * @return true if team has reached maximum size
     */
    public boolean isFull() {
        return members.size() >= teamSize;
    }

    /**
     * Counts participants of a specific personality type
     * @param type The personality type to count
     * @return Number of participants with that type
     */
    public int getPersonalityCount(PersonalityType type) {
        int count = 0;
        for (Participant p : members) {
            if (p.getPersonalityType() == type) {
                count++;
            }
        }
        return count;
    }

    /**
     * Counts participants with a specific role
     * @param role The role to count
     * @return Number of participants with that role
     */
    public int getRoleCount(Role role) {
        int count = 0;
        for (Participant p : members) {
            if (p.getPreferredRole() == role) {
                count++;
            }
        }
        return count;
    }

    /**
     * Counts participants preferring a specific game
     * @param game The game name
     * @return Number of participants preferring that game
     */
    public int getGameCount(String game) {
        int count = 0;
        for (Participant p : members) {
            if (p.getPreferredGame().equalsIgnoreCase(game)) {
                count++;
            }
        }
        return count;
    }

    // Getters and Setters
    public String getTeamId() { return teamId; }
    public void setTeamId(String teamId) { this.teamId = teamId; }

    public List<Participant> getMembers() { return new ArrayList<>(members); }

    public int getTeamSize() { return teamSize; }
    public void setTeamSize(int teamSize) { this.teamSize = teamSize; }

    public double getAverageSkill() { return averageSkill; }

    public int getCurrentSize() { return members.size(); }

    @Override
    public String toString() {
        return String.format("Team[id=%s, size=%d/%d, avgSkill=%.2f, balanced=%b]",
                teamId, members.size(), teamSize, averageSkill, isBalanced());
    }
}