package eventmanagement.observer;

public interface Subject {
    void registerObserver(EventObserver o);
    void removeObserver(EventObserver o);
    void notifyObservers();
}
