package ECIP;

import java.util.*;


public class ECIP extends CIP {

    protected Map<Integer, Integer> indicator = new HashMap<>();
    protected Vector<Integer> location = new Vector<>();
    protected Vector<String> d = new Vector<>();
    protected int unReadCidNum;
    protected long time=0;
    protected int f1 = 0;//random arranged identification phase的时隙
    protected int f2 = 0;// rearranged identification phase的时隙






    public ECIP() {
    }

    public ECIP(List<Tag> tagList, int unReadCidNum, int f) {
        super(tagList);
        this.unReadCidNum = unReadCidNum;
        this.f1 = f;
    }

    public ECIP(List<Tag> tagList, int unReadCidNum){
        super(tagList);
        //this.unReadTagNum = tagList.size();
        this.unReadCidNum = unReadCidNum;
    }

    public ECIP(List<Tag> tagList, int unReadCidNum, int f1, int f2) {
        super(tagList);
        //this.unReadTagNum = tagList.size();
        this.unReadCidNum = unReadCidNum;
        this.f1 = f1;
        this.f2 = f2;
    }


    /**
     * simulate random identification phase
     * at the same time, construct indicator
     * the first round of ecip is the same as cip
     */
    protected int randomIdentificationPhase() {
        int frameSize = f1;
        /*
        tags in tagList select slot, construct CidMap and SlotToTagList
        */
        // generate random seed
        int random = (int) (100 * Math.random());
        selectSlot(random, frameSize);

        //printCidMap();
        int num = recognizeCid(false);


        //System.out.println("in randomIdentificationPhase()");

        //System.out.println("in round " + round + " readTagNum = " + readCidNumInOneRound);


        unReadCidNum -= num;
        return num;
    }

    /**
     * simulate rearranged identification phase process
     */
    protected int rearrangedIdentificationPhase() {


            /*
            modify location, and structure d using CidMap and indicator;
             */
        constructLocationAndStructureD();

            /*
             active tags select slot based on x index
             at the same time, construct CidMap and slotToTagList
             */
        selectSlotBasedOnXIndex();
        //printCidMap();
            /*
            recognize cid using partial cid and combinecid()
            at the same time, construct location
             */
        int num = recognizeCid(true);

        unReadCidNum -= num;
        return num;
    }

    public double identifyAll() {

        int repeated = 0;
        int round = 1;
        int num1, num2;
        int cidnum = 0;


            System.out.println("round "+round);
            output+="第 "+round+" 轮开始（随机分配阶段）！\n";


            num1 = randomIdentificationPhase();
            cidnum += num1;

            System.out.println("identify cids in random identification phase: "+num1);
            output+="在第 "+round+" 轮（随机分配阶段），共识别 "+num1+" 个类别ID\n\n";
            if(num1==0) repeated++;

            while(unReadCidNum > 0) {
                round++;
                System.out.println("round "+round);
                output+="第 "+round+" 轮开始（重新分配阶段）！\n";

                num2 = rearrangedIdentificationPhase();
                cidnum +=num2;
                System.out.println("identify cids in rearranged identification phase: "+num2);
                output+="在第 "+round+" 轮（重新分配阶段），共识别 "+num2+" 个类别ID\n\n";


                //System.out.println("the time of round "+round+" is:"+oneRoundTime);
                System.out.println(" ");
                if (num2 == 0) repeated++;

                if (repeated >32) {
                    //System.out.println("未能识别的tag数目 = " + unReadTagNum);
                    System.out.println("未能识别的cid数目 = "+unReadCidNum);
                    System.out.println("识别的cid数目 = "+cidnum);
                    System.out.println("round = " + round);
                    System.out.println("repeated round > 32, stop!");

                    output+="重复轮次>32, 识别停止！\n";
                    output+="需要识别的类别ID数目："+(unReadCidNum+cidnum)+", 未能识别的类别ID数目："+unReadCidNum+", 识别的类别ID数目："+cidnum+ ", 识别率:"+(unReadCidNum*1.0/(unReadCidNum+cidnum))+"\n";
                    analysis+="需要识别的类别ID数目："+(unReadCidNum+cidnum)+", 未能识别的类别ID数目："+unReadCidNum+", 识别的类别ID数目："+cidnum+ ", 识别率:"+(unReadCidNum*1.0/(unReadCidNum+cidnum))+"\n";

                    break;
                }
                //oneRoundTime = 0;
            }

            // 计算时间，存储在time中
        time();

        System.out.println("需要时间约: "+time*1.0/1000 + "s");
        System.out.println("识别的cid数目 = "+cidnum);
        output+="识别结束！\n";
        output+="需要识别的类别ID数目："+(unReadCidNum+cidnum)+", 识别的类别ID数量："+cidnum+", 识别的类别ID数目："+cidnum+", 识别率：100%"+", 需要时间约： "+time*1.0/1000 + " s\n";
        output+="模拟结束！\n";

        analysis+="需要识别的类别ID数目："+(unReadCidNum+cidnum)+", 识别的类别ID数量："+cidnum+", 识别的类别ID数目："+cidnum+", 识别率：100%"+", 需要时间约： "+time*1.0/1000 + " s\n";

        return time;

    }

