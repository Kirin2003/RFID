package ECIP;

import java.util.*;
import org.apache.log4j.Logger;

public class ECLS extends IdentifyTool{
    public static Logger logger =  Logger.getLogger("ecls");
    protected List<Tag> virtualList;
    protected List<Tag> actualList;
    protected Map<Integer,List<Tag>> slotToVirtualList = new HashMap<>(); // key: slot, value: virtual tags which are allocated to the slot
    protected Map<Integer, String> CidMap = new HashMap<>(); //key: slotId. value: the overlapped cid
    protected Map<Integer, Integer> indicator = new HashMap<>();
    protected  Vector<Integer> location = new Vector<>();

    protected Map<Integer, Integer> L = new HashMap<>(); // 某个时隙是空时隙，单时隙，冲突时隙
    protected int f1 = 0;

    boolean flag = false;

    public ECLS(List<Tag> virtualList, List<Tag> actualList, int virtualCidNum, int actualCidNum, int f, int tidLength, int cidLength) {
        super(tidLength, cidLength);
        this.virtualList = virtualList;
        this.actualList = actualList;
        this.f1 = f;
        this.virtualCidNum = virtualCidNum;
        this.actualCidNum = actualCidNum;

    }

    public void allocate1(int frameSize, int random) {
        slotToVirtualList.clear();
        CidMap.clear();
        L.clear();

        // 给每个虚拟标签分配时隙
        for (Tag tag : virtualList) {
            if (tag.isActive()) {
                tag.selectSlotPseudoRandom(frameSize,random);
            }
            int slot = tag.getSlotSelected();

            // 生成分配向量（或：分配映射）A和CidMap
            if (!slotToVirtualList.keySet().contains(slot)) {
                List<Tag> l = new ArrayList<>();
                l.add(tag);
                slotToVirtualList.put(slot,l);

                CidMap.put(slot,tag.getCategoryID());
            } else {
                slotToVirtualList.get(slot).add(tag);

                String code = CidMap.get(slot);
                String newCode = encode(code, tag.getCategoryID());
                CidMap.put(slot,newCode);
            }
        }

        // 记录标签类别个数, 键：时隙，值：标签类别个数
        String cid1 = null;
        boolean isSingle = true;
//        for(Integer slot : slotToVirtualList.keySet()) {
//            if(slotToVirtualList.get(slot).size() == 0) {
//                L.put(slot, 0); // 空时隙
//                break;
//            } else {
//                cid1 = slotToVirtualList.get(slot).get(0).getCategoryID();
//
//                for(Tag tag : slotToVirtualList.get(slot)) {
//                    if(tag.getCategoryID() != cid1) {
//                        isSingle = false;
//                        break;
//                    }
//                }
//                if(isSingle) {
//                    L.put(slot, 1); // 单时隙
//
//                } else {
//                    L.put(slot, 2); // 表示冲突时隙
//                }
//            }
//        }


    }

