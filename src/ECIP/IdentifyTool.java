package ECIP;
import java.util.*;

public abstract class IdentifyTool {
    public String output = "";
    public String analysis = "";
    public double  time;


    public Set<String> presentCids = new HashSet<>();
    public Set<String> missingCids = new HashSet<>();
    public int virtualCidNum = 0;
    public int actualCidNum = 0;

    public int f;

    public double te = 0.4;
    public double tcid;
    public double tid;

    public IdentifyTool() {

    }
    public IdentifyTool(int tidLength, int cidLength) {
        tcid = cidLength * 0.025 + 0.4;
        tid = tidLength * 0.025 + 0.4;
    }

    public abstract double identifyAll();

   public abstract  double time() ;

   //public abstract void analysis();
}
