package org.example.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Habit {


    private final String    id;
    private String          name;
    private String          description;
    private String          category;
    private final LocalDate createdDate;


    private List<LocalDate> completedDates;


    private int goalDays;       // 0 = no goal


    private int bestStreak;



    public Habit(String name, String description, String category, int goalDays) {
        this.id             = UUID.randomUUID().toString();
        this.name           = name.trim();
        this.description    = description.trim();
        this.category       = (category == null || category.isBlank()) ? "General" : category.trim();
        this.createdDate    = LocalDate.now();
        this.completedDates = new ArrayList<>();
        this.goalDays       = Math.max(0, goalDays);
        this.bestStreak     = 0;
    }


    public boolean markCompleted(LocalDate date) {
        if (completedDates.contains(date)) return false;
        completedDates.add(date);
        completedDates.sort(null);
        int current = getCurrentStreak();
        if (current > bestStreak) bestStreak = current;
        return true;
    }


    public boolean isCompletedOn(LocalDate date) {
        return completedDates.contains(date);
    }


    public int getCurrentStreak() {
        if (completedDates.isEmpty()) return 0;
        LocalDate cursor = LocalDate.now();
        if (!completedDates.contains(cursor)) cursor = cursor.minusDays(1);
        int streak = 0;
        while (completedDates.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }


    public int getBestStreak() { return bestStreak; }

    public int completionsInLastDays(int days) {
        LocalDate from = LocalDate.now().minusDays(days - 1L);
        return (int) completedDates.stream().filter(d -> !d.isBefore(from)).count();
    }
    public double completionRate(int days) {
        if (days <= 0) return 0;
        return (double) completionsInLastDays(days) / days;
    }


    public int totalCompletions() { return completedDates.size(); }


    public boolean isGoalReached() {
        return goalDays > 0 && getCurrentStreak() >= goalDays;
    }


    public int goalProgressPercent() {
        if (goalDays <= 0) return 0;
        return Math.min(100, getCurrentStreak() * 100 / goalDays);
    }


    public String          getId()              { return id; }
    public String          getName()            { return name; }
    public String          getDescription()     { return description; }
    public String          getCategory()        { return category; }
    public LocalDate       getCreatedDate()     { return createdDate; }
    public List<LocalDate> getCompletedDates()  { return completedDates; }
    public int             getGoalDays()        { return goalDays; }

    public void setName(String name)            { this.name = name.trim(); }
    public void setDescription(String d)        { this.description = d.trim(); }
    public void setCategory(String c)           { this.category = (c == null || c.isBlank()) ? "General" : c.trim(); }
    public void setGoalDays(int g)              { this.goalDays = Math.max(0, g); }

    @Override
    public String toString() {
        return String.format("Habit{name='%s', category='%s', streak=%d}",
                name, category, getCurrentStreak());
    }
}