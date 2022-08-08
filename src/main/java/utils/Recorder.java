package utils;

import java.util.ArrayList;
import java.util.List;

public class Recorder {
    public double totalExecutionTime = 0;

    public int roundCount;
    public int slotCount;

    public int emptySlotCount;
    public int singletonSlotCount;
    public int collisionSlotCount;

    public int recognizedNum = 0;
    public int recognizedMissingTagNum = 0;
    public int recognizedActualTagNum = 0;

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