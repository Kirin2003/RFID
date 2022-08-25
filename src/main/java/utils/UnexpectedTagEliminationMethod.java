package utils;

import base.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.apache.logging.log4j.Logger;

/**
 * @author Kirin Huang
 * @date 2022/8/10 下午10:21
 */
public class UnexpectedTagEliminationMethod {
    public static void BloomFilterMethod(int numberOfHashFunctions, double falsePositiveRatio, Environment environment, Logger logger) {
        //第一阶段, 所有阅读器同时工作, 去除意外标签, 等待所有阅读器工作完毕再进行下一阶段, 这样意外标签去除的多, 对下一阶段干扰的就少
        BloomFilter bf = new BloomFilter(logger);
        List<Tag> expectedTagList = environment.getExpectedTagList();
        int bloomFilterSize = (int) Math.ceil((-expectedTagList.size() * numberOfHashFunctions) / Math.log(1 - Math.pow(falsePositiveRatio, 1.0 / numberOfHashFunctions)));
        Random random = new Random(System.currentTimeMillis());
        List<Integer> randomInts = new ArrayList<>();
        for(int i = 0; i < numberOfHashFunctions; i++) {
            randomInts.add(random.nextInt(100));
        }
        List<Integer> bloomFilterVector = bf.genFilterVector(numberOfHashFunctions,bloomFilterSize,randomInts,environment.getExpectedTagList());
        for (Reader_M reader : environment.getReaderList()){
            logger.error("<<<<<<<<<<<<<<<<<<<< 阅读器: " + reader.getID() + " >>>>>>>>>>>>>>>>>>>");
            int num = bf.membershipCheck(bloomFilterVector,reader.coveredAllTagList,numberOfHashFunctions,randomInts,bloomFilterSize);
            reader.recorder.eliminationTagNum = num;
            double t1 = bf.membershipCheckExecutionTime(bloomFilterVector);
            reader.recorder.totalExecutionTime += t1;
        }

    }
}