    // 计算时间
    public void time() {
        // 计算时间
        double d = unReadCidNum /f1;
        // the time of random allocation phase T_p1
        double t1 = f1*Math.exp(-d)*(-0.8)+f1*1.2;
        time += t1;
        //oneRoundTime += t1;

        // the time of broadcasting indicator vector in the first round:T_i
        double ti = f1 * (d*Math.exp(-d)+d)*(2.8)/96;
        time += ti;

        // 计算rearranged identification phase的时间 T_cs
        double tcs = 2.4*(unReadCidNum-f2+f2*Math.exp(-unReadCidNum*1.0/f2));
        time+=tcs;
    }


    /**
     * recognize cid using CidMap and slotToTagList
     * construct indicator vector: for every slots, if it is a category-collision slot,the corresponding
     * bit in V I is set to i(修改：i-th category-collision slot); otherwise, the bit value is ‘-1’.
     *
     * @param isPartial : whether it is a partial cid
     */
    int recognizeCid(boolean isPartial) {

        indicator.clear();
        int readCidNumInOneRound = 0;
        int i = 0; // i-th category-collision slot

        for (Integer slotId : CidMap.keySet()) {
            String[] strs = decodeCID(CidMap.get(slotId));
            int l = strs.length;

            // category-compatible slot
            if (l == 1) {
                for (Tag tag : slotToTagList.get(slotId)) {
                    tag.setActive(false);
                    tagList.remove(tag);
                   // unReadTagNum--;
                }

                // recognize CID and construct indicator
                addCid(strs, isPartial, slotId);
                readCidNumInOneRound++;
                indicator.put(slotId, -1);
            } else if (l == 2) {
                for (Tag tag : slotToTagList.get(slotId)) {
                    tag.setActive(false);
                    tagList.remove(tag);
                   // unReadTagNum--;
                }

                // recognize CIDs
                addCid(strs, isPartial, slotId);
                readCidNumInOneRound += 2;
                indicator.put(slotId, -1);
            } else {
                indicator.put(slotId, i);
                ++i;
            }
        }

        //printIndicator();
        return readCidNumInOneRound;
    }


    /**
     * @param strs:     String array after decoding using Manchester
     * @param isPartial : whether it is a partial cid
     * @param slotId
     */
    void addCid(String[] strs, boolean isPartial, int slotId) {
        if (isPartial) {
            for (String str : strs) {
                String cid = combineCID(slotId, str);
                System.out.println("identify cid:" +cid+"\n");
                output+="识别类别ID："+cid+"\n";
                categoryIDs.add(cid);
                //System.out.println(cid);
            }

        } else {
            for (String str : strs) {
                System.out.println("identify cid:"+str+"\n");
                output+="识别类别ID："+str+"\n";
                categoryIDs.add(str);
                //System.out.println(str);
            }
        }
    }


