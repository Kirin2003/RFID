package utils;

import base.Tag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VectorMap {
    private List<Integer> filterVector;
    private Map<Integer, List<Tag>> resultMap;

    public VectorMap(List<Integer> filterVector, Map<Integer, List<Tag>> resultMap) {
        this.filterVector = filterVector;
        this.resultMap = resultMap;
    }

    /**
     * Method used to create a vector map for the reader
     * @param frameSize frame size
     * @param random1 random1
     * @param functionID ???
     * @param theTagList list of tags
     * @return vector map
     */
    public static VectorMap genBaseVectorMap(int frameSize, int random1, int functionID, List<Tag> theTagList){
        List<Integer> filterVector = new ArrayList<>();
        Map<Integer, List<Tag>> resultMap = new HashMap<>();
        for (int i = 0; i < frameSize; i++){
            filterVector.add(0); // First it sets the vector to all 0s according to the frameSize
        }
        for (Tag tag: theTagList) {
            if (tag.isActive()){
                int index = tag.selectSlotPseudoRandom(frameSize, functionID, random1);
                filterVector.set(index, filterVector.get(index) + 1);
                if (resultMap.containsKey(index)){
                    resultMap.get(index).add(tag);
                } else {
                    List<Tag> nTagList = new ArrayList<>();
                    nTagList.add(tag);
                    resultMap.put(index, nTagList);
                }
            }
        }
        // TODO 删除了打印resultMap的部分
        return new VectorMap(filterVector, resultMap);
    }

    public List<Integer> getFilterVector() {
        return filterVector;
    }

    public Map<Integer, List<Tag>> getResultMap() {
        return resultMap;
    }
}
