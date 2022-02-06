package ECIP;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Main_ECIPwithCLS {
    public static void main(String[] args) {
        int tagIDLength = 96;
        int cidLength = 6;
        int tagNum = 12;
        double missing = 0.75;
        List<Tag> virtualList = TagListGenerator.tagListFactory1(tagIDLength,cidLength,tagNum);
        List<Tag> actualList = TagListGenerator.highMissingListFactory(virtualList,missing);

        Logger logger = Logger.getLogger("main");
        logger.setLevel(Level.ALL);
        logger.log(Level.FINE,"virtual list size ="+virtualList.size()+" actual list size = "+actualList.size()+". The missing rate = "+missing);
        logger.log(Level.FINE,"tags in virtual list: ");
        printList(virtualList);
        logger.log(Level.FINE,"tags in actual list: ");
        printList(actualList);
//        ECIPwithCLS test = new ECIPwithCLS(virtualList,actualList);
//        long time1 = System.currentTimeMillis();
//        test.ecipWithCLS();
//        long time2 = System.currentTimeMillis();
//        System.out.println("using cls");
//        System.out.println("time spent = "+(time2-time1));

//        ECIP ecipTest = new ECIP(virtualList);
//        long time3 = System.currentTimeMillis();
//        ecipTest.ecip();
//        long time4 = System.currentTimeMillis();
//        System.out.println("not using cls");
//        System.out.println("time spent = "+(time4-time3));

        }


    public static void printList(List<Tag> tagList) {
        for (Tag tag : tagList) {
            System.out.println(tag.getCategoryID());
        }
    }

}



