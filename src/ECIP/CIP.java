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
        categoryIDs = new HashSet<>();
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

            if(tag.isActive()){
                String cid = tag.selectSlotPseudoRandom(frameSize, random);

                //At the same time, it will overlap CID and then construct CidMap
                constructCidMapAndSlotToTagList(tag, cid, slotToTagList);
            }
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
        int cidnum = 0;
        int repeated = 0;
        int unReadTagNum = tagList.size();     //the num of unread tag

        Iterator<Tag> iterator = null;  // used to modify tagList
        int num;        // count the num of recognized CID of every round.
        while(unReadCidNum > 0){


            num = 0;
            round++;
            output+="第 "+round+" 轮开始！\n";
            int frameSize = f;
            CidMap.clear();

            /*
            tags in tagList select slot
            */
            int random = (int)(100 * Math.random());


            selectSlot(random, frameSize);
            printCidMap();
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
                        output+=cid[0]+"\n";
                        output+=cid[1]+"\n";
                        System.out.println("CiD recognized in the " + round + "th round are " + cid[0] + " and "+ cid[1]);
                        num += 2;
                    }else if (cid.length == 1){
                        // recognize CID


                        categoryIDs.add(cid[0]);
                        System.out.println("CID recognized in the "+ round +"th round is " + cid[0]);
                        output+=cid[0]+"\n";
                        num++;
                    }
                }
            }
            output+="在第 "+round+" 轮，共识别 "+num+" 个类别ID\n\n";
            System.out.println("the " + round + "-th round identify "+num+" cids" );
            if(num == 0) {
                repeated ++;
            }
            cidnum += num;
            num = 0;

            if(repeated >= 20) {
                System.out.println("没有识别类别ID的轮次超过20，停止！");
                output+="没有识别类别ID的轮次超过20，停止！\n";
                output+="需要识别的类别ID数目："+(unReadCidNum+cidnum)+", 未能识别的类别ID数目："+unReadCidNum+", 识别的类别ID数目："+cidnum+ ", 识别率:"+(unReadCidNum*1.0/(unReadCidNum+cidnum))+"\n\n";
                output+="模拟结束！\n";
                analysis+="需要识别的类别ID数目："+(unReadCidNum+cidnum)+", 未能识别的类别ID数目："+unReadCidNum+", 识别的类别ID数目："+cidnum+ ", 识别率:"+(unReadCidNum*1.0/(unReadCidNum+cidnum))+"\n";

                System.out.println(output);
                break;
            }
        }

        // 计算时间，存储在time中
        time();

        System.out.println("需要时间约: "+time*1.0/1000 + "s");
        System.out.println("识别的cid数目 = "+cidnum);
        if(repeated < 20) {
            output+="识别结束！\n";
            output+="需要识别的类别ID数目："+(unReadCidNum+cidnum)+", 识别的类别ID数量："+cidnum+", 识别的类别ID数目："+cidnum+", 识别率：100%"+", 需要时间约： "+time*1.0/1000 + " s\n";
            output+="模拟结束！\n";

            analysis+="需要识别的类别ID数目："+(unReadCidNum+cidnum)+", 识别的类别ID数量："+cidnum+", 识别的类别ID数目："+cidnum+", 识别率：100%"+", 需要时间约： "+time*1.0/1000 + " s\n";

        }
        return time;
    }

    @Override
    public double time() {
        return 0;
    }


    public void printCategoryIDs() {
        for(String s : categoryIDs) {
            System.out.println(s);
        }
    }

    public void printCidMap() {
        for (Integer slotId : CidMap.keySet()) {
            System.out.println("slotId = " + slotId + "code = " + CidMap.get(slotId));
        }
    }

    public void setTagList(List<Tag> tagList) {
        this.tagList = tagList;

    }

    public void printSlotToTagList(Map<Integer, List<Tag>> stringListMap) {
        for(Integer in : stringListMap.keySet()) {
            System.out.println("slot:"+in);
            System.out.println("taglist:");
            for(Tag tag : stringListMap.get(in)) {
                System.out.println(tag.getCategoryID());
            }
        }
    }

//    public List<String> getCategoryIDs() {
//        return categoryIDs;
//    }
}