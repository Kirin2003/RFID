package ECIP;

import java.awt.im.InputSubset;

public interface IObserver {
    public void update(ISubject subject, String message);
}
