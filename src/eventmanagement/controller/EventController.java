package eventmanagement.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import eventmanagement.model.*;
import eventmanagement.observer.*;
import eventmanagement.user.*;
import eventmanagement.view.*;

public class EventController {
    private static int nextEventId = 1000;

    private final EventView view = new EventView();
    private final Map<Event, EventObservable> observables = new HashMap<>();
    private final List<Event> events = new ArrayList<>();
    private int nextAttendanceId = 1;

    public void createEvent() {
        Event event = new Event(nextEventId++, "New Event", new java.util.Date(), "TBD", 100);
        control(event);
        view.showEvent(event);
    }

    public void updateEvent(Event event) {
        event.updateDetails();
        control(event).notifyObservers();
    }

    public void cancelEvent(Event event) {
        event.cancel();
        control(event).notifyObservers();
    }

    public void registerAttendee(Attendee a, Event e) {
        view.showRegistrationForm();
        EventRegistration r = a.registerForEvent(e);
        r.confirm();
        if (r.getStatus().equals("CONFIRMED")) {
            r.generateTicket();
        }
        view.showEvent(e);
    }

    public void checkIn(EventRegistration r) {
        if (r.getTicket() != null && r.getTicket().validate()) {
            AttendanceRecord record = new AttendanceRecord(nextAttendanceId++);
            record.markPresent();
            r.setAttendanceRecord(record);
        }
    }

    public List<Event> getEvents() { return events; }

    // sets up the observer wiring for an event the first time the controller sees it
    private EventObservable control(Event event) {
        EventObservable observable = observables.get(event);
        if (observable == null) {
            observable = new EventObservable(event);
            observable.registerObserver(view);
            new NotificationObserver(observable);
            observables.put(event, observable);
            events.add(event);
        }
        return observable;
    }
}
