package org.example.ui;

import org.example.achievement.Badge;
import org.example.achievement.BadgeService;
import org.example.goal.GoalService;
import org.example.model.Habit;
import org.example.service.HabitService;
import org.example.stats.StatsRenderer;
import org.example.stats.StatsService;
import org.example.util.Printer;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import static org.example.util.Printer.*;


public class ConsoleUI {

    private static final List<String> PRESET_CATEGORIES = Arrays.asList(
            "Health", "Fitness", "Learning", "Mindfulness",
            "Productivity", "Finance", "Social", "Other"
    );


    private final HabitService   service;
    private final BadgeService   badgeService;
    private final GoalService    goalService;
    private final StatsRenderer  statsRenderer;
    private final StatsService   statsService;
    private final Scanner        scanner;

    public ConsoleUI(HabitService service) {
        this.service       = service;
        this.badgeService  = new BadgeService();
        this.goalService   = new GoalService();
        this.statsRenderer = new StatsRenderer();
        this.statsService  = new StatsService();
        this.scanner       = new Scanner(System.in);
    }

    public void start() {
        printBanner();
        boolean running = true;
        while (running) {
            printMenu();
            String choice = readLine("Your choice").trim();
            blankLine();
            switch (choice) {
                case "1"  -> handleAddHabit();
                case "2"  -> handleShowHabits();
                case "3"  -> handleCompleteHabit();
                case "4"  -> handleEditHabit();
                case "5"  -> handleDeleteHabit();
                case "6"  -> handleStatsDashboard();
                case "7"  -> handleWeeklyReview();
                case "8"  -> handleMonthlyReview();
                case "9"  -> handleAchievements();
                case "10" -> handleByCategory();
                case "0"  -> { info("Goodbye! Keep building those habits. 🌱"); running = false; }
                default   -> warn("Please enter a number from 0 to 10.");
            }
            if (running) pause();
        }
        scanner.close();
    }

    private void handleAddHabit() {
        header("ADD NEW HABIT");

        // Name
        String name = readLine("Habit name");
        if (name.isBlank()) { error("Name cannot be empty."); return; }

        // Description
        String description = readLine("Description (Enter to skip)");

        // Category
        String category = pickCategory();

        // Goal
        int goalDays = 0;
        String goalInput = readLine("Streak goal in days (Enter to skip, e.g. 30)");
        if (!goalInput.isBlank()) {
            try { goalDays = Math.max(0, Integer.parseInt(goalInput.trim())); }
            catch (NumberFormatException e) { warn("Not a number — goal skipped."); }
        }

        try {
            Habit added = service.addHabit(name, description, category, goalDays);
            success("Habit \"" + added.getName() + "\" added!"
                    + "  [" + CYAN + category + RESET + "]"
                    + (goalDays > 0 ? "   Goal: " + goalDays + " days" : ""));
        } catch (IllegalArgumentException e) {
            error(e.getMessage());
        }
    }

    private void handleShowHabits() {
        List<Habit> habits = service.getAllHabits();
        header("ALL HABITS");

        if (habits.isEmpty()) {
            info("No habits yet. Add one with option 1.");
            return;
        }

        // Table header
        System.out.printf(BOLD + "  %-3s %-22s %-12s %-10s %-7s %-7s %-7s %-14s%n" + RESET,
                "#", "Name", "Category", "Today?", "Streak", "Best", "Goal", "Badges");
        divider();

        for (int i = 0; i < habits.size(); i++) {
            Habit  h      = habits.get(i);
            String today  = h.isCompletedOn(LocalDate.now())
                    ? GREEN + "✔ Done  " + RESET
                    : YELLOW + "○ Pend  " + RESET;

            int    streak = h.getCurrentStreak();
            String sColor = streak >= 10 ? GREEN : streak >= 5 ? YELLOW : RESET;

            System.out.printf("  %-3d %-22s %-12s %-18s %s%-5d%s  %-5d  %-7s %s%n",
                    i + 1,
                    truncate(h.getName(), 20),
                    truncate(h.getCategory(), 10),
                    today,
                    sColor, streak, RESET,
                    h.getBestStreak(),
                    goalService.goalColumn(h),
                    badgeService.badgeLine(h));
        }
        divider();
        System.out.printf("  Total: %d habit(s)  |  Done today: %s%d%s / %d%n",
                habits.size(),
                GREEN, statsService.totalCompletedToday(habits), RESET, habits.size());
    }

