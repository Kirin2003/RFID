package ECIP;

import java.util.*;
public class TagListGenerator {
    /**
     * generate tag list randomly
     * @param tagIDLength the length of TID
     * @param categoryIDLength the length of CID
     * @param tagnum the num of tags
     * @return
     */
    public static List<Tag> tagListFactory1(int tagIDLength, int categoryIDLength, int tagnum){
        ArrayList<Tag> tagList = new ArrayList<>();
        for (int i = 0; i < tagnum; i++) {
            tagList.add(new Tag());
        }
        allocateRandom(tagList, tagIDLength, categoryIDLength);
        return tagList;
    }

    public static List<Tag> highMissingListFactory(List<Tag> tagList, double missingRate) {
        int size = (int)(tagList.size()*(1-missingRate));

        List<Tag> actualList = new ArrayList<>();
        long time = System.currentTimeMillis();
        Random random = new Random(time);
        int i = 0;
        Set<Tag> set = new HashSet<>();
        while(true) {
            i = random.nextInt(tagList.size());
            set.add(tagList.get(i));
            if (set.size() >= size) {
                break;
            }
        }
        for (Tag tag : set) {
            actualList.add(tag);
        }
        return actualList;
    }

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

//    public static List<Tag> tagListGenerator(boolean isRandom, int tagIDLength, int categoryIDLength, int tagNum, int tagsPerCid, Vector<String> tagids, Vector<String> cids) {
//        if (isRandom) {
//            tagList = TagListGenerator.tagListFactory2(tagIDLength, categoryIDLength, tagNum,tagsPerCid);
//        } else {
//            tagList = TagListGenerator.tagListFactory3(tagIDLength,categoryIDLength,tagNum,tagids, cids);
//        }
//
//        if (missing) {
//            actualList=TagListGenerator.highMissingListFactory(tagList,missingRate);
//        }
//    }

    private static void allocateRandom(List<Tag> tagList, int tagIDLength, int categoryIDLength){
        for (Tag tag : tagList) {
            tag.setTagID(getRandomID(tagIDLength));
            tag.setCategoryID(getRandomID(categoryIDLength));
            //System.out.println("categoryID = "+tag.getCategoryID());
        }
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

}

