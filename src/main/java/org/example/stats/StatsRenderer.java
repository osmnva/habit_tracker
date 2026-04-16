package org.example.stats;

import org.example.model.Habit;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;

public class StatsRenderer {

    // ── ANSI ──────────────────────────────────────────────────────────────────
    private static final String RESET  = "\u001B[0m";
    private static final String BOLD   = "\u001B[1m";
    private static final String DIM    = "\u001B[2m";
    private static final String GREEN  = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String RED    = "\u001B[31m";
    private static final String CYAN   = "\u001B[36m";
    private static final String BLUE   = "\u001B[34m";

    private static final int BAR_WIDTH = 20;

    private final StatsService svc = new StatsService();

    public void printDashboard(List<Habit> habits) {
        sectionHeader("📊  STATISTICS DASHBOARD");

        long   doneToday = svc.totalCompletedToday(habits);
        double avgStreak = svc.averageStreak(habits);
        double rate7     = svc.overallCompletionRate(habits, 7)  * 100;
        double rate30    = svc.overallCompletionRate(habits, 30) * 100;

        System.out.printf("  %-30s %s%d / %d%s%n",
                "Completed today:", GREEN + BOLD, doneToday, habits.size(), RESET);
        System.out.printf("  %-30s %s%.1f days%s%n",
                "Average current streak:", CYAN, avgStreak, RESET);
        System.out.printf("  %-30s %s%.0f%%%s%n",
                "7-day completion rate:",  rateColor(rate7),  rate7,  RESET);
        System.out.printf("  %-30s %s%.0f%%%s%n",
                "30-day completion rate:", rateColor(rate30), rate30, RESET);

        svc.topStreakHabit(habits).ifPresent(h ->
                System.out.printf("  %-30s %s%s%s  (%d days)%n",
                        "🔥 Top streak habit:",
                        YELLOW + BOLD, h.getName(), RESET, h.getCurrentStreak()));

        divider();
        System.out.println(BOLD + "  30-day heat map" + RESET
                + "  " + DIM + "(█ = done  ░ = missed)" + RESET);
        System.out.println();

        for (Habit h : habits) {
            int    streak = h.getCurrentStreak();
            double rate   = h.completionRate(30) * 100;
            String sColor = streak >= 7 ? GREEN : streak >= 3 ? YELLOW : RESET;
            System.out.printf("  %-22s %s%s%s  streak:%s%2d%s  rate:%s%3.0f%%%s%n",
                    truncate(h.getName(), 20),
                    sColor, svc.heatMapRow(h), RESET,
                    sColor, streak, RESET,
                    rateColor(rate), rate, RESET);
        }

        divider();
        System.out.println(BOLD + "  14-day activity (all habits)" + RESET);
        System.out.println();
        printDailyBarChart(habits, 14);

        divider();
        System.out.println(BOLD + "  7-day completion rate per habit" + RESET);
        System.out.println();
        for (Habit h : habits) {
            double r = h.completionRate(7) * 100;
            printPercentBar(truncate(h.getName(), 20), r, BAR_WIDTH);
        }
        divider();
    }


