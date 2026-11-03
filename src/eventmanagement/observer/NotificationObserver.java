package eventmanagement.observer;

import eventmanagement.model.*;

public class NotificationObserver implements EventObserver {
    private String eventTitle;
    private String eventStatus;
    private EventObservable subject;

    // the constructor receives the subject and registers this observer with it
    public NotificationObserver(EventObservable subject) {
        this.subject = subject;
        subject.registerObserver(this);
    }

    // when update() is called, save the new state and call display()
    @Override
    public void update(Event event) {
        this.eventTitle = event.getTitle();
        this.eventStatus = event.getStatus();
        display();
    }

    public void display() {
        System.out.println("Notification: event \"" + eventTitle + "\" is now " + eventStatus);
    }
}
