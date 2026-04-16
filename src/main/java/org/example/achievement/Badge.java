package org.example.achievement;

import org.example.model.Habit;

import java.util.function.Predicate;

public enum Badge {


    FIRST_STEP   ("🌱", "First Step",       "Complete for the first time",        h -> h.totalCompletions() >= 1),
    BEGINNER     ("🔹", "Beginner",          "3 days in a row",                    h -> h.getCurrentStreak() >= 3),
    ON_FIRE      ("🔥", "On Fire",           "5 days in a row",                    h -> h.getCurrentStreak() >= 5),
    STRONG       ("💪", "Strong",            "10 days in a row",                   h -> h.getCurrentStreak() >= 10),
    UNSTOPPABLE  ("⚡", "Unstoppable",       "14 days in a row",                   h -> h.getCurrentStreak() >= 14),
    MASTER       ("🏆", "Master",            "30 days in a row",                   h -> h.getCurrentStreak() >= 30),
    LEGEND       ("👑", "Legend",            "100 days in a row",                  h -> h.getCurrentStreak() >= 100),


    DEDICATED    ("📌", "Dedicated",         "50 total completions",               h -> h.totalCompletions() >= 50),
    VETERAN      ("🎖️", "Veteran",           "200 total completions",              h -> h.totalCompletions() >= 200),


    PERFECT_WEEK ("⭐", "Perfect Week",      "7/7 days this week",                 h -> h.completionsInLastDays(7) == 7),
    CONSISTENT   ("📅", "Consistent",        "80 %+ rate over 30 days",            h -> h.completionRate(30) >= 0.80),
    IRON_HABIT   ("🔑", "Iron Habit",        "90 %+ rate over 30 days",            h -> h.completionRate(30) >= 0.90),


    GOAL_CRUSHER ("🎯", "Goal Crusher",      "Streak goal reached!",               Habit::isGoalReached);

    public final String            emoji;
    public final String            title;
    public final String            description;
    private final Predicate<Habit> condition;

    Badge(String emoji, String title, String description, Predicate<Habit> condition) {
        this.emoji       = emoji;
        this.title       = title;
        this.description = description;
        this.condition   = condition;
    }
    public boolean isUnlocked(Habit habit) {
        return condition.test(habit);
    }
}