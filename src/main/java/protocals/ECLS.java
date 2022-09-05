package protocals;

import base.Tag;
import org.apache.logging.log4j.Logger;
import utils.*;

import java.util.*;

/**
 * @author Kirin Huang
 * @date 2022/8/8 下午10:19
 */
public class ECLS extends IdentifyTool{
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
    public ECLS(Logger logger, Recorder recorder, Environment environment) {
        super(logger, recorder, environment);

        int expectedActualCidNum = 0;
        Set<String> expectedActualCidSet = new HashSet<>();
        for(Tag tag : environment.getActualTagList()) {
            expectedActualCidSet.add(tag.getCategoryID());
        }
        expectedActualCidNum = expectedActualCidSet.size();

        Set<String> expectedCidSet = new HashSet<>();
        for(Tag tag : environment.getExpectedTagList()) {
            expectedCidSet.add(tag.getCategoryID());
        }
        int expectedCidNum = expectedCidSet.size();
        logger.debug("环境中的期望类别数=[ "+expectedCidNum+" ] 环境中真实存在的类别数=[ "+expectedActualCidNum+" ]");
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
            logger.error("<<<<<<<<<<<<<<<<<<<< 阅读器: " + reader.getID() + " >>>>>>>>>>>>>>>>>>>");
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

        // 期望的标签列表, 可能有缺失；每个阅读器只覆盖了一个小范围, 但期望的标签列表是整个仓库的
        List<Tag> expectedTagList = environment.getExpectedTagList();

        List<Tag> coveredActualTagList = reader_m.coverActualTagList;

        boolean useCLS; // 是否使用CLS
        int recognizedCidNum = 0; // 总共识别的类别数目
        int recognizedCidNumCurrentRound = 0; // 在一轮中识别的类别数目
        int recognizedMissCidNumCurrentRound = 0;
        int recognizedActualCidNumCurrentRound = 0;
        int filterVectorLength = 0; // filter vector长度
        int slotSumNum = 0; // 时隙数目

        // filter generation phase之前, 时隙中期望标签为0,1,2,3,>3的个数
        int[] beforeFilter = new int[]{0, 0, 0, 0, 0};
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

        Set<String> expectedActualCidSet = new HashSet<>();
        for(Tag tag : coveredActualTagList) {
            expectedActualCidSet.add(tag.getCategoryID());
        }
        int expectedActualCidNum = expectedActualCidSet.size();

        Set<String> expectedCidSet = new HashSet<>();
        for(Tag tag : expectedTagList) {
            expectedCidSet.add(tag.getCategoryID());
        }
        int expectedCidNum = expectedCidSet.size();
        logger.debug("该阅读器需要识别的标签数=[ "+expectedCidNum+" ] 该阅读器识别范围内真实存在的类别数=[ "+expectedActualCidNum+" ]");
        double missRate = (expectedCidNum - expectedActualCidNum) * 1.0 / expectedCidNum;


        Map<Integer, List<Tag>> collisionTagListMap = new HashMap<>(); // 冲突时隙的时隙-标签列表映射
        int collisionTagListIndex = 0;

        while (recognizedCidNum < expectedCidNum) { // 当识别数目小于期望标签数目时一直循环
            ++recorder1.roundCount;
            recorder1.missingRateList.add( missRate);
            System.out.println();
            logger.info("################### 第 "+recorder1.roundCount+" 轮 #######################");
            System.out.println();

            // CLS需要一个随机数, SFMTI需要两个随机数
            int random1 = (int) (100 * Math.random());
            int random2 = (int) (100 * Math.random());

            int frameSize = 0;
            double rho = 0;

            /**
             * 1 优化时隙
             */
            logger.error("缺失率 : " + missRate);
//            if (missRate > 0.679){ // 缺失率>0.679, 使用cls, 优化时隙
//                frameSize = CLS_OptimizeFrame(missRate,expectedCidNum,recognizedCidNum);
//                useCLS = true;
//            }else{ // 缺失率<=0.679, 使用SFMTI, 优化时隙
//                // TODO
//                frameSize = SFMTI_OptimizeFrame(expectedCidNum,recognizedCidNum);
//                useCLS = false;
//            }
            frameSize = CLS_OptimizeFrame(missRate,expectedCidNum,recognizedCidNum);
            useCLS = true;

            /**
             * 2 阅读器为标签分配时隙, 生成filter vector, expMap
             */
            logger.info("----------------生成filter vector--------------");
            logger.info("时隙 = ["+frameSize + "] 随机数1 = ["+random1 + "] 随机数2 = ["+random2 + "]");
            List<Integer> filterVector;
            if (useCLS){ // 使用cls算法生成filter vector
                filterVector = this.CLS_genFilterVector(frameSize, random1, expectedTagList, beforeFilter, afterFilter, recorder1);
                logger.warn("使用CLS");
            }else { // 使用sfmti算法生成filter vector
                filterVector = this.SFMTI_genFilterVector(frameSize, random1, random2, expectedTagList, beforeFilter, afterFilter, recorder1);
                logger.warn("使用SFMTI");
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

            Map<Integer, Set<String>> expSlotToCidSet = new HashMap<>();
            for(Integer slot : expMap.keySet()) {
                List<Tag> expTagList = expMap.get(slot);
                for(Tag tag : expTagList) {
                    if(tag.isActive()) {
                        if(!expSlotToCidSet.keySet().contains(slot)) {
                            Set<String> cidSet = new HashSet<>();
                            cidSet.add(tag.getCategoryID());
                            expSlotToCidSet.put(slot,cidSet);
                        } else {
                            Set<String> cidSet = expSlotToCidSet.get(slot);
                            cidSet.add(tag.getCategoryID());
                            expSlotToCidSet.put(slot,cidSet);
                        }
                    }

                }
            }

            /**
             * 3 验证阶段, 真实存在的标签回应, 生成verMap, resultMap
             */
            logger.info("-----------------------验证-----------------------");
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

            logger.debug("ver map: tagListSort output (sort in ascending order according to the size of the first Slot)：");
            // 打印verMap
            Iterator vit = verMap.keySet().iterator();
            while (vit.hasNext()){
                Integer key = (Integer) vit.next();
                List<String> strList = verMap.get(key);
                for (int i = 0; i < strList.size(); i++)
                    logger.debug(strList.get(i));
            }

            // TODO add: 去掉filter vector中为0的元素
            filterVector.removeIf(integer -> integer == 0);

            // 键: 时隙, 值: 回应的标签列表, 只有存在的标签, 没有缺失的标签
            // resultMap与verMap的不同在于, verMap是标签的tagId(字符串)列表, resultMap是标签列表
            Map<Integer, List<Tag>> resultMap = new HashMap<>();
            Map<Integer, Set<String>> actualSlotToCidSet = new HashMap<>();
            for (Tag tag: coveredActualTagList) {
                if(tag.isActive()){
                    int afterSlot = tag.getSlotSelected();
                    if (resultMap.containsKey(afterSlot)){
                        resultMap.get(afterSlot).add(tag);
                        actualSlotToCidSet.get(afterSlot).add(tag.getCategoryID());
                    }else {
                        List<Tag> nTagList = new ArrayList<>();
                        nTagList.add(tag);
                        resultMap.put(afterSlot, nTagList);

                        Set<String> actualCidSetInSlot = new HashSet<>();
                        actualCidSetInSlot.add(tag.getCategoryID());
                        actualSlotToCidSet.put(afterSlot,actualCidSetInSlot);
                    }
                }
            }

            // 打印resultMap和actualSlotToCidSet
            logger.debug("打印resultMap：");
            for(Integer slot : resultMap.keySet()) {
                List<Tag> value = resultMap.get(slot);
                Set<String> tagstr = actualSlotToCidSet.get(slot);
                logger.debug("时隙 = "+ slot + " 标签ID = "+tagstr.toString());
                logger.debug("时隙 = "+ slot + "标签 = "+value.toString());
            }
//

            /**
             * 4 识别结果
             */
            logger.info("----------------------识别结果-------------------------");
            int slotLength = filterVector.size();
            int roundSlotCount = 0;

            for (int i = 0; i < slotLength; i++){
                if(filterVector.get(i) != 0) { // 期望中有标签回应
                    roundSlotCount++;
                    slotSumNum++;


                    recorder1.slotCount ++;
                    // 分析实际得到的回应情况
                    if (!actualSlotToCidSet.containsKey(i)) { // 有0个类别回应
                        realReply[0]++;
                        if(filterVector.get(i) == 2){ // Unreconcilable 2 Collisions
                            twoTurnToZero++;
                        }else if (filterVector.get(i) == 3){ // Unreconcilable 3 Collisions
                            threeTurnToZero++;
                        }
                        // 预期有标签回应(filterVector.get(i) > 0), 实际没有标签回应(slotResponseList.size() == 0), 全部缺失!
                        Set<String> str = expSlotToCidSet.get(i);
                        for (Tag tag : expMap.get(i)){
                            tag.setActive(false);
                        }
                        recognizedCidNum += str.size();

                        recognizedCidNumCurrentRound += str.size();
                        recognizedMissCidNumCurrentRound+=str.size();
                        recorder1.missingCids.addAll(str);
                        logger.debug("时隙=" + (i) + " 结果=空时隙, 缺失类别列表="+str.toString());
                        recorder1.emptySlotCount ++; //统计为空Slot的总数

                    } else if (actualSlotToCidSet.get(i).size() == 1) { // 同1个类别的标签回应
                        realReply[1]++;
                        if (useCLS){
                            if(filterVector.get(i) == 1){ // use CLS, 预期是1, 结果是1
                                recorder1.singletonSlotCount ++;
                                logger.debug("时隙=" + (i) + " 结果=单时隙" + actualSlotToCidSet.get(i).toString());
                                for(Tag tag : expMap.get(i)) {
                                    tag.setActive(false);
                                }
                                recognizedCidNumCurrentRound++;
                                recognizedCidNum ++;
                                recognizedActualCidNumCurrentRound ++;
                                recorder1.actualCids.add(expMap.get(i).get(0).getCategoryID());

                            }else{ // use CLS, 预期是1, 结果大于1, 无法识别

                                if(filterVector.get(i) == 2){ // 2 changed to 1
                                    twoTurnToOne++;
                                }else if (filterVector.get(i) == 3){ // 3 changed to 1
                                    threeTurnToOne++;
                                }
                                List<Tag> tmplist = expMap.get(i);
                                Set<String> str = new HashSet<>();
                                for (Tag tag : tmplist){
                                    if(!actualSlotToCidSet.get(i).contains(tag.getCategoryID()))
                                        str.add(tag.getCategoryID());
                                }
                                recorder1.noResult ++;
                                logger.debug("时隙=" + (i) + " 结果=无法识别 " + actualSlotToCidSet.get(i).toString()+":其他="+str);
                                collisionTagListMap.put(collisionTagListIndex++, tmplist);

                            }
                        }else{ // use SFMTI, 只有一个标签回应必然是单时隙, 不可能是有缺失的冲突时隙, 因为SFMTI已经解决冲突了, 没解决的设置为filterVector.get(i) == 0
//                            for(Tag tag : expMap.get(i))
//                            {
//                                tag.setActive(false);
//                            }
                            // TODO
                            for(Tag tag : reader_m.coveredAllTagList) {
                                if(tag.getCategoryID() .equals( expMap.get(i).get(0).getCategoryID())) {
                                    tag.setActive(false);
                                }
                            }

                            recognizedCidNumCurrentRound++;
                            recognizedCidNum ++;
                            logger.debug("时隙="+(i) + " 结果=单时隙 "+actualSlotToCidSet.get(i).toString());
                            recorder1.singletonSlotCount ++;
                            recorder1.actualCids.add(expMap.get(i).get(0).getCategoryID());
                            recognizedActualCidNumCurrentRound ++;
                        }
                    } else { // 有多个标签回应
                        realReply[2]++;
                        if(filterVector.get(i) == 2 && actualSlotToCidSet.get(i).size() == 2){
                            twoTurnToTwo++; // 2->2
                        }else if(filterVector.get(i) == 3 && actualSlotToCidSet.get(i).size() == 2){
                            threeTurnToTwo++;  // 3->2
                        }else if(filterVector.get(i) == 3 && actualSlotToCidSet.get(i).size() == 3){
                            threeTurnToThree++;  // 3->3
                        }


                        logger.debug("时隙=" + (i) + " 结果=冲突时隙 选择这一时隙的类别列表=" + actualSlotToCidSet.get(i).toString());
                        recorder1.collisionSlotCount++; //统计为collision的总数

                        // 期望标签的映射, 即便有缺失的, 不知道是哪个缺失, 这里把期望标签全部记录到了冲突时隙-标签列表中
                        List<Tag> tmplist = expMap.get(i);
                        collisionTagListMap.put(collisionTagListIndex++, tmplist); // 冲突时隙-标签列表

                    }

                }else{ // 期望中没有标签回应
                    // 空时隙
                    logger.debug("空时隙"+i);
                }
            }
            recorder1.roundSlotCountList.add(roundSlotCount);

            logger.info("本轮filter vector的长度: " + filterVector.size());
            logger.info("本轮识别完成后filter vector的总长度: " + filterVectorLength);
            logger.info("本轮识别完成后将2-冲突时隙调和成功的时隙数: " + resolvedFromTwoCollision);
            logger.info("本轮识别完成后将3-冲突时隙调和成功的时隙数:" + resolvedFromThreeCollision);
            logger.info("本轮阅读器识别到存在的类别数:"+recognizedActualCidNumCurrentRound + " 缺失的类别数:"+recognizedMissCidNumCurrentRound);
            logger.info("本轮时隙数: " + roundSlotCount);
            logger.info("本轮识别结束后时隙总数 : " + slotSumNum);

            logger.debug("缺失率: "+missRate);
            logger.debug("帧长: " + frameSize+"; filter vector长度: " + filterVector.size() + ";");

            realReply[0]=0;
            realReply[1]=0;
            realReply[2]=0;

            recorder1.recognizedMissingCidNumList.add(recognizedMissCidNumCurrentRound);
            recorder1.recognizedActualCidNumList.add(recognizedActualCidNumCurrentRound);
            recorder1.recognizedCidNumList.add(recognizedMissCidNumCurrentRound+recognizedActualCidNumCurrentRound);
            recorder1.recognizedActualCidNum = recorder1.actualCids.size();
            recorder1.recognizedMissingCidNum = recorder1.missingCids.size();
            recorder1.recognizedCidNum = recorder1.actualCids.size() + recorder1.missingCids.size();

            if(expectedCidNum > recognizedCidNum) {
                //TODO 修改缺失率计算方法,缺失率为类别的缺失率而非标签的缺失率
                missRate = (expectedCidNum - expectedActualCidNum - recorder1.recognizedMissingCidNum)*1.0 / (expectedCidNum - recognizedCidNum);
//                missRate = 1.0*((expectedTagNum - recognizedTagNum) - (coveredActualTagList.size() - (recognizedTagNum-recognizedMissingTag.size()))) / (expectedTagNum - recognizedTagNum);
            }
        }


        logger.info("----------------------------------------");
        logger.info("在filter之前的时隙类型：0:["+beforeFilter[0]+"个] 1:["+beforeFilter[1]+"个]"+" 2:["+beforeFilter[2]+"个] 3:["+beforeFilter[3]+"个] >3:["+beforeFilter[4]+"个]");
        logger.info("在filter之后的时隙类型 0:["+afterFilter[0]+"个] 1:["+afterFilter[1]+"个]"+" 2:["+afterFilter[2]+"个] 3:["+afterFilter[3]+"个] >3:["+afterFilter[4]+"个]");

        logger.info("时隙类型的转变: 2->0： "+twoTurnToZero);
        logger.info("时隙类型的转变: 3->0： "+threeTurnToZero);
        logger.info("时隙类型的转变: 2->1： "+twoTurnToOne);
        logger.info("时隙类型的转变: 2->2： "+twoTurnToTwo);
        logger.info("时隙类型的转变: 3->1： "+threeTurnToOne);
        logger.info("时隙类型的转变: 3->2： "+threeTurnToTwo);
        logger.info("时隙类型的转变: 3->3： "+threeTurnToThree);
        logger.info("时隙类型的转变: 2-->：调和成功: "+ resolvedFromTwoCollision);
        logger.info("时隙类型的转变: 3-->：调和成功: "+ resolvedFromThreeCollision);
        logger.info("filter vector的总长度: "+ filterVectorLength);
        logger.error("阅读器的总执行时间: [" + recorder1.totalExecutionTime+ " ms]");

        recorder1.recognizedActualCidNum = recorder1.actualCids.size();
        recorder1.recognizedMissingCidNum = recorder1.missingCids.size();
        recorder1.recognizedCidNum = recorder1.actualCids.size() + recorder1.missingCids.size();
    }

    private int SFMTI_OptimizeFrame(int expectedCidNum, int recognizedCidNum) {
        int f = (int)Math.ceil(((double)(expectedCidNum - recognizedCidNum)) / 1.68);
        return Math.max(f, 15);
    }

    private int CLS_OptimizeFrame(double missRate, int expectedTagNum, int recognizedTagNum) {
        double rho = RHOUtils.getBestRho(missRate,"rho-opt-TSA.txt");
        int f = (int)Math.ceil(((double)(expectedTagNum - recognizedTagNum)) / rho);
        return Math.max(f,15);
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
                tmpStr = "[inner]slot = "+index+" 对应vector中的值 = 0 (则afterSlot直接赋值 -1)"+" x1 = " + x1 + " X2 = "+x2 +"  X3 = "+x3 + " afterSlot = -1 category ID = " + tag.getCategoryID();
                tag.fillMap(expMap, -1, tag);
                tag.fillMap(verMap, index, tmpStr);
                tag.setSlotSelected(-1);
                break;
            case 1:
                tmpStr = "[inner]slot = "+index+" 对应vector中的值 = 1 (则afterSlot应等于x1 + x2 + x3)"+" x1 = " + x1 + " X2 = "+x2 +"  X3 = "+x3 + " afterSlot = "+sumX+" category ID  = " + tag.getCategoryID();
                tag.fillMap(expMap, sumX, tag);
                tag.fillMap(verMap, index, tmpStr);
                tag.setSlotSelected(sumX);
                break;
            case 2:
                int indexApp2 = tag.hash2(2,  random2);
                if (indexApp2 == 0){
                    tmpStr = "[inner]slot = "+index+" 对应vector中的值 = 2 secondSlot = 0"+ " (则afterSlot应等于x1 + x2 + x3)"+" x1 = " + x1 + " X2 = "+x2 +"  X3 = "+x3 + " afterSlot = "+sumX+" category ID  = " + tag.getCategoryID();
                    tag.fillMap(expMap, sumX, tag);
                    tag.fillMap(verMap, index, tmpStr);
                    tag.setSlotSelected(sumX);
                }else {
                    int tmpSlot = X + x2 + x3 * 2;
                    tmpStr = "[inner]slot = "+index+" 对应vector中的值 = 2 secondSlot = "+indexApp2+ " (则afterSlot应等于X + x2 + x3 * 2)"+" x1 = " + x1 + " X2 = "+x2 +"  X3 = "+x3 + "X = "+X+" afterSlot = "+tmpSlot+" category ID  = " + tag.getCategoryID();
                    tag.fillMap(expMap, tmpSlot, tag);
                    tag.fillMap(verMap, index, tmpStr);
                    tag.setSlotSelected(tmpSlot);
                }
                break;
            case 3:
                int indexApp3 = tag.hash2(3,  random2);
                if (indexApp3 == 0){
                    tmpStr = "[inner]slot = "+index+" 对应vector中的值 = 3 secondSlot = 0 (则afterSlot应等于x1 + x2 +x3)"+" x1 = " + x1 + " X2 = "+x2 +"  X3 = "+x3 + " afterSlot = "+sumX+" category ID  = " + tag.getCategoryID();
                    tag.fillMap(expMap, sumX, tag);
                    tag.fillMap(verMap, index, tmpStr);
                    tag.setSlotSelected(sumX);
                }else {
                    int thrSlot = X + x2 + x3 * 2 + indexApp3 - 1;
                    tmpStr = "[inner]slot = "+index+" 对应vector中的值 = 3 secondSlot = "+indexApp3+" (则afterSlot应等于X + x2 + x3 * 2 + secondSlot - 1)"+" x1 = " + x1 + " X2 = "+x2 +"  X3 = "+x3 + "X = "+X+ " afterSlot = "+thrSlot+" category ID  = " + tag.getCategoryID();
                    tag.fillMap(expMap, thrSlot, tag);
                    tag.fillMap(verMap, index, tmpStr);
                    tag.setSlotSelected(thrSlot);
                }
                break;
            default:
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
                tmpStr = "[inner]slot = "+index+" 对应vector中的值 = 0 (则afterSlot直接赋值 -1) category ID = " + tag.getCategoryID();
                tag.fillMap(expMap, -1, tag);
                tag.fillMap(verMap, index, tmpStr);
                tag.setSlotSelected(-1);
                break;
            case 1:
                tmpStr = "[inner]slot = "+index+" 对应vector中的值 = 1 (则afterSlot应等于x1 + x2 + x3 + x4)"+" x1 = " + x1 + " X2 = "+x2 +"  X3 = "+x3 +"  X4 = "+x4 + " afterSlot = "+sumX+" category ID = " + tag.getCategoryID();
                tag.fillMap(expMap, sumX, tag);
                tag.fillMap(verMap, index, tmpStr);
                tag.setSlotSelected(sumX);
                break;
            case 2:
                tmpStr = "[inner]slot = "+index+" 对应vector中的值 = 2 (则afterSlot应等于x1 + x2 + x3 + x4)"+" x1 = " + x1 + " X2 = "+x2 +"  X3 = "+x3 +"  X4 = "+x4 + " afterSlot = "+sumX+" category ID = " + tag.getCategoryID();
                tag.fillMap(expMap, sumX, tag);
                tag.fillMap(verMap, index, tmpStr);
                tag.setSlotSelected(sumX);
                break;
            case 3:
                tmpStr = "[inner]slot = "+index+" 对应vector中的值 = 3 (则afterSlot应等于x1 + x2 +x3 + x4)"+" x1 = " + x1 + " X2 = "+x2 +"  X3 = "+x3 +"  X4 = "+x4 + " afterSlot = "+sumX+" category ID = " + tag.getCategoryID();
                tag.fillMap(expMap, sumX, tag);
                tag.fillMap(verMap, index, tmpStr);
                tag.setSlotSelected(sumX);
                break;
            default:
                tmpStr = "[inner]slot = "+index+" 对应vector中的值 > 3 (则afterSlot应等于x1 + x2 +x3 + x4)"+" x1 = " + x1 + " X2 = "+x2 +"  X3 = "+x3 +"  X4 = "+x4 + " afterSlot = "+sumX+" category ID = " + tag.getCategoryID();
                tag.fillMap(expMap, sumX, tag);
                tag.fillMap(verMap, index, tmpStr);
                tag.setSlotSelected(sumX);
                break;
        }

    }

