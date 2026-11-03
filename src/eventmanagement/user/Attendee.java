package eventmanagement.user;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import eventmanagement.model.*;

public class Attendee implements User {
    private static int nextRegistrationId = 1;

    private final int attendeeId;
    private final String name;
    private final String email;
    private final List<EventRegistration> registrations = new ArrayList<>();

    public Attendee(int attendeeId, String name, String email) {
        this.attendeeId = attendeeId;
        this.name = name;
        this.email = email;
    }

    @Override public String getName() { return name; }
    @Override public String getEmail() { return email; }
    public int getAttendeeId() { return attendeeId; }

    public EventRegistration registerForEvent(Event event) {
        EventRegistration r = new EventRegistration(nextRegistrationId++, new Date(), this, event);
        event.addRegistration(r);
        registrations.add(r);
        return r;
    }

    // cancels this attendee's most recent registration
    public void cancelRegistration() {
        if (!registrations.isEmpty()) {
            registrations.get(registrations.size() - 1).cancel();
        }
    }
}
