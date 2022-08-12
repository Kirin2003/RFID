package protocals;

import base.SlotResponse;
import base.Tag;
import org.apache.logging.log4j.Logger;
import utils.*;

import java.util.*;

/**
 * @author Kirin Huang
 * @date 2022/8/8 下午10:19
 */
public class EDLS extends IdentifyTool{
    /**
     * 哈希函数的个数, 用于意外标签去除阶段
     */
    int numberOfHashFunctions = 1;
    /**
     * 假阳性误报率, 即意外标签通过成员检查的比率
     */
    double falsePositiveRatio = 0.01;


    /**
     * EDLS构造函数
     * @param logger 记录算法运行中的信息, 便于调试
     * @param recorder 记录器, 记录算法输出结果
     * @param environment 环境,里面有标签的数目,标签id和类别id列表,位置等信息和阅读器的数目,位置等信息
     */
    public EDLS(Logger logger, Recorder recorder, Environment environment) {
        super(logger, recorder, environment);

    }

    /**
     * EDLS算法执行入口
     * 多阅读器场景
     * 第一阶段, 所有阅读器同时工作, 去除意外标签, 等待所有阅读器工作完毕再进行下一阶段, 这样意外标签去除的多, 对下一阶段干扰的就少
     * 第二阶段, 所有阅读器同时工作, 识别存在标签, 所有阅读器工作完毕后, 所有阅读器识别的存在标签之和是存在标签, 不再存在标签中的是缺失标签
     */
    @Override
    public void execute(){
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
            logger.error("<<<<<<<<<<<<<<<<<<<< Reader: " + reader + " >>>>>>>>>>>>>>>>>>>");
            // 每一次需要reset(),将环境中所有预期标签设为活跃, 因为有些标签在其他阅读器中识别为缺失, 但在某个阅读器中识别为存在, 这个标签是存在的
            environment.reset();
            identify( reader);
        }
    }

    /**
     * 第二阶段, 识别类别阶段(每个阅读器识别它范围内的类别)
     * @param reader_m 阅读器
     */
    public void identify(Reader_M reader_m){
        Recorder recorder1 = reader_m.recorder;
        // 第几轮
        recorder1.roundCount ++;
        double missRate = (double) (environment.getExpectedTagList().size() - environment.getActualTagList().size()) / environment.getExpectedTagList().size();

        // 期望的标签列表, 可能有缺失；每个阅读器只覆盖了一个小范围, 但期望的标签列表是整个仓库的
        List<Tag> expectedTagList = environment.getExpectedTagList();
        int expectedTagNum = expectedTagList.size();

        List<Tag> coveredActualTagList = reader_m.coverActualTagList;

        boolean useCLS; // 是否使用CLS
        int recognized = 0; // 总共识别的数目
        int recognizedCurrentRound = 0; // 在一轮中识别的数目
        int round = 0; // 轮数
        int filterVectorLength = 0; // filter vector长度
        int slotSumNum = 0; // 时隙数目

        // filter generation phase之前, 时隙中期望标签为0,1,2,3,>3的个数
        int[] beforeFilter = new int[]{0, 0, 0, 0, 0};
        // SFMTI : filter generation phase之后, 空时隙和未解决冲突的时隙的数目, 原本就是单时隙的时隙数目, 已经解决冲突的的2-collision个数,已经解决冲突的的3-collision个数,>3的冲突时隙个数
        // CLS: filter generation phase之后, 空时隙, 单时隙, 2-collision时隙, 3-collision时隙, >3的冲突时隙个数
        // SFMTI和CLS的一个区别就是:SFMTI有调和冲突, CLS没有!
        int[] afterFilter = new int[]{0, 0, 0, 0, 0};
        // 实际回复的, 只能识别空时隙, 单时隙, 冲突时隙, 因此分别记录0,1,>=2的
        int[] realReply = new int[]{0, 0, 0};

        int twoTurnToZero = 0;
        int threeTurnToZero = 0;
        int twoTurnToOne = 0;
        int twoTurnToTwo = 0;
        int threeTurnToOne = 0;
        int threeTurnToTwo = 0;
        int threeTurnToThree = 0;

        int resolvedFromTwoCollision = 0;
        int resolvedFromThreeCollision = 0;
        int recognizedMissRound = 0;


        Map<Integer, List<Tag>> collisionTagListMap = new HashMap<>(); // 冲突时隙的时隙-标签列表映射
        int collisionTagListIndex = 0;
        // 识别为缺失的标签
        List<Tag> recognizedMissingTag = new ArrayList<>();

        logger.info("recognizedMissingTag = [" + recognizedMissingTag.size() + "]; tagNum = [" + expectedTagNum + "]; ReaderCoveringTagListSize = [" + coveredActualTagList.size() + "]; recognized = [" + recognized + "]; missingRatio = [" + missRate + "]");
        while (recognized < expectedTagNum) { // 当识别数目小于期望标签数目时一直循环
            ++round;
            System.out.println();
            logger.info("################### ROUND: " + round + " #######################");
            System.out.println();

            // 在本轮中识别的数目
            recognizedCurrentRound = 0;

            // CLS需要一个随机数, SFMTI需要两个随机数
            int random1 = (int) (100 * Math.random());
            int random2 = (int) (100 * Math.random());

            int frameSize = 0;
            double rho = 0;

            // optimize frame size
            logger.error("missRate : " + missRate);
            
            if (missRate > 0.679){ // 缺失率>0.679, 使用cls, 优化时隙
                rho = RHOUtils.getBestRho(missRate,"rho-opt-TSA.txt");

                frameSize = (int)Math.ceil(((double)(expectedTagNum - recognized)) / rho);
                useCLS = true;
            }else{ // 缺失率<=0.679, 使用SFMTI, 优化时隙
                frameSize = (int)Math.ceil(((double)(expectedTagNum - recognized)) / 1.68);
                logger.debug("tagNum="+expectedTagNum+";frameSize ="+frameSize);
                useCLS = false;
            }

            logger.info("----------------Generating Vector--------------");
            logger.info("frameSize = ["+frameSize + "] random1 = ["+random1 + "] random2 = ["+random2 + "]");
            List<Integer> filterVector;
            if (useCLS){ // 使用cls算法生成filter vector
                filterVector = this.CLS_genFilterVector(frameSize, random1, expectedTagList, beforeFilter, afterFilter, recorder1);
                logger.warn("use CLS");
            }else { // 使用sfmti算法生成filter vector
                filterVector = this.SFMTI_genFilterVector(frameSize, random1, random2, expectedTagList, beforeFilter, afterFilter, recorder1);
                logger.warn("use SFMTI");
            }

            // X是解决了冲突的冲突时隙和单时隙
            int X = 0;
            for (int i = 0; i < frameSize; i++){
                if (filterVector.get(i) > 0)
                    ++X;
            }

            // expMap 顾名思义 期望标签的映射, 键为时隙, 值为标签列表
            Map<Integer, List<Tag>> expMap = new HashMap<>();
            for (Tag tag : expectedTagList) {
                if(tag.isActive()){
                    if (useCLS)
                        CLS_SelectSlot(tag, random1, frameSize, filterVector, expMap, null);
                    else
                        SFMTI_SelectSlot(tag, random1, random2, frameSize, filterVector, X, expMap, null);
                }
            }

            logger.info("-----------------------Verify-----------------------");
            logger.debug("X: "+X);

            //Get the Map of the Tag to the corresponding tagList slot, so that it can be sorted and
            //displayed in ascending order according to the size of the slot when verifying.
            // 键为filter vector的索引, 值为阅读器范围内的标签id和选择的时隙的列表,
            Map<Integer, List<String>> verMap = new TreeMap<>();
            for (Tag tag : coveredActualTagList) {
                if(tag.isActive()){
                    if (useCLS)
                        CLS_SelectSlot(tag, random1, frameSize, filterVector, null, verMap);
                    else
                        SFMTI_SelectSlot(tag, random1, random2, frameSize, filterVector, X, null, verMap);
                }
            }

            logger.debug("tagListSort output (sort in ascending order according to the size of the first Slot)：");
            // 打印verMap
            Iterator vit = verMap.keySet().iterator();
            while (vit.hasNext()){
                Integer key = (Integer) vit.next();
                List<String> strList = verMap.get(key);
                for (int i = 0; i < strList.size(); i++)
                    logger.debug(strList.get(i));
            }

            // 键: 时隙, 值: 回应的标签列表, 只有存在的标签, 没有缺失的标签
            // resultMap与verMap的不同在于, verMap是标签的tagId(字符串)列表, resultMap是标签列表
            Map<Integer, List<Tag>> resultMap = new HashMap<>();
            for (Tag tag: coveredActualTagList) {
                if(tag.isActive()){
                    int nIndex = tag.getSlotSelected();
//                    logger.info("[inner] tagID: "+tag.getTagID()+" Slot: "+tag.getSlotSelected());
                    if (resultMap.containsKey(nIndex)){
                        resultMap.get(nIndex).add(tag);
                    }else {
                        List<Tag> nTagList = new ArrayList<>();
                        nTagList.add(tag);
                        resultMap.put(nIndex, nTagList);
                    }
                }
            }

            // 打印resultMap
            logger.debug("active true's in tagList：");
            Iterator it = resultMap.keySet().iterator();
            while (it.hasNext()){
                Integer key = (Integer) it.next();
                List<Tag> value = resultMap.get(key);
                List<String> tagstr = new ArrayList<>();
                for (int i = 0; i < value.size(); i++)
                    tagstr.add(value.get(i).getTagID());
                logger.debug("Slot = "+ key + " tagID = "+tagstr.toString());
            }

            logger.info("----------------------Identification Results-------------------------");
            int slotLength = filterVector.size();
            int roundSlotCount = 0;
            int value0 = 0; // value0表示空时隙的数目

            for (int i = 0; i < slotLength; i++){
                if(filterVector.get(i) != 0) { // 期望中有标签回应
                    roundSlotCount++;
                    slotSumNum++;
                    // 有回应的时隙列表
                    List<SlotResponse> slotResponseList = new ArrayList<>();
                    for (Tag tag : coveredActualTagList) {

                        SlotResponse slotResponse = tag.executeSlot((i-value0));
                        if (slotResponse != null) {
                            slotResponseList.add(slotResponse);
                        }
                    }

                    recorder1.slotCount ++;
                    // 分析实际得到的回应情况
                    if (slotResponseList.size() == 0) { // 有0个标签回应
                        realReply[0]++;
                        if(filterVector.get(i) == 2){ // Unreconcilable 2 Collisions
                            twoTurnToZero++;
                        }else if (filterVector.get(i) == 3){ // Unreconcilable 3 Collisions
                            threeTurnToZero++;
                        }
                        // 预期有标签回应(filterVector.get(i) > 0), 实际没有标签回应(slotResponseList.size() == 0), 全部缺失!
                        List<Tag> tmplist = expMap.get(i-value0);
                        List<String> str = new ArrayList<>();
//                        System.out.println("tmplist == null?"+(tmplist == null));
                        for (Tag tag : tmplist){
                            str.add(tag.getTagID());
                            recognizedCurrentRound++;
                            tag.setActive(false);
                        }
                        recognizedMissingTag.addAll(tmplist);
                        recognizedMissRound = tmplist.size();
                        logger.debug("slotID=" + (i-value0) + " slotResult=empty, missing tag="+str.toString());
                        recorder1.emptySlotCount ++; //统计为空Slot的总数

                    } else if (slotResponseList.size() == 1) { // 有1个标签回应
                        realReply[1]++;
                        if (useCLS){
                            if(filterVector.get(i) == 1){ // use CLS, 预期是1, 结果是1
                                recorder1.singletonSlotCount ++;
                                logger.debug("slotID=" + (i-value0) + " slotResult=singleton" + slotResponseList.get(0).getContent());
                                slotResponseList.get(0).getTag().setActive(false);
                                recognizedCurrentRound++;
                            }else{ // use CLS, 预期是1, 结果大于1, 无法识别

                                if(filterVector.get(i) == 2){ // 2 changed to 1
                                    twoTurnToOne++;
                                }else if (filterVector.get(i) == 3){ // 3 changed to 1
                                    threeTurnToOne++;
                                }
                                List<Tag> tmplist = expMap.get(i-value0);
                                List<String> str = new ArrayList<>();
                                for (Tag tag : tmplist){
                                    if(!tag.getTagID().equals(slotResponseList.get(0).getContent()))
                                        str.add(tag.getTagID());
                                }
                                recorder1.noResult ++;
                                logger.debug("slotID=" + (i-value0) + " slotResult=noResult " + slotResponseList.get(0).getContent()+":others="+str);
                                collisionTagListMap.put(collisionTagListIndex++, tmplist);

                            }
                        }else{ // use SFMTI, 只有一个标签回应必然是单时隙, 不可能是有缺失的冲突时隙, 因为SFMTI已经解决冲突了, 没解决的设置为filterVector.get(i) == 0
                            slotResponseList.get(0).getTag().setActive(false);
                            recognizedCurrentRound++;
                            logger.debug("slotID="+(i-value0) + " slotResult=single "+slotResponseList.get(0).getContent());
                            recorder1.singletonSlotCount ++;
                        }
                    } else { // 有多个标签回应
                        realReply[2]++;
                        if(filterVector.get(i) == 2 && slotResponseList.size() == 2){
                            twoTurnToTwo++; // 2->2
                        }else if(filterVector.get(i) == 3 && slotResponseList.size() == 2){
                            threeTurnToTwo++;  // 3->2
                        }else if(filterVector.get(i) == 3 && slotResponseList.size() == 3){
                            threeTurnToThree++;  // 3->3
                        }

                        // 打印slotResponseList
                        String strTag = "";
                        for (SlotResponse slot : slotResponseList) {
                            strTag += slot.getTag().getTagID() + ":";
                        }

                        logger.debug("slotID=" + (i-value0) + " slotResult=collision tagID=" + strTag);
                        recorder1.collisionSlotCount++; //统计为collision的总数

                        // 期望标签的映射, 即便有缺失的, 不知道是哪个缺失, 这里把期望标签全部记录到了冲突时隙-标签列表中
                        List<Tag> tmplist = expMap.get(i-value0);
                        collisionTagListMap.put(collisionTagListIndex++, tmplist); // 冲突时隙-标签列表

                    }

                }else{ // 期望中没有标签回应
                    // 空时隙
                    value0++;
                }
            }

            logger.info("Round's Filter Vector Length: " + filterVector.size());
            logger.info("After round fliterVector total length: " + filterVectorLength);
            logger.info("After round Resolved from 2 collision: " + resolvedFromTwoCollision);
            logger.info("After round resolved from 3 collision:" + resolvedFromThreeCollision);
            logger.info("Round's slot number: " + roundSlotCount);
            logger.info("After round slot number: " + slotSumNum);

            recorder1.recognizedTagNum +=recognizedCurrentRound;
            recognized+=recognizedCurrentRound;

            if(expectedTagNum > recognized) {
                missRate = 1.0*((expectedTagNum - recognized) - (coveredActualTagList.size() - (recognized-recognizedMissingTag.size()))) / (expectedTagNum - recognized);
            }
            System.out.println("Reader miss rate: "+missRate);
            System.out.println("Frame Size: " + frameSize+"; Filter Size: " + filterVector.size() + ";");
            System.out.println("Recognized missing tag: "+recognizedMissingTag.size()+"; Number of tags present: "+(recognized-recognizedMissingTag.size()) + ";");

            realReply[0]=0;
            realReply[1]=0;
            realReply[2]=0;
        }
        logger.info("----------------------------------------");
        logger.info("Slot types before filter：0:["+beforeFilter[0]+"个] 1:["+beforeFilter[1]+"个]"+" 2:["+beforeFilter[2]+"个] 3:["+beforeFilter[3]+"个] >3:["+beforeFilter[4]+"个]");
        logger.info("Slot types after filter 0:["+afterFilter[0]+"个] 1:["+afterFilter[1]+"个]"+" 2:["+afterFilter[2]+"个] 3:["+afterFilter[3]+"个] >3:["+afterFilter[4]+"个]");

        logger.info("2->0：twoTurnToZero: "+twoTurnToZero);
        logger.info("3->0：threeTurnToZero: "+threeTurnToZero);
        logger.info("2->1：twoTurnToOne: "+twoTurnToOne);
        logger.info("2->2：twoTurnToTwo: "+twoTurnToTwo);
        logger.info("3->1：threeTurnToOne: "+threeTurnToOne);
        logger.info("3->2：threeTurnToTwo: "+threeTurnToTwo);
        logger.info("3->3：threeTurnToThree: "+threeTurnToThree);
        logger.info("2-->：resolvedFromTwoCollision: "+ resolvedFromTwoCollision);
        logger.info("3-->：resolvedFromThreeCollision: "+ resolvedFromThreeCollision);
        logger.info("Total vector length：FILTER_VECTOR_LENGTH: "+ filterVectorLength);
        logger.error("TOTAL EXECUTION TIME: [" + recorder1.totalExecutionTime+ " ms]");

        
    }

    private void SFMTI_SelectSlot(Tag tag, int random1, int random2, int frameSize, List<Integer> filterVector, int X, Map<Integer, List<Tag>> expMap, Map<Integer, List<String>> verMap) {
        int index = tag.hash2(frameSize, random1);
        int x1 = 0;
        int x2 = 0;
        int x3 = 0;
        for (int i = 0; i < index; i++){
            if (filterVector.get(i) == 1){
                x1++;
            }else if(filterVector.get(i) == 2){
                x2++;
            }else if(filterVector.get(i) == 3){
                x3++;
            }
        }
        int sumX = x1 + x2 + x3;
        String tmpStr;
        switch (filterVector.get(index)){
            case 0:
                tmpStr = "[inner]slot = "+index+" 对应vector中的值 = 0 (则afterSlot直接赋值 -1)"+" x1 = " + x1 + " X2 = "+x2 +"  X3 = "+x3 + " afterSlot = -1 tagID = " + tag.getTagID();
                tag.fillMap(expMap, -1, tag);
                tag.fillMap(verMap, index, tmpStr);
                tag.setSlotSelected(-1);
                break;
            case 1:
                tmpStr = "[inner]slot = "+index+" 对应vector中的值 = 1 (则afterSlot应等于x1 + x2 + x3)"+" x1 = " + x1 + " X2 = "+x2 +"  X3 = "+x3 + " afterSlot = "+sumX+" tagID = " + tag.getTagID();
                tag.fillMap(expMap, sumX, tag);
                tag.fillMap(verMap, index, tmpStr);
                tag.setSlotSelected(sumX);
                break;
            case 2:
                int indexApp2 = tag.hash2(2,  random2);
                if (indexApp2 == 0){
                    tmpStr = "[inner]slot = "+index+" 对应vector中的值 = 2 secondSlot = 0"+ " (则afterSlot应等于x1 + x2 + x3)"+" x1 = " + x1 + " X2 = "+x2 +"  X3 = "+x3 + " afterSlot = "+sumX+" tagID = " + tag.getTagID();
                    tag.fillMap(expMap, sumX, tag);
                    tag.fillMap(verMap, index, tmpStr);
                    tag.setSlotSelected(sumX);
                }else {
                    int tmpSlot = X + x2 + x3 * 2;
                    tmpStr = "[inner]slot = "+index+" 对应vector中的值 = 2 secondSlot = "+indexApp2+ " (则afterSlot应等于X + x2 + x3 * 2)"+" x1 = " + x1 + " X2 = "+x2 +"  X3 = "+x3 + " afterSlot = "+tmpSlot+" tagID = " + tag.getTagID();
                    tag.fillMap(expMap, tmpSlot, tag);
                    tag.fillMap(verMap, index, tmpStr);
                    tag.setSlotSelected(tmpSlot);
                }
                break;
            case 3:
                int indexApp3 = tag.hash2(3,  random2);
                if (indexApp3 == 0){
                    tmpStr = "[inner]slot = "+index+" 对应vector中的值 = 3 secondSlot = 0 (则afterSlot应等于x1 + x2 +x3)"+" x1 = " + x1 + " X2 = "+x2 +"  X3 = "+x3 + " afterSlot = "+sumX+" tagID = " + tag.getTagID();
                    tag.fillMap(expMap, sumX, tag);
                    tag.fillMap(verMap, index, tmpStr);
                    tag.setSlotSelected(sumX);
                }else {
                    int thrSlot = X + x2 + x3 * 2 + indexApp3 - 1;
                    tmpStr = "[inner]slot = "+index+" 对应vector中的值 = 3 secondSlot = "+indexApp3+" (则afterSlot应等于X + x2 + x3 * 2 + secondSlot - 1)"+" x1 = " + x1 + " X2 = "+x2 +"  X3 = "+x3 + " afterSlot = "+thrSlot+" tagID = " + tag.getTagID();
                    tag.fillMap(expMap, thrSlot, tag);
                    tag.fillMap(verMap, index, tmpStr);
                    tag.setSlotSelected(thrSlot);
                }
                break;
        }
    }

    private void CLS_SelectSlot(Tag tag, int random1, int frameSize, List<Integer> filterVector, Map<Integer, List<Tag>> expMap, Map<Integer, List<String>> verMap) {
        int index = tag.hash2(frameSize,random1);
        int x1 = 0;
        int x2 = 0;
        int x3 = 0;
        int x4 = 0;
        // 其实是跳过了空时隙的
        for (int i = 0; i < index; i++){

            if (filterVector.get(i) == 1){
                x1++;
            }else if(filterVector.get(i) == 2){
                x2++;
            }else if(filterVector.get(i) == 3){
                x3++;
            }else if(filterVector.get(i) > 3){
                x4++;
            }
        }
        int sumX = x1 + x2 + x3 + x4;
        String tmpStr = "";
        // generate filter vector phase, 从原始分配的时隙-标签映射到跳过了空时隙的时隙-标签映射列表
        switch (filterVector.get(index)){
            case 0:
                tmpStr = "[inner]slot = "+index+" 对应vector中的值 = 0 (则afterSlot直接赋值 -1) tagID = " + tag.getTagID();
                tag.fillMap(expMap, -1, tag);
                tag.fillMap(verMap, index, tmpStr);
                tag.setSlotSelected(-1);
                break;
            case 1:
                tmpStr = "[inner]slot = "+index+" 对应vector中的值 = 1 (则afterSlot应等于x1 + x2 + x3 + x4)"+" x1 = " + x1 + " X2 = "+x2 +"  X3 = "+x3 +"  X4 = "+x4 + " afterSlot = "+sumX+" tagID = " + tag.getTagID();
                tag.fillMap(expMap, sumX, tag);
                tag.fillMap(verMap, index, tmpStr);
                tag.setSlotSelected(sumX);
                break;
            case 2:
                tmpStr = "[inner]slot = "+index+" 对应vector中的值 = 2 (则afterSlot应等于x1 + x2 + x3 + x4)"+" x1 = " + x1 + " X2 = "+x2 +"  X3 = "+x3 +"  X4 = "+x4 + " afterSlot = "+sumX+" tagID = " + tag.getTagID();
                tag.fillMap(expMap, sumX, tag);
                tag.fillMap(verMap, index, tmpStr);
                tag.setSlotSelected(sumX);
                break;
            case 3:
                tmpStr = "[inner]slot = "+index+" 对应vector中的值 = 3 (则afterSlot应等于x1 + x2 +x3 + x4)"+" x1 = " + x1 + " X2 = "+x2 +"  X3 = "+x3 +"  X4 = "+x4 + " afterSlot = "+sumX+" tagID = " + tag.getTagID();
                tag.fillMap(expMap, sumX, tag);
                tag.fillMap(verMap, index, tmpStr);
                tag.setSlotSelected(sumX);
                break;
            default:
                tmpStr = "[inner]slot = "+index+" 对应vector中的值 > 3 (则afterSlot应等于x1 + x2 +x3 + x4)"+" x1 = " + x1 + " X2 = "+x2 +"  X3 = "+x3 +"  X4 = "+x4 + " afterSlot = "+sumX+" tagID = " + tag.getTagID();
                tag.fillMap(expMap, sumX, tag);
                tag.fillMap(verMap, index, tmpStr);
                tag.setSlotSelected(sumX);
                break;
        }
    }

    // 实现时, CLS 使用前后, filter vector没变, 论文中提到0->00, 1->01, >=2 -> 1, 实现时只在计算时间部分考虑了
    public List<Integer> CLS_genFilterVector(int frameSize, int random1, List<Tag> theTagList,int[] beforeFilter, int[] afterFilter, Recorder recorder){
        // 本轮标签将会有回应的时隙数量
        int responseNum = 0;

        VectorMap vectorMap = VectorMap.genBaseVectorMap(frameSize, random1, 0, theTagList);
        List<Integer> filterVector = vectorMap.getFilterVector();
        Map<Integer, List<Tag>> resultMap = vectorMap.getResultMap();
        // 统计在使用CLS前, filter vector中各类时隙有多少, 比如, 空时隙有多少, 单时隙有多少, 等等
        for (int i = 0; i <filterVector.size(); i++){
            int tmpValue = filterVector.get(i);
            if (tmpValue == 0) {
                beforeFilter[0]++;
            }
            else if (tmpValue == 1) {
                beforeFilter[1]++;
            }
            else if (tmpValue == 2) {
                beforeFilter[2]++;
            }
            else if (tmpValue == 3) {
                beforeFilter[3]++;
            }
            else if (tmpValue > 3) {
                beforeFilter[4]++;
            }
        }
        logger.info("Before using CLS. Slot types in the current round 0,1,2,3,>3: 0:["+beforeFilter[0]+"个] 1:["+beforeFilter[1]+"个]"+" 2:["+beforeFilter[2]+"个] 3:["+beforeFilter[3]+"个] >3:["+beforeFilter[4]+"个]");

        int len = filterVector.size();
        logger.info("Generating Filter Vector: ");
        // 打印filter vector的内容
        for (int k = 0; k < len; k++) {
            int element = filterVector.get(k);
            if (element >= 2) {
                List<String> tagStr = new ArrayList<>();
                for (int i = 0; i < resultMap.get(k).size(); i++)
                    tagStr.add(resultMap.get(k).get(i).getTagID());
                logger.debug("Slot = " + k + " tagID = " + tagStr.toString());
            } else if (element == 1) {
                if (resultMap.get(k).size() != 1) {
                    logger.error("ERROR！");
                    logger.info("ERROR！");
                }
                logger.debug("Slot = " + k + " tagID = " + resultMap.get(k).get(0).getTagID());
            } else if (element == 0) {
                logger.debug("Slot = " + k);
            }
        }


        logger.info("Final filterVector: ["+filterVector.toString() + "]");
        // CLS 直接跳过空时隙, 不要计算空时隙的时间, 即计算时间时不考虑afterFilter[0]
        for (int i = 0; i <filterVector.size(); i++){
            int tmpValue = filterVector.get(i);
            if (tmpValue == 0) {
                afterFilter[0]++;
            } else if (tmpValue == 1) {
                afterFilter[1]++;
                responseNum++;
            } else if (tmpValue == 2) {
                afterFilter[2]++;
                responseNum++;
            } else if (tmpValue == 3) {
                afterFilter[3]++;
                responseNum++;
            } else if (tmpValue > 3) {
                afterFilter[4]++;
                responseNum++;
            }
        }

        double t1 = clsRoundExecutionTime(filterVector, responseNum);
        recorder.totalExecutionTime += t1;
        logger.info("Round Execution Time: " + t1);
        logger.info("After using CLS. Slot types in the current round 0,1,2,3,>3: 0:["+afterFilter[0]+"个] 1:["+afterFilter[1]+"个]"+" 2:["+afterFilter[2]+"个] 3:["+afterFilter[3]+"个] >3:["+afterFilter[4]+"个]");
        return filterVector;
    }

    public List<Integer> SFMTI_genFilterVector(int frameSize, int random1, int random2, List<Tag> theTagList, int[] beforeFilter, int[] afterFilter, Recorder recorder){

        // beforeFilter[]和afterFilter[]是累加的, 记录的是所有轮次的数据
        // 在本轮, 使用SFMTI前后, 各种类型的时隙个数
        int[] before = new int[]{0,0,0,0,0};
        int[] after = new int[]{0,0,0,0,0};

        VectorMap vectorMap = VectorMap.genBaseVectorMap(frameSize, random1, 0, theTagList);
        List<Integer> filterVector = vectorMap.getFilterVector();
        if (filterVector == null)
            System.out.println("+++++++++++++++++++++++++++++++++++++++++++ERROR FILTER VECTOR NULL+++++++++++++++++++++++++++++++++++++");
        Map<Integer, List<Tag>> resultMap = vectorMap.getResultMap();

        for (int i = 0; i <filterVector.size(); i++){
            int tmpValue = filterVector.get(i);
            if (tmpValue == 0) {
                before[0]++;
                beforeFilter[0]++;
            }
            else if (tmpValue == 1) {
                before[1]++;
                beforeFilter[1]++;
            }
            else if (tmpValue == 2) {
                before[2] ++;
                beforeFilter[2]++;
            }
            else if (tmpValue == 3) {
                before[3]++;
                beforeFilter[3]++;
            }
            else if (tmpValue > 3) {
                before[4]++;
                beforeFilter[4]++;
            }
        }
        logger.info("Before using SFMTI. Slot types in the current round 0,1,2,3,>3: 0:["+before[0]+"个] 1:["+before[1]+"个]"+" 2:["+before[2]+"个] 3:["+before[3]+"个] >3:["+before[4]+"个]");

        logger.info("Generating Filter Vector: ");
        for (int k = 0; k < filterVector.size(); k++){
            int element = filterVector.get(k);
            if(element > 3){
                filterVector.set(k, 0); // 大于3的冲突时隙设为0, 忽略
                List<String> tagStr = new ArrayList<>();
                for (int i = 0; i < resultMap.get(k).size(); i++)
                    tagStr.add(resultMap.get(k).get(i).getTagID());
                logger.debug("Slot = " + k + " tagID = "+ tagStr);
            }else if(element == 2 || element == 3){
                int total = resultMap.get(k).size();
                Map<Integer, Integer> map = new HashMap<>();
                for (Tag tag : resultMap.get(k)){
                    int slot = tag.hash2(total,  random2); // 调和冲突
                    logger.debug("Slot = " + k + " secondSlot = "+slot + " tagID = "+tag.getTagID());
                    if (map.containsKey(slot)){
                        map.put(slot, map.get(slot) + 1);
                    } else{
                        map.put(slot, 1);
                    }
                }
                Iterator iterator = map.keySet().iterator();
                // while这段是打印map, map的键为: 时隙, 值为该时隙对应的标签个数
                while (iterator.hasNext()){
                    Integer key = (Integer) iterator.next();
                    Integer value = map.get(key);
                    logger.debug("[inner] map key = "+key+" value = "+value);
                }
                if (map.size()<element){// 没有调和成功, map的长度小于element数目, 根据抽屉原理, 必然有多个标签又选择同一个时隙, 再次起冲突
                    filterVector.set(k, 0);
                }else{ // 调和成功
                    // append vector
                    // 不需要filterVector.set(k,1), 最后还要统计调和了的2-collision和3-collision
                    for (int m = 0; m < total - 1; m++)
                        filterVector.add(1);

                }
            }else if (element == 1) {
//                if(resultMap.get(k).size() != 1)
//                    logger.error("ERROR！");
//                logger.debug("Slot = " + k + " tagID = "+resultMap.get(k).get(0).getTagID());
            }else if(element == 0){
                logger.debug("Slot = " + k);
            }
            logger.debug("[inner] tmpFilter:"+ filterVector);
        }
        logger.info("Final filterVector: ["+ filterVector + "]");
        for (int i = 0; i <filterVector.size(); i++){
            int tmpValue = filterVector.get(i);
            if (tmpValue == 0){
                afterFilter[0]++;
                after[0]++;
            }

            else if (tmpValue == 1){
                afterFilter[1]++;
                after[1]++;
            }

            else if (tmpValue == 2){
                afterFilter[2]++;
                after[2]++;
            }

            else if (tmpValue == 3){
                afterFilter[3]++;
                after[3]++;
            }

            else if (tmpValue > 3){
                afterFilter[4]++;
                after[4]++;
            }

        }


        double t1= sfmtiRoundExecutionTime(filterVector, after[1]+after[2]+after[3]);
        recorder.totalExecutionTime += t1;
        logger.info("Round Execution Time: " + t1);
        logger.info("After using SFMTI. Slot types in the current round 0,1,2,3,>3: 0:["+after[0]+"个] 1:["+after[1]+"个]"+" 2:["+after[2]+"个] 3:["+after[3]+"个] >3:["+after[4]+"个]");
        logger.info("After using SFMTI. Slot types number until now 0,1,2,3,>3: 0:["+afterFilter[0]+"个] 1:["+afterFilter[1]+"个]"+" 2:["+afterFilter[2]+"个] 3:["+afterFilter[3]+"个] >3:["+afterFilter[4]+"个]");
        return filterVector;
    }

