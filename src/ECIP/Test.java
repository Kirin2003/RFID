package ECIP;

import java.sql.SQLOutput;
import java.util.regex.Pattern;

public class Test {
    public static void main(String[] args) {
        String input2 = "002";
        System.out.println(Pattern.matches("^[0-9]*[1-9][0-9]*$", input2));

        String input = "110X";

        System.out.println(Pattern.matches("^(0|1)*$",input));

    }

}
