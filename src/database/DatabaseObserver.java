package database;

/** Observer pattern: UI screens implement this and register with JsonRepository (the subject). */
public interface DatabaseObserver {
    /** Called after every change saved to the database, so the screen can refresh itself. */
    void databaseChanged();
}