    private void handleCompleteHabit() {
        header("MARK AS COMPLETED");
        handleShowHabits();
        if (service.getAllHabits().isEmpty()) return;

        int index = readIndex("Habit number");
        if (index < 0) return;

        // Date
        String dateStr = readLine("Date YYYY-MM-DD (Enter = today)");
        LocalDate date;
        if (dateStr.isBlank()) {
            date = LocalDate.now();
        } else {
            try { date = LocalDate.parse(dateStr.trim()); }
            catch (DateTimeParseException e) {
                error("Invalid date format. Use YYYY-MM-DD (e.g. 2025-04-17).");
                return;
            }
        }

        try {
            // Snapshot badges BEFORE so we can detect new ones
            Habit habitBefore = service.getByIndex(index);
            List<Badge> before = badgeService.getUnlocked(habitBefore);

            Habit habit = service.markCompleted(index, date);

            List<Badge> after    = badgeService.getUnlocked(habit);
            List<Badge> newBadges = badgeService.getNewlyUnlocked(before, after);

            success("\"" + habit.getName() + "\" marked for " + date
                    + "  |  Streak: " + BOLD + habit.getCurrentStreak() + RESET + " day(s)");

            // Goal progress
            if (habit.getGoalDays() > 0) {
                blankLine();
                goalService.printProgressBar(habit);
            }

            // 🎉 Newly earned badges
            if (!newBadges.isEmpty()) {
                blankLine();
                achievement("Achievement" + (newBadges.size() > 1 ? "s" : "") + " unlocked!");
                for (Badge b : newBadges) {
                    System.out.println("     " + b.emoji + "  " + BOLD + b.title + RESET
                            + "  — " + b.description);
                }
            }

        } catch (IllegalArgumentException e) {
            error(e.getMessage());
        }
    }

    private void handleEditHabit() {
        header("EDIT HABIT");
        handleShowHabits();
        if (service.getAllHabits().isEmpty()) return;

        int index = readIndex("Habit number to edit");
        if (index < 0) return;

        Habit current;
        try { current = service.getByIndex(index); }
        catch (IllegalArgumentException e) { error(e.getMessage()); return; }

        System.out.println();
        info("Leave blank to keep the current value.");
        System.out.printf("  Current name        : %s%s%s%n", CYAN, current.getName(), RESET);
        System.out.printf("  Current description : %s%n", current.getDescription());
        System.out.printf("  Current category    : %s%n", current.getCategory());
        System.out.printf("  Current goal        : %d days%n", current.getGoalDays());
        blankLine();

        String newName  = readLine("New name (Enter to keep)");
        String newDesc  = readLine("New description (Enter to keep)");
        String newCat   = readLine("New category (Enter to keep, or pick from presets above)");

        String goalStr  = readLine("New goal days (Enter to keep, 0 = remove goal)");
        int newGoal = -1; // -1 means "no change" in the service
        if (!goalStr.isBlank()) {
            try { newGoal = Math.max(0, Integer.parseInt(goalStr.trim())); }
            catch (NumberFormatException e) { warn("Invalid number — goal unchanged."); }
        }

        try {
            Habit updated = service.editHabit(index,
                    newName.isBlank()  ? null : newName,
                    newDesc.isBlank()  ? null : newDesc,
                    newCat.isBlank()   ? null : newCat,
                    newGoal);
            success("\"" + updated.getName() + "\" updated.");
        } catch (IllegalArgumentException e) {
            error(e.getMessage());
        }
    }
    private void handleDeleteHabit() {
        header("DELETE HABIT");
        handleShowHabits();
        if (service.getAllHabits().isEmpty()) return;

        int index = readIndex("Habit number to delete");
        if (index < 0) return;

        try {
            Habit habit = service.getByIndex(index);
            String confirm = readLine(
                    "Delete \"" + habit.getName() + "\" and ALL its history? (yes / no)");
            if (confirm.equalsIgnoreCase("yes") || confirm.equalsIgnoreCase("y")) {
                success("Habit \"" + service.deleteHabit(index) + "\" deleted.");
            } else {
                info("Deletion cancelled.");
            }
        } catch (IllegalArgumentException e) {
            error(e.getMessage());
        }
    }


