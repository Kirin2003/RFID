package protocals;

import base.Tag;
import org.apache.logging.log4j.Logger;
import utils.*;

import java.util.*;

/**
 * @author Kirin Huang
 * @date 2022/8/8 下午10:19
 */
public class CIP extends IdentifyTool{
    /**
     * 哈希函数的个数, 用于意外标签去除阶段
     */
    int numberOfHashFunctions = 1;
    /**
     * 假阳性误报率, 即意外标签通过成员检查的比率
     */
    double falsePositiveRatio = 0.01;

    /**
     * 键为时隙, 值为选择该时隙的真实存在的标签的类别id编码后的数据
     */
    protected Map<Integer, String> CidMap = new HashMap<>();
    /**
     * 键为时隙, 值为选择该时隙的真实存在的标签列表
     */
    protected Map<Integer,List<Tag>> slotToTagList = new HashMap<>();


    /**
     * CIP构造函数
     * @param logger 记录算法运行中的信息, 便于调试
     * @param recorder 记录器, 记录算法输出结果
     * @param environment 环境,里面有标签的数目,标签id和类别id列表,位置等信息和阅读器的数目,位置等信息
     */
    public CIP(Logger logger, Recorder recorder, Environment environment) {
        super(logger, recorder, environment);

    }

    /**
     * CIP算法执行的入口
     * 多阅读器场景
     * 第一阶段, 所有阅读器同时工作, 去除意外标签, 等待所有阅读器工作完毕再进行下一阶段, 这样意外标签去除的多, 对下一阶段干扰的就少
     * 第二阶段, 所有阅读器同时工作, 识别存在标签, 所有阅读器工作完毕后, 所有阅读器识别的存在标签之和是存在标签, 不再存在标签中的是缺失标签
     */
    @Override
    public void execute() {
        List<Reader_M> readers = environment.getReaderList();

        /**
         * 第一阶段, 意外标签去除阶段
          */
        unexpectedTagElimination();

        // 第一阶段的时间: 所有阅读器的执行时间中最长的作为第一阶段的时间
        double maxTime = 0;
        for(Reader_M reader_m : readers) {
            double t1 = reader_m.recorder.totalExecutionTime;
            if(t1 > maxTime) {
                maxTime = t1;
            }
        }
        recorder.totalExecutionTime = maxTime;
        logger.error("第一阶段结束, 所有阅读器的总时间:[ "+maxTime+" ]ms");
        for(Reader_M reader_m : readers) {
            reader_m.recorder.totalExecutionTime = maxTime;
        }

        /**
         * 第二阶段, 识别存在的类别和缺失的类别的阶段
          */
        identify();

        // 第二阶段所有阅读器的执行时间中最长的作为第二阶段的时间
        double maxTime2 = 0;
        for(Reader_M reader_m : readers) {
            double t1 = reader_m.recorder.totalExecutionTime;
            if(t1 > maxTime2) {
                maxTime2 = t1;
            }
        }
        recorder.totalExecutionTime = maxTime2;
        logger.error("第二阶段结束, 所有阅读器的总时间:[ "+maxTime2+" ]ms");

        for(Reader_M reader : readers) {
            recorder.actualCids.addAll(reader.recorder.actualCids);
        }

        for(Tag tag : environment.getExpectedTagList()) {
            String cid = tag.getCategoryID();
            if(!recorder.actualCids.contains(cid)){
                recorder.missingCids.add(cid);
            }
        }
    }


    /**
     * 第一阶段, 意外标签去除阶段, 使用布隆过滤器
     */
    public void unexpectedTagElimination() {
        UnexpectedTagEliminationMethod.BloomFilterMethod(numberOfHashFunctions, falsePositiveRatio,environment,logger);

    }

    /**
     * 第二阶段, 识别类别阶段(多阅读器)
     */
    public void identify() {
        for (Reader_M reader : environment.getReaderList()) {
            logger.error("<<<<<<<<<<<<<<<<<<<< Reader: " + reader.getID() + " >>>>>>>>>>>>>>>>>>>");
            // 每一次需要reset(),将环境中所有预期标签设为活跃, 因为有些标签在其他阅读器中识别为缺失, 但在某个阅读器中识别为存在, 这个标签是存在的
            environment.reset();
            identify( reader);
        }
    }


