package org.example.stats;

import org.example.model.Habit;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

public class StatsService {

    public long totalCompletedToday(List<Habit> habits) {
        LocalDate today = LocalDate.now();
        return habits.stream().filter(h -> h.isCompletedOn(today)).count();
    }

    public Optional<Habit> topStreakHabit(List<Habit> habits) {
        return habits.stream().max(Comparator.comparingInt(Habit::getCurrentStreak));
    }

    public double averageStreak(List<Habit> habits) {
        if (habits.isEmpty()) return 0;
        return habits.stream().mapToInt(Habit::getCurrentStreak).average().orElse(0);
    }

    public double overallCompletionRate(List<Habit> habits, int days) {
        if (habits.isEmpty()) return 0;
        return habits.stream().mapToDouble(h -> h.completionRate(days)).average().orElse(0);
    }

    public LinkedHashMap<LocalDate, Boolean> buildGrid(Habit habit, int days) {
        LinkedHashMap<LocalDate, Boolean> grid = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            grid.put(d, habit.isCompletedOn(d));
        }
        return grid;
    }
    public LinkedHashMap<LocalDate, Boolean> buildWeekGrid(Habit habit, int weekOffset) {
        LocalDate today     = LocalDate.now();
        LocalDate monday    = today.with(WeekFields.ISO.dayOfWeek(), 1).plusWeeks(weekOffset);
        LinkedHashMap<LocalDate, Boolean> grid = new LinkedHashMap<>();
        for (int i = 0; i < 7; i++) {
            LocalDate d = monday.plusDays(i);
            grid.put(d, habit.isCompletedOn(d));
        }
        return grid;
    }

    public Map<DayOfWeek, Long> completionsByDayOfWeek(Habit habit) {
        return habit.getCompletedDates().stream()
                .collect(Collectors.groupingBy(LocalDate::getDayOfWeek, Collectors.counting()));
    }

    public Optional<DayOfWeek> bestDayOfWeek(Habit habit) {
        return completionsByDayOfWeek(habit).entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);
    }

    public Map<Integer, Integer> completionsPerWeekLast4(Habit habit) {
        LocalDate today = LocalDate.now();
        Map<Integer, Integer> result = new LinkedHashMap<>();
        for (int w = 3; w >= 0; w--) {
            LocalDate monday = today.with(WeekFields.ISO.dayOfWeek(), 1).minusWeeks(w);
            int count = 0;
            for (int d = 0; d < 7; d++) {
                if (habit.isCompletedOn(monday.plusDays(d))) count++;
            }
            int weekNum = monday.get(WeekFields.ISO.weekOfWeekBasedYear());
            result.put(weekNum, count);
        }
        return result;
    }

    public LinkedHashMap<LocalDate, Integer> dailyActivityAllHabits(List<Habit> habits, int days) {
        LocalDate today = LocalDate.now();
        LinkedHashMap<LocalDate, Integer> map = new LinkedHashMap<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            int count = (int) habits.stream().filter(h -> h.isCompletedOn(d)).count();
            map.put(d, count);
        }
        return map;
    }
    public String heatMapRow(Habit habit) {
        StringBuilder sb = new StringBuilder();
        buildGrid(habit, 30).values().forEach(done -> sb.append(done ? "█" : "░"));
        return sb.toString();
    }
}
