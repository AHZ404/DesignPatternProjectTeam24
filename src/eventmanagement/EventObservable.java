package eventmanagement;

import java.util.ArrayList;
import java.util.List;

public class EventObservable implements Subject {
    private final List<EventObserver> observers = new ArrayList<>();
    private final Event event;

    public EventObservable(Event event) {
        this.event = event;
    }

    @Override
    public void registerObserver(EventObserver o) {
        observers.add(o);
    }

    @Override
    public void removeObserver(EventObserver o) {
        observers.remove(o);
    }

    @Override
    public void notifyObservers() {
        for (EventObserver o : observers) {
            o.update(event);
        }
    }
}
