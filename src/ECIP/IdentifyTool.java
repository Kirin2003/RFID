package ECIP;

public abstract class IdentifyTool {
    public String output = "";
    public String analysis = "";
    public double  time;
    public abstract double identifyAll();

   public abstract  double time() ;
}