    // 实现时, CLS 使用前后, filter vector没变, 论文中提到0->00, 1->01, >=2 -> 1, 实现时只在计算时间部分考虑了
    public List<Integer> CLS_genFilterVector(int frameSize, int random1, List<Tag> theTagList,int[] beforeFilter, int[] afterFilter, Recorder recorder1){
        int[] before = new int[]{0,0,0,0,0};
        int[] after = new int[]{0,0,0,0,0};
        // 本轮标签将会有回应的时隙数量
        int responseNum = 0;

        VectorMap vectorMap = VectorMap.genBaseVectorMap2(frameSize, random1, 0, theTagList);
        List<Integer> filterVector = vectorMap.getFilterVector();
        Map<Integer, List<Tag>> resultMap = vectorMap.getResultMap();
        // 统计在使用CLS前, filter vector中各类时隙有多少, 比如, 空时隙有多少, 单时隙有多少, 等等
        for (int i = 0; i <filterVector.size(); i++){
            int tmpValue = filterVector.get(i);
            if (tmpValue == 0) {
                beforeFilter[0]++;
                before[0] ++;
            }
            else if (tmpValue == 1) {
                beforeFilter[1]++;
                before[1]++;
            }
            else if (tmpValue == 2) {
                beforeFilter[2]++;
                before[2]++;
            }
            else if (tmpValue == 3) {
                beforeFilter[3]++;
                before[3]++;
            }
            else if (tmpValue > 3) {
                beforeFilter[4]++;
                before[4]++;
            }
        }
        logger.info("在使用CLS之前, filter vector的时隙类型 0,1,2,3,>3: 0:["+before[0]+"个] 1:["+before[1]+"个]"+" 2:["+before[2]+"个] 3:["+before[3]+"个] >3:["+before[4]+"个]");

        int len = filterVector.size();
        logger.info("生成filter vector: ");
        // 打印filter vector的内容
        for (int k = 0; k < len; k++) {
            int element = filterVector.get(k);
            if (element >= 2) {
                List<String> tagStr = new ArrayList<>();
                for (int i = 0; i < resultMap.get(k).size(); i++)
                    tagStr.add(resultMap.get(k).get(i).getCategoryID());
                logger.debug("时隙 = " + k + " 类别 = " + tagStr.toString());
            } else if (element == 1) {

                logger.debug("时隙 = " + k + " 类别 = " + resultMap.get(k).get(0).getCategoryID());
            } else if (element == 0) {
                logger.debug("时隙 = " + k+" 空时隙");
            }
        }


        logger.info("最终的 filterVector: ["+filterVector.toString() + "]");
        // CLS 直接跳过空时隙, 不要计算空时隙的时间, 即计算时间时不考虑afterFilter[0]
        for (int i = 0; i <filterVector.size(); i++){
            int tmpValue = filterVector.get(i);
            if (tmpValue == 0) {
                afterFilter[0]++;
                after[0]++;
            } else if (tmpValue == 1) {
                afterFilter[1]++;
                after[1]++;
                responseNum++;
            } else if (tmpValue == 2) {
                afterFilter[2]++;
                after[2]++;
                responseNum++;
            } else if (tmpValue == 3) {
                afterFilter[3]++;
                after[3]++;
                responseNum++;
            } else if (tmpValue > 3) {
                afterFilter[4]++;
                after[4]++;
                responseNum++;
            }
        }

        double t1 = clsRoundExecutionTime(filterVector, responseNum);
        recorder1.totalExecutionTime += t1;
        recorder1.executionTimeList.add(t1);
        logger.info("本轮时间: " + t1);
        logger.info("使用CLS之后, filter vector中的时隙类型 0,1,2,3,>3: 0:["+after[0]+"个] 1:["+after[1]+"个]"+" 2:["+after[2]+"个] 3:["+after[3]+"个] >3:["+after[4]+"个]");
        return filterVector;
    }

