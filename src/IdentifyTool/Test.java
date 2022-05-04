package IdentifyTool;
import java.util.*;

public class Test {

    public static void main(String[] args) {
        int tagLength = 96;
        int cidLength = 32;
        int tagNum = 100000;
        int tagNumPerCid = 100;
        int cidNum = tagNum / tagNumPerCid;
        double missingRate = 0.25;
        int actualCidNum = (int)Math.ceil(cidNum*(1-missingRate)); // 向上取整

        int testNum = 1;
        List<Double> t = new ArrayList<>();
        double avg = 0;

        for (int i = 0; i < testNum; i++) {
            List<Tag> tagList = TagListGenerator.tagListFactory2(tagLength, cidLength, tagNum, tagNumPerCid);
            List<Tag> actualList = TagListGenerator.highMissingListFactory2(tagList,cidNum,missingRate);
            IdentifyTool iden = new ECLS(tagList,actualList,cidNum,actualCidNum,tagLength,cidLength);
            iden.identifyAll();
            double time = iden.getTime();
            t.add(time);
            avg+=time;
        }

        System.out.println(t);
        System.out.println("avg="+avg /testNum);

    }


}
