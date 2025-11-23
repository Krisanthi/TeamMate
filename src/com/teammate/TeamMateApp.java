package com.teammate;

import com.teammate.model.*;
import com.teammate.service.*;
import com.teammate.util.*;
import java.util.*;

/**
 * Main application for TeamMate system
 * Handles authentication, participant registration, and organizer functions
 */
public class TeamMateApp {

    private static final String ORGANIZER_PASSWORD = "Teammate";
    private static final String PARTICIPANT_CSV = "participants_sample.csv";
    private static final Scanner scanner = new Scanner(System.in);
    private static UserService userService;
    private static TeamService teamService;

    public static void main(String[] args) {
        Logger.logInfo("Application started");

        System.out.println("================================================================");
        System.out.println("   TeamMate: Intelligent Team Formation System");
        System.out.println("   University Gaming Club Management");
        System.out.println("================================================================\n");

        try {
            userService = new UserService(PARTICIPANT_CSV);
            teamService = new TeamService();

            // Auto-load participants on startup
            System.out.println("[INFO] Loading participant data...");
            int count = userService.loadFromCSV(PARTICIPANT_CSV);
            System.out.println("[SUCCESS] Loaded " + count + " participants from database\n");

            boolean exit = false;
            while (!exit) {
                displayWelcomeMenu();
                String choice = getUserInput("Enter your choice (1-3): ");

                switch (choice) {
                    case "1": participantFlow(); break;
                    case "2": organizerFlow(); break;
                    case "3":
                        exit = true;
                        System.out.println("\n[INFO] Thank you for using TeamMate. Goodbye!");
                        Logger.logInfo("User exit");
                        break;
                    default:
                        System.err.println("[ERROR] Invalid choice. Please enter 1, 2, or 3.");
                }
            }

        } catch (Exception e) {
            System.err.println("[ERROR] Application error: " + e.getMessage());
            Logger.logException("Critical error", e);
        } finally {
            if (scanner != null) scanner.close();
            Logger.logInfo("Application terminated");
        }
    }

    private static void displayWelcomeMenu() {
        System.out.println("\n================================================================");
        System.out.println("                    WELCOME TO TEAMMATE");
        System.out.println("================================================================");
        System.out.println("  1. Participant Login/Register");
        System.out.println("  2. Organizer Login");
        System.out.println("  3. Exit");
        System.out.println("================================================================");
    }

    private static void participantFlow() {
        System.out.println("\n[PARTICIPANT MENU]");
        System.out.println("----------------------------------------------------------------");
        System.out.println("  1. Register as New Participant");
        System.out.println("  2. Login as Existing Participant");
        System.out.println("  3. Back to Main Menu");
        System.out.println("----------------------------------------------------------------");

        String choice = getUserInput("Enter your choice (1-3): ");

        switch (choice) {
            case "1": registerParticipant(); break;
            case "2": loginParticipant(); break;
            case "3": return;
            default: System.err.println("[ERROR] Invalid choice.");
        }
    }

