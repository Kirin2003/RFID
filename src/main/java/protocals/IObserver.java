package protocals;

public interface IObserver {
    public void update(ISubject subject, String message);
}
