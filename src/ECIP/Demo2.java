package ECIP;

import java.util.ArrayList;
import java.util.List;

public class Demo2 {

    public static void main(String[] args) {
        List<Tag> tagList = new ArrayList<>();
        Tag tag1 = new Tag("01101", 0);
        Tag tag2 = new Tag("01101", 0);
        Tag tag3 = new Tag("01100", 2);
        Tag tag4 = new Tag("01100", 2);
        Tag tag5 = new Tag("01001", 2);
        Tag tag6 = new Tag("01001", 2);
        Tag tag7 = new Tag("11101", 4);
        Tag tag8 = new Tag("11101", 4);
        Tag tag9 = new Tag("11111", 4);
        Tag tag10 = new Tag("11011", 4);
        Tag tag11 = new Tag("10100", 4);
        Tag tag12 = new Tag("10100", 4);
        tagList.add(tag1);
        tagList.add(tag2);
        tagList.add(tag3);
        tagList.add(tag4);
        tagList.add(tag5);
        tagList.add(tag6);
        tagList.add(tag7);
        tagList.add(tag8);
        tagList.add(tag9);
        tagList.add(tag10);
        tagList.add(tag11);
        tagList.add(tag12);

        ECIPDemo(tagList);



    }

    public static void test() {
//        test constructIndicator() and constructLocationAndStructureD()
//        Map<Integer, String> CidMap1 = new HashMap<>();
//        CidMap1.put(0000,"XX000");
//        CidMap1.put(0001,"0XX00");
//        CidMap1.put(0002,"0X000");
//        CidMap1.put(0003,"1XXXX");
//        CidMap1.put(0005,"11111");
//
//
//
//        ECIP ecip1 = new ECIP();
//        ecip1.setCidMap(CidMap1);
//        ecip1.constructIndicator();
//        ecip1.constructLocationAndStructureD();
//
//
//        ecip1.printIndicator();
//        ecip1.printLocation();
//        ecip1.printStructureD();
//
//        Map<Integer, String> CidMap2 = new HashMap<>();
//        CidMap2.put(0000,"111XX000");
//        CidMap2.put(0001,"1110XX00");
//        CidMap2.put(0002,"1010X000");
//        CidMap2.put(0003,"111XXX0X");
//        CidMap2.put(0005,"11110011");
//
//        ECIP ecip2 = new ECIP();
//        ecip2.setCidMap(CidMap2);
//        ecip2.constructIndicator();
//        ecip2.constructLocationAndStructureD();
//
//
//        ecip2.printIndicator();
//        ecip2.printLocation();
//        ecip2.printStructureD();
//
//        ArrayList<Tag> tagList = new ArrayList<>();
//        //tagList.add(new Tag("10000",0));
//        //tagList.add(new Tag("11000",0));
//        tagList.add(new Tag("01000", 1));
//        tagList.add(new Tag("00100", 1));
//        tagList.add(new Tag("10001", 2));
//        tagList.add(new Tag("01001", 2));
//        tagList.add(new Tag("00101", 2));
//
//        ECIP ecip3 = new ECIP(tagList);
//
//        Map<Integer, String> CidMap = new HashMap<>();
//        //CidMap.put(0,"1X000");
//        CidMap.put(1, "0XX00");
//        CidMap.put(2, "XXX01");
//        ecip3.setCidMap(CidMap);
//        ecip3.constructLocationAndStructureD();
//        CidMap.clear();
//        for (Tag tag : tagList) {
//            ecip3.selectSlotBasedOnXIndex();
//            System.out.println("cid = " + tag.getCategoryID());
//            System.out.println(" slot = " + tag.getSlotSelected());
//        }
//
//        System.out.println(ecip3.combineCID(1, "000"));
//        System.out.println(ecip3.combineCID(0, "100"));
//        System.out.println(ecip3.combineCID(3, "0000"));
    }


    public static void ECIPDemo(List<Tag> tagList) {
        ECIP ecip = new ECIP(tagList, 6);
        System.out.println("random identification phase");

        for (Tag tag : tagList) {
            ecip.constructCidMapAndSlotToTagList(tag, tag.getCategoryID(), ecip.slotToTagList);
        }
        ecip.printCidMap();
        ecip.recognizeCid(false);

        int count = 0;
        while (ecip.unReadCidNum > 0) {
            System.out.println("rearranged identification phase");
            ecip.rearrangedIdentificationPhase();
            count++;

            for (Tag tag : ecip.tagList) {
                System.out.println("cid:" + tag.getCategoryID() + " slot:" + tag.getSlotSelected());
            }
            System.out.println(" ");
        }


        System.out.println("CidMap:");
        for (String cid : ecip.categoryIDs) {
            System.out.println(cid);
        }
    }

    public static void ECIPwithCLSDemo(List<Tag> tagList, List<Tag> virtuallist) {
        ECIPwithCLS e = new ECIPwithCLS(tagList,6);
        System.out.println("random identification phase");
        for (Tag tag :tagList) {
            //e.constructCidMapAndSlotToTagList(tag, tag.getCategoryID(), e.slotToTagList);
        }
        e.printCidMap();
    }
}