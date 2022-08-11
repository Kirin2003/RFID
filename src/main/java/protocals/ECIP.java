package protocals;

import base.Tag;
import org.apache.logging.log4j.Logger;
import utils.Environment;
import utils.Reader_M;
import utils.Recorder;
import utils.UnexpectedTagEliminationMethod;

import java.util.*;

/**
 * @author Kirin Huang
 * @date 2022/8/8 下午10:19
 */
public class ECIP extends IdentifyTool{
    int numberOfHashFunctions = 1;//哈希函数的个数, 用于意外标签去除阶段
    double falsePositiveRatio = 0.01;//假阳性误报率, 即意外标签通过成员检查的比率

    protected Map<Integer, String> CidMap = new HashMap<>(); //store the slotId and the overlapped cid
    protected Map<Integer,List<Tag>> slotToTagList = new HashMap<>();


    public ECIP(Logger logger, Recorder recorder, Environment environment) {
        super(logger, recorder, environment);
    }

    @Override
    public void execute() {
        List<Reader_M> readers = environment.getReaderList();

        unexpectedTagElimination();

        // 第一阶段所有阅读器用时中最长的作为第一阶段的时间
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

        identify();

        // 第二阶段所有阅读器中用时最长的作为第二阶段的时间
        double maxTime2 = 0;
        for(Reader_M reader_m : readers) {
            double t1 = reader_m.recorder.totalExecutionTime;
            if(t1 > maxTime2) {
                maxTime2 = t1;
            }
        }
        recorder.totalExecutionTime = maxTime2;
    }

    public void identify() {
        for (Reader_M reader : environment.getReaderList()) {
            logger.error("<<<<<<<<<<<<<<<<<<<< Reader: " + reader + " >>>>>>>>>>>>>>>>>>>");
            // 每一次需要reset(),将环境中所有预期标签设为活跃, 因为有些标签在其他阅读器中识别为缺失, 但在某个阅读器中识别为存在, 这个标签是存在的
            environment.reset();
            identify( reader);
        }
    }

    public void unexpectedTagElimination() {
        UnexpectedTagEliminationMethod.BloomFilterMethod(numberOfHashFunctions, falsePositiveRatio,environment,logger);
    }

    public void identify(Reader_M reader_m) {
        Recorder recorder1 = reader_m.recorder;
        int repeated = 0; // 没有识别到一个标签类别的轮数
        int f1 = 0; // 时隙
        int expectedTagNum = environment.getExpectedTagList().size();// 预期标签数目
        int unReadCidNum = environment.getExpectedCidNum();// 没有识别完的类别数目, 初始值为系统预期的类别数目
        int readCidNumInOneRound = 0;
        Map<Integer, String> CidMap = new HashMap<>(); // 键为时隙, 值为编码之后的标签id
        Map<Integer, List<Tag>> slotToTagList = new HashMap<>(); // 键为时隙, 值为在这个时隙回应的标签列表(是存在的标签)
        Map<Integer, Integer> indicator = new HashMap<>();
        Vector<Integer> location = new Vector<>();
        Vector<String> d = new Vector<>();
        List<Tag> actualTagList = environment.getActualTagList();
        boolean flag = false; // 是否循环


        // 1 优化时隙
        f1 = optimizeFrameSize(unReadCidNum);
        // 2 标签选择时隙(随机)
        int random = (int) (Math.random());
        for (Tag tag : actualTagList) {
            if (tag.isActive()) {
                tag.hash2(f1, random);
                // 同时, 构造CidMap和slotToTagList, 存入的是完整的类别id
                int slot = tag.getSlotSelected();
                String cid = tag.categoryID;
                if (!CidMap.containsKey(slot)) {
                    CidMap.put(slot, cid);
                    List<Tag> tagList = new ArrayList<>();
                    tagList.add(tag);
                    slotToTagList.put(slot, tagList);
                } else {
                    String newData = encode(CidMap.get(slot), cid);
                    CidMap.put(slot, newData);
                    slotToTagList.get(slot).add(tag);
                }
            }
        }
        // 3 识别
        for (Integer slotId : CidMap.keySet()) {
            String[] strs = decodeCID(CidMap.get(slotId));
            int l = strs.length;

            // 可以识别类别的时隙, category-compatible slot
            if (l == 1) {
                for (Tag tag : slotToTagList.get(slotId)) {
                    tag.setActive(false);
                }
                recorder1.recognizedActualCidNum++;
                recorder1.actualCids.add(strs[0]);
                readCidNumInOneRound++;
            } else if (l == 2) {
                for (Tag tag : slotToTagList.get(slotId)) {
                    tag.setActive(false);
                }
                recorder1.actualCids.add(strs[0]);
                recorder1.actualCids.add(strs[1]);
                recorder1.recognizedActualCidNum += 2;
                readCidNumInOneRound += 2;
            } else {
                // 有冲突时隙
                flag = true; // 需要新一轮识别
            }
        }

            // 4 构造indicator, location, structure d
            // 5 标签选择时隙(重排)
            // 6 识别



    }

    public double calculateTime(int readCidNum) {
        if(readCidNum > 0) {
            return 2.31 * readCidNum;
        } else {
            // 9 * 0.4 + 1.2
            return 4.8;
        }
    }

    public int optimizeFrameSize(int unReadCidNum) {
        int f1 = (int)(Math.ceil(0.98*unReadCidNum));
        if (f1 > 10) {
            f1 = 10;
        }
        return f1;
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

}
