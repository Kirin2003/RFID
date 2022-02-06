package ECIP;

public class Time {
    // 计算CIP的时间
    public static double time1(int f, int n) {
        double time = 0;
        time = (-0.8)*f+(0.4+f*1.2)*Math.exp(n*1.0/f);
        return time;
    }

    // 计算ECIP的时间
    public static double time2(int f, int n) {
        double time = 0;
        time = (0.4 + f*Math.exp(-n*1.0/f)*(1.6+2.8/96)-f*1.2)+(2.4+2.8/96*Math.exp(-n*1.0)*n/f);

        return time;
    }

    // 计算ECIPwithCLS的时间
    public static double time3() {
        double time = 0;

        return time;
    }
}
