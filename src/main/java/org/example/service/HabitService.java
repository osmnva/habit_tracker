package org.example.service;

import org.example.model.Habit;
import org.example.repository.HabitRepository;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class HabitService {

    private final HabitRepository repository;
    private final List<Habit>     habits;

    public HabitService(HabitRepository repository) {
        this.repository = repository;
        this.habits     = repository.loadAll();
    }


    public List<Habit> getAllHabits() {
        return Collections.unmodifiableList(habits);
    }

    public Habit getByIndex(int oneBased) {
        if (oneBased < 1 || oneBased > habits.size())
            throw new IllegalArgumentException("No habit at position " + oneBased + ".");
        return habits.get(oneBased - 1);
    }


    public boolean nameExists(String name) {
        return habits.stream().anyMatch(h -> h.getName().equalsIgnoreCase(name.trim()));
    }


    public List<String> getCategories() {
        return habits.stream()
                .map(Habit::getCategory)
                .filter(c -> !c.isBlank())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }


    public List<Habit> filterByCategory(String category) {
        return habits.stream()
                .filter(h -> h.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }


    public List<Habit> sortedByStreak() {
        return habits.stream()
                .sorted(Comparator.comparingInt(Habit::getCurrentStreak).reversed())
                .collect(Collectors.toList());
    }

    public Habit addHabit(String name, String description, String category, int goalDays) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Habit name cannot be empty.");
        if (nameExists(name))
            throw new IllegalArgumentException(
                    "A habit named \"" + name.trim() + "\" already exists.");

        Habit habit = new Habit(name, description, category, goalDays);
        habits.add(habit);
        save();
        return habit;
    }

    public Habit editHabit(int oneBased,
                           String newName, String newDescription,
                           String newCategory, int newGoalDays) {
        Habit habit = getByIndex(oneBased);

        if (newName != null && !newName.isBlank()) {
            // reject rename to existing name (unless it's the same habit)
            if (!habit.getName().equalsIgnoreCase(newName) && nameExists(newName))
                throw new IllegalArgumentException(
                        "Another habit is already named \"" + newName.trim() + "\".");
            habit.setName(newName);
        }
        if (newDescription != null) habit.setDescription(newDescription);
        if (newCategory    != null && !newCategory.isBlank()) habit.setCategory(newCategory);
        if (newGoalDays    >= 0)    habit.setGoalDays(newGoalDays);

        save();
        return habit;
    }

    public Habit markCompleted(int oneBased, LocalDate date) {
        Habit habit = getByIndex(oneBased);
        if (!habit.markCompleted(date))
            throw new IllegalArgumentException(
                    "\"" + habit.getName() + "\" is already marked on " + date + ".");
        save();
        return habit;
    }
    public String deleteHabit(int oneBased) {
        Habit habit = getByIndex(oneBased);
        String name = habit.getName();
        habits.remove(habit);
        save();
        return name;
    }



    private void save() {
        repository.saveAll(habits);
    }
}