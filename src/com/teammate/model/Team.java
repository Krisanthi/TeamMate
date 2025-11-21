package com.teammate.model;

import java.util.*;

/**
 * Represents a formed team with multiple participants
 * Provides methods to check team balance and composition
 */
public class Team {
    private String teamId;
    private List<Participant> members;
    private int teamSize;
    private double averageSkill;

    /**
     * Constructor with team ID and size
     */
    public Team(String teamId, int teamSize) {
        this.teamId = teamId;
        this.teamSize = teamSize;
        this.members = new ArrayList<>();
        this.averageSkill = 0.0;
    }

    /**
     * Adds a member to the team if space available
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
     */
    public boolean removeMember(Participant participant) {
        boolean removed = members.remove(participant);
        if (removed) {
            calculateAverageSkill();
        }
        return removed;
    }

    /**
     * Calculates the average skill level of team members
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
     * Checks if team is balanced according to criteria:
     * - Has diverse roles (at least 3 different)
     * - Has mixed personalities
     * - Not too many from same game
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
     */
    public boolean isFull() {
        return members.size() >= teamSize;
    }

    /**
     * Gets count of specific personality type in team
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
     * Gets count of specific role in team
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
     * Gets count of specific game in team
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
