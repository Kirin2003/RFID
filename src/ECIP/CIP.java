package ECIP;

import java.util.*;


public class CIP extends IdentifyTool{
    protected List<Tag> actualList;
    protected List<Tag> virtualList;
    protected Map<Integer, String> CidMap = new HashMap<>(); //store the slotId and the overlapped cid
    protected Map<Integer,List<Tag>> slotToTagList = new HashMap<>();
    protected boolean flag = true;


    public CIP(List<Tag> virtualList, List<Tag> actualList, int virtualCidNum, int actualCidNum, int f, int tidLength, int cidLength, int warningNum, String warningCid) {
        super(tidLength, cidLength, warningNum,warningCid);
        this.virtualList = virtualList;
        this.actualList = actualList;
        this.virtualCidNum = virtualCidNum;
        this.actualCidNum = actualCidNum;
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


    /**
     * selects a slot based on the random hash function H(CID, r) mod f
     * @param random random seed
     * @param frameSize
     */
    protected void selectSlot(int random, int frameSize) {
        CidMap.clear();
        slotToTagList.clear();

        for (Tag tag : actualList) {

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

    protected int randomIdentificationPhase() {
        // 优化帧长
        int frameSize = f;

        // generate random seed
        int random = (int)(100 * Math.random());
        selectSlot(random, frameSize);


        // identify
        int readCidNumInOneRound = 0;
        for (Integer slotId : CidMap.keySet()) {
            String[] strs = decodeCID(CidMap.get(slotId));
            int l = strs.length;

            // category-compatible slot
            if (l == 1) {
                for (Tag tag : slotToTagList.get(slotId)) {
                    tag.setActive(false);
                }

                // recognize CID and construct indicator
                System.out.println("identify cid:" +strs[0]+"\n");
                output+="识别类别ID："+strs[0]+"存在\n";
                presentCids.add(strs[0]);
                readCidNumInOneRound++;
            } else if (l == 2) {
                for (Tag tag : slotToTagList.get(slotId)) {
                    tag.setActive(false);
                }

                // recognize CIDs
                System.out.println("identify cid:" +strs[0]+"\n");
                System.out.println("identify cid:" +strs[1]+"\n");
                output+="识别类别ID："+strs[0]+"存在\n";
                output+="识别类别ID："+strs[1]+"存在\n";
                presentCids.add(strs[0]);
                presentCids.add(strs[1]);
                readCidNumInOneRound += 2;

            } else { // 冲突时隙


                flag = true; // 需要新一轮识别
            }
        }


        return readCidNumInOneRound;
    }


    /**
     * cip process simulation
     */
    public double identifyAll(){
        int round = 1;
        int cidnum = 0;
        int repeated = 0;

        // 记录系统中待识别的类别ID数，优化时隙
        int unReadCidNum = actualCidNum;
        int f1 = (int)(Math.ceil(1.53*unReadCidNum));
        f =f1 > 10 ? f1:10;


        Iterator<Tag> iterator = null;  // used to modify tagList
        int num;        // count the num of recognized CID of every round.
        while( flag ) {
            flag = false;
            output+="第 "+round+" 轮开始（重新分配阶段）！\n";

            num = randomIdentificationPhase();
            output+="在第 "+round+" 轮（随机分配阶段），共识别 "+num+" 个类别ID\n\n";

            time += 2.31 * num;
            round++;
            // 优化时隙
            unReadCidNum -= num;
            int f2 = (int)(Math.ceil(1.53*unReadCidNum));
            f =f2 > 10 ? f2:10;

            if(num == 0)  {
                repeated ++;
                time += (9*0.4 + 1.2);
            }
            if(repeated >= 2) {//因为未识别任何cid的轮次过多而提前停止
                break;
            }

        }



        Set<String> virtualCids = new HashSet<>();
        for(Tag tag : virtualList) {
            virtualCids.add(tag.getCategoryID());
        }

        // 缺失的标签
        for(Tag tag : virtualList) {
            String cid = tag.getCategoryID();

            // 没有识别到存在的cid认为缺失
            if (!presentCids.contains(cid)) {
                changeMissingCids(cid);
            }
        }

        int presentNum = presentCids.size();
        int missingNum = virtualCidNum -presentNum;
        int misidentification = actualCidNum - presentNum;



        if(repeated < 2) { // 全部识别
            output+="识别结束！\n";
            output+="需要识别的类别ID数目："+(virtualCidNum)+", 识别存在的类别ID数量："+presentNum+"， 识别缺失的类别ID数目："+missingNum+", 准确率：100%"+", 需要时间约： "+ String.format("%.4f", time*1.0/1000) + " s\n";
            output+="模拟结束！\n";
            output+="需要识别的类别ID数目："+(virtualCidNum)+", 识别存在的类别ID数量："+presentNum+"， 识别缺失的类别ID数目："+missingNum+", 准确率：100%"+", 需要时间约： "+String.format("%.4f", time*1.0/1000) + " s\n";

        } else { // 部分识别，因为未识别任何cid的轮次过多而停止
            output+="由于冲突时隙，未能识别类别ID的轮次过多，提前停止！可能影响准确率！\n";
            output+="需要识别的类别ID数目："+(virtualCidNum)+", 识别存在的类别ID数量："+presentNum+"， 识别缺失的类别ID数目："+missingNum+", 准确率："+(1- (misidentification*1.0/virtualCidNum) )+", 需要时间约： "+String.format("%.4f", time*1.0/1000) + " s\n";
            output+="模拟结束！\n";
            analysis+="需要识别的类别ID数目："+(virtualCidNum)+", 识别存在的类别ID数量："+presentNum+"， 识别缺失的类别ID数目："+missingNum+", 准确率："+(1- (misidentification*1.0/virtualCidNum) )+", 需要时间约： "+String.format("%.4f", time*1.0/1000) + " s\n";
        }

        analysis+="识别存在的类别ID为：\n";
        for(String cid : presentCids) {
            analysis+=cid+"\n";
        }

        return time;
    }


    public static double time(int actualCidNum) {
        return 2.31*actualCidNum;
    }


    public void printCategoryIDs() {
        for(String s : presentCids) {
            System.out.println(s);
        }
    }

    public void printCidMap() {
        for (Integer slotId : CidMap.keySet()) {
            System.out.println("slotId = " + slotId + "code = " + CidMap.get(slotId));
        }
    }

    public void setTagList(List<Tag> tagList) {
        this.actualList = tagList;

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


}