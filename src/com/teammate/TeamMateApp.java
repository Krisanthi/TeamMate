package com.teammate;

import com.teammate.model.*;
import com.teammate.service.*;
import com.teammate.util.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * TeamMate: Intelligent Team Formation System
 * Main Application Class
 *
 * @author 2425596
 * @version 1.0
 * @since 2025-11-17
 */
public class TeamMateApp {

    private static final String INPUT_FILE = "participants_sample.csv";
    private static final String OUTPUT_FILE = "formed_teams.csv";
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════╗");
        System.out.println("║   TeamMate: Team Formation System v1.0    ║");
        System.out.println("║   University Gaming Club Management       ║");
        System.out.println("╚════════════════════════════════════════════╝\n");

        try {
            // Step 1: Load participants from CSV
            System.out.println(" Loading participant data...");
            FileHandler fileHandler = new FileHandler(INPUT_FILE, OUTPUT_FILE);
            List<Participant> participants = fileHandler.loadParticipants();
            System.out.println(" Successfully loaded " + participants.size() + " participants\n");

            // Step 2: Display statistics
            displayStatistics(participants);

            // Step 3: Get team size from user
            int teamSize = getTeamSize(participants.size());

            // Step 4: Form teams using concurrent processing
            System.out.println("\n  Processing team formation...");
            TeamBuilder teamBuilder = new TeamBuilder(participants, teamSize);

            // Use ExecutorService for concurrent team formation
            ExecutorService executor = Executors.newFixedThreadPool(3);
            Future<List<Team>> futureTeams = executor.submit(() -> teamBuilder.formTeams());

            System.out.println(" Teams are being formed (using concurrent threads)...");
            List<Team> formedTeams = futureTeams.get(); // Wait for completion
            executor.shutdown();

            System.out.println("✓ Successfully formed " + formedTeams.size() + " teams\n");

            // Step 5: Display formed teams
            displayTeams(formedTeams);

            // Step 6: Save to CSV
            System.out.print("\n Save teams to CSV? (y/n): ");
            String response = scanner.nextLine().trim().toLowerCase();
            if (response.equals("y") || response.equals("yes")) {
                fileHandler.saveTeams(formedTeams);
                System.out.println("✓ Teams saved to " + OUTPUT_FILE);
            }

            System.out.println("\n Team formation completed successfully!");

        } catch (InvalidInputException e) {
            System.err.println(" Input Error: " + e.getMessage());
            Logger.logError("Invalid input: " + e.getMessage());
        } catch (FileProcessingException e) {
            System.err.println(" File Error: " + e.getMessage());
            Logger.logError("File processing error: " + e.getMessage());
        } catch (ExecutionException | InterruptedException e) {
            System.err.println(" Concurrency Error: " + e.getMessage());
            Logger.logError("Thread execution error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println(" Unexpected Error: " + e.getMessage());
            Logger.logError("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
            Logger.logInfo("Application terminated");
        }
    }

    /**
     * Displays statistical overview of loaded participants
     */
    private static void displayStatistics(List<Participant> participants) {
        System.out.println(" PARTICIPANT STATISTICS");
        System.out.println("─────────────────────────────────────────");

        // Count by personality type
        Map<PersonalityType, Long> personalityCount = new HashMap<>();
        Map<Role, Long> roleCount = new HashMap<>();
        Map<String, Long> gameCount = new HashMap<>();

        for (Participant p : participants) {
            personalityCount.merge(p.getPersonalityType(), 1L, Long::sum);
            roleCount.merge(p.getPreferredRole(), 1L, Long::sum);
            gameCount.merge(p.getPreferredGame(), 1L, Long::sum);
        }

        System.out.println("\nPersonality Distribution:");
        personalityCount.forEach((type, count) ->
                System.out.printf("  %s: %d (%.1f%%)\n", type, count,
                        (count * 100.0 / participants.size())));

        System.out.println("\nTop 3 Games:");
        gameCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(3)
                .forEach(e -> System.out.printf("  %s: %d players\n", e.getKey(), e.getValue()));

        double avgSkill = participants.stream()
                .mapToInt(Participant::getSkillLevel)
                .average()
                .orElse(0.0);
        System.out.printf("\nAverage Skill Level: %.2f\n", avgSkill);
        System.out.println("─────────────────────────────────────────\n");
    }

    /**
     * Gets valid team size from user input
     */
    private static int getTeamSize(int totalParticipants) throws InvalidInputException {
        System.out.print("Enter desired team size (3-10): ");

        try {
            String input = scanner.nextLine().trim();
            int size = Integer.parseInt(input);

            if (size < 3 || size > 10) {
                throw new InvalidInputException("Team size must be between 3 and 10");
            }

            int maxTeams = totalParticipants / size;
            if (maxTeams < 2) {
                throw new InvalidInputException(
                        "Not enough participants for at least 2 teams of size " + size);
            }

            System.out.println("✓ Team size set to: " + size);
            System.out.println("  (Can form approximately " + maxTeams + " teams)");

            return size;

        } catch (NumberFormatException e) {
            throw new InvalidInputException("Please enter a valid number");
        }
    }

    /**
     * Displays all formed teams with detailed information
     */
    private static void displayTeams(List<Team> teams) {
        System.out.println("\n FORMED TEAMS");
        System.out.println("═════════════════════════════════════════");

        for (int i = 0; i < teams.size(); i++) {
            Team team = teams.get(i);
            System.out.println("\n" + team.getTeamId() + " (Avg Skill: " +
                    String.format("%.2f", team.getAverageSkill()) + ")");
            System.out.println("─────────────────────────────────────────");

            for (Participant member : team.getMembers()) {
                System.out.printf("  • %-15s | %s | %-12s | %s (Skill: %d)\n",
                        member.getName(),
                        member.getPersonalityType(),
                        member.getPreferredRole(),
                        member.getPreferredGame(),
                        member.getSkillLevel());
            }

            // Display team composition
            System.out.println("\n  Composition:");
            System.out.println("    Personalities: " + getPersonalityDistribution(team));
            System.out.println("    Roles: " + getRoleDistribution(team));
            System.out.println("    Games: " + getGameDistribution(team));
            System.out.println("    Balance Score: " + (team.isBalanced() ? "✓ Balanced" : " Review Needed"));
        }

        System.out.println("\n═════════════════════════════════════════");
    }

    private static String getPersonalityDistribution(Team team) {
        Map<PersonalityType, Long> dist = new HashMap<>();
        for (Participant p : team.getMembers()) {
            dist.merge(p.getPersonalityType(), 1L, Long::sum);
        }
        return dist.toString();
    }

    private static String getRoleDistribution(Team team) {
        Set<Role> roles = new HashSet<>();
        for (Participant p : team.getMembers()) {
            roles.add(p.getPreferredRole());
        }
        return roles.size() + " different roles";
    }

    private static String getGameDistribution(Team team) {
        Set<String> games = new HashSet<>();
        for (Participant p : team.getMembers()) {
            games.add(p.getPreferredGame());
        }
        return games.size() + " different games";
    }
}