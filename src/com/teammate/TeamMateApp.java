package com.teammate;

import com.teammate.model.*;
import com.teammate.service.*;
import com.teammate.util.*;
import java.util.*;

/**
 * TeamMate: Intelligent Team Formation System
 *
 * Main Application Class - Entry point for the TeamMate application.
 * This system helps university gaming clubs form balanced teams for tournaments
 * and events based on personality traits, skills, roles, and game preferences.
 *
 * Features:
 * - Participant registration with auto-generated IDs (P101, P102, ...)
 * - 5-question personality survey
 * - Team formation with concurrent processing (3 threads)
 * - CSV file operations (load/export)
 * - CRUD operations for participants
 * - Role-based access (Organizer/Participant)
 *
 * Use Cases Implemented:
 * - UC-01: Register Participant
 * - UC-02: Login Participant
 * - UC-03: Login Organizer
 * - UC-04: Manage Participants (Search, Update, Delete, View All)
 * - UC-05: Form Teams (Generate, View Unassigned, Export)
 *
 * @author Student Name
 * @version 1.0
 * @since 2025
 */
public class TeamMateApp {

    // Organizer password for authentication
    private static final String ORGANIZER_PASSWORD = "Teammate";

    // CSV file paths
    private static final String PARTICIPANT_CSV = "participants_sample.csv";

    // Scanner for user input
    private static final Scanner scanner = new Scanner(System.in);

    // Service layer objects
    private static UserService userService;
    private static TeamService teamService;

    /**
     * Main method - Entry point of the application
     *
     * Initializes services and displays the welcome menu.
     * Handles user selection between Participant/Organizer/Exit.
     *
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        Logger.logInfo("TeamMate application started");

        // Display application header
        System.out.println("================================================================");
        System.out.println("   TeamMate: Intelligent Team Formation System");
        System.out.println("   University Gaming Club Management");
        System.out.println("================================================================\n");

        try {
            // Initialize services
            userService = new UserService(PARTICIPANT_CSV);
            teamService = new TeamService();

            boolean exit = false;
            while (!exit) {
                displayWelcomeMenu();
                String choice = getUserInput("Enter your choice (1-3): ");

                switch (choice) {
                    case "1":
                        participantFlow();
                        break;
                    case "2":
                        organizerFlow();
                        break;
                    case "3":
                        exit = true;
                        System.out.println("\n[INFO] Thank you for using TeamMate. Goodbye!");
                        Logger.logInfo("User chose to exit application");
                        break;
                    default:
                        System.err.println("[ERROR] Invalid choice. Please enter 1, 2, or 3.");
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

    /**
     * Displays the welcome menu with main options
     *
     * Shows three options: Participant Login/Register, Organizer Login, Exit
     */
    private static void displayWelcomeMenu() {
        System.out.println("\n================================================================");
        System.out.println("                    WELCOME TO TEAMMATE");
        System.out.println("================================================================");
        System.out.println("  1. Participant Login/Register");
        System.out.println("  2. Organizer Login");
        System.out.println("  3. Exit");
        System.out.println("================================================================");
    }

    /**
     * Handles participant flow - registration or login
     *
     * Presents options for new participant registration or existing participant login.
     */
    private static void participantFlow() {
        System.out.println("\n[PARTICIPANT MENU]");
        System.out.println("----------------------------------------------------------------");
        System.out.println("  1. Register as New Participant");
        System.out.println("  2. Login as Existing Participant");
        System.out.println("  3. Back to Main Menu");
        System.out.println("----------------------------------------------------------------");

        String choice = getUserInput("Enter your choice (1-3): ");

        switch (choice) {
            case "1":
                registerParticipant();
                break;
            case "2":
                loginParticipant();
                break;
            case "3":
                return;
            default:
                System.err.println("[ERROR] Invalid choice.");
        }
    }

