package ECIP;

public class OptimizeECLS {
    /**
     *
     * @param p missing rate
     * @param a n/f
     * @return 识别一个类别所需最小时间
     */
    public static double tavg(double p, double a) {
        double x = Math.exp(-a);
        if(a == 0) throw new IllegalArgumentException("时隙不为零，输入不合法");
        return ( (1+x+a*x)*0.03+(1-x)*0.4 )/(a*p*Math.exp(-a*(1-p) + (1-p)*a*x));
    }

    /**
     *
     * @param p missing rate
     * @return 最小的识别时间对应的时隙（最优时隙）
     */
    public static double optimize(double p) {
        double minf = 0.01;
        double mint = tavg(p,minf);
        for(double f = 0.5; f < 50; f += 0.01) {
            double t = tavg(p,f);
            if(t < mint) {
                mint = t;
                minf = f;
            }
        }
        return minf;
    }

    public static void main(String[] args) {
        System.out.println(OptimizeECLS.optimize(0.75));
    }
}
