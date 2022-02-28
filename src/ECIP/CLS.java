package ECIP;

import java.util.*;
import org.apache.log4j.Logger;



public class CLS {

    /**
     * By comparing slotToVirtualList and CidMap, remove the missing tag
     * If a slot  in the slotToVirtualList isn't in the CidMap, the corresponding tags are all missing
     */
    public  static int removeMissing(Map<Integer,List<Tag>> slotToVirtualList, Map<Integer, String> CidMap, List<Tag> virtualList, Logger logger, Set<String> totalMissingCids, String missingOutput) {

        logger.debug("\n");
        logger.debug("By comparing slotToVirtualList and CidMap, the reader removes the missing tag\n" +
                "     * If a slot  in the slotToVirtualList isn't in the CidMap, the corresponding tags are all missing");
        Set<String > missingCids = new HashSet<>();

        for (Integer slot : slotToVirtualList.keySet()) {
            if(!CidMap.containsKey(slot)) {

                for (Tag tag : slotToVirtualList.get(slot)) {

                    virtualList.remove(tag);
                    missingCids.add(tag.getCategoryID());

                    logger.debug("remove tag (cid:"+tag.getCategoryID()+")");
                }
            }
        }

        // 将识别为缺失的CID写入output
        for(String cid : missingCids) {
            missingOutput+="类别"+cid+"缺失\n";
            totalMissingCids.add(cid);
        }

        // After removing missing tags, update slotToVirtualList
        slotToVirtualList.clear();
        for (Tag tag:virtualList) {
            int slot = tag.getSlotSelected();
            if (!slotToVirtualList.keySet().contains(slot)) {
                List<Tag> l = new ArrayList<>();
                l.add(tag);
                slotToVirtualList.put(slot,l);
            } else {
                slotToVirtualList.get(slot).add(tag);
            }
        }

        logger.debug("after removing, virtualList:");
        //printList(virtualList);
        logger.debug("after removing, map slotToTagList:");
        //printSlotToVirtualList();

        //返回识别到缺失的类别ID数量，也就是missingcids的大小
        return missingCids.size();
    }
}
