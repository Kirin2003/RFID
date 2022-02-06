package ECIP;

import java.util.*;

public class CIP extends IdentifyTool{
    protected List<Tag> tagList;
    protected Set<String> categoryIDs;
    protected Map<Integer, String> CidMap = new HashMap<>(); //store the slotId and the overlapped cid
    protected Map<Integer,List<Tag>> slotToTagList = new HashMap<>();
    protected int f;
    protected int time;
    protected int unReadCidNum;


    public CIP() {}
    public CIP(List<Tag> tagList){
        this.tagList = tagList;
        categoryIDs = new HashSet<>();
    }

    public CIP(List<Tag> tagList, int unReadCidNum, int f) {
        this.tagList = tagList;
        this.unReadCidNum = unReadCidNum;
        this.f = f;
    }

    /**
     * constructCidMapAndSlotToTagList
     * @param tag
     * @param str cid or partial cid
     */
    void constructCidMapAndSlotToTagList(Tag tag, String str, Map<Integer,List<Tag>>slotToTagList) {
        Integer slotSelected = tag.getSlotSelected();


        // the slot is empty
        if (!CidMap.containsKey(slotSelected)){
            CidMap.put(slotSelected, str);
            List<Tag> tagList = new ArrayList<>();
            tagList.add(tag);
            slotToTagList.put(slotSelected, tagList);
        }
        else { // the slot already has tag and CID
            // overlap CID

            String newData = encode(CidMap.get(slotSelected), str);
            CidMap.put(slotSelected, newData);
            slotToTagList.get(slotSelected).add(tag);
        }
    }

    /**
     * overlap category ID
     * @param s1
     * @param s2
     * @return overlapped CID
     */

    private String encode(String s1, String s2){
        //System.out.println("s1 = " + s1+ "s2 =" + s2);
        int i = 0;
        StringBuilder stringBuilder = new StringBuilder();
        while(i < s1.length()){
            if(s1.charAt(i) == s2.charAt(i)){
                stringBuilder.append(s1.charAt(i));
            }else{
                // If the bits at the same position in strings from different tags are not the same, the reader will decode this bit as ‘X’
                stringBuilder.append('X');
            }

            i++;
        }
        return stringBuilder.toString();
    }

//    public static void main(String[] args) {
//        //test encode
//        System.out.println(encode("10000", "10111"));
//    }

    /**
     * selects a slot based on the random hash function H(CID, r) mod f
     * @param random random seed
     * @param frameSize
     */
    protected void selectSlot(int random, int frameSize) {
        CidMap.clear();
        slotToTagList.clear();

        for (Tag tag : tagList) {
            String cid = tag.selectSlotPseudoRandom(frameSize, random);

            //At the same time, it will overlap CID and then construct CidMap
            constructCidMapAndSlotToTagList(tag, cid, slotToTagList);
        }
    }

/**
 * exploit Manchester coding scheme to decode the aggregated signals received in collision slots.
 * @param data:
 */
protected String[] decodeCID(String data){
    // empty slot
    if (data == null){
        return null;
    }
    // category-compatible slot
    int index1 = data.indexOf('X');
    if (index1 == -1){
        // the reader receives the data with no collision bit ‘X’, it identifies a CID
        return new String[]{data};
    }
    int index2 = data.lastIndexOf('X');
    if (index1 == index2){
        // the reader receives the data with only one collision bit ‘X’, it identifies two CIDs. One is the collision bit ‘X’ is set to ‘0’, the other is set to ‘1’.
        String s1 = data.replace('X', '0');
        String s2 = data.replace('X', '1');
        return new String[]{s1, s2};
    }
    // category-collision slot
    return new String[]{};
}


    /**
     * cip process simulation
     */
    public double identifyAll(){
        int round = 0;
        int unReadTagNum = tagList.size();     //the num of unread tag
        Iterator<Tag> iterator = null;  // used to modify tagList
        int num;        // count the num of recognized CID of every round.
        while(unReadCidNum > 0){
            num = 0;
            round++;
            int frameSize = f;
            CidMap.clear();

            /*
            tags in tagList select slot
            */
            int random = (int)(100 * Math.random());

            selectSlot(random, frameSize);

            /*
                recognize cid
             */
            for (Integer slotId : CidMap.keySet()) {
                String[] cid = decodeCID(CidMap.get(slotId));
                // category-compatible slot
                if(cid.length == 2 || cid.length == 1){
                    iterator = tagList.iterator();
                    while (iterator.hasNext()){
                        Tag tag = iterator.next();
                        //deactivate all the tags belong to this category
                        if(tag.getSlotSelected().equals(slotId)){
                            tag.setActive(false);
                            unReadTagNum--;
                            iterator.remove();
                        }
                    }


                    if (cid.length == 2){
                        // recognize CIDs
                        categoryIDs.add(cid[0]);
                        categoryIDs.add(cid[1]);
                        System.out.println("CiD recognized in the " + round + "th round are " + cid[0] + " and "+ cid[1]);
                        num += 2;
                    }else{
                        // recognize CID
                        categoryIDs.add(cid[0]);
                        System.out.println("CID recognized in the "+ round +"th round is " + cid[0]);
                        num++;
                    }
                }
            }
            System.out.println("the " + round + "-th round identify "+num+" cids" );
        }

        // 等待修改，hxq
        double time = 0;
        return time;
    }

    public void printCidMap() {
        for (Integer slotId : CidMap.keySet()) {
            System.out.println("slotId = " + slotId + "code = " + CidMap.get(slotId));
        }
    }

    public void setTagList(List<Tag> tagList) {
        this.tagList = tagList;
    }

//    public List<String> getCategoryIDs() {
//        return categoryIDs;
//    }
}