    /**
     * Registers new participant with auto-generated ID and personality survey
     */
    private static void registerParticipant() {
        Logger.logInfo("Registration initiated");

        try {
            System.out.println("\n[NEW PARTICIPANT REGISTRATION]");
            System.out.println("================================================================");

            String name = getUserInput("Enter your full name: ");
            if (!ValidationUtils.isNotEmpty(name)) {
                throw new InvalidInputException("Name cannot be empty");
            }

            String email = getUserInput("Enter your email: ");
            if (!ValidationUtils.isValidEmail(email)) {
                throw new InvalidInputException("Invalid email format");
            }

            // Conduct personality survey (5 questions, 1-5 scale)
            System.out.println("\n[PERSONALITY SURVEY]");
            System.out.println("Rate each statement from 1 (Strongly Disagree) to 5 (Strongly Agree)");
            System.out.println("----------------------------------------------------------------");

            int[] responses = new int[5];
            String[] questions = {
                    "Q1: I enjoy taking the lead and guiding others during group activities.",
                    "Q2: I prefer analyzing situations and coming up with strategic solutions.",
                    "Q3: I work well with others and enjoy collaborative teamwork.",
                    "Q4: I am calm under pressure and can help maintain team morale.",
                    "Q5: I like making quick decisions and adapting in dynamic situations."
            };

            for (int i = 0; i < 5; i++) {
                System.out.println("\n" + questions[i]);
                boolean validResponse = false;
                while (!validResponse) {
                    try {
                        String input = getUserInput("Your response (1-5): ");
                        int response = Integer.parseInt(input);
                        if (response < 1 || response > 5) {
                            throw new InvalidInputException("Response must be between 1 and 5");
                        }
                        responses[i] = response;
                        validResponse = true;
                    } catch (NumberFormatException e) {
                        System.err.println("[ERROR] Please enter a valid number between 1 and 5");
                    } catch (InvalidInputException e) {
                        System.err.println("[ERROR] " + e.getMessage());
                    }
                }
            }

            // Calculate personality: Total (5-25) * 4 = Score (50-100)
            int personalityScore = PersonalityClassifier.calculateScore(responses);
            PersonalityType personalityType = PersonalityClassifier.classify(personalityScore);

            System.out.println("\n[SURVEY RESULTS]");
            System.out.println("Your Personality Score: " + personalityScore);
            System.out.println("Your Personality Type: " + personalityType);
            System.out.println("Description: " + personalityType.getDescription());

            System.out.println("\n[GAMING PREFERENCES]");
            String preferredGame = getUserInput("Enter your preferred game/sport (e.g., FIFA, CS:GO, Basketball): ");
            if (!ValidationUtils.isNotEmpty(preferredGame)) {
                throw new InvalidInputException("Preferred game cannot be empty");
            }

            System.out.println("\nAvailable Roles:");
            System.out.println("  1. Strategist - Focuses on tactics and planning");
            System.out.println("  2. Attacker - Frontline player with offensive tactics");
            System.out.println("  3. Defender - Protects and supports team stability");
            System.out.println("  4. Supporter - Jack-of-all-trades, adapts roles");
            System.out.println("  5. Coordinator - Communication lead");

            Role preferredRole = null;
            boolean validRole = false;
            while (!validRole) {
                String roleChoice = getUserInput("Select your preferred role (1-5): ");
                try {
                    int choice = Integer.parseInt(roleChoice);
                    switch (choice) {
                        case 1: preferredRole = Role.STRATEGIST; validRole = true; break;
                        case 2: preferredRole = Role.ATTACKER; validRole = true; break;
                        case 3: preferredRole = Role.DEFENDER; validRole = true; break;
                        case 4: preferredRole = Role.SUPPORTER; validRole = true; break;
                        case 5: preferredRole = Role.COORDINATOR; validRole = true; break;
                        default: System.err.println("[ERROR] Invalid choice. Please enter 1-5.");
                    }
                } catch (NumberFormatException e) {
                    System.err.println("[ERROR] Please enter a valid number.");
                }
            }

            String skillInput = getUserInput("Enter your skill level (1-10): ");
            int skillLevel = Integer.parseInt(skillInput);
            if (!ValidationUtils.isValidSkillLevel(skillLevel)) {
                throw new InvalidInputException("Skill level must be between 1 and 10");
            }

            // Register with auto-generated ID
            Participant participant = userService.registerParticipant(name, email, preferredGame,
                    skillLevel, preferredRole, personalityScore);

            System.out.println("\n[SUCCESS] Registration completed successfully!");
            System.out.println("Your Participant ID: " + participant.getId());
            System.out.println("Welcome to TeamMate, " + name + "!");
            Logger.logInfo("Participant registered: " + participant.getId());

            participantMenu(participant);

        } catch (InvalidInputException e) {
            System.err.println("[ERROR] Registration failed: " + e.getMessage());
            Logger.logWarning("Registration failed: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("[ERROR] Invalid number format. Registration cancelled.");
        } catch (Exception e) {
            System.err.println("[ERROR] Registration error: " + e.getMessage());
            Logger.logException("Registration error", e);
        }
    }

    private static void loginParticipant() {
        try {
            System.out.println("\n[PARTICIPANT LOGIN]");
            String id = getUserInput("Enter your Participant ID: ");

            if (!ValidationUtils.isValidParticipantId(id)) {
                throw new InvalidInputException("Invalid ID format");
            }

            Participant participant = userService.getParticipant(id);

            if (participant == null) {
                System.err.println("[ERROR] Participant not found. Please register first.");
                Logger.logWarning("Login failed: " + id + " not found");
                return;
            }

            System.out.println("[SUCCESS] Welcome back, " + participant.getName() + "!");
            Logger.logInfo("Participant logged in: " + id);

            participantMenu(participant);

        } catch (InvalidInputException e) {
            System.err.println("[ERROR] " + e.getMessage());
        } catch (Exception e) {
            System.err.println("[ERROR] Login failed: " + e.getMessage());
            Logger.logException("Login error", e);
        }
    }

    private static void participantMenu(Participant participant) {
        boolean logout = false;

        while (!logout) {
            System.out.println("\n[PARTICIPANT MENU - " + participant.getName() + "]");
            System.out.println("================================================================");
            System.out.println("  1. View My Profile");
            System.out.println("  2. View My Team");
            System.out.println("  3. Update Profile");
            System.out.println("  4. Retake Personality Survey");
            System.out.println("  5. Logout");
            System.out.println("================================================================");

            String choice = getUserInput("Enter your choice (1-5): ");

            switch (choice) {
                case "1": viewParticipantProfile(participant); break;
                case "2": viewParticipantTeam(participant); break;
                case "3": updateParticipantProfile(participant); break;
                case "4": retakeSurvey(participant); break;
                case "5":
                    logout = true;
                    System.out.println("[INFO] Logged out successfully.");
                    Logger.logInfo("Participant logout: " + participant.getId());
                    break;
                default: System.err.println("[ERROR] Invalid choice.");
            }
        }
    }

    private static void viewParticipantProfile(Participant participant) {
        System.out.println("\n[MY PROFILE]");
        System.out.println("================================================================");
        System.out.println("Participant ID:     " + participant.getId());
        System.out.println("Name:               " + participant.getName());
        System.out.println("Email:              " + participant.getEmail());
        System.out.println("Preferred Game:     " + participant.getPreferredGame());
        System.out.println("Skill Level:        " + participant.getSkillLevel() + "/10");
        System.out.println("Preferred Role:     " + participant.getPreferredRole());
        System.out.println("Personality Type:   " + participant.getPersonalityType());
        System.out.println("Personality Score:  " + participant.getPersonalityScore());
        System.out.println("================================================================");
    }

    private static void viewParticipantTeam(Participant participant) {
        Team team = teamService.getTeamByParticipant(participant.getId());

        if (team == null) {
            System.out.println("\n[INFO] You are not currently assigned to any team.");
            System.out.println("[INFO] Teams are formed by the organizer.");
            return;
        }

        System.out.println("\n[MY TEAM - " + team.getTeamId() + "]");
        System.out.println("================================================================");
        System.out.println("Team Average Skill: " + String.format("%.2f", team.getAverageSkill()));
        System.out.println("\nTeam Members:");
        System.out.println("----------------------------------------------------------------");

        for (Participant member : team.getMembers()) {
            String marker = member.getId().equals(participant.getId()) ? " (YOU)" : "";
            System.out.printf("%-15s | %-10s | %-12s | %-15s%s\n",
                    member.getName(),
                    member.getPersonalityType(),
                    member.getPreferredRole(),
                    member.getPreferredGame(),
                    marker);
        }

        System.out.println("================================================================");
    }

    private static void updateParticipantProfile(Participant participant) {
        System.out.println("\n[UPDATE PROFILE]");
        System.out.println("Leave blank to keep current value");
        System.out.println("----------------------------------------------------------------");

        try {
            String email = getUserInput("New email [" + participant.getEmail() + "]: ");
            if (!email.isEmpty()) {
                if (ValidationUtils.isValidEmail(email)) {
                    participant.setEmail(email);
                } else {
                    System.err.println("[WARNING] Invalid email format. Keeping current value.");
                }
            }

            String game = getUserInput("New preferred game [" + participant.getPreferredGame() + "]: ");
            if (!game.isEmpty()) {
                participant.setPreferredGame(game);
            }

            String skillInput = getUserInput("New skill level [" + participant.getSkillLevel() + "]: ");
            if (!skillInput.isEmpty()) {
                try {
                    int skill = Integer.parseInt(skillInput);
                    if (ValidationUtils.isValidSkillLevel(skill)) {
                        participant.setSkillLevel(skill);
                    } else {
                        System.err.println("[WARNING] Invalid skill level. Keeping current value.");
                    }
                } catch (NumberFormatException e) {
                    System.err.println("[WARNING] Invalid number. Keeping current value.");
                }
            }

            userService.updateParticipant(participant);
            System.out.println("[SUCCESS] Profile updated successfully!");

        } catch (Exception e) {
            System.err.println("[ERROR] Update failed: " + e.getMessage());
        }
    }

    private static void retakeSurvey(Participant participant) {
        System.out.println("\n[RETAKE PERSONALITY SURVEY]");
        System.out.println("Rate each statement from 1 (Strongly Disagree) to 5 (Strongly Agree)");
        System.out.println("----------------------------------------------------------------");

        try {
            int[] responses = new int[5];
            String[] questions = {
                    "Q1: I enjoy taking the lead and guiding others during group activities.",
                    "Q2: I prefer analyzing situations and coming up with strategic solutions.",
                    "Q3: I work well with others and enjoy collaborative teamwork.",
                    "Q4: I am calm under pressure and can help maintain team morale.",
                    "Q5: I like making quick decisions and adapting in dynamic situations."
            };

            for (int i = 0; i < 5; i++) {
                System.out.println("\n" + questions[i]);
                boolean validResponse = false;
                while (!validResponse) {
                    try {
                        String input = getUserInput("Your response (1-5): ");
                        int response = Integer.parseInt(input);
                        if (response < 1 || response > 5) {
                            throw new InvalidInputException("Response must be between 1 and 5");
                        }
                        responses[i] = response;
                        validResponse = true;
                    } catch (NumberFormatException e) {
                        System.err.println("[ERROR] Please enter a valid number.");
                    }
                }
            }

            int oldScore = participant.getPersonalityScore();
            int newScore = PersonalityClassifier.calculateScore(responses);
            participant.setPersonalityScore(newScore);

            System.out.println("\n[NEW SURVEY RESULTS]");
            System.out.println("Previous Score: " + oldScore);
            System.out.println("New Score: " + newScore);
            System.out.println("New Type: " + participant.getPersonalityType());

            userService.updateParticipant(participant);
            System.out.println("[SUCCESS] Survey updated successfully!");

        } catch (Exception e) {
            System.err.println("[ERROR] Survey update failed: " + e.getMessage());
        }
    }

    private static void organizerFlow() {
        System.out.println("\n[ORGANIZER LOGIN]");
        String password = getUserInput("Enter organizer password: ");

        if (!password.equals(ORGANIZER_PASSWORD)) {
            System.err.println("[ERROR] Invalid password. Access denied.");
            Logger.logWarning("Failed organizer login");
            return;
        }

        System.out.println("[SUCCESS] Welcome, Organizer!");
        Logger.logInfo("Organizer logged in");

        organizerMenu();
    }

    private static void organizerMenu() {
        boolean logout = false;

        while (!logout) {
            System.out.println("\n[ORGANIZER MENU]");
            System.out.println("================================================================");
            System.out.println("  1. View All Participants");
            System.out.println("  2. Generate Teams");
            System.out.println("  3. View All Teams");
            System.out.println("  4. Export Teams to CSV");
            System.out.println("  5. View Statistics");
            System.out.println("  6. Search Participant");
            System.out.println("  7. Add Participant");
            System.out.println("  8. Update Participant");
            System.out.println("  9. Delete Participant");
            System.out.println(" 10. Logout");
            System.out.println("================================================================");

            String choice = getUserInput("Enter your choice (1-10): ");

            switch (choice) {
                case "1": viewAllParticipants(); break;
                case "2": generateTeams(); break;
                case "3": viewAllTeams(); break;
                case "4": exportTeamsToCSV(); break;
                case "5": viewStatistics(); break;
                case "6": searchParticipant(); break;
                case "7": registerParticipant(); break;
                case "8": organizerUpdateParticipant(); break;
                case "9": deleteParticipant(); break;
                case "10":
                    logout = true;
                    System.out.println("[INFO] Logged out successfully.");
                    Logger.logInfo("Organizer logout");
                    break;
                default: System.err.println("[ERROR] Invalid choice.");
            }
        }
    }

    private static void viewAllParticipants() {
        List<Participant> participants = userService.getAllParticipants();

        if (participants.isEmpty()) {
            System.out.println("\n[INFO] No participants registered yet.");
            return;
        }

        System.out.println("\n[ALL PARTICIPANTS]");
        System.out.println("================================================================");
        System.out.printf("%-8s | %-20s | %-12s | %-10s | %-10s | Skill\n",
                "ID", "Name", "Game", "Role", "Type");
        System.out.println("----------------------------------------------------------------");

        for (Participant p : participants) {
            System.out.printf("%-8s | %-20s | %-12s | %-10s | %-10s | %d\n",
                    p.getId(),
                    p.getName().length() > 20 ? p.getName().substring(0, 17) + "..." : p.getName(),
                    p.getPreferredGame(),
                    p.getPreferredRole(),
                    p.getPersonalityType(),
                    p.getSkillLevel());
        }

        System.out.println("================================================================");
        System.out.println("Total Participants: " + participants.size());
    }

    /**
     * Generates balanced teams using concurrent processing
     */
    private static void generateTeams() {
        try {
            List<Participant> participants = userService.getAllParticipants();

            if (participants.isEmpty()) {
                System.err.println("[ERROR] No participants available.");
                return;
            }

            System.out.println("\n[GENERATE TEAMS]");
            System.out.println("Total Participants: " + participants.size());

            String sizeInput = getUserInput("Enter team size (3-10): ");
            int teamSize = Integer.parseInt(sizeInput);

            if (!ValidationUtils.isValidTeamSize(teamSize, participants.size())) {
                throw new InvalidInputException("Invalid team size or insufficient participants");
            }

            System.out.println("[INFO] Generating teams using concurrent processing...");
            long startTime = System.currentTimeMillis();

            List<Team> teams = teamService.generateTeams(participants, teamSize);

            long endTime = System.currentTimeMillis();
            double duration = (endTime - startTime) / 1000.0;

            System.out.println("[SUCCESS] Generated " + teams.size() + " teams in " +
                    String.format("%.2f", duration) + " seconds");
            Logger.logInfo("Generated " + teams.size() + " teams");

            viewAllTeams();

        } catch (Exception e) {
            System.err.println("[ERROR] Team generation failed: " + e.getMessage());
            Logger.logException("Team generation error", e);
        }
    }

    private static void viewAllTeams() {
        List<Team> teams = teamService.getAllTeams();

        if (teams.isEmpty()) {
            System.out.println("\n[INFO] No teams formed yet. Use 'Generate Teams' first.");
            return;
        }

        System.out.println("\n[ALL TEAMS]");
        System.out.println("================================================================");

        for (Team team : teams) {
            System.out.println("\n" + team.getTeamId() + " (Members: " + team.getCurrentSize() +
                    ", Avg Skill: " + String.format("%.2f", team.getAverageSkill()) + ")");
            System.out.println("----------------------------------------------------------------");

            for (Participant member : team.getMembers()) {
                System.out.printf("  %-15s | %-10s | %-12s | %-15s | Skill: %d\n",
                        member.getName(),
                        member.getPersonalityType(),
                        member.getPreferredRole(),
                        member.getPreferredGame(),
                        member.getSkillLevel());
            }
        }

        System.out.println("\n================================================================");
        System.out.println("Total Teams: " + teams.size());
    }

    private static void exportTeamsToCSV() {
        try {
            List<Team> teams = teamService.getAllTeams();

            if (teams.isEmpty()) {
                System.err.println("[ERROR] No teams to export. Generate teams first.");
                return;
            }

            String filePath = getUserInput("Enter output file path [formed_teams.csv]: ");
            if (filePath.isEmpty()) {
                filePath = "formed_teams.csv";
            }

            teamService.exportToCSV(teams, filePath);
            System.out.println("[SUCCESS] Teams exported to: " + filePath);

        } catch (Exception e) {
            System.err.println("[ERROR] Export failed: " + e.getMessage());
        }
    }

    private static void viewStatistics() {
        List<Participant> participants = userService.getAllParticipants();
        List<Team> teams = teamService.getAllTeams();

        System.out.println("\n[SYSTEM STATISTICS]");
        System.out.println("================================================================");
        System.out.println("Total Participants: " + participants.size());
        System.out.println("Total Teams: " + teams.size());

        if (!participants.isEmpty()) {
            Map<PersonalityType, Long> personalityDist = new HashMap<>();

            for (Participant p : participants) {
                personalityDist.merge(p.getPersonalityType(), 1L, Long::sum);
            }

            System.out.println("\nPersonality Distribution:");
            personalityDist.forEach((type, count) ->
                    System.out.printf("  %s: %d (%.1f%%)\n", type, count, count * 100.0 / participants.size()));

            double avgSkill = participants.stream()
                    .mapToInt(Participant::getSkillLevel)
                    .average()
                    .orElse(0.0);
            System.out.printf("\nAverage Skill Level: %.2f\n", avgSkill);
        }

        System.out.println("================================================================");
    }

    private static void searchParticipant() {
        String id = getUserInput("Enter Participant ID: ");
        Participant p = userService.getParticipant(id);

        if (p == null) {
            System.err.println("[ERROR] Participant not found.");
            return;
        }

        System.out.println("\n[PARTICIPANT DETAILS]");
        System.out.println("ID: " + p.getId() + " | Name: " + p.getName());
        System.out.println("Email: " + p.getEmail());
        System.out.println("Game: " + p.getPreferredGame() + " | Role: " + p.getPreferredRole());
        System.out.println("Skill: " + p.getSkillLevel() + " | Personality: " + p.getPersonalityType());
    }

    private static void organizerUpdateParticipant() {
        try {
            String id = getUserInput("Enter Participant ID: ");
            Participant p = userService.getParticipant(id);

            if (p == null) {
                System.err.println("[ERROR] Participant not found.");
                return;
            }

            System.out.println("\n[UPDATE: " + p.getName() + "]");

            String name = getUserInput("New name [" + p.getName() + "]: ");
            if (!name.isEmpty()) p.setName(name);

            String email = getUserInput("New email [" + p.getEmail() + "]: ");
            if (!email.isEmpty() && ValidationUtils.isValidEmail(email)) p.setEmail(email);

            String game = getUserInput("New game [" + p.getPreferredGame() + "]: ");
            if (!game.isEmpty()) p.setPreferredGame(game);

            String skillInput = getUserInput("New skill [" + p.getSkillLevel() + "]: ");
            if (!skillInput.isEmpty()) {
                int skill = Integer.parseInt(skillInput);
                if (ValidationUtils.isValidSkillLevel(skill)) p.setSkillLevel(skill);
            }

            userService.updateParticipant(p);
            System.out.println("[SUCCESS] Participant updated!");

        } catch (Exception e) {
            System.err.println("[ERROR] Update failed: " + e.getMessage());
        }
    }

    private static void deleteParticipant() {
        try {
            String id = getUserInput("Enter Participant ID to delete: ");
            Participant p = userService.getParticipant(id);

            if (p == null) {
                System.err.println("[ERROR] Participant not found.");
                return;
            }

            System.out.println("\n[DELETE: " + p.getName() + " (" + id + ")]");
            String confirm = getUserInput("Confirm deletion? (yes/no): ");

            if (!confirm.equalsIgnoreCase("yes")) {
                System.out.println("[INFO] Deletion cancelled.");
                return;
            }

            userService.deleteParticipant(id);

            if (teamService.getTeamByParticipant(id) != null) {
                teamService.clearAllTeams();
                System.out.println("[INFO] Teams cleared. Please regenerate.");
            }

            System.out.println("[SUCCESS] Participant deleted!");

        } catch (Exception e) {
            System.err.println("[ERROR] Deletion failed: " + e.getMessage());
        }
    }

    private static String getUserInput(String prompt) {
        System.out.print(prompt);
        try {
            return scanner.nextLine().trim();
        } catch (Exception e) {
            return "";
        }
    }
}