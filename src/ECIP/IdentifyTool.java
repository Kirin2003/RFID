package ECIP;
import java.util.*;

public abstract class IdentifyTool {
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
}
