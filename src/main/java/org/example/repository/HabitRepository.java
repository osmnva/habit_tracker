package org.example.repository;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.example.model.Habit;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


public class HabitRepository {

    private static final String DEFAULT_FILE = "habits.json";
    private final Path filePath;
    private final Gson gson;

    public HabitRepository() {
        this(DEFAULT_FILE);
    }


    HabitRepository(String fileName) {
        this.filePath = Paths.get(fileName);
        this.gson     = buildGson();
    }


    public List<Habit> loadAll() {
        if (!Files.exists(filePath)) return new ArrayList<>();
        try {
            String json = Files.readString(filePath);
            if (json.isBlank()) return new ArrayList<>();
            Type listType = new TypeToken<List<Habit>>() {}.getType();
            List<Habit> habits = gson.fromJson(json, listType);
            return habits != null ? habits : new ArrayList<>();
        } catch (IOException e) {
            System.err.println("[Repository] Cannot read " + filePath + ": " + e.getMessage());
            return new ArrayList<>();
        } catch (JsonSyntaxException e) {
            System.err.println("[Repository] Corrupted JSON in " + filePath + " — starting fresh.");
            return new ArrayList<>();
        }
    }

    public void saveAll(List<Habit> habits) {
        try {
            Files.writeString(filePath, gson.toJson(habits));
        } catch (IOException e) {
            System.err.println("[Repository] Cannot write " + filePath + ": " + e.getMessage());
        }
    }



    private Gson buildGson() {

        JsonSerializer<LocalDate>   ser   = (src, t, ctx) -> new JsonPrimitive(src.toString());
        JsonDeserializer<LocalDate> deser = (json, t, ctx) -> LocalDate.parse(json.getAsString());

        return new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, ser)
                .registerTypeAdapter(LocalDate.class, deser)
                .setPrettyPrinting()
                .create();
    }
}