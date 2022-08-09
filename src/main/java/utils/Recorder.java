package utils;

import java.util.ArrayList;
import java.util.List;

public class Recorder {
    public double totalExecutionTime = 0; // 总执行时间

    public int roundCount = 0; // 总轮数
    public int slotCount = 0; // 总时隙数

    public int emptySlotCount = 0; // 总空时隙数
    public int singletonSlotCount = 0; // 总单时隙数
    public int collisionSlotCount = 0; // 总冲突时隙数

    public int recognizedNum = 0; // 识别到的标签总数
    public int recognizedMissingTagNum = 0; // 识别到的缺失标签总数
    public int recognizedActualTagNum = 0; // 识别到的存在标签总数

    public int eliminationNum = 0;// 总去除的意外标签数

    public List<Integer> frameSizeList = new ArrayList<>(); // 每一轮的时隙长度
    public List<String> missingCids = new ArrayList<>(); // 识别到的缺失标签列表
    public List<String> actualCids = new ArrayList<>(); // 识别到的存在标签列表

    public List<Integer> recognizeNumList = new ArrayList<>();//每一轮的识别标签的总数

    public List<Integer> recognizeMissingNumList = new ArrayList<>(); // 每一轮的识别的缺失标签的总数
    public List<Integer> recognizeActualNumList = new ArrayList<>(); // 每一轮的识别存在标签的总数


    public List<Double> executionTimeList = new ArrayList<>(); // 每一轮的执行时间
}