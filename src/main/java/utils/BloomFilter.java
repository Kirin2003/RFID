package utils;

import base.Tag;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;


public class BloomFilter {

    Logger logger;


    public BloomFilter(Logger logger){

        this.logger = logger;

    }


    /**
     * Method that generates a bloom filter at the reader side. Maps the expected tag list
     * answers to a List.
     * @param numberOfHashFunctions
     * @param bloomFilterSize
     * @param randomInts
     * @return
     */
    public  List<Integer> genFilterVector(double numberOfHashFunctions, int bloomFilterSize, List<Integer> randomInts, List<Tag> tagList){
        List<Integer> bloomFilterVector = new ArrayList<>();
        System.out.println(bloomFilterSize);
        // 布隆过滤器全部初始化为0
        for (int i = 0; i < bloomFilterSize; i++){
            bloomFilterVector.add(0);
        }

        // 将每一个期望标签映射到布隆过滤器中, 当多个期望标签映射到同一个过滤器位置, 也设置为1
        if (numberOfHashFunctions == 1) {
            int random = randomInts.get(0);
            for (Tag tag : tagList) {
                if (tag.isActive()) {
                    int index = tag.hash1(bloomFilterSize, random);
                    bloomFilterVector.set(index, 1);
                }
            }
        } else {
            for (int i = 0; i < numberOfHashFunctions; i++) {
                // TODO
                int r = randomInts.get(i) % bloomFilterSize;
                for (Tag tag : tagList) {
                    if (tag.isActive()) {
                        int index = tag.selectSlotPseudoRandom(bloomFilterSize, 0, r);
                        bloomFilterVector.set(index, 1);

                    }
                }
            }
        }

        return bloomFilterVector;
    }


    public  int membershipCheck(List<Integer> bloomFilterVector, List<Tag> tagList, int numberOfHashFunctions, List<Integer> randomInts, int bloomFilterSize){


        // 消除意外标签, 在未压缩的布隆过滤器中, 如果标签对应的数不为1, 是意外标签
        // 此处, 没有写压缩布隆过滤器的代码, 只是计算了压缩布隆过滤器的长度, 便于计算时间, 消除意外标签是通过未压缩的布隆过滤器实现的
        int eliminatedTagNum = 0;
        for (Tag tag : tagList) {
            // TODO 应该改成使用k个hash函数, 如果每次布隆过滤器中的位都是1, 则是存在的标签, 否则是失去的标签
            // 检查意外标签, 并去除
            if (numberOfHashFunctions == 1) {
                int randomInt = randomInts.get(0);
                if (tag.isActive()) {
                    int index = tag.hash1(bloomFilterSize, randomInt);
                    if (bloomFilterVector.get(index) == 0) {
                        tag.setActive(false);
                        eliminatedTagNum++;
                    }
                }
            } else {
                for (int i = 0; i < numberOfHashFunctions; i++) {
                    int r = randomInts.get(i) % bloomFilterSize;
                    if (tag.isActive()) {
                        int index = tag.selectSlotPseudoRandom(bloomFilterSize, 0, r);
                        if (bloomFilterVector.get(index) == 0) {
                            tag.setActive(false);
                            eliminatedTagNum++;
                            break;
                        }
                    }
                }
            }
        }
        logger.error("Total number of eliminated unknown tags after membership Check: [" + eliminatedTagNum + "]");
        return eliminatedTagNum;
    }

    public double membershipCheckExecutionTime(List<Integer> originalBloomFilterVector) {
        /*计算压缩后的布隆过滤器长度*/
        StringBuilder sb = new StringBuilder();

        for(Integer i : originalBloomFilterVector) {
            sb.append(i.toString());
        }

        String[] segments = sb.toString().split("1");

        int maxlen = 0;
        for(String segment : segments) {
            if(segment.length() > maxlen) {
                maxlen = segment.length();
            }
        }
        // 把每个segement(0字符串)压缩成l-bit, 如000...00(10个0)压缩成1010, l=4
        int l = (int)Math.ceil(Math.log(maxlen+1)/Math.log(2));
        int compLen = segments.length*l;
        System.out.println("The compressed filter vector length:"+compLen);

        // TODO 修改! 对论文中的公式的理解
        double executionTime = 2.4 * segments.length / (96*1.0 / l);
        logger.info("The execution time for 1 round of phase one:"+executionTime);
        return executionTime;
    }


}