    public List<Integer> SFMTI_genFilterVector(int frameSize, int random1, int random2, List<Tag> theTagList, int[] beforeFilter, int[] afterFilter, Recorder recorder1){

        // beforeFilter[]和afterFilter[]是累加的, 记录的是所有轮次的数据
        // 在本轮, 使用SFMTI前后, 各种类型的时隙个数
        int[] before = new int[]{0,0,0,0,0};
        int[] after = new int[]{0,0,0,0,0};

        VectorMap vectorMap = VectorMap.genBaseVectorMap2(frameSize, random1, 0, theTagList);
        List<Integer> filterVector = vectorMap.getFilterVector();
        if (filterVector == null)
            System.out.println("+++++++++++++++++++++++++++++++++++++++++++ERROR FILTER VECTOR NULL+++++++++++++++++++++++++++++++++++++");
        Map<Integer, List<Tag>> resultMap = vectorMap.getResultMap();
        Map<Integer, Set<String>> slotToCidSet = vectorMap.getSlotToCidSet();

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
        logger.info("在使用SFMTI之前, filter vector的时隙类型 0,1,2,3,>3: 0:["+before[0]+"个] 1:["+before[1]+"个]"+" 2:["+before[2]+"个] 3:["+before[3]+"个] >3:["+before[4]+"个]");

        logger.info("生成 Filter Vector: ");
        int oriFilterVectorSize = filterVector.size();
        for (int k = 0; k < oriFilterVectorSize; k++){
            int element = filterVector.get(k);
            if(element > 3){
                filterVector.set(k, 0); // 大于3的冲突时隙设为0, 忽略
                logger.debug("时隙 = " + k + " 类别 = "+ resultMap.get(k));
            }else if(element == 2 || element == 3){
//                int total = resultMap.get(k).size();
                int total = element;
//                Map<Integer, Integer> map = new HashMap<>();
                Map<Integer, Set<String>> secondSlotToCidSet = new HashMap<>();

                for (Tag tag : theTagList){
                    if(tag.getSlotSelected() == k) {
                        int secondSlot = tag.hash2(total,  random2); // 调和冲突

                        logger.debug("第一次选择的时隙 = " + k + " 第二次选择的时隙 = "+secondSlot + " 类别 = "+tag.getCategoryID());
                        if(!secondSlotToCidSet.containsKey(secondSlot)) {
                            Set<String> cidSetInSecondSlot = new HashSet<>();
                            cidSetInSecondSlot.add(tag.getCategoryID());
                            secondSlotToCidSet.put(secondSlot,cidSetInSecondSlot);
                        } else {
                            secondSlotToCidSet.get(secondSlot).add(tag.getCategoryID());
                        }

                    }

                }
                Iterator iterator = secondSlotToCidSet.keySet().iterator();
                // while这段是打印map, map的键为: 时隙, 值为该时隙对应的标签个数
                while (iterator.hasNext()){
                    Integer key = (Integer) iterator.next();
                    Integer value = secondSlotToCidSet.get(key).size();
                    logger.debug("[inner] secondSlotToCidSet key = "+key+" value = "+value);
                }

                // TODO
                boolean flag = true;
                if(secondSlotToCidSet.size() < element) {
                    flag = false;

                }
                else {
                    for(Integer key :secondSlotToCidSet.keySet()) {
                        if (secondSlotToCidSet.get(key).size() != 1) {
                            flag = false;

                            break;
                        }
                    }
                }
                if(flag) {
                    for (int m = 0; m < total - 1; m++)
                        filterVector.add(1);
                } else {
                    filterVector.set(k, 0);
                }


            }else if (element == 1) {
                logger.debug("时隙 = " + k + " 类别 = "+resultMap.get(k).get(0).getCategoryID());
            }else if(element == 0){
                logger.debug("时隙 = " + k);
            }
            logger.debug("[inner] tmpFilter:"+ filterVector);
        }

        logger.info("最终的filter vector: ["+ filterVector + "]");
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
        recorder1.totalExecutionTime += t1;
        recorder1.executionTimeList.add(t1);
        logger.info("本轮识别时间: " + t1);
        logger.debug("在使用SFMTI之后. filter vector:"+filterVector);
        logger.info("在使用SFMTI之后, filter vector的时隙类型 0,1,2,3,>3: 0:["+after[0]+"个] 1:["+after[1]+"个]"+" 2:["+after[2]+"个] 3:["+after[3]+"个] >3:["+after[4]+"个]");
        return filterVector;
    }




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

