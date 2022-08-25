package utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Recorder {
    public double totalExecutionTime = 0; // 总执行时间

    public int roundCount = 0; // 总轮数
    public int slotCount = 0; // 总时隙数

    public int emptySlotCount = 0; // 总空时隙数
    public int singletonSlotCount = 0; // 总单时隙数
    public int collisionSlotCount = 0; // 总冲突时隙数
    public int noResult = 0;

    public int recognizedTagNum = 0; // 识别到的标签总数
    public int recognizedMissingTagNum = 0; // 识别到的缺失标签总数
    public int recognizedActualTagNum = 0; // 识别到的存在标签总数

    public int recognizedCidNum = 0; // 识别到的类别总数
    public int recognizedMissingCidNum = 0; // 识别到的缺失类别总数
    public int recognizedActualCidNum = 0; // 识别到的存在类别总数
    public int eliminationTagNum = 0;// 总去除的意外标签数


    public List<Integer> roundSlotCountList = new ArrayList<>(); // 每一轮的时隙长度
    public Set<String> missingCids = new HashSet<>(); // 识别到的缺失标签列表
    public Set<String> actualCids = new HashSet<>(); // 识别到的存在标签列表

    public List<Integer> recognizedTagNumList = new ArrayList<>();//每一轮的识别标签的总数

    public List<Integer> recognizeMissingTagNumList = new ArrayList<>(); // 每一轮的识别的缺失标签的总数
    public List<Integer> recognizeActualTagNumList = new ArrayList<>(); // 每一轮的识别存在标签的总数
    public List<Integer> recognizedCidNumList = new ArrayList<>(); // 每一轮识别的类别总数
    public List<Integer> recognizedMissingCidNumList = new ArrayList<>(); // 每一轮识别的缺失类别总数
    public List<Integer> recognizedActualCidNumList = new ArrayList<>(); // 每一轮识别的存在类别总数

    public List<Double> executionTimeList = new ArrayList<>(); // 每一轮的执行时间
    public List<Double> missingRateList = new ArrayList<>(); // 每一轮的缺失率

    /**
     * 识别结束后得到结果分析
     * @return
     */
    public String getAnalysis() {
        StringBuilder sb = new StringBuilder();
        sb.append("-----------------------识别结果-----------------------\n");
        sb.append("识别存在的类别如下:\n");
        for(String str : actualCids) {
            sb.append(str+"\n");
        }
        sb.append("识别缺失的类别如下:\n");
        for(String str : missingCids) {
            sb.append(str+"\n");
        }
        return sb.toString();
    }
}