package ECIP;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import java.util.*;

public abstract class IdentifyTool implements ISubject {
    protected String output = "";
    protected String analysis = "";
    protected double  time = 0;


    protected Set<String> presentCids = new HashSet<>();
    protected Set<String> missingCids = new HashSet<>();
    protected int virtualCidNum = 0;
    protected int actualCidNum = 0;

    protected int f;

    protected double te = 0.4;
    protected double tcid;
    protected double tid;

    protected Vector<IObserver> iObservers = new Vector<>();


    public IdentifyTool(int tidLength, int cidLength) {
        tcid = cidLength * 0.025 + 0.4;
        tid = tidLength * 0.025 + 0.4;
    }

    public abstract double identifyAll();

    public double getTime() {
        return time;
    }

    public String getOutput() {
        return output;
    }

    public String getAnalysis() {
        return analysis;
    }

    @Override
    public boolean add(IObserver iObserver) {
        if (iObserver != null && !iObservers.contains(iObserver)) {

            return iObservers.add(iObserver);
        }
        return false;
    }

    @Override
    public boolean remove(IObserver iObserver) {
        return iObservers.remove(iObserver);
    }

    @Override
    public void notifyAllObjects() {
        for(IObserver iObserver : iObservers) {
            iObserver.update(this);
        }
    }

    @Override
    public void setMissingCids(Set<String> missingCids) {
        this.missingCids = missingCids;
    }

    @Override
    public String report() {
        return null;
    }
}
