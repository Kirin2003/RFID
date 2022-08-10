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
    int numberOfHashFunctions = 1;//哈希函数的个数, 用于意外标签去除阶段
    double falsePositiveRatio = 0.01;//假阳性误报率, 即意外标签通过成员检查的比率

    protected Map<Integer, String> CidMap = new HashMap<>(); //store the slotId and the overlapped cid
    protected Map<Integer,List<Tag>> slotToTagList = new HashMap<>();


    public CIP(Logger logger, Recorder recorder, Environment environment) {
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


    public void unexpectedTagElimination() {
        UnexpectedTagEliminationMethod.BloomFilterMethod(numberOfHashFunctions, falsePositiveRatio,environment,logger);

    }

    public void identify() {
        for (Reader_M reader : environment.getReaderList()) {
            logger.error("<<<<<<<<<<<<<<<<<<<< Reader: " + reader + " >>>>>>>>>>>>>>>>>>>");
            // 每一次需要reset(),将环境中所有预期标签设为活跃, 因为有些标签在其他阅读器中识别为缺失, 但在某个阅读器中识别为存在, 这个标签是存在的
            environment.reset();
            identify( reader);
        }
    }


    public void identify(Reader_M reader_m) {

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

    protected void readerMInit(Environment environment)
    {
        List<Reader_M> readerMList = environment.getReaderList();

        //  设置期望列表
        for(Reader_M reader : readerMList)
        {
            reader.expectedTagList.addAll(environment.getExpectedTagList());
        }

        //  设置,覆盖范围,针对expectedTagList，实际可能不存在
        for(Reader_M reader : readerMList)
        {
            int realReply =0;
            for(Tag tag : environment.getExpectedTagList())
            {
                double x = reader.getLocation().getX() - tag.getLocation().getX();
                double y = reader.getLocation().getY() - tag.getLocation().getY();
                if(reader.getCoverActualTagList()== null) { reader.setCoverActualTagList(new ArrayList<Tag>());  }
                if(x * x + y * y < reader.getReadingRadius() * reader.getReadingRadius())
                {
                    reader.getCoverActualTagList().add(tag);
                    if(environment.getActualTagList().contains(tag)) { realReply++; }
                }
            }
            reader.realReply = realReply;
        }
    }


}
