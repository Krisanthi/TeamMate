package com.teammate;

import com.teammate.model.*;
import com.teammate.service.*;
import com.teammate.util.*;
import java.util.*;

/**
 * TeamMate: Intelligent Team Formation System
 *
 * UPDATED VERSION:
 * - Removed "Clear All Teams" option (option 10)
 * - Changed all "Back/Exit" options to "0" for consistency
 * - Exit is now option 0 in all menus
 * - CSV load appears after organizer authentication
 *
 * @author Student Name
 * @version 2.1
 * @since 2025
 */
public class TeamMateApp {

    private static final String ORGANIZER_PASSWORD = "Teammate";
    private static final String PARTICIPANT_CSV = "participants_sample.csv";
    private static final Scanner scanner = new Scanner(System.in);
    private static UserService userService;
    private static TeamService teamService;

    public static void main(String[] args) {
        Logger.logInfo("TeamMate application started");

        System.out.println("================================================================");
        System.out.println("   TeamMate: Intelligent Team Formation System");
        System.out.println("   University Gaming Club Management");
        System.out.println("================================================================\n");

        try {
            userService = new UserService(PARTICIPANT_CSV);
            teamService = new TeamService();

            boolean exit = false;
            while (!exit) {
                displayWelcomeMenu();
                String choice = getUserInput("Enter your choice (0-2): ");

                switch (choice) {
                    case "1":
                        participantFlow();
                        break;
                    case "2":
                        organizerFlow();
                        break;
                    case "0":
                        exit = true;
                        System.out.println("\n[INFO] Thank you for using TeamMate. Goodbye!");
                        Logger.logInfo("User chose to exit application");
                        break;
                    default:
                        System.err.println("[ERROR] Invalid choice. Please enter 0, 1, or 2.");
                }
            }

        } catch (Exception e) {
            System.err.println("[ERROR] Application error: " + e.getMessage());
            Logger.logException("Critical application error", e);
        } finally {
            if (scanner != null) {
                scanner.close();
            }
            Logger.logInfo("Application terminated");
        }
    }

    private static void displayWelcomeMenu() {
        System.out.println("\n================================================================");
        System.out.println("                    WELCOME TO TEAMMATE");
        System.out.println("================================================================");
        System.out.println("  1. Participant Login/Register");
        System.out.println("  2. Organizer Login");
        System.out.println("  0. Exit");
        System.out.println("================================================================");
    }

    private static void participantFlow() {
        System.out.println("\n[PARTICIPANT MENU]");
        System.out.println("----------------------------------------------------------------");
        System.out.println("  1. Register as New Participant");
        System.out.println("  2. Login as Existing Participant");
        System.out.println("  0. Back to Main Menu");
        System.out.println("----------------------------------------------------------------");

        String choice = getUserInput("Enter your choice (0-2): ");

        switch (choice) {
            case "1":
                registerParticipant();
                break;
            case "2":
                loginParticipant();
                break;
            case "0":
                return;
            default:
                System.err.println("[ERROR] Invalid choice.");
        }
    }

