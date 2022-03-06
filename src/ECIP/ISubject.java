package ECIP;
import java.util.*;

public interface ISubject {
     boolean add(IObserver iObserver);
     boolean remove(IObserver iObserver);
     void notifyAllObjects();
     void setMissingCids(Set<String> missingCids);
     String report();


}
