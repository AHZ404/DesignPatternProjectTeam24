package eventmanagement.view;

import eventmanagement.model.*;
import eventmanagement.observer.*;

public class EventView implements EventObserver {
    private String title;
    private String details;

    @Override
    public void update(Event event) {
        showEvent(event);
    }

    public void showEvent(Event event) {
        title = event.getTitle();
        details = event.getDate() + " | " + event.getLocation() + " | " + event.getStatus();
        System.out.println("[View] " + title + " | " + details);
    }

    public void showRegistrationForm() {
        System.out.println("[View] Registration form: name, email, event");
    }
}
