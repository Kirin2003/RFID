package IdentifyTool;

import java.util.*;


public class ECIP extends CIP {

    protected Map<Integer, Integer> indicator = new HashMap<>();
    protected Vector<Integer> location = new Vector<>();
    protected Vector<String> d = new Vector<>();

    protected boolean flag = false; // 是否需要新一轮识别

    public ECIP(List<Tag> virtualList, List<Tag> actualList, int virtualCidNum, int actualCidNum, int f, int tidLength, int cidLength,int warningNum, String warningCid) {
        super(virtualList, actualList, virtualCidNum, actualCidNum, f, tidLength, cidLength,warningNum,warningCid);

    }

    public ECIP(List<Tag> tagList, int cidNum, int tidLength, int cidLength) {
        super(tagList,cidNum,tidLength,cidLength);
    }

    public ECIP(List<Tag> tagList, List<Tag> actualList, int cidNum, int actualNum, int tagLength, int cidLength) {
        this(tagList,actualList,cidNum,actualNum,0,tagLength,cidLength,1000000,"");
    }


    /**
     * simulate random identification phase
     * at the same time, construct indicator
     * the first round of ecip is the same as cip
     */
    protected int randomIdentificationPhase() {
        int frameSize = (int)(Math.ceil(0.98*actualCidNum));

        // generate random seed
        int random = (int) (100 * Math.random());
        selectSlot(random, frameSize);

        //printCidMap();
        int num = recognizeCid(false);

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

        return num;
    }

    public double identifyAll() {
        // 记录统计信息
        // 未能识别类别ID的轮次数目
        int repeated = 0;
        // 识别轮次
        int round = 1;
        // 随机识别阶段识别的类别ID数，每一次重新分配阶段识别的类别ID数
        int num1, num2;
        // 每一轮时隙个数的集合
        List<Integer> fs = new ArrayList<>();
        // 存储每一轮的空时隙，单时隙，能识别两个类别ID的时隙，冲突时隙个数
        // two表示该时隙可以识别两个类别ID
        int empty = 0, single = 0, two = 0, categoryCollision = 0;
        // 每一轮的空时隙，单时隙，能识别两个类别ID的时隙，冲突时隙个数的集合
        List<Integer> emptySlots = new ArrayList<>();
        List<Integer> singleSlots = new ArrayList<>();
        List<Integer> twoCidSlots = new ArrayList<>();
        List<Integer> categoryCollisionSlots = new ArrayList<>();
        // 存储空时隙，单时隙，能识别两个类别ID的时隙，冲突时隙总个数
        int totalEmp = 0, totalSing = 0, totalTwo = 0, totalCollision = 0;

        // 优化时隙
        f = (int)(Math.ceil(0.98 * actualCidNum));
        fs.add(f);

        System.out.println("round "+round);
        output+="第 "+round+" 轮开始（随机分配阶段）！\n";

        num1 = randomIdentificationPhase();

        // 根据slotToTagList信息求每一轮的空时隙，单时隙，能识别两个类别ID的时隙，类别冲突时隙个数
        for(Integer slot : CidMap.keySet()) {
            String code = CidMap.get(slot);
            if(!code.contains("X")) {
                single ++;

            } else if(code.indexOf("X")!=code.lastIndexOf("X")) {
                two ++;

            } else {
                categoryCollision++;
            }

        }
        empty = f - single - two - categoryCollision;

        totalEmp += empty;
        totalSing += single;
        totalTwo += two;
        totalCollision += categoryCollision;

        emptySlots.add(empty);
        singleSlots.add(single);
        twoCidSlots.add(two);
        categoryCollisionSlots.add(categoryCollision);

        empty = single = two = categoryCollision = 0;


        System.out.println("identify cids in random identification phase: "+num1);
        output+="在第 "+round+" 轮（随机分配阶段），共识别 "+num1+" 个类别ID\n\n";


        if(num1==0) {
            repeated++;
            time += (f-1)*0.4 + 1.2;
        }

        while(flag) { // 当存在类别冲突时隙时，需要继续识别
            flag = false;
            round++;
            System.out.println("round "+round);
            output+="第 "+round+" 轮开始（重新分配阶段）！\n";

            num2 = rearrangedIdentificationPhase();
            // 重新分配阶段，不需要优化时隙
            f=CidMap.size();
            fs.add(f);

            // 根据slotToTagList信息求每一轮的空时隙，单时隙，能识别两个类别ID的时隙，类别冲突时隙个数
            for(Integer slot : CidMap.keySet()) {
                String code = CidMap.get(slot);
                if(!code.contains("X")) {
                    single ++;

                } else if(code.indexOf("X")==code.lastIndexOf("X")) {
                    two ++;

                } else {
                    categoryCollision++;
                }

            }

            empty = f - single - two - categoryCollision;


            totalEmp += empty;
            totalSing += single;
            totalTwo += two;
            totalCollision += categoryCollision;

            emptySlots.add(empty);
            singleSlots.add(single);
            twoCidSlots.add(two);
            categoryCollisionSlots.add(categoryCollision);

            empty = single = two = categoryCollision = 0;


            System.out.println("identify cids in rearranged identification phase: "+num2);
            output+="在第 "+round+" 轮（重新分配阶段），共识别 "+num2+" 个类别ID\n\n";


            //System.out.println("the time of round "+round+" is:"+oneRoundTime);
            System.out.println(" ");
            if (num2 == 0) { // 避免死循环，记录未识别任何cid的轮次
                repeated++;
                time += (f-1)*0.4 + 1.2;
            }

            if (repeated >=32) {//因为未识别任何cid的轮次过多而提前停止

                break;
            }


        }

        // 根据所有识别轮次的空时隙，单时隙，能识别两个类别ID的时隙，冲突时隙总个数计算时间
        time += totalEmp * 0.4 + (totalSing+totalCollision+totalTwo)*tcid;
        System.out.println("time:"+time);
        int roundNum = emptySlots.size();
        System.out.println("frame:"+fs);
        System.out.println("empty:"+emptySlots+" single:"+singleSlots+" two:"+twoCidSlots+" collision:"+categoryCollisionSlots);




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




        if(repeated < 32) { // 全部识别
            output+="识别结束！\n";
            output+="需要识别的类别ID数目："+(virtualCidNum)+", 识别存在的类别ID数量："+presentNum+"， 识别缺失的类别ID数目："+missingNum+", 准确率：100%"+", 需要时间约： "+ String.format("%.2f", time*1.0/1000) + " s\n";
            output+="模拟结束！\n";
            analysis+="需要识别的类别ID数目："+(virtualCidNum)+", 识别存在的类别ID数量："+presentNum+"， 识别缺失的类别ID数目："+missingNum+", 准确率：100%"+", 需要时间约： "+String.format("%.2f", time*1.0/1000) + " s\n";

        } else { // 部分识别，因为未识别任何cid的轮次过多而停止
            output+="由于冲突时隙，未能识别类别ID的轮次过多，提前停止！可能影响准确率！";
            output+="需要识别的类别ID数目："+(virtualCidNum)+", 识别存在的类别ID数量："+presentNum+"， 识别缺失的类别ID数目："+missingNum+", 准确率："+(1- (misidentification*1.0/virtualCidNum) )+", 需要时间约： "+String.format("%.2f", time*1.0/1000) + " s\n";
            output+="模拟结束！\n";
            analysis+="需要识别的类别ID数目："+(virtualCidNum)+", 识别存在的类别ID数量："+presentNum+"， 识别缺失的类别ID数目："+missingNum+", 准确率："+(1- (misidentification*1.0/virtualCidNum) )+", 需要时间约： "+String.format("%.2f", time*1.0/1000) + " s\n";
        }

        analysis+="识别存在的类别ID有"+presentNum+"个，分别为：\n";
        for(String cid : presentCids) {
            analysis+=cid+"\n";
        }

        analysis+="识别缺失的类别ID有"+missingNum+"个，分别为：\n";
        for(String cid : missingCids) {
            analysis+=cid+"\n";
        }
        return time;

    }



