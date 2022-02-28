package ECIP;

import java.util.*;
public class TagListGenerator {
    //useless
    /**
     *
     * @param tagList
     * @param missingRate
     * @return
     */
    public static List<Tag> highMissingListFactory(List<Tag> tagList, double missingRate) {
        int size = (int)Math.ceil(tagList.size()*(1-missingRate)); // 向上取整

        List<Tag> actualList = new ArrayList<>();
        long time = System.currentTimeMillis();
        Random random = new Random(time);
        int i = 0;
        Set<Tag> tagSet = new HashSet<>();
        Set<String> cidSet = new HashSet<>();
        while(tagSet.size() < size) {
            i = random.nextInt(tagList.size());
            Tag tag = tagList.get(i);
            String cid = tag.getCategoryID();
            if (!cidSet.contains(cid)) {
                tagSet.add(tag);
                cidSet.add(cid);
            }

        }
        for (Tag tag : tagSet) {
            actualList.add(tag);
        }
        return actualList;
    }

    /**
     *
     * @param tagList
     * @param missingRate cid的缺失率，而不是标签的缺失率
     * @return
     */
    public static List<Tag> highMissingListFactory2(List<Tag> tagList, int virtualCidNum, double missingRate) {
        int size = (int)Math.ceil(virtualCidNum*(1-missingRate)); // 向上取整


        List<Tag> actualList = new ArrayList<>();
        long time = System.currentTimeMillis();
        Random random = new Random(time);
        int i = 0;
        Set<Tag> tagSet = new HashSet<>();
        Set<String> cidSet = new HashSet<>();
        while(tagSet.size() < size) {
            i = random.nextInt(tagList.size());
            Tag tag = tagList.get(i);
            String cid = tag.getCategoryID();
            if (!cidSet.contains(cid)) {
                tagSet.add(tag);
                cidSet.add(cid);
            }

        }
        for (Tag tag : tagSet) {
            actualList.add(tag);
        }
        return actualList;
    }



    // 指定cid时用，现在不用
    public static List<Tag> tagListFactory3(int tagIDLength, int categoryIDLength, int tagnum, Vector<String> tagids, Vector<String> cids) {
        List<Tag> tagList = null;
        String tagid, cid;
        for (int i = 0; i < tagnum; i++) {
            tagid = tagids.get(i);
            cid = cids.get(i);
            tagList.add(new Tag(tagid, cid));
        }
        return tagList;
    }

    /**
     * Compared with tagListFactory, tagListFactory2 controls thr number of tags in each category equal to density
     * @param tagIDLength
     * @param categoryIDLength
     * @param tagsPerCid :  the number of tags in each category
     * @return
     */
    public static List<Tag> tagListFactory2(int tagIDLength, int categoryIDLength, int all, int tagsPerCid) {
        ArrayList<Tag> tagList = new ArrayList<>();
        for (int i = 0; i < all; i++) {
            tagList.add(new Tag());
        }
        allocateRandom2(tagList, tagIDLength, categoryIDLength, tagsPerCid);
        return tagList;
    }


    /**
     * Compared with allocateRandom, allocateRandom2 controls thr number of tags in each category equal to density
     * @param tagList
     * @param tagIDLength
     * @param categoryIDLength
     * @param density : the number of tags in each category
     */
    private static void allocateRandom2(List<Tag> tagList, int tagIDLength, int categoryIDLength, int density) {
        int num = 0;
        String cid = getRandomID(categoryIDLength);
        for (Tag tag : tagList) {
            tag.setTagID(getRandomID(tagIDLength));
            num ++;
            tag.setCategoryID(cid);
            if(num%density==0) cid=getRandomID(categoryIDLength);
        }
    }
    /**
     * @param IDLength 字符串长度
     * @return  返回长度为tagIDLength的数字串，可作为tagID
     */
    private static String getRandomID(int IDLength){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < IDLength; i++)
            sb.append(Math.random() > 0.5 ? '1' : '0');
        return sb.toString();
    }

    // 测试函数
    public static void main(String[] args) {
        List<Tag> tagList = TagListGenerator.tagListFactory2(10, 5, 10, 4);
        System.out.println("tag list");
        for(Tag tag : tagList) {
            System.out.println("tag id:"+tag.getTagID()+" cid:"+tag.getCategoryID());
        }
//        List<Tag> actualList = TagListGenerator.highMissingListFactory2(tagList,10, 0.7);
//        System.out.println("actual List");
//        for(Tag tag : actualList) {
//            System.out.println("tag id:"+tag.getTagID()+" cid:"+tag.getCategoryID());
//        }
    }

}

