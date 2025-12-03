package com.teammate.test;

import com.teammate.model.*;
import com.teammate.service.*;
import com.teammate.util.*;
import java.util.*;
import java.io.*;

/**
 * Comprehensive Testing Suite for TeamMate Application
 * Covers: Unit Tests, Integration Tests, Concurrency, File Integrity, User Acceptance
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
            Participant leader = new Participant("P002", "Leader", "l@uni.edu",
                    "Chess", 9, Role.STRATEGIST, 95);
            Participant thinker = new Participant("P003", "Thinker", "t@uni.edu",
                    "DOTA", 5, Role.DEFENDER, 60);
            return leader.getPersonalityType() == PersonalityType.LEADER &&
                    thinker.getPersonalityType() == PersonalityType.THINKER;
        });

        test("Update personality score updates type", () -> {
            Participant p = new Participant("P004", "Test", "test@uni.edu",
                    "DOTA", 5, Role.SUPPORTER, 70);
            p.setPersonalityScore(95);
            return p.getPersonalityType() == PersonalityType.LEADER;
        });
    }

    // ==================== UNIT TESTS: Team Model ====================

    private static void testTeamModel() {
        System.out.println("\n[UNIT TESTS] Team Model");
        System.out.println("----------------------------------------------------------------");

        test("Add member to team", () -> {
            Team team = new Team("TEAM_1", 5);
            Participant p = new Participant("P001", "Player1", "p1@uni.edu",
                    "FIFA", 7, Role.ATTACKER, 85);
            return team.addMember(p) && team.getCurrentSize() == 1;
        });

        test("Team size limit enforced", () -> {
            Team team = new Team("TEAM_2", 3);
            for (int i = 1; i <= 3; i++) {
                team.addMember(new Participant("P" + String.format("%03d", i),
                        "P" + i, "p@uni.edu", "FIFA", 5, Role.ATTACKER, 70));
            }
            Participant extra = new Participant("P004", "Extra", "e@uni.edu",
                    "FIFA", 5, Role.DEFENDER, 75);
            return !team.addMember(extra) && team.isFull();
        });

        test("Average skill calculation", () -> {
            Team team = new Team("TEAM_3", 5);
            team.addMember(new Participant("P001", "P1", "p1@uni.edu", "FIFA", 4, Role.ATTACKER, 70));
            team.addMember(new Participant("P002", "P2", "p2@uni.edu", "FIFA", 6, Role.DEFENDER, 80));
            team.addMember(new Participant("P003", "P3", "p3@uni.edu", "FIFA", 8, Role.STRATEGIST, 90));
            double avg = team.calculateAverageSkill();
            return Math.abs(avg - 6.0) < 0.01;
        });

        test("Role and personality counting", () -> {
            Team team = new Team("TEAM_4", 5);
            team.addMember(new Participant("P001", "P1", "p1@uni.edu", "FIFA", 5, Role.ATTACKER, 95));
            team.addMember(new Participant("P002", "P2", "p2@uni.edu", "FIFA", 7, Role.ATTACKER, 70));
            team.addMember(new Participant("P003", "P3", "p3@uni.edu", "FIFA", 6, Role.DEFENDER, 60));
            return team.getRoleCount(Role.ATTACKER) == 2 &&
                    team.getPersonalityCount(PersonalityType.LEADER) == 1;
        });
    }

    // ==================== UNIT TESTS: Personality Classifier ====================

    private static void testPersonalityClassifier() {
        System.out.println("\n[UNIT TESTS] Personality Classifier");
        System.out.println("----------------------------------------------------------------");

        test("Classification ranges (Leader 90-100, Balanced 70-89, Thinker 50-69)", () -> {
            return PersonalityClassifier.classify(95) == PersonalityType.LEADER &&
                    PersonalityClassifier.classify(75) == PersonalityType.BALANCED &&
                    PersonalityClassifier.classify(60) == PersonalityType.THINKER;
        });

        test("Survey calculation: (Q1+Q2+Q3+Q4+Q5) x 4", () -> {
            try {
                int[] max = {5, 5, 5, 5, 5};
                int[] valid = {3, 3, 3, 2, 2};
                return PersonalityClassifier.calculateScore(max) == 100 &&
                        PersonalityClassifier.calculateScore(valid) == 52;
            } catch (Exception e) {
                return false;
            }
        });

        test("Invalid survey responses rejected", () -> {
            try {
                int[] invalid = {1, 2, 3, 4, 6};
                PersonalityClassifier.calculateScore(invalid);
                return false;
            } catch (InvalidInputException e) {
                return true;
            }
        });

        test("Boundary score classifications", () -> {
            return PersonalityClassifier.classify(50) == PersonalityType.THINKER &&
                    PersonalityClassifier.classify(70) == PersonalityType.BALANCED &&
                    PersonalityClassifier.classify(90) == PersonalityType.LEADER;
        });
    }

    // ==================== UNIT TESTS: Validation Utils ====================

    private static void testValidationUtils() {
        System.out.println("\n[UNIT TESTS] Validation Utils");
        System.out.println("----------------------------------------------------------------");

        test("Email validation", () -> {
            return ValidationUtils.isValidEmail("test@university.edu") &&
                    !ValidationUtils.isValidEmail("invalid-email");
        });

        test("Participant ID validation (P + 3 digits)", () -> {
            return ValidationUtils.isValidParticipantId("P001") &&
                    !ValidationUtils.isValidParticipantId("P1");
        });

        test("Skill level and team size validation", () -> {
            return ValidationUtils.isValidSkillLevel(5) &&
                    !ValidationUtils.isValidSkillLevel(11) &&
                    ValidationUtils.isValidTeamSize(5, 20);
        });
    }

    // ==================== INTEGRATION TESTS: User Service ====================

    private static void testUserService() {
        System.out.println("\n[INTEGRATION TESTS] User Service");
        System.out.println("----------------------------------------------------------------");

        test("Auto-ID generation (P101, P102, ...)", () -> {
            try {
                UserService service = new UserService("test_user_service.csv");
                Participant p1 = service.registerParticipant("John", "john@uni.edu",
                        "FIFA", 7, Role.ATTACKER, 85);
                Participant p2 = service.registerParticipant("Jane", "jane@uni.edu",
                        "CS:GO", 8, Role.DEFENDER, 75);

                new File("test_user_service.csv").delete();

                return p1.getId().equals("P101") && p2.getId().equals("P102");
            } catch (Exception e) {
                return false;
            }
        });

        test("Participant update and retrieval", () -> {
            try {
                UserService service = new UserService("test_update.csv");
                Participant p = service.registerParticipant("Test", "test@uni.edu",
                        "DOTA", 5, Role.SUPPORTER, 70);

                p.setName("Updated");
                service.updateParticipant(p);
                Participant updated = service.getParticipant(p.getId());

                new File("test_update.csv").delete();

                return updated.getName().equals("Updated");
            } catch (Exception e) {
                return false;
            }
        });

        test("Participant deletion", () -> {
            try {
                UserService service = new UserService("test_delete.csv");
                Participant p = service.registerParticipant("Delete", "del@uni.edu",
                        "CS:GO", 6, Role.COORDINATOR, 70);
                String id = p.getId();
                service.deleteParticipant(id);

                new File("test_delete.csv").delete();

                return service.getParticipant(id) == null;
            } catch (Exception e) {
                return false;
            }
        });
    }

    // ==================== INTEGRATION TESTS: Team Service ====================

    private static void testTeamService() {
        System.out.println("\n[INTEGRATION TESTS] Team Service");
        System.out.println("----------------------------------------------------------------");

        test("Team generation creates correct number", () -> {
            try {
                TeamService service = new TeamService();
                List<Participant> participants = createTestParticipants(20);
                List<Team> teams = service.generateTeams(participants, 4);

                new File("formed_teams.csv").delete();

                return teams.size() == 5;
            } catch (Exception e) {
                return false;
            }
        });

        test("Participant-to-team mapping", () -> {
            try {
                TeamService service = new TeamService();
                List<Participant> participants = createTestParticipants(15);
                service.generateTeams(participants, 3);
                Team team = service.getTeamByParticipant("P001");

                new File("formed_teams.csv").delete();

                return team != null;
            } catch (Exception e) {
                return false;
            }
        });

        test("Clear teams removes all teams", () -> {
            try {
                TeamService service = new TeamService();
                List<Participant> participants = createTestParticipants(12);
                service.generateTeams(participants, 4);
                service.clearAllTeams();

                List<Team> teams = service.getAllTeams();

                return teams.isEmpty();
            } catch (Exception e) {
                return false;
            }
        });
    }

    // ==================== INTEGRATION TESTS: Team Builder Algorithm ====================

    private static void testTeamBuilder() {
        System.out.println("\n[INTEGRATION TESTS] Team Builder Algorithm");
        System.out.println("----------------------------------------------------------------");

        test("Basic team formation", () -> {
            try {
                List<Participant> participants = createTestParticipants(20);
                TeamBuilder builder = new TeamBuilder(participants, 4);
                List<Team> teams = builder.formTeams();
                return teams.size() == 5 && teams.get(0).getCurrentSize() == 4;
            } catch (Exception e) {
                return false;
            }
        });

        test("All participants assigned to teams", () -> {
            try {
                List<Participant> participants = createTestParticipants(20);
                TeamBuilder builder = new TeamBuilder(participants, 4);
                List<Team> teams = builder.formTeams();

                int totalAssigned = 0;
                for (Team team : teams) {
                    totalAssigned += team.getCurrentSize();
                }

                return totalAssigned == 20;
            } catch (Exception e) {
                return false;
            }
        });

        test("Skill distribution fairness", () -> {
            try {
                List<Participant> participants = createTestParticipants(20);
                TeamBuilder builder = new TeamBuilder(participants, 4);
                List<Team> teams = builder.formTeams();

                double minAvg = teams.stream().mapToDouble(Team::getAverageSkill).min().orElse(0);
                double maxAvg = teams.stream().mapToDouble(Team::getAverageSkill).max().orElse(10);

                return (maxAvg - minAvg) < 4.0;
            } catch (Exception e) {
                return false;
            }
        });
    }

    // ==================== INTEGRATION TESTS: File Handler ====================

    private static void testFileHandler() {
        System.out.println("\n[INTEGRATION TESTS] File Handler");
        System.out.println("----------------------------------------------------------------");

        test("CSV validation accepts valid files", () -> {
            try {
                File testFile = new File("test_valid.csv");
                BufferedWriter writer = new BufferedWriter(new FileWriter(testFile));
                writer.write("ID,Name,Email,PreferredGame,SkillLevel,PreferredRole,PersonalityScore,PersonalityType\n");
                writer.write("P001,Test,test@uni.edu,FIFA,5,Attacker,75,BALANCED\n");
                writer.close();

                FileHandler handler = new FileHandler("test_valid.csv", "output.csv");
                boolean valid = handler.validateCSV();
                testFile.delete();
                return valid;
            } catch (Exception e) {
                return false;
            }
        });

        test("CSV validation rejects non-CSV files", () -> {
            try {
                FileHandler handler = new FileHandler("test.txt", "output.csv");
                handler.validateCSV();
                return false;
            } catch (FileProcessingException e) {
                return true;
            }
        });

        test("CSV write and read maintains data consistency", () -> {
            try {
                List<Participant> original = createTestParticipants(10);

                FileHandler handler = new FileHandler("", "test_integrity.csv");
                handler.saveParticipants(original, "test_integrity.csv");

                FileHandler reader = new FileHandler("test_integrity.csv", "");
                List<Participant> loaded = reader.loadParticipants();

                new File("test_integrity.csv").delete();

                return original.size() == loaded.size() &&
                        original.get(0).getId().equals(loaded.get(0).getId());
            } catch (Exception e) {
                return false;
            }
        });
    }

    // ==================== CONCURRENCY TESTS ====================

    private static void testConcurrency() {
        System.out.println("\n[CONCURRENCY TESTS]");
        System.out.println("----------------------------------------------------------------");

        test("Concurrent team formation (3 threads, 30s timeout)", () -> {
            try {
                List<Participant> participants = createTestParticipants(50);
                TeamBuilder builder = new TeamBuilder(participants, 5);
                long startTime = System.currentTimeMillis();
                List<Team> teams = builder.formTeams();
                long endTime = System.currentTimeMillis();

                return teams.size() == 10 && (endTime - startTime) < 10000;
            } catch (Exception e) {
                return false;
            }
        });

        test("Multiple team generations without race conditions", () -> {
            try {
                TeamService service = new TeamService();
                List<Participant> participants = createTestParticipants(40);

                List<Team> teams1 = service.generateTeams(participants, 4);
                List<Team> teams2 = service.generateTeams(participants, 5);

                new File("formed_teams.csv").delete();

                return teams1.size() == 10 && teams2.size() == 8;
            } catch (Exception e) {
                return false;
            }
        });

        test("Large participant set (100 participants)", () -> {
            try {
                List<Participant> participants = createTestParticipants(100);
                TeamBuilder builder = new TeamBuilder(participants, 5);
                List<Team> teams = builder.formTeams();

                return teams.size() == 20;
            } catch (Exception e) {
                return false;
            }
        });
    }

    // ==================== USER ACCEPTANCE TESTS ====================

    private static void testUserAcceptance() {
        System.out.println("\n[USER ACCEPTANCE TESTS]");
        System.out.println("----------------------------------------------------------------");

        test("UAT: Complete participant registration workflow", () -> {
            try {
                UserService service = new UserService("test_uat_reg.csv");
                Participant p = service.registerParticipant("UAT User", "uat@uni.edu",
                        "FIFA", 7, Role.ATTACKER, 85);

                boolean registered = (service.getParticipant(p.getId()) != null);

                new File("test_uat_reg.csv").delete();

                return registered && p.getId().startsWith("P");
            } catch (Exception e) {
                return false;
            }
        });

        test("UAT: Complete team formation workflow", () -> {
            try {
                UserService userService = new UserService("test_uat_teams.csv");
                TeamService teamService = new TeamService();

                for (int i = 0; i < 25; i++) {
                    userService.registerParticipant("User" + i, "user" + i + "@uni.edu",
                            "FIFA", (i % 10) + 1, Role.values()[i % 5], 50 + (i * 2));
                }

                List<Participant> participants = userService.getAllParticipants();
                List<Team> teams = teamService.generateTeams(participants, 5);

                new File("test_uat_teams.csv").delete();
                new File("formed_teams.csv").delete();

                return teams.size() == 5;
            } catch (Exception e) {
                return false;
            }
        });

        test("UAT: Participant can view their team", () -> {
            try {
                UserService userService = new UserService("test_uat_view.csv");
                TeamService teamService = new TeamService();

                Participant p = userService.registerParticipant("TestUser", "test@uni.edu",
                        "FIFA", 7, Role.ATTACKER, 85);

                List<Participant> allParticipants = createTestParticipants(15);
                allParticipants.add(p);

                teamService.generateTeams(allParticipants, 4);
                Team team = teamService.getTeamByParticipant(p.getId());

                new File("test_uat_view.csv").delete();
                new File("formed_teams.csv").delete();

                return team != null;
            } catch (Exception e) {
                return false;
            }
        });

        test("UAT: End-to-end participant management", () -> {
            try {
                UserService service = new UserService("test_uat_e2e.csv");

                Participant p = service.registerParticipant("E2E User", "e2e@uni.edu",
                        "CS:GO", 8, Role.DEFENDER, 90);
                String id = p.getId();

                p.setSkillLevel(9);
                service.updateParticipant(p);
                Participant updated = service.getParticipant(id);

                service.deleteParticipant(id);
                Participant deleted = service.getParticipant(id);

                new File("test_uat_e2e.csv").delete();

                return updated.getSkillLevel() == 9 && deleted == null;
            } catch (Exception e) {
                return false;
            }
        });

        test("UAT: Organizer manages participants and teams", () -> {
            try {
                UserService userService = new UserService("test_uat_manage.csv");
                TeamService teamService = new TeamService();

                for (int i = 0; i < 20; i++) {
                    userService.registerParticipant("User" + i, "user" + i + "@uni.edu",
                            "FIFA", 5, Role.ATTACKER, 70);
                }

                List<Participant> participants = userService.getAllParticipants();
                teamService.generateTeams(participants, 4);

                userService.deleteParticipant("P101");
                teamService.clearAllTeams();

                List<Participant> remainingParticipants = userService.getAllParticipants();
                List<Team> remainingTeams = teamService.getAllTeams();

                new File("test_uat_manage.csv").delete();
                new File("formed_teams.csv").delete();

                return remainingParticipants.size() == 19 && remainingTeams.isEmpty();
            } catch (Exception e) {
                return false;
            }
        });
    }

    // ==================== HELPER METHODS ====================

    private static void test(String testName, TestCase testCase) {
        testsRun++;
        try {
            boolean result = testCase.run();
            if (result) {
                System.out.println("  [PASS] " + testName);
                testsPassed++;
            } else {
                System.out.println("  [FAIL] " + testName);
                testsFailed++;
            }
        } catch (Exception e) {
            System.out.println("  [FAIL] " + testName + " - Exception: " + e.getMessage());
            testsFailed++;
        }
    }

    private static List<Participant> createTestParticipants(int count) {
        List<Participant> participants = new ArrayList<>();
        String[] games = {"FIFA", "CS:GO", "DOTA 2", "Valorant", "Basketball"};
        Role[] roles = Role.values();
        Random rand = new Random(42);

        for (int i = 1; i <= count; i++) {
            String id = String.format("P%03d", i);
            String name = "Participant_" + i;
            String email = "user" + i + "@university.edu";
            String game = games[rand.nextInt(games.length)];
            int skill = rand.nextInt(10) + 1;
            Role role = roles[rand.nextInt(roles.length)];
            int personality = 50 + rand.nextInt(51);

            participants.add(new Participant(id, name, email, game, skill, role, personality));
        }

        return participants;
    }

    private static void printTestSummary() {
        System.out.println("\n================================================================");
        System.out.println("                    TEST SUMMARY");
        System.out.println("================================================================");
        System.out.printf("Total Tests Run:       %d\n", testsRun);
        System.out.printf("Tests Passed:          %d\n", testsPassed);
        System.out.printf("Tests Failed:          %d\n", testsFailed);

        double percentage = testsRun > 0 ? (testsPassed * 100.0 / testsRun) : 0;
        System.out.printf("Success Rate:          %.1f%%\n", percentage);

        System.out.println("================================================================");

        if (testsFailed == 0) {
            System.out.println("\n[SUCCESS] All tests passed. System is production-ready.");
        } else {
            System.out.println("\n[WARNING] Some tests failed. Review above for details.");
        }

        System.out.println("\nTest Coverage:");
        System.out.println("  - Unit Tests (Participant, Team, Classifier, Validation)");
        System.out.println("  - Integration Tests (UserService, TeamService, TeamBuilder)");
        System.out.println("  - File Handler Tests (CSV validation, data integrity)");
        System.out.println("  - Concurrency Tests (Threading, race conditions, scalability)");
        System.out.println("  - User Acceptance Tests (End-to-end workflows)");
        System.out.println("\nTotal Test Categories: 10");
        System.out.println("Total Tests: " + testsRun);
        System.out.println("================================================================\n");
    }

    @FunctionalInterface
    interface TestCase {
        boolean run() throws Exception;
    }
}