    /**
     * UC-01: Register New Participant
     *
     * With EXACT 5 personality survey questions and EXACT role descriptions
     */
    private static void registerParticipant() {
        Logger.logInfo("New participant registration initiated");

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

            // EXACT 5 PERSONALITY SURVEY QUESTIONS (as per specification)
            System.out.println("\n[PERSONALITY SURVEY]");
            System.out.println("Rate each statement from 1 (Strongly Disagree) to 5 (Strongly Agree)");
            System.out.println("================================================================");

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

            int personalityScore = PersonalityClassifier.calculateScore(responses);
            PersonalityType personalityType = PersonalityClassifier.classify(personalityScore);

            System.out.println("\n[SURVEY RESULTS]");
            System.out.println("Your Personality Type: " + personalityType.getDisplayName());
            System.out.println("Score: " + personalityScore + "/100");
            System.out.println("Description: " + personalityType.getDescription());

            String preferredGame = getUserInput("\nEnter your preferred game (eg: FIFA, Chess, Basketball, CS:GO, Dota 2, Valorant): ");
            if (!ValidationUtils.isNotEmpty(preferredGame)) {
                throw new InvalidInputException("Game/sport cannot be empty");
            }

            // EXACT ROLE DESCRIPTIONS (as per specification)
            System.out.println("\n[SELECT YOUR PREFERRED ROLE]");
            System.out.println("================================================================");
            System.out.println("1. Strategist");
            System.out.println("   Focuses on tactics and planning. Keeps the bigger picture in mind during gameplay.");
            System.out.println("\n2. Attacker");
            System.out.println("   Frontline player. Good reflexes, offensive tactics, quick execution.");
            System.out.println("\n3. Defender");
            System.out.println("   Protects and supports team stability. Good under pressure and team-focused.");
            System.out.println("\n4. Supporter");
            System.out.println("   Jack-of-all-trades. Adapts roles, ensures smooth coordination.");
            System.out.println("\n5. Coordinator");
            System.out.println("   Communication lead. Keeps the team informed and organized in real time.");
            System.out.println("================================================================");

            Role preferredRole = null;
            boolean validRole = false;
            while (!validRole) {
                String roleChoice = getUserInput("Select your role (1-5): ");
                try {
                    int choice = Integer.parseInt(roleChoice);
                    switch (choice) {
                        case 1: preferredRole = Role.STRATEGIST; validRole = true; break;
                        case 2: preferredRole = Role.ATTACKER; validRole = true; break;
                        case 3: preferredRole = Role.DEFENDER; validRole = true; break;
                        case 4: preferredRole = Role.SUPPORTER; validRole = true; break;
                        case 5: preferredRole = Role.COORDINATOR; validRole = true; break;
                        default: System.err.println("[ERROR] Please enter a number between 1 and 5");
                    }
                } catch (NumberFormatException e) {
                    System.err.println("[ERROR] Please enter a valid number");
                }
            }

            int skillLevel = 0;
            boolean validSkill = false;
            while (!validSkill) {
                String skillInput = getUserInput("Enter your skill level (1-10): ");
                try {
                    skillLevel = Integer.parseInt(skillInput);
                    if (!ValidationUtils.isValidSkillLevel(skillLevel)) {
                        throw new InvalidInputException("Skill level must be between 1 and 10");
                    }
                    validSkill = true;
                } catch (NumberFormatException e) {
                    System.err.println("[ERROR] Please enter a valid number");
                } catch (InvalidInputException e) {
                    System.err.println("[ERROR] " + e.getMessage());
                }
            }

            Participant participant = userService.registerParticipant(
                    name, email, preferredGame, skillLevel, preferredRole, personalityScore);

            System.out.println("\n[SUCCESS] Registration Complete!");
            System.out.println("Your Participant ID: " + participant.getId());
            System.out.println("Save this ID for future logins.");

        } catch (InvalidInputException e) {
            System.err.println("[ERROR] Registration failed: " + e.getMessage());
            Logger.logError("Registration failed: " + e.getMessage());
        }
    }

    /**
     * UC-02: Login Participant
     */
    private static void loginParticipant() {
        String id = getUserInput("Enter your Participant ID: ");
        Participant participant = userService.getParticipant(id);

        if (participant == null) {
            System.err.println("[ERROR] If participants loaded already, participant not found. Please register first.Else, organiser didn't load participants yet..");
            return;
        }

        System.out.println("\n[WELCOME] " + participant.getName());
        participantMenu(participant);
    }

    private static void participantMenu(Participant participant) {
        boolean back = false;
        while (!back) {
            System.out.println("\n[PARTICIPANT MENU - " + participant.getName() + "]");
            System.out.println("----------------------------------------------------------------");
            System.out.println("  1. View My Profile");
            System.out.println("  2. Update My Profile");
            System.out.println("  3. View My Team");
            System.out.println("  0. Back to Main Menu");
            System.out.println("----------------------------------------------------------------");

            String choice = getUserInput("Enter your choice (0-3): ");

            switch (choice) {
                case "1":
                    viewParticipantProfile(participant);
                    break;
                case "2":
                    updateParticipantProfile(participant);
                    break;
                case "3":
                    viewParticipantTeam(participant);
                    break;
                case "0":
                    back = true;
                    break;
                default:
                    System.err.println("[ERROR] Invalid choice.");
            }
        }
    }

    private static void viewParticipantProfile(Participant participant) {
        System.out.println("\n[MY PROFILE]");
        System.out.println("================================================================");
        System.out.println("ID: " + participant.getId());
        System.out.println("Name: " + participant.getName());
        System.out.println("Email: " + participant.getEmail());
        System.out.println("Preferred Game: " + participant.getPreferredGame());
        System.out.println("Skill Level: " + participant.getSkillLevel() + "/10");
        System.out.println("Preferred Role: " + participant.getPreferredRole());
        System.out.println("Personality Type: " + participant.getPersonalityType() +
                " (Score: " + participant.getPersonalityScore() + "/100)");
        System.out.println("================================================================");
    }

    private static void updateParticipantProfile(Participant participant) {
        try {
            System.out.println("\n[UPDATE PROFILE]");
            System.out.println("Leave blank to keep current value");

            String name = getUserInput("New name [" + participant.getName() + "]: ");
            if (!name.isEmpty()) participant.setName(name);

            String email = getUserInput("New email [" + participant.getEmail() + "]: ");
            if (!email.isEmpty() && ValidationUtils.isValidEmail(email)) {
                participant.setEmail(email);
            }

            String game = getUserInput("New preferred game [" + participant.getPreferredGame() + "]: ");
            if (!game.isEmpty()) participant.setPreferredGame(game);

            String skillInput = getUserInput("New skill level [" + participant.getSkillLevel() + "]: ");
            if (!skillInput.isEmpty()) {
                int skill = Integer.parseInt(skillInput);
                if (ValidationUtils.isValidSkillLevel(skill)) {
                    participant.setSkillLevel(skill);
                }
            }

            userService.updateParticipant(participant);
            System.out.println("[SUCCESS] Profile updated!");

        } catch (Exception e) {
            System.err.println("[ERROR] Update failed: " + e.getMessage());
        }
    }

    private static void viewParticipantTeam(Participant participant) {
        Team team = teamService.getTeamByParticipant(participant.getId());

        if (team == null) {
            System.out.println("\n[INFO] You are not assigned to a team yet.");
            System.out.println("Teams will be formed by the organizer.");
            return;
        }

        System.out.println("\n[YOUR TEAM: " + team.getTeamId() + "]");
        System.out.println("================================================================");
        System.out.println("Team Size: " + team.getCurrentSize() + "/" + team.getTeamSize());
        System.out.println("Average Skill: " + String.format("%.2f", team.getAverageSkill()));
        System.out.println("\nTeam Members:");
        System.out.println("----------------------------------------------------------------");

        for (Participant member : team.getMembers()) {
            String marker = member.getId().equals(participant.getId()) ? " (YOU)" : "";
            System.out.printf("%-15s | %-10s | %-12s | Skill: %d%s\n",
                    member.getName(),
                    member.getPersonalityType(),
                    member.getPreferredRole(),
                    member.getSkillLevel(),
                    marker);
        }
        System.out.println("================================================================");
    }

    /**
     * UC-03: Organizer Login
     *
     * After authentication, CSV must be loaded successfully to access menu
     */
    private static void organizerFlow() {
        System.out.println("\n[ORGANIZER LOGIN]");
        String password = getUserInput("Enter organizer password: ");

        if (!password.equals(ORGANIZER_PASSWORD)) {
            System.err.println("[ERROR] Invalid password!");
            return;
        }

        System.out.println("[SUCCESS] Login successful!");

        // Load CSV - only proceed to menu if successful
        if (loadParticipantsFromCSV()) {
            organizerMenu();
        }
        // If load failed, automatically returns to main menu
    }

    private static void organizerMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n[ORGANIZER MENU]");
            System.out.println("================================================================");
            System.out.println("  1. View All Participants");
            System.out.println("  2. Search Participant");
            System.out.println("  3. Update Participant");
            System.out.println("  4. Delete Participant");
            System.out.println("  5. Generate Teams");
            System.out.println("  6. View All Teams with Statistics");
            System.out.println("  7. View Unassigned Participants");
            System.out.println("  8. Export Teams to CSV");
            System.out.println("  0. Back to Main Menu");
            System.out.println("================================================================");

            String choice = getUserInput("Enter your choice (0-8): ");

            switch (choice) {
                case "1": viewAllParticipants(); break;
                case "2": searchParticipant(); break;
                case "3": organizerUpdateParticipant(); break;
                case "4": deleteParticipant(); break;
                case "5": generateTeams(); break;
                case "6": viewAllTeamsWithStatistics(); break;
                case "7": viewUnassignedParticipants(); break;
                case "8": exportTeamsToCSV(); break;
                case "0": back = true; break;
                default: System.err.println("[ERROR] Invalid choice.");
            }
        }
    }

    private static boolean loadParticipantsFromCSV() {
        try {
            String filePath = getUserInput("Enter CSV file path [" + PARTICIPANT_CSV + "]: ");
            if (filePath.isEmpty()) {
                filePath = PARTICIPANT_CSV;
            }

            int count = userService.loadFromCSV(filePath);
            System.out.println("[SUCCESS] Loaded " + count + " participants from CSV");
            return true;  // Success

        } catch (FileProcessingException e) {
            System.err.println("[ERROR] Failed to load CSV: " + e.getMessage());
            return false;  // Failure
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
        System.out.printf("%-8s | %-20s | %-25s | %-10s | %-12s | %s\n",
                "ID", "Name", "Email", "Personality", "Role", "Skill");
        System.out.println("----------------------------------------------------------------");

        for (Participant p : participants) {
            System.out.printf("%-8s | %-20s | %-25s | %-10s | %-12s | %d\n",
                    p.getId(),
                    p.getName(),
                    p.getEmail(),
                    p.getPersonalityType(),
                    p.getPreferredRole(),
                    p.getSkillLevel());
        }

        System.out.println("================================================================");
        System.out.println("Total Participants: " + participants.size());
    }

    private static void generateTeams() {
        try {
            List<Participant> participants = userService.getAllParticipants();

            if (participants.isEmpty()) {
                System.err.println("[ERROR] No participants to form teams. Load participants first.");
                return;
            }

            System.out.println("\n[TEAM GENERATION]");
            System.out.println("Total Participants: " + participants.size());

            String sizeInput = getUserInput("Enter team size (3-10): ");
            int teamSize = Integer.parseInt(sizeInput);

            if (!ValidationUtils.isValidTeamSize(teamSize, participants.size())) {
                System.err.println("[ERROR] Invalid team size. Must be 3-10 and allow at least 2 teams.");
                return;
            }

            System.out.println("\n[INFO] Generating teams using concurrent algorithm...");

            List<Team> teams = teamService.generateTeams(participants, teamSize);

            System.out.println("\n[SUCCESS] Generated " + teams.size() + " teams!");
            System.out.println("Use '7. View All Teams with Statistics' to see details.");
            System.out.println("Use '9. Export Teams to CSV' to save the results.");

        } catch (NumberFormatException e) {
            System.err.println("[ERROR] Please enter a valid number.");
        } catch (Exception e) {
            System.err.println("[ERROR] Team generation failed: " + e.getMessage());
            Logger.logException("Team generation failed", e);
        }
    }

    /**
     * UC-05: View All Teams WITH COMPREHENSIVE STATISTICS
     */
    private static void viewAllTeamsWithStatistics() {
        List<Team> teams = teamService.getAllTeams();

        if (teams.isEmpty()) {
            System.out.println("\n[INFO] No teams formed yet. Use 'Generate Teams' first.");
            return;
        }

        System.out.println("\n[ALL TEAMS WITH STATISTICS]");
        System.out.println("================================================================");

        for (Team team : teams) {
            System.out.println("\n" + team.getTeamId() + " (Members: " + team.getCurrentSize() +
                    "/" + team.getTeamSize() + ", Avg Skill: " +
                    String.format("%.2f", team.getAverageSkill()) + ")");
            System.out.println("----------------------------------------------------------------");

            // Display members
            System.out.println("MEMBERS:");
            for (Participant member : team.getMembers()) {
                System.out.printf("  %-15s | %-10s | %-12s | %-15s | Skill: %d\n",
                        member.getName(),
                        member.getPersonalityType(),
                        member.getPreferredRole(),
                        member.getPreferredGame(),
                        member.getSkillLevel());
            }

            // Display statistics
            System.out.println("\nSTATISTICS:");

            // Personality distribution
            int leaders = team.getPersonalityCount(PersonalityType.LEADER);
            int balanced = team.getPersonalityCount(PersonalityType.BALANCED);
            int thinkers = team.getPersonalityCount(PersonalityType.THINKER);
            System.out.printf("  Personality: %d Leader, %d Balanced, %d Thinker\n",
                    leaders, balanced, thinkers);

            // Role distribution
            System.out.print("  Roles: ");
            for (Role role : Role.values()) {
                int count = team.getRoleCount(role);
                if (count > 0) {
                    System.out.print(role + "(" + count + ") ");
                }
            }
            System.out.println();

            // Game distribution
            System.out.print("  Games: ");
            Set<String> games = new HashSet<>();
            for (Participant p : team.getMembers()) {
                games.add(p.getPreferredGame());
            }
            for (String game : games) {
                int count = team.getGameCount(game);
                System.out.print(game + "(" + count + ") ");
            }
            System.out.println();

            // Balance status
            System.out.println("  Balance Status: " + (team.isBalanced() ? " BALANCED" : "✗ NEEDS ADJUSTMENT"));
        }

        System.out.println("\n================================================================");
        System.out.println("GLOBAL STATISTICS:");
        System.out.println("Total Teams: " + teams.size());

        // Global average skill
        double globalAvgSkill = teams.stream()
                .mapToDouble(Team::getAverageSkill)
                .average()
                .orElse(0.0);
        System.out.printf("Global Average Skill: %.2f\n", globalAvgSkill);

        // Balanced teams count
        long balancedTeams = teams.stream().filter(Team::isBalanced).count();
        System.out.printf("Balanced Teams: %d/%d (%.1f%%)\n",
                balancedTeams, teams.size(), (balancedTeams * 100.0 / teams.size()));
        System.out.println("================================================================");
    }

    private static void viewUnassignedParticipants() {
        List<Participant> allParticipants = userService.getAllParticipants();
        List<Team> teams = teamService.getAllTeams();

        Set<String> assignedIds = new HashSet<>();
        for (Team team : teams) {
            for (Participant p : team.getMembers()) {
                assignedIds.add(p.getId());
            }
        }

        List<Participant> unassigned = new ArrayList<>();
        for (Participant p : allParticipants) {
            if (!assignedIds.contains(p.getId())) {
                unassigned.add(p);
            }
        }

        System.out.println("\n[UNASSIGNED PARTICIPANTS]");
        System.out.println("================================================================");

        if (unassigned.isEmpty()) {
            System.out.println("All participants have been assigned to teams!");
            return;
        }

        System.out.printf("%-8s | %-20s | %-25s | %-10s | %-12s | %s\n",
                "ID", "Name", "Email", "Personality", "Role", "Skill");
        System.out.println("----------------------------------------------------------------");

        for (Participant p : unassigned) {
            System.out.printf("%-8s | %-20s | %-25s | %-10s | %-12s | %d\n",
                    p.getId(),
                    p.getName(),
                    p.getEmail(),
                    p.getPersonalityType(),
                    p.getPreferredRole(),
                    p.getSkillLevel());
        }

        System.out.println("================================================================");
        System.out.println("Total Participants: " + allParticipants.size());
        System.out.println("Unassigned: " + unassigned.size());
        System.out.println("Assigned: " + (allParticipants.size() - unassigned.size()));
    }

    private static void exportTeamsToCSV() {
        try {
            List<Team> teams = teamService.getAllTeams();

            if (teams.isEmpty()) {
                System.err.println("[ERROR] No teams to export. Generate teams first.");
                return;
            }

            teamService.exportToCSV(teams, "formed_teams.csv");
            System.out.println("[SUCCESS] Teams exported to: formed_teams.csv");

        } catch (Exception e) {
            System.err.println("[ERROR] Export failed: " + e.getMessage());
        }
    }

    private static void searchParticipant() {
        String id = getUserInput("Enter Participant ID: ");
        Participant p = userService.getParticipant(id);

        if (p == null) {
            System.err.println("[ERROR] Participant not found.");
            return;
        }

        System.out.println("\n[PARTICIPANT DETAILS]");
        System.out.println("================================================================");
        System.out.println("ID: " + p.getId());
        System.out.println("Name: " + p.getName());
        System.out.println("Email: " + p.getEmail());
        System.out.println("Game: " + p.getPreferredGame());
        System.out.println("Role: " + p.getPreferredRole());
        System.out.println("Skill: " + p.getSkillLevel());
        System.out.println("Personality: " + p.getPersonalityType() + " (" + p.getPersonalityScore() + ")");
        System.out.println("================================================================");
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
            System.out.println("Leave blank to keep current value");

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
            System.out.println("[SUCCESS] Participant deleted!");
            System.out.println("[INFO] Please regenerate teams if needed.");

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