    private void handleStatsDashboard() {
        List<Habit> habits = service.getAllHabits();
        if (habits.isEmpty()) { info("No habits yet."); return; }
        statsRenderer.printDashboard(habits);
    }
    private void handleWeeklyReview() {
        List<Habit> habits = service.getAllHabits();
        if (habits.isEmpty()) { info("No habits yet."); return; }

        header("WEEKLY REVIEW");
        handleShowHabits();

        int index = readIndex("Pick habit number");
        if (index < 0) return;

        Habit habit;
        try { habit = service.getByIndex(index); }
        catch (IllegalArgumentException e) { error(e.getMessage()); return; }

        int weekOffset = 0;
        boolean navigating = true;

        while (navigating) {
            statsRenderer.printWeeklyReview(habit, weekOffset);
            System.out.println("  [P] Previous week   [N] Next week   [Q] Quit");
            String nav = readLine("Navigation").trim().toUpperCase();
            switch (nav) {
                case "P" -> weekOffset--;
                case "N" -> { if (weekOffset < 0) weekOffset++; else warn("Already at current week."); }
                case "Q" -> navigating = false;
                default  -> warn("Press P, N, or Q.");
            }
        }
    }

    private void handleMonthlyReview() {
        List<Habit> habits = service.getAllHabits();
        if (habits.isEmpty()) { info("No habits yet."); return; }

        header("MONTHLY REVIEW");
        handleShowHabits();

        int index = readIndex("Pick habit number");
        if (index < 0) return;

        Habit habit;
        try { habit = service.getByIndex(index); }
        catch (IllegalArgumentException e) { error(e.getMessage()); return; }

        statsRenderer.printMonthlyReview(habit);
    }
    private void handleAchievements() {
        List<Habit> habits = service.getAllHabits();
        if (habits.isEmpty()) { info("No habits yet."); return; }

        header("ACHIEVEMENTS & BADGES");
        System.out.println("  " + DIM + "0 = overview of all habits" + RESET);
        blankLine();


        for (int i = 0; i < habits.size(); i++) {
            badgeService.printBadgeSummaryRow(i + 1, habits.get(i));
        }
        blankLine();

        int index = readIndex("Pick habit number (0 = full summary)");
        if (index < 0) return;

        if (index == 0) {

            for (Habit h : habits) {
                badgeService.printBadgePanel(h);
            }
        } else {
            Habit habit;
            try { habit = service.getByIndex(index); }
            catch (IllegalArgumentException e) { error(e.getMessage()); return; }
            badgeService.printBadgePanel(habit);
            if (habit.getGoalDays() > 0) {
                goalService.printProgressBar(habit);
            }
        }
    }

