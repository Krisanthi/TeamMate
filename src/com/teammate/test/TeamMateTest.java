package com.teammate.test;

import com.teammate.model.*;
import com.teammate.service.*;
import com.teammate.util.*;
import java.util.*;
import java.io.*;

/**
 * Comprehensive Testing Suite for TeamMate Application
 *
 * Total Tests: 50+ covering all functionality
 * Includes: Unit, Integration, Concurrency, Balance Validation, File Integrity, UAT
 *
 * @author Krisanthi Segar 2425596
 * @version 2.0 - WITH BALANCE VALIDATION TESTS
 * @since 2025
 */
public class TeamMateTest {

    private static int testsRun = 0;
    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("================================================================");
        System.out.println("   TeamMate Application - Comprehensive Testing Suite");
        System.out.println("================================================================\n");

        testParticipantModel();
        testTeamModel();
        testPersonalityClassifier();
        testValidationUtils();
        testUserService();
        testTeamService();
        testTeamBuilder();
        testBalanceValidation();  // NEW: Personality balance tests
        testFileHandler();
        testConcurrency();
        testUserAcceptance();

        printTestSummary();
    }

    // ==================== UNIT TESTS: Participant Model ====================

    private static void testParticipantModel() {
        System.out.println("\n[UNIT TESTS] Participant Model");
        System.out.println("----------------------------------------------------------------");

        test("Create participant with valid data", () -> {
            Participant p = new Participant("P001", "John", "john@uni.edu",
                    "FIFA", 7, Role.ATTACKER, 85);
            return p.getName().equals("John") &&
                    p.getSkillLevel() == 7 &&
                    p.getPersonalityType() == PersonalityType.BALANCED;
        });

        test("Personality type auto-classification", () -> {
            Participant leader = new Participant("P002", "Sarah", "sarah@uni.edu",
                    "Valorant", 9, Role.STRATEGIST, 95);
            Participant thinker = new Participant("P003", "Mike", "mike@uni.edu",
                    "CS2", 6, Role.DEFENDER, 65);
            return leader.getPersonalityType() == PersonalityType.LEADER &&
                    thinker.getPersonalityType() == PersonalityType.THINKER;
        });

        test("Participant equality based on ID", () -> {
            Participant p1 = new Participant("P001", "John", "john@uni.edu",
                    "FIFA", 7, Role.ATTACKER, 85);
            Participant p2 = new Participant("P001", "Jane", "jane@uni.edu",
                    "Dota", 8, Role.SUPPORTER, 70);
            return p1.equals(p2);
        });

        test("Update personality score updates type", () -> {
            Participant p = new Participant("P004", "Test", "test@uni.edu",
                    "LOL", 5, Role.COORDINATOR, 65);
            PersonalityType oldType = p.getPersonalityType();
            p.setPersonalityScore(95);
            return oldType == PersonalityType.THINKER &&
                    p.getPersonalityType() == PersonalityType.LEADER;
        });
    }

    // ==================== UNIT TESTS: Team Model ====================

    private static void testTeamModel() {
        System.out.println("\n[UNIT TESTS] Team Model");
        System.out.println("----------------------------------------------------------------");

        test("Create team and add members", () -> {
            Team team = new Team("TEAM_1", 5);
            Participant p = new Participant("P001", "John", "john@uni.edu",
                    "FIFA", 7, Role.ATTACKER, 85);
            return team.addMember(p) && team.getCurrentSize() == 1;
        });

        test("Team rejects members when full", () -> {
            Team team = new Team("TEAM_2", 2);
            Participant p1 = new Participant("P001", "John", "j@uni.edu",
                    "FIFA", 7, Role.ATTACKER, 85);
            Participant p2 = new Participant("P002", "Jane", "ja@uni.edu",
                    "FIFA", 6, Role.DEFENDER, 75);
            Participant p3 = new Participant("P003", "Bob", "b@uni.edu",
                    "FIFA", 8, Role.SUPPORTER, 80);
            team.addMember(p1);
            team.addMember(p2);
            return !team.addMember(p3) && team.isFull();
        });

        test("Calculate average skill correctly", () -> {
            Team team = new Team("TEAM_3", 5);
            team.addMember(new Participant("P001", "A", "a@u.edu", "G", 6, Role.ATTACKER, 85));
            team.addMember(new Participant("P002", "B", "b@u.edu", "G", 8, Role.DEFENDER, 75));
            team.addMember(new Participant("P003", "C", "c@u.edu", "G", 10, Role.SUPPORTER, 70));
            return Math.abs(team.calculateAverageSkill() - 8.0) < 0.01;
        });

        test("Remove member decreases size and updates avg", () -> {
            Team team = new Team("TEAM_4", 5);
            Participant p1 = new Participant("P001", "A", "a@u.edu", "G", 10, Role.ATTACKER, 85);
            Participant p2 = new Participant("P002", "B", "b@u.edu", "G", 4, Role.DEFENDER, 75);
            team.addMember(p1);
            team.addMember(p2);
            team.removeMember(p1);
            return team.getCurrentSize() == 1 && Math.abs(team.getAverageSkill() - 4.0) < 0.01;
        });

        test("Get personality count", () -> {
            Team team = new Team("TEAM_5", 5);
            team.addMember(new Participant("P001", "A", "a@u.edu", "G", 6, Role.ATTACKER, 95));
            team.addMember(new Participant("P002", "B", "b@u.edu", "G", 7, Role.DEFENDER, 92));
            team.addMember(new Participant("P003", "C", "c@u.edu", "G", 8, Role.SUPPORTER, 65));
            return team.getPersonalityCount(PersonalityType.LEADER) == 2 &&
                    team.getPersonalityCount(PersonalityType.THINKER) == 1;
        });
    }

    // ==================== UNIT TESTS: PersonalityClassifier ====================

    private static void testPersonalityClassifier() {
        System.out.println("\n[UNIT TESTS] PersonalityClassifier");
        System.out.println("----------------------------------------------------------------");

        test("Calculate score from responses", () -> {
            try {
                int[] responses = {5, 5, 4, 4, 5};
                int score = PersonalityClassifier.calculateScore(responses);
                return score == 92;
            } catch (Exception e) {
                return false;
            }
        });

        test("Classify LEADER correctly", () -> {
            return PersonalityClassifier.classify(95) == PersonalityType.LEADER &&
                    PersonalityClassifier.classify(90) == PersonalityType.LEADER;
        });

        test("Classify BALANCED correctly", () -> {
            return PersonalityClassifier.classify(85) == PersonalityType.BALANCED &&
                    PersonalityClassifier.classify(70) == PersonalityType.BALANCED;
        });

        test("Classify THINKER correctly", () -> {
            return PersonalityClassifier.classify(65) == PersonalityType.THINKER &&
                    PersonalityClassifier.classify(50) == PersonalityType.THINKER;
        });

        test("Reject invalid response count", () -> {
            try {
                int[] responses = {5, 5, 4};
                PersonalityClassifier.calculateScore(responses);
                return false;
            } catch (InvalidInputException e) {
                return true;
            }
        });

        test("Reject out-of-range responses", () -> {
            try {
                int[] responses = {5, 6, 4, 3, 2};
                PersonalityClassifier.calculateScore(responses);
                return false;
            } catch (InvalidInputException e) {
                return true;
            }
        });

        test("Enforce minimum score of 50", () -> {
            try {
                int[] responses = {1, 1, 1, 1, 1};
                int score = PersonalityClassifier.calculateScore(responses);
                return score == 50;
            } catch (Exception e) {
                return false;
            }
        });
    }

    // ==================== UNIT TESTS: ValidationUtils ====================

    private static void testValidationUtils() {
        System.out.println("\n[UNIT TESTS] ValidationUtils");
        System.out.println("----------------------------------------------------------------");

        test("Valid email format", () -> {
            return ValidationUtils.isValidEmail("user@university.edu") &&
                    ValidationUtils.isValidEmail("john.doe@gmail.com");
        });

        test("Invalid email format", () -> {
            return !ValidationUtils.isValidEmail("notanemail") &&
                    !ValidationUtils.isValidEmail("@nodomain.com");
        });

        test("Valid participant ID format", () -> {
            return ValidationUtils.isValidParticipantId("P101") &&
                    ValidationUtils.isValidParticipantId("P999");
        });

        test("Invalid participant ID format", () -> {
            return !ValidationUtils.isValidParticipantId("P10") &&
                    !ValidationUtils.isValidParticipantId("101");
        });

        test("Valid skill level", () -> {
            return ValidationUtils.isValidSkillLevel(1) &&
                    ValidationUtils.isValidSkillLevel(10) &&
                    ValidationUtils.isValidSkillLevel(5);
        });

        test("Invalid skill level", () -> {
            return !ValidationUtils.isValidSkillLevel(0) &&
                    !ValidationUtils.isValidSkillLevel(11);
        });

        test("Valid team size", () -> {
            return ValidationUtils.isValidTeamSize(5, 20) &&
                    ValidationUtils.isValidTeamSize(3, 12);
        });

        test("Invalid team size", () -> {
            return !ValidationUtils.isValidTeamSize(2, 10) &&
                    !ValidationUtils.isValidTeamSize(5, 8);
        });

        test("Sanitize input removes dangerous chars", () -> {
            String input = "<script>alert('xss')</script>";
            String sanitized = ValidationUtils.sanitizeInput(input);
            return !sanitized.contains("<") && !sanitized.contains(">");
        });
    }

    // ==================== INTEGRATION TESTS: UserService ====================

    private static void testUserService() {
        System.out.println("\n[INTEGRATION TESTS] UserService");
        System.out.println("----------------------------------------------------------------");

        test("Register participant with auto-generated ID", () -> {
            try {
                UserService service = new UserService("test_participants.csv");
                Participant p = service.registerParticipant("Test User", "test@uni.edu",
                        "FIFA", 7, Role.ATTACKER, 85);
                return p.getId().matches("P\\d{3,}");
            } catch (Exception e) {
                return false;
            }
        });

        test("Get participant by ID", () -> {
            try {
                UserService service = new UserService("test_participants.csv");
                Participant p = service.registerParticipant("John", "john@uni.edu",
                        "FIFA", 8, Role.DEFENDER, 75);
                Participant retrieved = service.getParticipant(p.getId());
                return retrieved != null && retrieved.getName().equals("John");
            } catch (Exception e) {
                return false;
            }
        });

        test("Update participant", () -> {
            try {
                UserService service = new UserService("test_participants.csv");
                Participant p = service.registerParticipant("Jane", "jane@uni.edu",
                        "LOL", 6, Role.SUPPORTER, 80);
                p.setSkillLevel(9);
                service.updateParticipant(p);
                Participant updated = service.getParticipant(p.getId());
                return updated.getSkillLevel() == 9;
            } catch (Exception e) {
                return false;
            }
        });

        test("Delete participant", () -> {
            try {
                UserService service = new UserService("test_participants.csv");
                Participant p = service.registerParticipant("Bob", "bob@uni.edu",
                        "Dota", 5, Role.COORDINATOR, 70);
                String id = p.getId();
                service.deleteParticipant(id);
                return service.getParticipant(id) == null;
            } catch (Exception e) {
                return false;
            }
        });

        test("Get all participants", () -> {
            try {
                UserService service = new UserService("test_participants.csv");
                service.registerParticipant("A", "a@u.edu", "G", 7, Role.ATTACKER, 85);
                service.registerParticipant("B", "b@u.edu", "G", 6, Role.DEFENDER, 75);
                return service.getAllParticipants().size() >= 2;
            } catch (Exception e) {
                return false;
            }
        });
    }

    // ==================== INTEGRATION TESTS: TeamService ====================

    private static void testTeamService() {
        System.out.println("\n[INTEGRATION TESTS] TeamService");
        System.out.println("----------------------------------------------------------------");

        test("Generate teams from participants", () -> {
            try {
                List<Participant> participants = createTestParticipants(15);
                TeamService service = new TeamService();
                List<Team> teams = service.generateTeams(participants, 5);
                return teams.size() == 3;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        });

        test("Get team by participant", () -> {
            try {
                List<Participant> participants = createTestParticipants(12);
                TeamService service = new TeamService();
                service.generateTeams(participants, 4);
                Team team = service.getTeamByParticipant(participants.get(0).getId());
                return team != null;
            } catch (Exception e) {
                return false;
            }
        });

        test("Export teams to CSV", () -> {
            try {
                List<Participant> participants = createTestParticipants(10);
                TeamService service = new TeamService();
                List<Team> teams = service.generateTeams(participants, 5);
                service.exportToCSV(teams, "test_teams.csv");
                File file = new File("test_teams.csv");
                return file.exists() && file.length() > 0;
            } catch (Exception e) {
                return false;
            }
        });

        test("Clear all teams", () -> {
            try {
                List<Participant> participants = createTestParticipants(12);
                TeamService service = new TeamService();
                service.generateTeams(participants, 4);
                service.clearAllTeams();
                return service.getAllTeams().isEmpty();
            } catch (Exception e) {
                return false;
            }
        });
    }

    // ==================== INTEGRATION TESTS: TeamBuilder ====================

    private static void testTeamBuilder() {
        System.out.println("\n[INTEGRATION TESTS] TeamBuilder");
        System.out.println("----------------------------------------------------------------");

        test("Form balanced teams", () -> {
            try {
                List<Participant> participants = createTestParticipants(20);
                TeamBuilder builder = new TeamBuilder(participants, 5);
                List<Team> teams = builder.formTeams();
                return teams.size() == 4 && teams.get(0).getCurrentSize() == 5;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        });

        test("All participants assigned", () -> {
            try {
                List<Participant> participants = createTestParticipants(15);
                TeamBuilder builder = new TeamBuilder(participants, 5);
                List<Team> teams = builder.formTeams();
                int totalAssigned = teams.stream().mapToInt(Team::getCurrentSize).sum();
                return totalAssigned == 15;
            } catch (Exception e) {
                return false;
            }
        });

        test("Teams have reasonable skill balance", () -> {
            try {
                List<Participant> participants = createTestParticipants(20);
                TeamBuilder builder = new TeamBuilder(participants, 5);
                List<Team> teams = builder.formTeams();
                double globalAvg = teams.stream()
                        .mapToDouble(Team::getAverageSkill)
                        .average()
                        .orElse(0.0);
                return teams.stream()
                        .allMatch(t -> Math.abs(t.getAverageSkill() - globalAvg) < 2.5);
            } catch (Exception e) {
                return false;
            }
        });

        test("Teams have minimum role diversity", () -> {
            try {
                List<Participant> participants = createTestParticipants(20);
                TeamBuilder builder = new TeamBuilder(participants, 5);
                List<Team> teams = builder.formTeams();

                for (Team team : teams) {
                    Set<Role> roles = new HashSet<>();
                    for (Participant p : team.getMembers()) {
                        roles.add(p.getPreferredRole());
                    }
                    if (roles.size() < 3) return false;
                }
                return true;
            } catch (Exception e) {
                return false;
            }
        });
    }

    // ==================== NEW: BALANCE VALIDATION TESTS ====================

    private static void testBalanceValidation() {
        System.out.println("\n[BALANCE VALIDATION TESTS] Personality Distribution");
        System.out.println("----------------------------------------------------------------");

        test("Each team has exactly 1 Leader", () -> {
            try {
                List<Participant> participants = createMixedPersonalityParticipants(20);
                TeamBuilder builder = new TeamBuilder(participants, 5);
                List<Team> teams = builder.formTeams();

                for (Team team : teams) {
                    int leaderCount = team.getPersonalityCount(PersonalityType.LEADER);
                    if (leaderCount != 1) {
                        System.err.println("    FAIL: " + team.getTeamId() + " has " + leaderCount + " leaders");
                        return false;
                    }
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        });

        test("Each team has 1-2 Thinkers", () -> {
            try {
                List<Participant> participants = createMixedPersonalityParticipants(20);
                TeamBuilder builder = new TeamBuilder(participants, 5);
                List<Team> teams = builder.formTeams();

                for (Team team : teams) {
                    int thinkerCount = team.getPersonalityCount(PersonalityType.THINKER);
                    if (thinkerCount < 1 || thinkerCount > 2) {
                        System.err.println("    FAIL: " + team.getTeamId() + " has " + thinkerCount + " thinkers");
                        return false;
                    }
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        });

        test("Each team has Balanced types for remaining slots", () -> {
            try {
                List<Participant> participants = createMixedPersonalityParticipants(20);
                TeamBuilder builder = new TeamBuilder(participants, 5);
                List<Team> teams = builder.formTeams();

                for (Team team : teams) {
                    int balancedCount = team.getPersonalityCount(PersonalityType.BALANCED);
                    if (balancedCount < 2) {
                        System.err.println("    FAIL: " + team.getTeamId() + " has only " + balancedCount + " balanced");
                        return false;
                    }
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        });

        test("No team has more than 3 players from same game", () -> {
            try {
                List<Participant> participants = createMixedPersonalityParticipants(20);
                TeamBuilder builder = new TeamBuilder(participants, 5);
                List<Team> teams = builder.formTeams();

                for (Team team : teams) {
                    Map<String, Integer> gameCounts = new HashMap<>();
                    for (Participant p : team.getMembers()) {
                        gameCounts.merge(p.getPreferredGame(), 1, Integer::sum);
                    }
                    if (gameCounts.values().stream().anyMatch(count -> count > 3)) {
                        return false;
                    }
                }
                return true;
            } catch (Exception e) {
                return false;
            }
        });

        test("Teams maintain personality balance with large dataset", () -> {
            try {
                List<Participant> participants = createMixedPersonalityParticipants(100);
                TeamBuilder builder = new TeamBuilder(participants, 5);
                List<Team> teams = builder.formTeams();

                int violations = 0;
                for (Team team : teams) {
                    int leaders = team.getPersonalityCount(PersonalityType.LEADER);
                    int thinkers = team.getPersonalityCount(PersonalityType.THINKER);

                    if (leaders != 1 || thinkers < 1 || thinkers > 2) {
                        violations++;
                    }
                }

                return violations == 0;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        });

        test("Personality distribution is fair across teams", () -> {
            try {
                List<Participant> participants = createMixedPersonalityParticipants(40);
                TeamBuilder builder = new TeamBuilder(participants, 5);
                List<Team> teams = builder.formTeams();

                double avgLeaders = teams.stream()
                        .mapToInt(t -> t.getPersonalityCount(PersonalityType.LEADER))
                        .average().orElse(0);

                return Math.abs(avgLeaders - 1.0) < 0.1;
            } catch (Exception e) {
                return false;
            }
        });
    }

    // ==================== TESTS: FileHandler ====================

    private static void testFileHandler() {
        System.out.println("\n[FILE INTEGRITY TESTS] FileHandler");
        System.out.println("----------------------------------------------------------------");

        test("Load participants from CSV", () -> {
            try {
                createSampleCSV("test_load.csv");
                FileHandler handler = new FileHandler("test_load.csv", "");
                List<Participant> participants = handler.loadParticipants();
                return participants.size() > 0;
            } catch (Exception e) {
                return false;
            }
        });

        test("Save participants to CSV", () -> {
            try {
                List<Participant> participants = createTestParticipants(5);
                FileHandler handler = new FileHandler("", "test_save.csv");
                handler.saveParticipants(participants, "test_save.csv");
                File file = new File("test_save.csv");
                return file.exists() && file.length() > 0;
            } catch (Exception e) {
                return false;
            }
        });

        test("Validate CSV file exists", () -> {
            try {
                createSampleCSV("test_validate.csv");
                FileHandler handler = new FileHandler("test_validate.csv", "");
                return handler.validateCSV();
            } catch (Exception e) {
                return false;
            }
        });

        test("Reject non-existent CSV file", () -> {
            try {
                FileHandler handler = new FileHandler("nonexistent.csv", "");
                handler.validateCSV();
                return false;
            } catch (FileProcessingException e) {
                return true;
            }
        });
    }

    // ==================== TESTS: Concurrency ====================

    private static void testConcurrency() {
        System.out.println("\n[CONCURRENCY TESTS] Thread Safety");
        System.out.println("----------------------------------------------------------------");

        test("Concurrent team generation (100 participants)", () -> {
            try {
                List<Participant> participants = createTestParticipants(100);
                TeamService service = new TeamService();
                List<Team> teams = service.generateTeams(participants, 5);
                return teams.size() == 20;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        });

        test("Thread-safe participant assignment", () -> {
            try {
                List<Participant> participants = createTestParticipants(50);
                TeamService service = new TeamService();
                List<Team> teams = service.generateTeams(participants, 5);

                Set<String> assignedIds = new HashSet<>();
                for (Team team : teams) {
                    for (Participant p : team.getMembers()) {
                        if (!assignedIds.add(p.getId())) {
                            return false;
                        }
                    }
                }
                return true;
            } catch (Exception e) {
                return false;
            }
        });
    }

    // ==================== USER ACCEPTANCE TESTS ====================

    private static void testUserAcceptance() {
        System.out.println("\n[USER ACCEPTANCE TESTS] End-to-End Scenarios");
        System.out.println("----------------------------------------------------------------");

        test("UAT: Complete registration workflow", () -> {
            try {
                UserService service = new UserService("uat_participants.csv");
                Participant p = service.registerParticipant("UAT User", "uat@uni.edu",
                        "FIFA", 7, Role.ATTACKER, 85);
                return p.getId() != null && service.getParticipant(p.getId()) != null;
            } catch (Exception e) {
                return false;
            }
        });

        test("UAT: Team generation and export", () -> {
            try {
                List<Participant> participants = createTestParticipants(30);
                TeamService service = new TeamService();
                List<Team> teams = service.generateTeams(participants, 5);
                service.exportToCSV(teams, "uat_teams.csv");
                File file = new File("uat_teams.csv");
                return file.exists() && teams.size() == 6;
            } catch (Exception e) {
                return false;
            }
        });

        test("UAT: CRUD operations workflow", () -> {
            try {
                UserService service = new UserService("uat_crud.csv");

                Participant p = service.registerParticipant("CRUD Test", "crud@uni.edu",
                        "LOL", 6, Role.SUPPORTER, 75);
                String id = p.getId();

                p.setSkillLevel(8);
                service.updateParticipant(p);

                Participant updated = service.getParticipant(id);
                if (updated.getSkillLevel() != 8) return false;

                service.deleteParticipant(id);
                return service.getParticipant(id) == null;
            } catch (Exception e) {
                return false;
            }
        });
    }

    // ==================== HELPER METHODS ====================

    private static List<Participant> createTestParticipants(int count) {
        List<Participant> participants = new ArrayList<>();
        Role[] roles = Role.values();
        String[] games = {"FIFA", "LOL", "Valorant", "CS2", "Dota"};

        for (int i = 0; i < count; i++) {
            int personalityScore = 50 + (i % 50);
            participants.add(new Participant(
                    "P" + String.format("%03d", i + 1),
                    "Player" + (i + 1),
                    "player" + (i + 1) + "@uni.edu",
                    games[i % games.length],
                    (i % 10) + 1,
                    roles[i % roles.length],
                    personalityScore
            ));
        }
        return participants;
    }

    private static List<Participant> createMixedPersonalityParticipants(int count) {
        List<Participant> participants = new ArrayList<>();
        Role[] roles = Role.values();
        String[] games = {"FIFA", "LOL", "Valorant", "CS2", "Dota"};

        int leadersNeeded = count / 5;
        int thinkersNeeded = (count / 5) * 2;
        int balancedNeeded = count - leadersNeeded - thinkersNeeded;

        for (int i = 0; i < leadersNeeded; i++) {
            participants.add(new Participant(
                    "P" + String.format("%03d", i + 1),
                    "Leader" + (i + 1),
                    "leader" + (i + 1) + "@uni.edu",
                    games[i % games.length],
                    (i % 10) + 1,
                    roles[i % roles.length],
                    90 + (i % 11)
            ));
        }

        for (int i = 0; i < thinkersNeeded; i++) {
            participants.add(new Participant(
                    "P" + String.format("%03d", leadersNeeded + i + 1),
                    "Thinker" + (i + 1),
                    "thinker" + (i + 1) + "@uni.edu",
                    games[i % games.length],
                    (i % 10) + 1,
                    roles[i % roles.length],
                    50 + (i % 20)
            ));
        }

        for (int i = 0; i < balancedNeeded; i++) {
            participants.add(new Participant(
                    "P" + String.format("%03d", leadersNeeded + thinkersNeeded + i + 1),
                    "Balanced" + (i + 1),
                    "balanced" + (i + 1) + "@uni.edu",
                    games[i % games.length],
                    (i % 10) + 1,
                    roles[i % roles.length],
                    70 + (i % 20)
            ));
        }

        Collections.shuffle(participants);
        return participants;
    }

    private static void createSampleCSV(String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("ID,Name,Email,PreferredGame,SkillLevel,PreferredRole,PersonalityScore,PersonalityType");
            writer.println("P101,John,john@uni.edu,FIFA,7,ATTACKER,85,BALANCED");
            writer.println("P102,Jane,jane@uni.edu,LOL,8,DEFENDER,75,BALANCED");
            writer.println("P103,Bob,bob@uni.edu,Valorant,9,STRATEGIST,95,LEADER");
        } catch (IOException e) {
            System.err.println("Failed to create sample CSV");
        }
    }

    private static void test(String description, TestCase testCase) {
        testsRun++;
        try {
            boolean passed = testCase.run();
            if (passed) {
                testsPassed++;
                System.out.println( "[PASSED]"+ description);
            } else {
                testsFailed++;
                System.err.println("[FAILED]"+description);
            }
        } catch (Exception e) {
            testsFailed++;
            System.err.println( "[FAILED]"+ description + " (Exception: " + e.getMessage() + ")");
        }
    }

    private static void printTestSummary() {
        System.out.println("\n================================================================");
        System.out.println("                    TEST SUMMARY");
        System.out.println("================================================================");
        System.out.println("Total Tests Run: " + testsRun);
        System.out.println("Tests Passed: " + testsPassed );
        System.out.println("Tests Failed: " + testsFailed );
        System.out.println("Success Rate: " + String.format("%.2f", (testsPassed * 100.0 / testsRun)) + "%");
        System.out.println("================================================================");

        if (testsFailed == 0) {
            System.out.println("\n All tests passed! The application is working correctly.");
        } else {
            System.err.println("\n  Some tests failed. Please review the failures above.");
        }
    }

    @FunctionalInterface
    interface TestCase {
        boolean run() throws Exception;
    }
}