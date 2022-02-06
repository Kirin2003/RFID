package ECIP;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

public class CLS {

    /**
     * By comparing slotToVirtualList and CidMap, remove the missing tag
     * If a slot  in the slotToVirtualList isn't in the CidMap, the corresponding tags are all missing
     */
    public  static void removeMissing(Map<Integer,List<Tag>> slotToVirtualList, Map<Integer, String> CidMap, List<Tag> virtualList, Logger logger) {

        logger.debug("\n");
        logger.debug("By comparing slotToVirtualList and CidMap, the reader removes the missing tag\n" +
                "     * If a slot  in the slotToVirtualList isn't in the CidMap, the corresponding tags are all missing");
        for (Integer slot : slotToVirtualList.keySet()) {
            if(!CidMap.containsKey(slot)) {

                for (Tag tag : slotToVirtualList.get(slot)) {

                    virtualList.remove(tag);

                    logger.debug("remove tag (cid:"+tag.getCategoryID()+")");
                }
            }
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
    }
}
