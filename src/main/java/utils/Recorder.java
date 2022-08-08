package utils;

import java.util.ArrayList;
import java.util.List;

public class Recorder {
    public double totalExecutionTime = 0;

    public int roundCount = 0;
    public int slotCount = 0;

    public int emptySlotCount = 0;
    public int singletonSlotCount = 0;
    public int collisionSlotCount = 0;

    public int recognizedNum = 0;
    public int recognizedMissingTagNum = 0;
    public int recognizedActualTagNum = 0;

    public int eliminationNum = 0;

    public List<Integer> frameSizeList = new ArrayList<>();
    public List<String> missingCids = new ArrayList<>();
    public List<String> actualCids = new ArrayList<>();

    /**
     * 每一轮的已经识别的数目
     * The number of tags identified in each round
     */

    public List<Integer> recognizeNumList = new ArrayList<>();

    public List<Integer> recognizeMissingNumList = new ArrayList<>();
    public List<Integer> recognizeActualNumList = new ArrayList<>();


    public List<Double> executionTimeList = new ArrayList<>();



}