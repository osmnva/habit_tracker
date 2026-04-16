package org.example.goal;

import org.example.model.Habit;

public class GoalService {

    private static final String RESET  = "\u001B[0m";
    private static final String BOLD   = "\u001B[1m";
    private static final String DIM    = "\u001B[2m";
    private static final String GREEN  = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String RED    = "\u001B[31m";

    private static final int BAR_WIDTH = 25;
    public void printProgressBar(Habit habit) {
        if (habit.getGoalDays() <= 0) {
            System.out.println("  " + DIM + "No goal set." + RESET);
            return;
        }

        int  pct     = habit.goalProgressPercent();
        int  streak  = habit.getCurrentStreak();
        int  goal    = habit.getGoalDays();
        int  filled  = (int) Math.round((double) pct / 100 * BAR_WIDTH);
        int  empty   = BAR_WIDTH - filled;

        String color  = pct >= 100 ? GREEN : pct >= 60 ? YELLOW : RED;
        String bar    = color + "█".repeat(filled) + RESET + DIM + "░".repeat(empty) + RESET;

        System.out.printf("  🎯 Goal %dd  [%s]  %s%d%%%s  (%d/%d days)%n",
                goal, bar, BOLD + color, pct, RESET,
                Math.min(streak, goal), goal);

        if (habit.isGoalReached()) {
            System.out.println("  " + GREEN + BOLD + "  🎉  GOAL REACHED — Congratulations!" + RESET);
        } else {
            System.out.printf("  %s   %d more day(s) to go.%n", DIM, goal - streak, RESET);
        }
    }

    public String goalColumn(Habit habit) {
        if (habit.getGoalDays() <= 0) return DIM + "—" + RESET;
        int pct = habit.goalProgressPercent();
        String color = pct >= 100 ? GREEN : pct >= 60 ? YELLOW : RESET;
        String check = habit.isGoalReached() ? "✔" : "";
        return color + check + pct + "%" + RESET;
    }
}
