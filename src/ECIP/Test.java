package ECIP;
import java.util.*;
public class Test {
    public static void main(String[] args) {
        String s = "1010X";
        int xindex = s.indexOf('X');
        System.out.println("xindex = "+xindex);
        String sub = s.substring(0,xindex);
        System.out.println("sub = "+sub);
    }
}
