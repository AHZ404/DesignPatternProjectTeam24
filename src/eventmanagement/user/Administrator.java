package eventmanagement.user;

import java.util.Date;
import eventmanagement.model.*;

public class Administrator implements User {
    private static int nextEventId = 1;

    private final int adminId;
    private final String name;
    private final String email;

    public Administrator(int adminId, String name, String email) {
        this.adminId = adminId;
        this.name = name;
        this.email = email;
    }

    @Override public String getName() { return name; }
    @Override public String getEmail() { return email; }
    public int getAdminId() { return adminId; }

    // creates a new event with default values (edit it with the Event setters)
    public Event createEvent() {
        return new Event(nextEventId++, "New Event", new Date(), "TBD", 100);
    }

    public void updateEvent(Event event) {
        event.updateDetails();
    }

    public void cancelEvent(Event event) {
        event.cancel();
    }
}
