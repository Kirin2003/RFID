package utils;

import base.Tag;

import java.util.*;

public class VectorMap {
    private List<Integer> filterVector; // 存储时隙中类别id的数目
    private Map<Integer, List<Tag>> resultMap; // 存储时隙中的标签列表
    private Map<Integer, Set<String>> slotToCidSet; // 存储时隙中类别id集合

    public VectorMap(List<Integer> filterVector, Map<Integer, List<Tag>> resultMap) {
        this.filterVector = filterVector;
        this.resultMap = resultMap;
    }

    public VectorMap(List<Integer> filterVector, Map<Integer, List<Tag>> resultMap, Map<Integer, Set<String>> slotToCidSet) {
        this.filterVector = filterVector;
        this.resultMap = resultMap;
        this.slotToCidSet = slotToCidSet;
    }

    //    /**
//     * Method used to create a vector map for the reader
//     * @param frameSize frame size
//     * @param random1 random1
//     * @param functionID ???
//     * @param theTagList list of tags
//     * @return vector map
//     */
//
//    public static VectorMap genBaseVectorMap(int frameSize, int random1, int functionID, List<Tag> theTagList){
//        List<Integer> filterVector = new ArrayList<>();
//        Map<Integer, Set<String>> resultMap = new HashMap<>();
//        for (int i = 0; i < frameSize; i++){
//            filterVector.add(0); // First it sets the vector to all 0s according to the frameSize
//        }
//        for (Tag tag: theTagList) {
//            if (tag.isActive()){
//                int index = tag.selectSlotPseudoRandom(frameSize, functionID, random1);
//                filterVector.set(index, filterVector.get(index) + 1);
//                if (resultMap.containsKey(index)){
//                    resultMap.get(index).add(tag.getCategoryID());
//                } else {
//                    Set<String> nCidList = new HashSet<>();
//                    nCidList.add(tag.getCategoryID());
//                    resultMap.put(index, nCidList);
//                }
//            }
//        }
//
//        return new VectorMap(filterVector, resultMap);
//    }

    /**
     * Similar to genBaseVectorMap, the only difference is that when tag selects frame, it uses category id instead of tag id
     * @param frameSize frame size
     * @param random1 random1
     * @param functionID ???
     * @param theTagList list of tags
     * @return vector map
     */
    // TODO
    public static VectorMap genBaseVectorMap2(int frameSize, int random1, int functionID, List<Tag> theTagList){
        List<Integer> filterVector = new ArrayList<>();
        Map<Integer, List<Tag>> resultMap = new HashMap<>();
        Map<Integer, Set<String>> slotToCidSet = new HashMap<>();
        for (int i = 0; i < frameSize; i++){
            filterVector.add(0); // First it sets the vector to all 0s according to the frameSize
        }
        for (Tag tag: theTagList) {
            if (tag.isActive()){
                int index = tag.hash2(frameSize, random1);

                if (resultMap.containsKey(index)){
                    resultMap.get(index).add(tag);
                    slotToCidSet.get(index).add(tag.getCategoryID());
                } else {
                    List<Tag> nTagList= new ArrayList<>();
                    resultMap.put(index,nTagList);
                    Set<String> nCidSet = new HashSet<>();
                    nCidSet.add(tag.getCategoryID());
                    slotToCidSet.put(index, nCidSet);
                }
            }
        }

        // filter vector 记录了该时隙中类别id的数目
        for(Integer slot : resultMap.keySet()) {

            filterVector.set(slot,slotToCidSet.get(slot).size());
        }

        return new VectorMap(filterVector, resultMap,slotToCidSet);
    }

    public List<Integer> getFilterVector() {
        return filterVector;
    }

    public Map<Integer, List<Tag>> getResultMap() {
        return resultMap;
    }

    public Map<Integer, Set<String>> getSlotToCidSet() {
        return slotToCidSet;
    }
}
