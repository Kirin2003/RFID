package ECIP;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import sun.rmi.runtime.Log;

import java.util.*;

public abstract class IdentifyTool implements ISubject {
    private static Logger logger = Logger.getLogger("IdentifyTool");

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

    protected int warningNum;
    protected String warningCid;
    protected boolean isWarning = true; // 是否需要弹出警告框，只警告一次

    protected Vector<IObserver> iObservers = new Vector<>();


    public IdentifyTool(int tidLength, int cidLength, int warningNum, String warningCid) {
        logger.setLevel(Level.DEBUG);
        tcid = cidLength * 0.025 + 0.4;
        tid = tidLength * 0.025 + 0.4;
        this.warningNum = warningNum;
        this.warningCid = warningCid;
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
    public void notifyAllObservers(String warningMessage) {
        logger.debug("notify all observers()");
        for(IObserver iObserver : iObservers) {
            iObserver.update(this, warningMessage);
        }
    }


    public void changeMissingCids( String missingCid) {
        missingCids.add(missingCid);
        invoke();
    }


    protected void invoke() {
        logger.debug("invoke()");
        if(!isWarning) {
            logger.debug("return");
            return;
        }

        int missingCidNum = missingCids.size();
        logger.debug("missing cid num:"+missingCidNum);
        String warningMessage = "";
        if(missingCidNum >= warningNum && missingCidNum <= warningNum +5) {
            warningMessage+="预警！缺失数量超过"+warningNum+"\n";
            warningMessage+="识别时间："+time+"\n";
        } else if (missingCids.contains(warningCid) ) {
            warningMessage += "预警！类别："+warningCid+"缺失\n";
            warningMessage += "识别时间："+time+"\n";
        }

        if(warningMessage.length()>0) {
            notifyAllObservers(warningMessage);
            logger.debug("弹出警告框");
            isWarning = false;
        }

    }
}
