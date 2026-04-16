package org.example;

import org.example.repository.HabitRepository;
import org.example.service.HabitService;
import org.example.ui.ConsoleUI;


public class Main {

    public static void main(String[] args) {
        HabitRepository repository = new HabitRepository();
        HabitService    service    = new HabitService(repository);
        ConsoleUI       ui         = new ConsoleUI(service);
        ui.start();
    }
}