    private void handleByCategory() {
        List<String> cats = service.getCategories();
        if (cats.isEmpty()) { info("No categories found."); return; }

        header("HABITS BY CATEGORY");

        for (int i = 0; i < cats.size(); i++) {
            String cat    = cats.get(i);
            List<Habit> inCat = service.filterByCategory(cat);
            long done = inCat.stream()
                    .filter(h -> h.isCompletedOn(LocalDate.now())).count();
            System.out.printf("  %2d.  %-18s  %s%d habit(s)%s   done today: %s%d%s%n",
                    i + 1, cat,
                    DIM, inCat.size(), RESET,
                    GREEN, done, RESET);
        }
        blankLine();

        String pick = readLine("Pick category number (Enter to cancel)");
        if (pick.isBlank()) return;
        int idx;
        try { idx = Integer.parseInt(pick.trim()); }
        catch (NumberFormatException e) { error("Invalid input."); return; }
        if (idx < 1 || idx > cats.size()) { error("Invalid number."); return; }

        String cat     = cats.get(idx - 1);
        List<Habit> filtered = service.filterByCategory(cat);

        blankLine();
        System.out.println(BOLD + "  ── " + CYAN + cat + RESET + BOLD + " ──" + RESET);
        divider();

        for (Habit h : filtered) {
            String today  = h.isCompletedOn(LocalDate.now())
                    ? GREEN + "Done" + RESET : YELLOW + "Pend" + RESET;
            int    streak = h.getCurrentStreak();
            String sColor = streak >= 7 ? GREEN : streak >= 3 ? YELLOW : RESET;

            System.out.printf("  %s  %-24s streak:%s%2d%s  best:%2d  %s%n",
                    today,
                    truncate(h.getName(), 22),
                    sColor, streak, RESET,
                    h.getBestStreak(),
                    badgeService.badgeLine(h));
        }
        divider();
    }

    private void printBanner() {
        System.out.println();
        System.out.println(BOLD + CYAN  + "╔══════════════════════════════════════════════╗" + RESET);
        System.out.println(BOLD + CYAN  + "║                                              ║" + RESET);
        System.out.println(BOLD + CYAN  + "║        HABIT TRACKER  v2.0                   ║" + RESET);
        System.out.println(BOLD + CYAN  + "║                                              ║" + RESET);
        System.out.println(BOLD + CYAN  + "║                                              ║" + RESET);
        System.out.println(BOLD + CYAN  + "╚══════════════════════════════════════════════╝" + RESET);
        System.out.println();
    }

    private void printMenu() {
        // Quick-status bar
        List<Habit> all = service.getAllHabits();
        if (!all.isEmpty()) {
            long done = statsService.totalCompletedToday(all);
            String bar = done == all.size() ? GREEN : done > 0 ? YELLOW : DIM;
            System.out.println(bar + "  Today: " + done + " / " + all.size()
                    + " habits done" + RESET);
            blankLine();
        }

        System.out.println(BOLD + "  ┌──────────────────────────────────────────────┐" + RESET);
        System.out.println(BOLD + "  │                   MAIN MENU                  │" + RESET);
        System.out.println(BOLD + "  ├────────────────────────┬─────────────────────┤" + RESET);
        System.out.println(      "  │  1.  Add habit         │  6.     Statistics  │");
        System.out.println(      "  │  2.  Show all habits   │  7.     Weekly      │");
        System.out.println(      "  │  3.  Complete habit    │  8.     Monthly     │");
        System.out.println(      "  │  4.  Edit habit        │  9.     Achievements│");
        System.out.println(      "  │  5.  Delete habit      │ 10.     Categories  │");
        System.out.println(BOLD + "  ├────────────────────────┴─────────────────────┤" + RESET);
        System.out.println(      "  │  0.  Exit                                    │");
        System.out.println(BOLD + "  └──────────────────────────────────────────────┘" + RESET);
    }
    private String pickCategory() {
        System.out.println();
        System.out.println("  Categories:");
        for (int i = 0; i < PRESET_CATEGORIES.size(); i++) {
            System.out.printf("    %d. %s%n", i + 1, PRESET_CATEGORIES.get(i));
        }
        String input = readLine("Pick number or type custom name (Enter = General)");
        if (input.isBlank()) return "General";
        try {
            int i = Integer.parseInt(input.trim());
            return (i >= 1 && i <= PRESET_CATEGORIES.size())
                    ? PRESET_CATEGORIES.get(i - 1) : "Other";
        } catch (NumberFormatException e) {
            return input.trim();
        }
    }
    private String readLine(String label) {
        Printer.prompt(label);
        return scanner.nextLine();
    }
    private int readIndex(String label) {
        String raw = readLine(label);
        try { return Integer.parseInt(raw.trim()); }
        catch (NumberFormatException e) {
            error("Please enter a valid number.");
            return -1;
        }
    }

    private void pause() {
        blankLine();
        System.out.print("  Press Enter to continue...");
        scanner.nextLine();
        blankLine();
    }
}