//    protected void readerMInit(Environment environment)
//    {
//        List<Reader_M> readerMList = environment.getReaderList();
//
//        //  设置期望列表
//        for(Reader_M reader : readerMList)
//        {
//            reader.expectedTagList.addAll(environment.getExpectedTagList());
//        }
//
//        //  设置,覆盖范围,针对expectedTagList，实际可能不存在
//        for(Reader_M reader : readerMList)
//        {
//            int realReply =0;
//            for(Tag tag : environment.getExpectedTagList())
//            {
//                double x = reader.getLocation().getX() - tag.getLocation().getX();
//                double y = reader.getLocation().getY() - tag.getLocation().getY();
//                if(reader.getCoverActualTagList()== null) { reader.setCoverActualTagList(new ArrayList<Tag>());  }
//                if(x * x + y * y < reader.getReadingRadius() * reader.getReadingRadius())
//                {
//                    reader.getCoverActualTagList().add(tag);
//                    if(environment.getActualTagList().contains(tag)) { realReply++; }
//                }
//            }
//            reader.realReply = realReply;
//        }
//    }



    /** 一轮SFMTI的执行时间
     *
     * @param filterVector
     * @param roundSlots
     */
    public double sfmtiRoundExecutionTime(List<Integer> filterVector, int roundSlots){
        // T = ceil(2f/96)*t_tag+(N1+2N2+3N3)*t_{short}, N1, N2, N3为可以调和的2-collision slots 和 3-collision slots数目, 用roundSlots记录
        double t1 = (double) Math.ceil(2.0 * filterVector.size()/96) * 2.4 + roundSlots * 0.4;
        return t1;
    }

    /**
     * 一轮CLS的执行时间
     * @param filterVector
     * @param responseSlotsNum 发送短消息的时隙数量
     */
    public double clsRoundExecutionTime(List<Integer> filterVector, int responseSlotsNum){
        int twoBit = 0;
        int oneBit = 0;

        for (int i = 0; i < filterVector.size(); i++){
            if (filterVector.get(i) == 0 || filterVector.get(i) == 1){
                twoBit++;
            } else {
                oneBit++;
            }
        }
        double readerReqTime = twoBit * 2 * 2.4 / 96 + oneBit * 2.4 / 96;

        double tagReplyTime = responseSlotsNum * 0.4;
        double t1 = readerReqTime + tagReplyTime;
        return t1;
    }
}

