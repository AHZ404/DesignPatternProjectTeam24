package eventmanagement;

public interface Subject {
    void registerObserver(EventObserver o);
    void removeObserver(EventObserver o);
    void notifyObservers();
}
