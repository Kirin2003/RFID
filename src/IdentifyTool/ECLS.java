package IdentifyTool;

import java.util.*;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class ECLS extends IdentifyTool{
    protected static Logger logger =  Logger.getLogger("ecls");
    protected List<Tag> virtualList;
    protected List<Tag> actualList;
    protected Map<Integer,List<Tag>> slotToVirtualList = new HashMap<>(); // key: slot, value: virtual tags which are allocated to the slot
    protected Map<Integer, String> CidMap = new HashMap<>(); //key: slotId. value: the overlapped cid
    protected Map<Integer, Integer> indicator = new HashMap<>();
    protected  Vector<Integer> location = new Vector<>();


    boolean flag = false;

    public ECLS(List<Tag> virtualList, List<Tag> actualList, int virtualCidNum, int actualCidNum, int f, int tidLength, int cidLength, int warningNum,String warningCid) {

        super(tidLength, cidLength,  warningNum, warningCid);
        logger.setLevel(Level.DEBUG);
        this.virtualList = virtualList;
        this.actualList = actualList;
        this.f = f;
        this.virtualCidNum = virtualCidNum;
        this.actualCidNum = actualCidNum;

    }

    public ECLS(List<Tag> tagList, int cidNum, int tidLength, int cidLength) {
        this(tagList,tagList,cidNum,cidNum,0,tidLength,cidLength,1000000,null);
    }

    public ECLS(List<Tag> tagList, List<Tag> actualList, int cidNum, int actualNum, int tagLength, int cidLength) {
        this(tagList,actualList,cidNum,actualNum,0,tagLength,cidLength,1000000,"");
    }

    protected void allocate1(int frameSize, int random) {
        slotToVirtualList.clear();
        CidMap.clear();


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


    }


    /**
     * overlap category ID
     * @param s1
     * @param s2
     * @return overlapped CID
     */

    protected String encode(String s1, String s2){
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

    protected Set<Integer> response1() {
        Set<Integer> presentSlots = new HashSet<>();
        for (Tag tag : actualList) {
            if(tag.isActive()) {
                presentSlots.add(tag.getSlotSelected());
            }

        }
        return presentSlots;
    }

    protected int identify1() {
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
                   /// System.out.println("识别类别ID："+cid+"缺失\n");
                    output+="识别类别ID："+cid+"缺失\n";
                    changeMissingCids(cid);
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
               /// System.out.println("识别类别ID："+cid+"存在\n");
                output+="识别类别ID："+cid+"存在\n";
                presentCids.add(cid);

                num++;
                actualCidNum--;

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

    protected int randomIdentificationPhase() {
        int random = (int) (100 * Math.random());
        int frameSize = f;
        allocate1(frameSize,random);

        int num = identify1();

        return num;
    }

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





    protected void allocate2() {
        /*
        重写：hxq, 2022-3-5
        重新分配，依据xindex分配，将冲突时隙中的标签按照第一个冲突位为0或1分为两部分，分别选择两个时隙
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
    }

    protected int rearrangedIdentificationPhase() {
        int num = 0;

        allocate2();
        num = identify1();
        return num;
    }


    @Override
    public double identifyAll() {
        // 记录统计信息
        // 未能识别类别ID的轮次数目
        int repeated = 0;
        // 识别轮次
        int round = 1;
        // 随机识别阶段识别的类别ID数，每一次重新分配阶段识别的类别ID数
        int num1, num2;
        // 缺失率
        double missingRate;
        int unReadVirtualCidNum;
        // 存在的类别ID个数，丢失的类别ID个数
        int presentNum;
        int missingNum;
        double a; // optimize n/f
        // 每一轮中阅读器发送请求的时间，标签回应请求的时间
        double time1 = 0, time2 = 0;
        // 每一轮中阅读器发送请求的时间，标签回应请求的时间的集合
        List<Double> time1s = new ArrayList<>(); List<Double> time2s = new ArrayList<>();
        // 存储每一轮的时隙
        List<Integer> fs = new ArrayList<>();
        // 存储每一轮的空时隙，单时隙，冲突时隙个数（由于识别机制不同，不需要象ECIP那样统计“能识别两个类别ID的时隙”）
        int empty = 0, single = 0,  collision = 0;

        List<Integer> emptySlots = new ArrayList<>();
        List<Integer> singleSlots = new ArrayList<>();
        List<Integer> collisionSlots = new ArrayList<>();

        // 存储空时隙，单时隙，能识别两个类别ID的时隙，冲突时隙总个数
        int totalEmp = 0, totalSing = 0, totalCollision = 0;
        // 存储每一轮的缺失率
        List<Double> missingRates = new ArrayList<>();


        System.out.println("round "+round);
        output+="第 "+round+" 轮开始（随机分配阶段）！\n";

        // 优化时隙
        unReadVirtualCidNum = virtualCidNum;
        missingRate = 1 - actualCidNum*1.0/virtualCidNum;
        a = OptimizeECLS.optimize(missingRate); // n/f
        f = (int)Math.ceil(unReadVirtualCidNum * 1.0 /a);
        missingRates.add(missingRate);

        num1 = randomIdentificationPhase();
        virtualCidNum-=num1;

        fs.add(f);
        // 根据slotToTagList信息求每一轮的空时隙，单时隙，能识别两个类别ID的时隙，类别冲突时隙个数

        for(Integer slot : CidMap.keySet()) {
            String code = CidMap.get(slot);
            if(!code.contains("X")) {
                single ++;

            } else {
                collision++;
            }

        }
        empty = f - single -  collision;

        totalEmp += empty;
        totalSing += single;

        totalCollision += collision;

        emptySlots.add(empty);
        singleSlots.add(single);

        collisionSlots.add(collision);

        // 计算时间
        // 第一部分：阅读器发送请求的时间
        time1= (empty+single)*tid/48+(f-empty-single)*tid/96;
        // 第二部分：标签回复短消息的时间
        time2=(f-empty)*0.4;
        time1s.add(time1);
        time2s.add(time2);

        time+=time1;

        time+= time2;

        empty = single  = collision = 0;

        System.out.println("identify cids in random identification phase: "+num1);
        output+="在第 "+round+" 轮（随机分配阶段），共识别 "+num1+" 个类别ID\n\n";


        if(num1==0) repeated++;

        while(flag) {
            flag = false;
            round++;
            System.out.println("round " + round);
            output += "第 " + round + " 轮开始（重新分配阶段）！\n";

            // 重排识别阶段，时隙数为新的CidMap的大小（因为没有空时隙），不需要优化时隙
//

            missingRate = 1 - actualCidNum * 1.0 / virtualCidNum;
            missingRates.add(missingRate);

            num2 = rearrangedIdentificationPhase();
            virtualCidNum -= num2;


            f = CidMap.size();
            fs.add(f);
            // 根据slotToTagList信息求每一轮的空时隙，单时隙，能识别两个类别ID的时隙，类别冲突时隙个数
            for (Integer slot : CidMap.keySet()) {
                String code = CidMap.get(slot);
                if (!code.contains("X")) {
                    single++;

                } else{
                    collision++;
            }
        }



            empty = f - single  - collision;


            totalEmp += empty;
            totalSing += single;

            totalCollision += collision;

            emptySlots.add(empty);
            singleSlots.add(single);

            collisionSlots.add(collision);

            // 计算时间
            // 第一部分：阅读器发送请求的时间
            time1= (empty+single)*tid/48+(f-empty-single)*tid/96;
            // 第二部分：标签回复短消息的时间
            time2=(f-empty)*0.4;
            time1s.add(time1);
            time2s.add(time2);

            time+=time1;

            time+= time2;

            empty = single = collision = 0;




           /// System.out.println("identify cids in rearranged identification phase: " + num2);
            output += "在第 " + round + " 轮（重新分配阶段），共识别 " + num2 + " 个类别ID\n\n";


           /// System.out.println(" ");
            if (num2 == 0) repeated++;

            if (repeated >= 32) {//因为未识别任何cid的轮次过多而提前停止

                break;
            }
        }

        System.out.println("frame:"+fs);
        System.out.println("empty:"+emptySlots+" single:"+singleSlots+" collision:"+collisionSlots);
        System.out.println("missing rate:"+missingRates);

             presentNum = presentCids.size();
             missingNum = missingCids.size();
            int misidentification = 0;

            if(repeated < 32) { // 全部识别
                output+="识别结束！\n";
                output+="需要识别的类别ID数目："+(virtualCidNum)+", 识别存在的类别ID数量："+presentNum+", 准确率：100%， 识别缺失的类别ID数目："+missingNum+", 准确率：100%"+", 需要时间约： "+ String.format("%.4f", time*1.0/1000) + " s\n";
                output+="模拟结束！\n";
                analysis+="需要识别的类别ID数目："+(virtualCidNum)+", 识别存在的类别ID数量："+presentNum+", 准确率：100%， 识别缺失的类别ID数目："+missingNum+", 准确率：100%"+", 需要时间约： "+String.format("%.4f", time*1.0/1000) + " s\n";

            } else { // 部分识别，因为未识别任何cid的轮次过多而停止
                output+="由于冲突时隙，未能识别类别ID的轮次过多，提前停止！可能影响准确率！";
                output+="需要识别的类别ID数目："+(virtualCidNum)+", 识别存在的类别ID数量："+presentNum+", 准确率：100%， 识别缺失的类别ID数目："+missingNum+", 准确率："+(misidentification*1.0/missingNum)+", 需要时间约： "+String.format("%.4f", time*1.0/1000) + " s\n";
                output+="模拟结束！\n";
                analysis+="需要识别的类别ID数目："+(virtualCidNum)+", 识别存在的类别ID数量："+presentNum+", 识别缺失的类别ID数目："+missingNum+", 准确率：100%"+", 需要时间约： "+String.format("%.4f", time*1.0/1000) + " s\n";
            }

        analysis+="识别存在的类别ID有"+presentNum+"个，分别为：\n";
        for(String cid : presentCids) {
            analysis+=cid+"\n";
        }

        analysis+="识别缺失的类别ID有"+missingNum+"个，分别为：\n";
        for(String cid : missingCids) {
            analysis+=cid+"\n";
        }

        System.out.println("time1:"+time1s);
        System.out.println("time2:"+time2s);
            return time;



    }



}