    /**
     * 第二阶段, 识别类别阶段(每个阅读器识别它范围内的类别)
     * @param reader_m 阅读器
     */
    public void identify(Reader_M reader_m) {
        Recorder recorder1 = reader_m.recorder;
        int repeated = 0; // 没有识别到一个标签类别的轮数
        int f1 = 0; // 时隙
        int expectedTagNum = environment.getExpectedTagList().size();// 预期标签数目
        int readCidNumInOneRound = 0;
        Map<Integer, String> CidMap = new HashMap<>(); // 键为时隙, 值为编码之后的标签id
        Map<Integer, List<Tag>> slotToTagList = new HashMap<>(); // 键为时隙, 值为在这个时隙回应的标签列表(是存在的标签)
        List<Tag> actualTagList = environment.getActualTagList();
        boolean flag = true; // 是否循环
        Set<String> expectedCidSet = new HashSet<>();
        Set<String> actualCidSet = new HashSet<>();
        for(Tag tag : environment.getExpectedTagList()){
            expectedCidSet.add(tag.getCategoryID());
        }
        int unReadCidNum = expectedCidSet.size();
        for(Tag tag : reader_m.getCoverActualTagList()){
            actualCidSet.add(tag.getCategoryID());
        }
        int actualCidNum = actualCidSet.size();
        logger.info("期望识别的类别数:[ "+unReadCidNum+" ],实际存在的类别数:[ "+actualCidNum+" ],缺失的类别数:[ "+(unReadCidNum-actualCidNum)+" ]");
        double missRate = 1-actualCidNum*1.0/unReadCidNum;
        recorder1.missingRateList.add(missRate);

        while(flag) {
            logger.info("################### 第 " + recorder1.roundCount + " 轮#######################");
            flag = false;



            /**
             * 1 优化时隙
              */
            f1 = optimizeFrameSize(unReadCidNum);
            recorder1.roundSlotCountList.add(f1);
            logger.info("-----------------------优化时隙-----------------------");
            logger.info("本轮最优帧长:["+f1+"]");

            /**
             * 2 标签选择时隙
              */
            int random = (int)(Math.random());
            for(Tag tag:actualTagList) {
                if(tag.isActive()) {
                    tag.hash2(f1,random);
                    // 同时, 构造CidMap和slotToTagList, 存入的是完整的类别id
                    int slot = tag.getSlotSelected();
                    String cid = tag.categoryID;
                    if(!CidMap.containsKey(slot)) {
                        CidMap.put(slot,cid);
                        List<Tag> tagList = new ArrayList<>();
                        tagList.add(tag);
                        slotToTagList.put(slot, tagList);
                    } else {
                        String newData = encode(CidMap.get(slot),cid);
                        CidMap.put(slot, newData);
                        slotToTagList.get(slot).add(tag);
                    }
                }
            }

            int empty = f1 - CidMap.size();
            double executionTimeOneRound = empty * 0.4 + CidMap.size() * 1.2;
            recorder1.totalExecutionTime += executionTimeOneRound;
            recorder1.executionTimeList.add(executionTimeOneRound);

            List<Integer> slots = new ArrayList<>(CidMap.keySet());
            slots.sort((integer, t1) -> integer > t1 ? 1 : -1);


            /**
             * 3 识别
              */
            logger.info("-----------------------识别结果-----------------------");
            for(Integer slotId : slots) {
                String[] strs = decodeCID(CidMap.get(slotId));
                int l = strs.length;

                // 可以识别类别的时隙, category-compatible slot
                if(l == 1) {
                    for (Tag tag : slotToTagList.get(slotId)) {
                        tag.setActive(false);
                    }
                    recorder1.actualCids.add(strs[0]);
                    readCidNumInOneRound++;
                    logger.info("时隙: ["+slotId+"] 识别存在的类别: "+strs[0]);
                } else if (l == 2) {
                    for (Tag tag : slotToTagList.get(slotId)) {
                        tag.setActive(false);
                    }
                    recorder1.actualCids.add(strs[0]);
                    recorder1.actualCids.add(strs[1]);
                    readCidNumInOneRound += 2;
                    logger.info("时隙: ["+slotId+"] 识别存在的类别: ["+strs[0]+", "+strs[1]+"]");
                } else {
                    // 有冲突时隙
                    flag = true; // 需要新一轮识别
                    logger.info("时隙: ["+slotId+"] 冲突时隙!");
                }
            }

            actualCidNum -= readCidNumInOneRound;
            missRate = 1-actualCidNum*1.0/unReadCidNum;
            recorder1.missingRateList.add(missRate);

            recorder1.recognizedActualCidNumList.add(readCidNumInOneRound);
            recorder1.recognizedCidNumList.add(readCidNumInOneRound);
            recorder1.recognizedMissingCidNumList.add(0); // cip只识别存在标签,不识别缺失标签,最后没有识别到的标签都认为是缺失标签
            recorder1.recognizedActualCidNum += readCidNumInOneRound;
            recorder1.recognizedCidNum += readCidNumInOneRound;
            logger.info("第 ["+recorder1.roundCount+"] 轮,识别到的存在的类别数: ["+readCidNumInOneRound + "] 缺失的类别数: [0], 花费的时间:["+executionTimeOneRound+"] ms");
            unReadCidNum -= readCidNumInOneRound;
            // 清零, 以便继续循环识别标签类别
            recorder1.roundCount ++;
            readCidNumInOneRound = 0;
            CidMap.clear();
            slotToTagList.clear();

            if(readCidNumInOneRound == 0) {
                repeated ++;
            }

            // 为防止死循环, 当未识别任何cid的轮次过多时停止
            if(repeated >= 2) {
                break;
            }

        }

        /**
         * 最后一轮识别完毕后, 所有没有被识别为存在的标签类别认为不存在
          */
        int missingCidNum = 0; // 记录缺失的类别数目
        for(Tag tag : environment.getExpectedTagList()) {
            String cid = tag.getCategoryID();
            if(!recorder1.actualCids.contains(cid)) {
                recorder1.missingCids.add(cid);
                changeMissingCids(cid);

            }
        }
        missingCidNum = recorder1.missingCids.size();
        recorder1.recognizedMissingCidNum +=missingCidNum;
        recorder1.recognizedCidNum += missingCidNum;
        logger.info("最后一轮识别结束, 所有没有被识别为存在的标签类别认为不存在, 识别缺失的类别数: ["+missingCidNum + "]");
        recorder1.recognizedMissingCidNumList.set(recorder1.roundCount-1,missingCidNum);
        int recognizedCidNumLastRound = recorder1.recognizedCidNumList.get(recorder1.roundCount-1)+recorder1.missingCids.size();
        recorder1.recognizedCidNumList.set(recorder1.roundCount-1,recognizedCidNumLastRound);

        // 总时间
//        recorder1.totalExecutionTime = calculateTime(recorder1.recognizedCidNum);
        logger.error("总时间: [" + recorder1.totalExecutionTime+ " ms]");


    }


    /**
     * 计算cip每一轮的理论时间或总时间
     * @param readCidNum 该轮识别到的cid数或所有轮次识别到的cid总数
     * @return 理论时间
     */
    public static double calculateTime(int readCidNum) {
        if(readCidNum > 0) {
            return 2.31 * readCidNum;
        } else {
            // 9 * 0.4 + 1.2
            return 4.8;
        }
    }

    public int optimizeFrameSize(int unReadCidNum) {
        int f1 = (int)(Math.ceil(1.53*unReadCidNum));
        return Math.max(f1,10);
    }

    /**
     * 对cid编码
     * @param s1 cid1
     * @param s2 cid2
     * @return 编码后的cid (overlapped cid)
     */

    private String encode(String s1, String s2){
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
     * 使用曼彻斯特编码来解码时隙中得到的编码数据
     * @param data 时隙中得到的编码数据
     * @return 解码得到的cid, 如果是空时隙或无法识别cid的时隙, 返回空的数组, 如果是单时隙, 返回包含一个cid的数组, 如果是多时隙, 返回包含两个cid的数组
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



}
