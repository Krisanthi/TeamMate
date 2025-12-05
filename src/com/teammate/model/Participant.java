package com.teammate.model;

/**
 * Participant - Represents a gaming club participant
 *
 * Contains all personal information, preferences, and personality data.
 *
 * @author Krisanthi Segar 2425596
 * @version 1.0
 * @since 2025
 */
public class Participant {
    private String id;
    private String name;
    private String email;
    private String preferredGame;
    private int skillLevel;
    private Role preferredRole;
    private int personalityScore;
    private PersonalityType personalityType;

    public Participant() {
    }

    public Participant(String id, String name, String email, String preferredGame,
                       int skillLevel, Role preferredRole, int personalityScore) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.preferredGame = preferredGame;
        this.skillLevel = skillLevel;
        this.preferredRole = preferredRole;
        this.personalityScore = personalityScore;
        this.personalityType = PersonalityClassifier.classify(personalityScore);
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPreferredGame() { return preferredGame; }
    public void setPreferredGame(String preferredGame) { this.preferredGame = preferredGame; }

    public int getSkillLevel() { return skillLevel; }
    public void setSkillLevel(int skillLevel) { this.skillLevel = skillLevel; }

    public Role getPreferredRole() { return preferredRole; }
    public void setPreferredRole(Role preferredRole) { this.preferredRole = preferredRole; }

    public int getPersonalityScore() { return personalityScore; }
    public void setPersonalityScore(int personalityScore) {
        this.personalityScore = personalityScore;
        this.personalityType = PersonalityClassifier.classify(personalityScore);
    }

    public PersonalityType getPersonalityType() { return personalityType; }
    public void setPersonalityType(PersonalityType personalityType) {
        this.personalityType = personalityType;
    }

    @Override
    public String toString() {
        return String.format("Participant[id=%s, name=%s, game=%s, skill=%d, role=%s, personality=%s(%d)]",
                id, name, preferredGame, skillLevel, preferredRole, personalityType, personalityScore);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Participant that = (Participant) obj;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}