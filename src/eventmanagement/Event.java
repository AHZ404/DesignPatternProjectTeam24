package eventmanagement;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Event {
    private final int eventId;
    private String title;
    private Date date;
    private String location;
    private int capacity;
    private String status;
    private final List<EventRegistration> registrations = new ArrayList<>();   // Event 1 -- 0..* EventRegistration

    public Event(int eventId, String title, Date date, String location, int capacity) {
        this.eventId = eventId;
        this.title = title;
        this.date = date;
        this.location = location;
        this.capacity = capacity;
        this.status = "ACTIVE";
    }

    public void updateDetails() {
        this.status = "UPDATED";
    }

    public void cancel() {
        this.status = "CANCELLED";
    }

    public boolean hasCapacity() {
        int confirmed = 0;
        for (EventRegistration r : registrations) {
            if (r.getStatus().equals("CONFIRMED")) {
                confirmed++;
            }
        }
        return confirmed < capacity;
    }

    void addRegistration(EventRegistration r) { registrations.add(r); }
    public List<EventRegistration> getRegistrations() { return registrations; }

    public int getEventId() { return eventId; }
    public String getTitle() { return title; }
    public Date getDate() { return date; }
    public String getLocation() { return location; }
    public int getCapacity() { return capacity; }
    public String getStatus() { return status; }

    public void setTitle(String title) { this.title = title; }
    public void setDate(Date date) { this.date = date; }
    public void setLocation(String location) { this.location = location; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
}
