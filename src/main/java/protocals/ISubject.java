package protocals;

public interface ISubject {
     boolean add(IObserver iObserver);
     boolean remove(IObserver iObserver);
     void notifyAllObservers(String message);
     //void changeMissingCids(Set<String> missingCids);
     //String report();


}
