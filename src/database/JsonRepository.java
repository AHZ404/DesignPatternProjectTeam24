package database;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/** Singleton: the one shared JSON database. Also the Observer subject that notifies UI screens of changes. */
public final class JsonRepository {
    private static JsonRepository instance;
    private final List<DatabaseObserver> observers = new ArrayList<>();
    private final Path filePath;
    private final ObjectMapper mapper;
    private final Database database;

    public static synchronized JsonRepository getInstance() {
        if (instance == null) instance = new JsonRepository(Paths.get("data", "database.json"));
        return instance;
    }

    private JsonRepository(Path filePath) {
        this.filePath = Objects.requireNonNull(filePath).toAbsolutePath();
        this.mapper = new ObjectMapper();
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.mapper.registerModule(new JavaTimeJsonModule());

        // Polymorphic typing so polymorphic interface fields like IPaymentStrategy persist correctly
        this.mapper.activateDefaultTyping(
                this.mapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL
        );

        this.database = load();
    }

    private Database load() {
        File file = filePath.toFile();

        // If file does not exist or is empty, create a fresh Database and save it
        if (!file.exists() || file.length() == 0) {
            System.out.println("No existing database found. Creating new JSON database at: " + filePath);
            Database newDb = new Database();
            save(newDb);
            return newDb;
        }

        // Persistent Load: Read existing JSON data from file into memory
        try {
            Database loadedDb = mapper.readValue(file, Database.class);
            System.out.println("Successfully loaded persistent database from: " + filePath);
            return loadedDb;
        } catch (Exception e) {
            System.err.println("Error loading JSON database (" + e.getMessage() + "). Preserving file and initializing in-memory fallback.");
            e.printStackTrace();
            return new Database();
        }
    }

    /** Synchronized disk write on every update */
    private synchronized void save(Database db) {
        try {
            if (filePath.getParent() != null) {
                Files.createDirectories(filePath.getParent());
            }
            mapper.writeValue(filePath.toFile(), db);
        } catch (IOException e) {
            throw new RuntimeException("Failed to persist JSON database to " + filePath, e);
        }
    }

    /** Thread-safe read */
    public synchronized <T> T read(Function<Database, T> operation) {
        return operation.apply(database);
    }

    /** Thread-safe update + Instant file persistence, then observers are notified */
    public <T> T update(Function<Database, T> operation) {
        T result;
        synchronized (this) {
            result = operation.apply(database);
            save(database); // Immediately flushes changes to data/database.json
        }
        notifyObservers();
        return result;
    }

    // --- Observer pattern (subject side) ---
    public void registerObserver(DatabaseObserver o) { observers.add(o); }

    public void removeObserver(DatabaseObserver o) { observers.remove(o); }

    public void notifyObservers() {
        for (DatabaseObserver o : new ArrayList<>(observers)) o.databaseChanged();
    }
}