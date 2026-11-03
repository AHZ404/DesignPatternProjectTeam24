package eventmanagement.model;

import java.util.Date;
import eventmanagement.user.*;

public class EventRegistration {
    private final int registrationId;
    private final Date registrationDate;
    private String status;
    private final Attendee attendee;
    private final Event event;
    private EventTicket ticket;                    // 1 -- 0..1 EventTicket
    private AttendanceRecord attendanceRecord;     // 1 -- 0..1 AttendanceRecord

    public EventRegistration(int registrationId, Date registrationDate, Attendee attendee, Event event) {
        this.registrationId = registrationId;
        this.registrationDate = registrationDate;
        this.attendee = attendee;
        this.event = event;
        this.status = "PENDING";
    }

    public void confirm() {
        if (event.hasCapacity() && !event.getStatus().equals("CANCELLED")) {
            status = "CONFIRMED";
        } else {
            System.out.println("Cannot confirm: event is full or cancelled.");
        }
    }

    public void cancel() {
        status = "CANCELLED";
    }

    public EventTicket generateTicket() {
        if (!status.equals("CONFIRMED")) {
            throw new IllegalStateException("Registration must be confirmed first.");
        }
        if (ticket == null) {
            ticket = new EventTicket(registrationId, "TCK-" + event.getEventId() + "-" + registrationId);
        }
        return ticket;
    }

    public void setAttendanceRecord(AttendanceRecord record) { this.attendanceRecord = record; }

    public int getRegistrationId() { return registrationId; }
    public Date getRegistrationDate() { return registrationDate; }
    public String getStatus() { return status; }
    public Attendee getAttendee() { return attendee; }
    public Event getEvent() { return event; }
    public EventTicket getTicket() { return ticket; }
    public AttendanceRecord getAttendanceRecord() { return attendanceRecord; }
}
