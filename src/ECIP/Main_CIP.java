package ECIP;

import java.util.*;

public class Main_CIP {

    public static void main(String[] args) {
        int tagIDLength = 96;//标签id长度
        int categoryIDLength = 32;//类别id长度
        boolean missing = false;
        double missingRate=0;//缺失率
        int tagsPerCid = 100;//平均有多少个标签是同一个类别id
        int tagNum = 10000;//系统中的标签数目
        int unReadCids = (int)Math.ceil(tagNum*1.0/tagsPerCid);//系统中没有识别到的cid
        boolean isRandom = true;//是否随机分配cid
        int f1=160;//random identification phase的时隙数
        int f2=98;// rearranged identification phase的时隙数
        int time = 100;//识别时间上限，单位：s
        Vector<String> tagids = null;
        Vector<String> cids = null;
        List<Tag> tagList=null;
        List<Tag> actualList = null;
        if (isRandom) {
            tagList = TagListGenerator.tagListFactory2(tagIDLength, categoryIDLength, tagNum,tagsPerCid);
        } else {
            tagList = TagListGenerator.tagListFactory3(tagIDLength,categoryIDLength,tagNum,tagids, cids);
        }

        if (missing) {
            actualList=TagListGenerator.highMissingListFactory(tagList,missingRate);
        }
        //test(tagNum, tagsPerCid,f1, f2,tagList);

        //test(tagNum,tagsPerCid,f1,f2,tagList,actualList);

        testCIP(tagNum,tagsPerCid,f1,f2,tagList);
    }



    // 无缺失率
    public static double testCIP(int tagNum, int tagsPerCid, int f1, int f2, List<Tag> tagList) {

        int unReadCids = (int)Math.ceil(tagNum*1.0/tagsPerCid);;//系统中需要识别的cid数
        CIP e = new CIP(tagList,unReadCids,f1);
        double time = e.identifyAll();

        return time;

    }


    // 无缺失率
    public static double test(int tagNum, int tagsPerCid, int f1, int f2, List<Tag> tagList) {

            int unReadCidNum = tagNum/tagsPerCid;//系统中需要识别的cid数
            ECIPwithCLS e = new ECIPwithCLS(tagList,unReadCidNum,f1,f2);
            double time = e.ecipNoRemove();

            return time;

    }

    // 有缺失
    public static double test2(int tagNum, int tagsPerCid, int f1, int f2, List<Tag> virtualList, List<Tag> actualList) {

        int unReadCidNum = tagNum/tagsPerCid;//系统中需要识别的cid数
        ECIPwithCLS e = new ECIPwithCLS(virtualList,actualList,unReadCidNum,f1,f2);
        double time = 0;
        //e.ecipWithCLS();

        return time;

    }
}