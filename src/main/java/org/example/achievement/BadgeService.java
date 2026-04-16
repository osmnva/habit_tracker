package org.example.achievement;

import org.example.model.Habit;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class BadgeService {

    private static final String RESET = "\u001B[0m";
    private static final String BOLD  = "\u001B[1m";
    private static final String DIM   = "\u001B[2m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW= "\u001B[33m";
    private static final String CYAN  = "\u001B[36m";


    public List<Badge> getUnlocked(Habit habit) {
        return Arrays.stream(Badge.values())
                .filter(b -> b.isUnlocked(habit))
                .collect(Collectors.toList());
    }
    public List<Badge> getNewlyUnlocked(List<Badge> before, List<Badge> after) {
        return after.stream()
                .filter(b -> !before.contains(b))
                .collect(Collectors.toList());
    }

    public String badgeLine(Habit habit) {
        List<Badge> earned = getUnlocked(habit);
        if (earned.isEmpty()) return DIM + "—" + RESET;
        return earned.stream().map(b -> b.emoji).collect(Collectors.joining(" "));
    }
    public void printBadgePanel(Habit habit) {
        List<Badge> unlocked = getUnlocked(habit);
        System.out.println();
        System.out.println(BOLD + "  🏅  Achievements — " + habit.getName() + RESET);
        System.out.println("  " + "─".repeat(56));

        for (Badge b : Badge.values()) {
            boolean earned = unlocked.contains(b);
            String mark    = earned ? GREEN + "✔" + RESET : DIM + "○" + RESET;
            String line    = earned
                    ? String.format("  %s  %s%-12s%s  %s", mark, BOLD, b.emoji + " " + b.title, RESET, b.description)
                    : String.format("  %s  %s%-12s  %s%s", mark, DIM,  "   " + b.title,         b.description, RESET);
            System.out.println(line);
        }

        System.out.println("  " + "─".repeat(56));
        System.out.printf("  Earned: %s%d%s / %d%n",
                YELLOW + BOLD, unlocked.size(), RESET, Badge.values().length);
        System.out.println();
    }
    public void printBadgeSummaryRow(int index, Habit habit) {
        List<Badge> earned = getUnlocked(habit);
        System.out.printf("  %2d. %-22s  %2d earned  %s%n",
                index,
                truncate(habit.getName(), 20),
                earned.size(),
                badgeLine(habit));
    }

    private String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}