    // 计算时间, 全过程的理论时间
    public static double time(int actualCidNum) {

        return 1.81 * actualCidNum;
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

                }

                // recognize CID and construct indicator
                addCid(strs, isPartial, slotId);
                readCidNumInOneRound++;
                indicator.put(slotId, -1);
            } else if (l == 2) {
                for (Tag tag : slotToTagList.get(slotId)) {
                    tag.setActive(false);

                }

                // recognize CIDs
                addCid(strs, isPartial, slotId);
                readCidNumInOneRound += 2;
                indicator.put(slotId, -1);
            } else {
                indicator.put(slotId, i);
                ++i;

                flag = true; // 需要新一轮识别
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

                ///System.out.println("identify cid:" +cid+"\n");
                output+="识别类别ID："+cid+"存在\n";
                presentCids.add(cid);

                //System.out.println(cid);
            }

        } else {
            for (String str : strs) {
                ///System.out.println("identify cid:"+str+"\n");
                output+="识别类别ID："+str+"存在\n";
                presentCids.add(str);

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

        for(Tag tag : actualList) {
            if (tag.isActive()) {
                int oldslot = tag.getSlotSelected();
                int j = indicator.get(oldslot);
                if(j!=-1) {
                    int xindex = location.get(j);
                    tag.selectSlotBasedOnXIndex(j,xindex);
                    String partialCid = tag.getCategoryID().substring(xindex + 1);
                    constructCidMapAndSlotToTagList(tag, partialCid, newSlotToTagList);
                }
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
        System.out.println("Cid Map");
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

}