    /**
     * construct location vector and structure d
     * Each element corresponds to an unidentified category-
     * collision slot and indicates the index X index of the first
     * collision bit ‘X’ in this slot.
     */
    protected void constructLocationAndStructureD() {
        Vector<Integer> newLocation = new Vector<>();
        Vector<String > newStructureD = new Vector<>();

        //System.out.println("in constructAndStructureD()");
        //System.out.println("indicator");
        //printIndicator();
        for (Integer slotID : indicator.keySet()) {

            if (indicator.get(slotID) != -1) {
                int i = indicator.get(slotID);//第i个冲突时隙

                //System.out.println("slot:"+slotID);
                // category-collision
                String data = CidMap.get(slotID);
                //System.out.println("cid map data:"+data);
                int xindex;
                String strBeforeX;
                if(location.isEmpty()) { // 第1次rearranged identification phase，data是完整的cid
                     xindex = data.indexOf('X');
                    newLocation.add(xindex);
                     strBeforeX = data.substring(0, data.indexOf('X'));
                    //System.out.println("str before x:"+strBeforeX);
                    newStructureD.add(strBeforeX);
                } else { // 之后的rearranged identification phase，此时data是部分cid，计算xindex时要注意
                    // 保留上一轮的location vector, structure D
                     xindex = location.get(slotID/2)+CidMap.get(slotID).indexOf('X')+1;
                    //System.out.println("i :"+i);
                    //System.out.println("new xindex:"+xindex);
                     newLocation.add(xindex);
                    //System.out.println("");
                     strBeforeX = d.get(slotID/2)+CidMap.get(slotID).substring(0,CidMap.get(slotID).indexOf('X'))+((slotID)%2);
                    //System.out.println("str before x:"+strBeforeX);
                     newStructureD.add(strBeforeX);

                }



            }
        }

        location = newLocation;
        d = newStructureD;

        //System.out.println("location vector:");
        //printLocation();
        //System.out.println("structure d");
        //printStructureD();



    }

    /**
     * in tag rearrangement phase, tag selects slot based on  x index, tags belonging to the same slot is divided to one or two parts
     * at the same time, construct CidMap and SlotToTagList
     */
    protected void selectSlotBasedOnXIndex() {

        CidMap.clear();
        Map<Integer, List<Tag>> newSlotToTagList = new HashMap<>();

        for(Tag tag : tagList) {
            int oldslot = tag.getSlotSelected();
            int j = indicator.get(oldslot);
            if(j!=-1) {
                int xindex = location.get(j);
                tag.selectSlotBasedOnXIndex(j,xindex);
                String partialCid = tag.getCategoryID().substring(xindex + 1);
                constructCidMapAndSlotToTagList(tag, partialCid, newSlotToTagList);
            }

        }
        slotToTagList = newSlotToTagList;
//
//
    }

    /**
     * combine CID
     *
     * @param slotID
     * @param after  string after x index
     * @return CID
     */
    protected String combineCID(int slotID, String after) {

        int j = slotID / 2;
        String before = d.get(j);
        String x = String.valueOf(slotID % 2);
        //System.out.println("j = " + j + "before = " + before + "x=" + x);
        //System.out.println();

        return before + x + after;


    }


    public void printCidMap() {
        for (Integer slotId : CidMap.keySet()) {
            String code = CidMap.get(slotId);
            boolean identifiable = !(code.contains("X")&&code.substring(code.indexOf("X")+1).contains("X"));
            System.out.println("slot = " + slotId + " code = " + code + " 是否可以识别:"+identifiable);
        }
    }

    public void printSlotToTagList() {
        for (Integer slot : slotToTagList.keySet()) {
            System.out.println("slot = " + slot);
            for (Tag tag : slotToTagList.get(slot)) {
                System.out.println("tag cid = " + tag.getCategoryID());
            }
        }
    }

    public void printIndicator() {
        for (Integer slotId : indicator.keySet()) {
            System.out.println("slot = " + slotId + " indicator = " + indicator.get(slotId));
        }
    }

    public void printStructureD() {
        for (int i = 0; i < d.size(); ++i) {
            System.out.println("the " + i + "-th collision slot : code before x = " + d.get(i));
        }
    }

    public void printLocation() {
        for (int i = 0; i < location.size(); i++) {
            System.out.println("the " + i + "-th collision slot : xindex = " + location.get(i));
        }
    }

    public void setTagList(List<Tag> tagList) {
        this.tagList = tagList;
    }

//    public Set<String> getCategoryIDs() {
//        return categoryIDs;
//    }

    public void setCidMap(Map<Integer, String> CidMap) {
        this.CidMap = CidMap;
    }
}