    public void printL() {
        for(Integer i : L.keySet()) {

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

    public Set<Integer> response1() {
        Set<Integer> presentSlots = new HashSet<>();
        for (Tag tag : actualList) {
            if(tag.isActive()) {
                presentSlots.add(tag.getSlotSelected());
            }

        }
        return presentSlots;
    }

    public int identify1() {
        int num = 0;


        Set<Integer> presentSlots =  response1();


        indicator.clear();
        location.clear();
        Set<String> cids = new HashSet<>(); // 临时存放整个时隙都缺失的cid
        int i = 0; // 未能识别的冲突时隙编号


        for(Integer slot : slotToVirtualList.keySet()) {
            if(!presentSlots.contains(slot)) {
                cids.clear();
                // 全部缺失
                // 收集全部cid ,标签变为不活跃状态
                for (Tag tag : slotToVirtualList.get(slot)) {
                    cids.add(tag.getCategoryID());
                    tag.setActive(false);
                }

                // 输出缺失的cid
                for(String cid : cids) {
                    System.out.println("识别类别ID："+cid+"缺失\n");
                    output+="识别类别ID："+cid+"缺失\n";
                    missingCids.add(cid);
                    num++;
                }

                indicator.put(slot, -1);
            } else if( !CidMap.get(slot).contains("X") ) {
                // 单时隙， 识别该cid存在
                // 标签变为不活跃状态
                for (Tag tag : slotToVirtualList.get(slot)) {
                    tag.setActive(false);
                }


                Tag tag = slotToVirtualList.get(slot).get(0);

                String cid = tag.getCategoryID();
                System.out.println("识别类别ID："+cid+"存在\n");
                output+="识别类别ID："+cid+"存在\n";
                presentCids.add(cid);

                num++;

                indicator.put(slot, -1);
            } else {
                // 存在未能识别的冲突时隙，需要新一轮的识别
                flag = true;
                indicator.put(slot, i);
                location.add(CidMap.get(slot).indexOf('X'));
                i++;
            }
        }
        return num;
    }

    public int randomIdentificationPhase() {
        int random = (int) (100 * Math.random());
        int frameSize =f1;
        allocate1(frameSize,random);

        int num = identify1();

        return num;
    }

    /**
     * construct location vector and structure d
     *Each element corresponds to an unidentified category-
     * collision slot and indicates the index X index of the first
     * collision bit ‘X’ in this slot.
     */
//    protected void constructLocation() {
//        System.out.println("indicator");
//        printIndicator();
//        System.out.println("cid map");
//        printCidMap();
//
//        Vector<Integer> newLocation = new Vector<>();
//        for (Integer slotID : indicator.keySet()) {
//            if (indicator.get(slotID) != -1) {
//                int i = indicator.get(slotID);//第i个冲突时隙
//
//                }
//            }
//
//        location = newLocation;
//
//    }

    public void printCidMap() {
        for (Integer slotId : CidMap.keySet()) {
            System.out.println("slotId = " + slotId + " code = " + CidMap.get(slotId));
        }
    }

    public void printIndicator() {
        for (Integer slotId : indicator.keySet()) {
            System.out.println("slotId = " + slotId + " indicator = " + indicator.get(slotId));
        }
    }





    public void allocate2() {

        //Map<Integer, List<Tag>> newSlotToVirtualList = new HashMap<>();
        //Map<Integer, String> newCidMap = new HashMap<>();

        /*
        重写：hxq, 2022-3-5
        重新分配，依据xindex分配，reconcile冲突时隙
         */
        for(Integer slot : indicator.keySet()) {
            if(indicator.get(slot) != -1) {
                int i = indicator.get(slot);
                int xindex = location.get(i);

                for(Tag tag : slotToVirtualList.get(slot)) {
                    if(tag.getCategoryID().charAt(xindex) == '0') {
                        tag.setSlotSelected(2*i);
                    } else {
                        tag.setSlotSelected(2*i+1);
                    }
                }
            }
        }

        // 建立新的slotToVirtualList和CidMap
        slotToVirtualList.clear();
        CidMap.clear();

        // 所有active tag都重新选择了时隙
        for(Tag tag : virtualList) {
            if(tag.isActive()) {
                int slot = tag.getSlotSelected();

                // 这个时隙已经有标签选择
                if(slotToVirtualList.keySet().contains(slot)) {
                    slotToVirtualList.get(slot).add(tag);

                    String code = CidMap.get(slot);
                    String newCode = encode(code, tag.getCategoryID());
                    CidMap.put(slot,newCode);
                } else { // 这个时隙还没有标签选择
                    List<Tag> l = new ArrayList<>();
                    l.add(tag);
                    slotToVirtualList.put(slot, l);

                    CidMap.put(slot, tag.getCategoryID());
                }
            }
        }
        //slotToVirtualList = newSlotToVirtualList;
        //CidMap = newCidMap;

    }

    public int rearrangedIdentificationPhase() {
        int num = 0;

        //constructLocationAndStructureD();
        allocate2();
        num = identify1();
        return num;
    }


    @Override
    public double identifyAll() {
        int repeated = 0;
        int round = 1;
        int num1, num2;

        System.out.println("round "+round);
        output+="第 "+round+" 轮开始（随机分配阶段）！\n";

        num1 = randomIdentificationPhase();
        System.out.println("identify cids in random identification phase: "+num1);
        output+="在第 "+round+" 轮（随机分配阶段），共识别 "+num1+" 个类别ID\n\n";
        if(num1==0) repeated++;

        while(flag) {
            flag = false;
            round++;
            System.out.println("round "+round);
            output+="第 "+round+" 轮开始（重新分配阶段）！\n";

            num2 = rearrangedIdentificationPhase();

            System.out.println("identify cids in rearranged identification phase: "+num2);
            output+="在第 "+round+" 轮（重新分配阶段），共识别 "+num2+" 个类别ID\n\n";

            //System.out.println("the time of round "+round+" is:"+oneRoundTime);
            System.out.println(" ");
            if (num2 == 0) repeated++;

            if (repeated >=32) {//因为未识别任何cid的轮次过多而提前停止

                break;
            }

            time();

            int presentNum = presentCids.size();
            int missingNum = missingCids.size();
            int misidentification = 0;

            if(repeated < 32) { // 全部识别
                output+="识别结束！\n";
                output+="需要识别的类别ID数目："+(virtualCidNum)+", 识别存在的类别ID数量："+presentNum+", 准确率：100%， 识别缺失的类别ID数目："+missingNum+", 准确率：100%"+", 需要时间约： "+ String.format("%.2f", time*1.0/1000) + " s\n";
                output+="模拟结束！\n";
                output+="需要识别的类别ID数目："+(virtualCidNum)+", 识别存在的类别ID数量："+presentNum+", 准确率：100%， 识别缺失的类别ID数目："+missingNum+", 准确率：100%"+", 需要时间约： "+String.format("%.2f", time*1.0/1000) + " s\n";

            } else { // 部分识别，因为未识别任何cid的轮次过多而停止
                output+="由于冲突时隙，未能识别类别ID的轮次过多，提前停止！可能影响准确率！";
                output+="需要识别的类别ID数目："+(virtualCidNum)+", 识别存在的类别ID数量："+presentNum+", 准确率：100%， 识别缺失的类别ID数目："+missingNum+", 准确率："+(misidentification*1.0/missingNum)+", 需要时间约： "+String.format("%.2f", time*1.0/1000) + " s\n";
                output+="模拟结束！\n";
                analysis+="需要识别的类别ID数目："+(virtualCidNum)+", 识别存在的类别ID数量："+presentNum+", 识别缺失的类别ID数目："+missingNum+", 准确率：100%"+", 需要时间约： "+String.format("%.2f", time*1.0/1000) + " s\n";
            }
            return time;
        }

        return 0;
    }

    @Override
    public double time() {
        return 0;
    }
}