    public void printWeeklyReview(Habit habit, int weekOffset) {
        sectionHeader("📅  WEEKLY REVIEW — " + habit.getName());

        LinkedHashMap<LocalDate, Boolean> grid = svc.buildWeekGrid(habit, weekOffset);
        String weekLabel = weekOffset == 0 ? "This week"
                : weekOffset == -1            ? "Last week"
                : "Week " + weekOffset;
        System.out.println("  " + CYAN + weekLabel + RESET);
        System.out.println();


        System.out.print("  ");
        for (LocalDate d : grid.keySet()) {
            System.out.printf("%-8s", d.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
        }
        System.out.println();


        System.out.print("  ");
        int done = 0;
        for (Map.Entry<LocalDate, Boolean> e : grid.entrySet()) {
            if (e.getValue()) { done++; System.out.print(GREEN + "  ✔     " + RESET); }
            else               System.out.print(RED   + "  ✘     " + RESET);
        }
        System.out.println();


        System.out.print("  ");
        for (LocalDate d : grid.keySet()) {
            System.out.printf(DIM + "%-8s" + RESET, d.getMonthValue() + "/" + d.getDayOfMonth());
        }
        System.out.println();
        System.out.println();
        System.out.printf("  Completed %s%d/7%s days  (%.0f%%)%n",
                BOLD, done, RESET, done / 7.0 * 100);


        svc.bestDayOfWeek(habit).ifPresent(dow ->
                System.out.println("  Best historical day: "
                        + YELLOW + dow.getDisplayName(TextStyle.FULL, Locale.ENGLISH) + RESET));

        divider();
    }
    public void printMonthlyReview(Habit habit) {
        sectionHeader("📅  MONTHLY REVIEW — " + habit.getName());

        LinkedHashMap<LocalDate, Boolean> grid = svc.buildGrid(habit, 30);
        int total = 0;

        System.out.println("  Last 30 days (row = 7 days):");
        System.out.println();

        List<Map.Entry<LocalDate, Boolean>> entries = new ArrayList<>(grid.entrySet());
        for (int i = 0; i < entries.size(); i++) {
            if (i % 7 == 0) System.out.print("  ");
            boolean done = entries.get(i).getValue();
            if (done) { total++; System.out.print(GREEN + " ██ " + RESET); }
            else        System.out.print(DIM   + " ░░ " + RESET);
            if ((i + 1) % 7 == 0 || i == entries.size() - 1) System.out.println();
        }

        System.out.println();
        System.out.printf("  Completed : %s%d/30%s days  (%.0f%%)%n",
                BOLD, total, RESET, total / 30.0 * 100);
        System.out.printf("  Streak    : %s%d%s days (current)  |  %s%d%s days (best)%n",
                GREEN, habit.getCurrentStreak(), RESET, YELLOW, habit.getBestStreak(), RESET);


        System.out.println();
        System.out.println(BOLD + "  Completions per week (last 4):" + RESET);
        System.out.println();
        Map<Integer, Integer> perWeek = svc.completionsPerWeekLast4(habit);
        for (Map.Entry<Integer, Integer> e : perWeek.entrySet()) {
            printPercentBar("Week " + e.getKey(), e.getValue() / 7.0 * 100, 14);
            System.out.printf("  %s(%d/7)%s%n", DIM, e.getValue(), RESET);
        }
        divider();
    }



    private void printDailyBarChart(List<Habit> habits, int days) {
        int maxHabits = habits.size();
        if (maxHabits == 0) return;

        LinkedHashMap<LocalDate, Integer> activity = svc.dailyActivityAllHabits(habits, days);

        for (Map.Entry<LocalDate, Integer> e : activity.entrySet()) {
            LocalDate d     = e.getKey();
            int       count = e.getValue();
            double    pct   = (double) count / maxHabits;
            int       bars  = (int) Math.round(pct * BAR_WIDTH);

            String label = d.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
                    + " " + d.getDayOfMonth();
            String color = count == maxHabits ? GREEN : count > 0 ? YELLOW : RED;

            System.out.printf("  %-8s %s%s%s %s%d/%d%s%n",
                    label,
                    color, "█".repeat(bars) + "░".repeat(BAR_WIDTH - bars), RESET,
                    DIM, count, maxHabits, RESET);
        }
    }


    private void printPercentBar(String label, double pct, int width) {
        int    filled = (int) Math.round(pct / 100 * width);
        int    empty  = width - filled;
        String color  = pct >= 80 ? GREEN : pct >= 50 ? YELLOW : RED;
        System.out.printf("  %-22s [%s%s%s%s]  %s%.0f%%%s%n",
                label,
                color, "█".repeat(filled), RESET, DIM + "░".repeat(empty) + RESET,
                BOLD + color, pct, RESET);
    }


    public void sectionHeader(String title) {
        System.out.println();
        System.out.println(BOLD + "  ══ " + title
                + " " + "═".repeat(Math.max(0, 48 - title.length())) + RESET);
        System.out.println();
    }

    private void divider() {
        System.out.println();
        System.out.println("  " + DIM + "─".repeat(56) + RESET);
        System.out.println();
    }

    private String rateColor(double pct) {
        if (pct >= 80) return GREEN;
        if (pct >= 50) return YELLOW;
        return RED;
    }

    private String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}