    /**
     * UC-01: Register New Participant
     *
     * Registers a new participant with:
     * - Auto-generated ID (P101, P102, P103, ...)
     * - Name and email
     * - 5-question personality survey
     * - Game preference
     * - Role selection
     * - Skill level (1-10)
     *
     * The personality survey classifies participants into:
     * - Leader (90-100)
     * - Balanced (70-89)
     * - Thinker (50-69)
     */
    private static void registerParticipant() {
        Logger.logInfo("New participant registration initiated");

        try {
            System.out.println("\n[NEW PARTICIPANT REGISTRATION]");
            System.out.println("================================================================");

            // Get name
            String name = getUserInput("Enter your full name: ");
            if (!ValidationUtils.isNotEmpty(name)) {
                throw new InvalidInputException("Name cannot be empty");
            }

            // Get email with validation
            String email = getUserInput("Enter your email: ");
            if (!ValidationUtils.isValidEmail(email)) {
                throw new InvalidInputException("Invalid email format");
            }

            // Personality Survey - 5 Questions
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

            // Collect survey responses with validation
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

            // Calculate personality score and classify
            int personalityScore = PersonalityClassifier.calculateScore(responses);
            PersonalityType personalityType = PersonalityClassifier.classify(personalityScore);

            // Display survey results
            System.out.println("\n[SURVEY RESULTS]");
            System.out.println("Your Personality Score: " + personalityScore);
            System.out.println("Your Personality Type: " + personalityType);
            System.out.println("Description: " + personalityType.getDescription());

            // Get gaming preferences
            System.out.println("\n[GAMING PREFERENCES]");
            System.out.println("eg: FIFA, CS:GO, DOTA 2, Valorant, Basketball, Chess");
            String preferredGame = getUserInput("Enter your preferred game/sport: ");
            if (!ValidationUtils.isNotEmpty(preferredGame)) {
                throw new InvalidInputException("Preferred game cannot be empty");
            }

            // Role selection with descriptions
            System.out.println("\n[SELECT YOUR ROLE]");
            System.out.println("================================================================");
            System.out.println("1. Strategist : Focuses on tactics and planning. Keeps the bigger picture in mind during gameplay.");
            System.out.println("2. Attacker : Frontline player. Good reflexes, offensive tactics, quick execution");
            System.out.println("3. Defender : Protects and supports team stability. Good under pressure and team-focused.");
            System.out.println("4. Supporter : Jack-of-all-trades. Adapts roles, ensures smooth coordination.");
            System.out.println("5. Coordinator : Communication lead. Keeps the team informed and organized in real time.");
            System.out.println("================================================================");

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
                        default:
                            System.err.println("[ERROR] Please select a role between 1 and 5");
                    }
                } catch (NumberFormatException e) {
                    System.err.println("[ERROR] Please enter a valid number");
                }
            }

            // Get skill level with validation
            int skillLevel = 0;
            boolean validSkill = false;
            while (!validSkill) {
                String skillInput = getUserInput("Rate your skill level (1-10): ");
                try {
                    skillLevel = Integer.parseInt(skillInput);
                    if (ValidationUtils.isValidSkillLevel(skillLevel)) {
                        validSkill = true;
                    } else {
                        System.err.println("[ERROR] Skill level must be between 1 and 10");
                    }
                } catch (NumberFormatException e) {
                    System.err.println("[ERROR] Please enter a valid number");
                }
            }

            // Register the participant (ID auto-generated)
            Participant newParticipant = userService.registerParticipant(name, email, preferredGame,
                    skillLevel, preferredRole, personalityScore);

            // Display success message with generated ID
            System.out.println("\n[REGISTRATION SUCCESSFUL]");
            System.out.println("================================================================");
            System.out.println("Your Participant ID: " + newParticipant.getId());
            System.out.println("Please save this ID for login.");
            System.out.println("================================================================");

            Logger.logInfo("Participant registered: " + newParticipant.getId());

        } catch (InvalidInputException e) {
            System.err.println("[ERROR] Registration failed: " + e.getMessage());
            Logger.logException("Participant registration failed", e);
        } catch (Exception e) {
            System.err.println("[ERROR] Unexpected error: " + e.getMessage());
            Logger.logException("Unexpected registration error", e);
        }
    }

    /**
     * UC-02: Login Existing Participant
     *
     * Allows existing participants to login using their ID.
     * Upon successful login, displays participant dashboard.
     *
     * @throws InvalidInputException if ID format is invalid
     */
    private static void loginParticipant() {
        try {
            String id = getUserInput("Enter your Participant ID: ");

            // Validate ID format
            if (!ValidationUtils.isValidParticipantId(id)) {
                System.err.println("[ERROR] Invalid ID format. Must be P### (e.g., P101)");
                return;
            }

            // Retrieve participant
            Participant participant = userService.getParticipant(id);

            if (participant == null) {
                System.err.println("[ERROR] Participant not found. Please register first.");
                return;
            }

            System.out.println("\n[WELCOME BACK, " + participant.getName() + "!]");
            System.out.println("================================================================");

            participantDashboard(participant);

        } catch (Exception e) {
            System.err.println("[ERROR] Login failed: " + e.getMessage());
            Logger.logException("Participant login failed", e);
        }
    }

    /**
     * Participant Dashboard - Main menu for logged-in participants
     *
     * Features:
     * - View assigned team
     * - Update profile (game, role, skill)
     * - Logout
     *
     * @param participant The logged-in participant
     */
    private static void participantDashboard(Participant participant) {
        boolean logout = false;

        while (!logout) {
            System.out.println("\n[PARTICIPANT DASHBOARD]");
            System.out.println("----------------------------------------------------------------");
            System.out.println("  1. View My Team");
            System.out.println("  2. Update My Profile");
            System.out.println("  0. Logout");
            System.out.println("----------------------------------------------------------------");

            String choice = getUserInput("Enter your choice: ");

            switch (choice) {
                case "1":
                    viewMyTeam(participant);
                    break;
                case "2":
                    updateMyProfile(participant);
                    break;
                case "0":
                    logout = true;
                    System.out.println("[INFO] Logged out successfully.");
                    break;
                default:
                    System.err.println("[ERROR] Invalid choice.");
            }
        }
    }

    /**
     * View Participant's Team Assignment
     *
     * Displays the team the participant is assigned to, including:
     * - Team ID
     * - Team members
     * - Average skill level
     * - Current marker for the participant
     *
     * @param participant The participant viewing their team
     */
    private static void viewMyTeam(Participant participant) {
        Team myTeam = teamService.getTeamByParticipant(participant.getId());

        if (myTeam == null) {
            System.out.println("\n[INFO] You are not assigned to a team yet.");
            System.out.println("Teams will be formed by the organizer.");
            return;
        }

        System.out.println("\n[YOUR TEAM: " + myTeam.getTeamId() + "]");
        System.out.println("================================================================");
        System.out.println("Team Size: " + myTeam.getCurrentSize() + "/" + myTeam.getTeamSize());
        System.out.println("Average Skill: " + String.format("%.2f", myTeam.getAverageSkill()));
        System.out.println("\n[TEAM MEMBERS]");
        System.out.println("----------------------------------------------------------------");

        for (Participant member : myTeam.getMembers()) {
            String marker = member.getId().equals(participant.getId()) ? " (YOU)" : "";
            System.out.printf("%-15s | %-10s | %-12s | %-15s | Skill: %d%s\n",
                    member.getName(),
                    member.getPersonalityType(),
                    member.getPreferredRole(),
                    member.getPreferredGame(),
                    member.getSkillLevel(),
                    marker);
        }

        System.out.println("================================================================");
    }

    /**
     * Update Participant Profile
     *
     * Allows participants to update:
     * - Preferred game
     * - Role
     * - Skill level
     *
     * Note: Personality type can only be changed by retaking the survey
     *
     * @param participant The participant updating their profile
     */
    private static void updateMyProfile(Participant participant) {
        try {
            System.out.println("\n[UPDATE YOUR PROFILE]");
            System.out.println("================================================================");
            System.out.println("Leave blank to keep current value");

            // Update game
            String game = getUserInput("New game [" + participant.getPreferredGame() + "]: ");
            if (ValidationUtils.isNotEmpty(game)) {
                participant.setPreferredGame(game);
            }

            // Update role
            System.out.println("\nCurrent Role: " + participant.getPreferredRole());
            System.out.println("1. Strategist  2. Attacker  3. Defender  4. Supporter  5. Coordinator");
            String roleChoice = getUserInput("New role (1-5) [keep current]: ");
            if (ValidationUtils.isNotEmpty(roleChoice)) {
                try {
                    int choice = Integer.parseInt(roleChoice);
                    switch (choice) {
                        case 1: participant.setPreferredRole(Role.STRATEGIST); break;
                        case 2: participant.setPreferredRole(Role.ATTACKER); break;
                        case 3: participant.setPreferredRole(Role.DEFENDER); break;
                        case 4: participant.setPreferredRole(Role.SUPPORTER); break;
                        case 5: participant.setPreferredRole(Role.COORDINATOR); break;
                        default: System.out.println("[INFO] Keeping current role");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("[INFO] Keeping current role");
                }
            }

            // Update skill level
            String skillInput = getUserInput("New skill level [" + participant.getSkillLevel() + "]: ");
            if (ValidationUtils.isNotEmpty(skillInput)) {
                try {
                    int skill = Integer.parseInt(skillInput);
                    if (ValidationUtils.isValidSkillLevel(skill)) {
                        participant.setSkillLevel(skill);
                    } else {
                        System.out.println("[INFO] Skill must be 1-10. Keeping current value.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("[INFO] Keeping current skill level");
                }
            }

            userService.updateParticipant(participant);
            System.out.println("\n[SUCCESS] Profile updated!");

        } catch (Exception e) {
            System.err.println("[ERROR] Update failed: " + e.getMessage());
        }
    }

    /**
     * UC-03: Organizer Login
     *
     * Authenticates organizer using password.
     * Upon successful authentication, displays organizer dashboard.
     */
    private static void organizerFlow() {
        System.out.println("\n[ORGANIZER LOGIN]");
        String password = getUserInput("Enter organizer password: ");

        if (!password.equals(ORGANIZER_PASSWORD)) {
            System.err.println("[ERROR] Incorrect password. Access denied.");
            Logger.logWarning("Failed organizer login attempt");
            return;
        }

        System.out.println("[SUCCESS] Organizer authenticated!");
        Logger.logInfo("Organizer logged in");

        organizerDashboard();
    }

    /**
     * Organizer Dashboard - Main menu for organizer
     *
     * UC-04: Manage Participants
     * - Load from CSV
     * - Search, Update, Delete
     * - View All
     *
     * UC-05: Form Teams
     * - Generate Teams
     * - View Unassigned Participants
     * - View All Teams
     * - Export to CSV
     */
    private static void organizerDashboard() {
        boolean logout = false;

        while (!logout) {
            System.out.println("\n[ORGANIZER DASHBOARD]");
            System.out.println("================================================================");
            System.out.println("  1. Load Participants from CSV");
            System.out.println("  2. Search Participant");
            System.out.println("  3. Update Participant");
            System.out.println("  4. Delete Participant");
            System.out.println("  5. View All Participants");
            System.out.println("  6. Generate Teams");
            System.out.println("  7. View Unassigned Participants");
            System.out.println("  8. View All Teams");
            System.out.println("  9. Export Teams to CSV");
            System.out.println("  0. Logout");
            System.out.println("================================================================");

            String choice = getUserInput("Enter your choice: ");

            switch (choice) {
                case "1":
                    loadParticipantsFromCSV();
                    break;
                case "2":
                    searchParticipant();
                    break;
                case "3":
                    organizerUpdateParticipant();
                    break;
                case "4":
                    deleteParticipant();
                    break;
                case "5":
                    viewAllParticipants();
                    break;
                case "6":
                    generateTeams();
                    break;
                case "7":
                    viewUnassignedParticipants();
                    break;
                case "8":
                    viewAllTeams();
                    break;
                case "9":
                    exportTeamsToCSV();
                    break;
                case "0":
                    logout = true;
                    System.out.println("[INFO] Organizer logged out.");
                    break;
                default:
                    System.err.println("[ERROR] Invalid choice.");
            }
        }
    }

    /**
     * UC-04 INCLUDE: Load Participants from CSV
     *
     * Loads participant data from the sample CSV file.
     * File format: ID,Name,Email,Game,Skill,Role,PersonalityScore
     *
     * @throws FileProcessingException if file cannot be read
     */
    private static void loadParticipantsFromCSV() {
        try {
            System.out.println("\n[LOADING PARTICIPANTS FROM CSV]");
            int count = userService.loadFromCSV(PARTICIPANT_CSV);
            System.out.println("[SUCCESS] Loaded " + count + " participants from " + PARTICIPANT_CSV);
            Logger.logInfo("Loaded " + count + " participants from CSV");

        } catch (FileProcessingException e) {
            System.err.println("[ERROR] Failed to load CSV: " + e.getMessage());
            Logger.logException("CSV loading failed", e);
        }
    }

    /**
     * UC-04 EXTEND: View All Participants
     *
     * Displays all registered participants in a table format showing:
     * - ID, Name, Email
     * - Personality Type, Role
     * - Skill Level
     */
    private static void viewAllParticipants() {
        List<Participant> participants = userService.getAllParticipants();

        if (participants.isEmpty()) {
            System.out.println("\n[INFO] No participants registered yet.");
            return;
        }

        System.out.println("\n[ALL PARTICIPANTS]");
        System.out.println("================================================================");
        System.out.printf("%-8s | %-20s | %-25s | %-10s | %-12s | Skill\n",
                "ID", "Name", "Email", "Personality", "Role");
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

    /**
     * UC-05 INCLUDE: Generate Teams
     *
     * Forms balanced teams using concurrent processing (3 threads).
     *
     * Algorithm ensures:
     * - Team size between 3-10
     * - Minimum 2 teams
     * - Diverse interests (max 2 same game per team)
     * - Role variety (min 3 different roles)
     * - Mixed personality types (min 2 types)
     *
     * Uses ExecutorService with 3 threads for concurrent processing.
     */
    private static void generateTeams() {
        try {
            List<Participant> participants = userService.getAllParticipants();

            if (participants.isEmpty()) {
                System.err.println("[ERROR] No participants loaded. Load from CSV first.");
                return;
            }

            String sizeInput = getUserInput("Enter team size (3-10): ");
            int teamSize = Integer.parseInt(sizeInput);

            if (teamSize < 3 || teamSize > 10) {
                System.err.println("[ERROR] Team size must be between 3 and 10");
                return;
            }

            int minParticipants = teamSize * 2;
            if (participants.size() < minParticipants) {
                System.err.println("[ERROR] Need at least " + minParticipants +
                        " participants to form 2 teams of size " + teamSize);
                return;
            }

            System.out.println("[INFO] Generating teams using concurrent processing...");
            long startTime = System.currentTimeMillis();

            List<Team> teams = teamService.generateTeams(participants, teamSize);

            long endTime = System.currentTimeMillis();
            double duration = (endTime - startTime) / 1000.0;

            System.out.println("[SUCCESS] Generated " + teams.size() + " teams in " +
                    String.format("%.2f", duration) + " seconds");

            // Show teams (but not statistics)
            viewAllTeams();

        } catch (Exception e) {
            System.err.println("[ERROR] Team generation failed: " + e.getMessage());
        }
    }

    /**
     * UC-05 EXTEND: View Unassigned Participants
     *
     * Shows participants who are not in any team yet.
     * Used AFTER Generate Teams to check assignment status.
     *
     * Displays:
     * - Unassigned participants table
     * - Total participants count
     * - Unassigned count
     * - Assigned count
     */
    private static void viewUnassignedParticipants() {
        List<Participant> allParticipants = userService.getAllParticipants();
        List<Participant> unassigned = new ArrayList<>();

        // Check which participants are not in any team
        for (Participant p : allParticipants) {
            if (teamService.getTeamByParticipant(p.getId()) == null) {
                unassigned.add(p);
            }
        }

        if (unassigned.isEmpty()) {
            System.out.println("\n[INFO] All participants are assigned to teams.");
            System.out.println("Total Participants: " + allParticipants.size());
            System.out.println("Unassigned: 0");
            return;
        }

        System.out.println("\n[UNASSIGNED PARTICIPANTS]");
        System.out.println("================================================================");
        System.out.printf("%-8s | %-20s | %-25s | %-10s | %-12s | Skill\n",
                "ID", "Name", "Email", "Personality", "Role");
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

        Logger.logInfo("Viewed unassigned participants: " + unassigned.size() + " unassigned");
    }

    /**
     * UC-05: View All Teams
     *
     * Displays all formed teams with their members.
     * Shows for each team:
     * - Team ID
     * - Team size
     * - Average skill level
     * - Member details (name, personality, role, game, skill)
     */
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

    /**
     * UC-05 EXTEND: Export Teams to CSV
     *
     * MANUAL export only - user must click this option.
     * Exports formed teams to formed_teams.csv.
     *
     * File format: TeamID,ParticipantID,Name,Role,Skill
     *
     * @throws FileProcessingException if file cannot be written
     */
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

    /**
     * UC-04 EXTEND: Search Participant
     *
     * Searches for a participant by ID and displays their details:
     * - ID, Name, Email
     * - Game, Role, Skill
     * - Personality Type
     */
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

    /**
     * UC-04 EXTEND: Update Participant
     *
     * Allows organizer to update participant details:
     * - Name
     * - Email (with validation)
     * - Game
     * - Skill level (with validation)
     *
     * Leave fields blank to keep current values.
     */
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

    /**
     * UC-04 EXTEND: Delete Participant
     *
     * Deletes a participant from the system.
     * Requires confirmation before deletion.
     * If participant is in a team, all teams are cleared.
     */
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

            // If participant was in a team, clear all teams
            if (teamService.getTeamByParticipant(id) != null) {
                teamService.clearAllTeams();
                System.out.println("[INFO] Teams cleared. Please regenerate.");
            }

            System.out.println("[SUCCESS] Participant deleted!");

        } catch (Exception e) {
            System.err.println("[ERROR] Deletion failed: " + e.getMessage());
        }
    }

    /**
     * Helper method to get user input
     *
     * @param prompt The prompt message to display
     * @return User input as trimmed string
     */
    private static String getUserInput(String prompt) {
        System.out.print(prompt);
        try {
            return scanner.nextLine().trim();
        } catch (Exception e) {
            return "";
        }
    }
}