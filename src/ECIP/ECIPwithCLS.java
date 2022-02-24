package ECIP;

import java.util.*;
import org.apache.log4j.Logger;


public class ECIPwithCLS extends IdentifyTool{
    // 得到记录器
    public static Logger logger =  Logger.getLogger("ecip");


    protected List<Tag> virtualList;
    protected List<Tag> actualList;
    protected Map<Integer,List<Tag>> slotToVirtualList = new HashMap<>(); // key: slot, value: virtual tags which are allocated to the slot

    protected Map<Integer, String> CidMap = new HashMap<>(); //key: slotId. value: the overlapped cid

    protected Map<Integer, Integer> indicator = new HashMap<>();
    protected  Vector<Integer> location = new Vector<>();
    protected  Vector<String> d = new Vector<>();

    protected int unReadCidNum;


    protected int f1 = 0;//random arranged identification phase的时隙
    protected int f2 = 0;// rearranged identification phase的时隙

    protected Set<String> totalPresentCids = new HashSet<>();
    public Set<String> totalMissingCids = new HashSet<>();
    public int missingCidNum = 0;
    public int totalPresence = 0;
    public int totalMissing = 0;
    public int totalCidNeedToIdentify = 0;
    public int round = 0;
    boolean flag = true;

    // 无缺失率
    public ECIPwithCLS(List<Tag> taglist, int unReadCids, int f1, int f2) {
        this(taglist, taglist, unReadCids, f1, f2);
    }

    public ECIPwithCLS(List<Tag> tagList, int unReadCids, int f1) {
        this(tagList,tagList,unReadCids,f1);
    }

    public ECIPwithCLS(List<Tag> tagList, int unReadCids) {
        this.actualList = tagList;
        this.virtualList = tagList;
        this.unReadCidNum = unReadCids;
        this.totalCidNeedToIdentify = unReadCids;
    }

    public ECIPwithCLS(List<Tag> virtualList, List<Tag> actualList, int unReadCids, int f) {
        this.virtualList = virtualList;
        this.actualList = actualList;
        this.f1 = f;

        this.unReadCidNum = unReadCids;
        this.totalCidNeedToIdentify = unReadCids;
        System.out.println("unreadCidNum ="+unReadCidNum);

    }

    // 有缺失率，主要用于高缺失率
    public ECIPwithCLS(List<Tag> virtualList, List<Tag> actualList, int unReadCids, int f1, int f2){
        this.virtualList = virtualList;
        this.actualList = actualList;
        this.f1 = f1;
        this.f2 = f2;
        this.unReadCidNum = unReadCids;
        logger.debug("\n");
        logger.debug("virtual tag list size ="+virtualList.size()+" actual tag list size ="+actualList.size());
        logger.debug("virtual tag list: ");
        //printList(virtualList);
        logger.debug("actual tag list: ");
        //printList(actualList);
    }

    /**
     * The reader allocates slots randomly for tags in virtual list
     * @param frameSize the size of frame
     * @param random a random number
     */
    public void allocateRandomly(int frameSize, int random) {
        slotToVirtualList.clear();


        for (Tag tag : virtualList) {
            tag.selectSlotPseudoRandom(frameSize,random);
            int slot = tag.getSlotSelected();

//            logger.debug("tag:"+tag.getCategoryID());
//            logger.debug("slot:"+tag.getSlotSelected());

            if (!slotToVirtualList.keySet().contains(slot)) {
                List<Tag> l = new ArrayList<>();
                l.add(tag);
                slotToVirtualList.put(slot,l);
            } else {
                slotToVirtualList.get(slot).add(tag);
            }
        }

        logger.debug("\n");
        logger.debug("The reader allocates slots randomly");
        logger.debug("map slotToVirtualList");
        //printSlotToVirtualList();

    }

    private void printSlotToVirtualList() {
        for (Integer slot : slotToVirtualList.keySet()) {
            System.out.println("slot="+slot);
            for (Tag tag : slotToVirtualList.get(slot)) {
                System.out.println(tag.getCategoryID());
            }
            System.out.println(" ");
        }
    }

    public void printList(List<Tag> tagList) {
        for (Tag tag : tagList) {
            System.out.println(tag.getCategoryID());
        }
    }

    /**
     * Actual tags select slot
     * @param f the size of frame
     * @param r a random number
     */
    public void selectRandomly(int f, int r) {
        CidMap.clear();

        logger.debug("tags in actual list select slots");
        for (Tag tag : actualList) {
            int slot = tag.getSlotSelected();
            String cid = tag.getCategoryID();

            logger.debug("tag cid:"+tag.getCategoryID());
            logger.debug("slot:"+tag.getSlotSelected());
            if (!CidMap.keySet().contains(slot)) { // the slot is empty
                CidMap.put(slot,cid);
            } else { // the slot already has tag and cid
                // overlap cid
                String code = CidMap.get(slot);
                String newCode = encode(code,cid);
                CidMap.put(slot,newCode);
            }
        }

        logger.debug("\n");
        logger.debug("Actual tags select slot");
        logger.debug("print CidMap. key: slot; value: overlapped code");
        //
        //printCidMap();

    }

    /**
     * By comparing slotToVirtualList and CidMap, remove the missing tag
     * If a slot  in the slotToVirtualList isn't in the CidMap, the corresponding tags are all missing
     */
    public void removeMissing() {

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

    /**
     * The random identification phase includes 4 parts:
     * First, the reader allocate slots for virtual tag list
     * Second, tags in actual tag list select slot and respond to the reader
     * Third, the reader receives the replies from tags, and remove missing tags
     * Fourth, identify cid
     * @return  the number of the identified cids
     */
    public int randomIdentificationPhase() {
        Set<String> missingCids = new HashSet<>();
        String presenceOutput = "";
        String missingOutput = "";
        int presenceNum = 0;
        int missingNum = 0;

        logger.debug("\n");
        logger.debug("in random identification phase");

        int random = (int)(100*Math.random());

        int frameSize = f1; // the size of frame is at least 20

        allocateRandomly(frameSize,random);
        //printSlotToVirtualList();
        selectRandomly(frameSize, random);
        //printCidMap();

        missingNum+=CLS.removeMissing(slotToVirtualList,CidMap,virtualList,logger,totalMissingCids,missingOutput);
        // 将缺失的类别ID打印出来


        //System.out.println("virtualList:");printList(virtualList);
        //System.out.println(" ");

        //System.out.println("actualList: ");printList(actualList);
        //System.out.println(" ");
        //identify
        indicator.clear();
        int readCidNumInOneRound = 0; // useless
        int i = 0;


        logger.debug("CidMap:");
        //printCidMap();

        // 输出识别开始
        round ++;
        output += "第"+round+"轮（随机分配阶段）开始！\n";
        for(Integer slot : CidMap.keySet()){
            String[] strs = decode(CidMap.get(slot));
            int l = strs.length;


            if(l == 1) {
                readCidNumInOneRound+=1;
                presenceNum += 1;
                totalPresentCids.add(strs[0]);
                presenceOutput+="类别"+strs[0]+"存在\n";



                for (Tag tag : slotToVirtualList.get(slot)) {
                    virtualList.remove(tag);
                    if (actualList.contains(tag)) actualList.remove(tag);
                    if(tag.getCategoryID()!=strs[0]) missingCids.add(tag.getCategoryID());
                }



                indicator.put(slot, -1);

                logger.debug("in slot NO."+slot+" code = "+CidMap.get(slot));
                logger.debug("identify one tag cid: "+strs[0]);
            } else if(l == 2){
                readCidNumInOneRound+=2;
                presenceNum += 2;
                totalPresentCids.add(strs[0]);
                totalPresentCids.add(strs[1]);
                presenceOutput+="类别"+strs[0]+"存在\n";
                presenceOutput+="类别"+strs[1]+"存在\n";
                for(Tag tag : slotToVirtualList.get(slot)){
                    virtualList.remove(tag);
                    if(actualList.contains(slot)) actualList.remove(slot);
                    if(tag.getCategoryID()!=strs[0] && tag.getCategoryID()!=strs[1]) missingCids.add(tag.getCategoryID());
                }

                indicator.put(slot, -1);

                logger.debug("in slot NO."+slot+" code = "+CidMap.get(slot));
                logger.debug("identify two tag cid: "+strs[0] + " "+ strs[1]);
            } else {
                indicator.put(slot, i);
                i++;


                logger.debug("in slot NO."+slot+" code = "+CidMap.get(slot));
                logger.debug("identify none tag cid");
            }
        }

        //存在的类别ID存在presenceOutput里
        // 缺失的类别ID存在missingOutput里
        for(String cid : missingCids) {
            missingOutput+="类别"+cid+"缺失！\n";
            totalMissingCids.add(cid);
        }

        // 一轮识别结束后，整合存在的类别和缺失的类别
        output+=presenceOutput;
        output+=missingOutput;

        // 一轮结束后，整合存在的类别数目和缺失的类别数目
        missingNum = missingCids.size();
        totalMissing += missingNum;
        totalPresence += presenceNum;

        // 输出本轮结果
        output+="第"+round+"轮（随机分配阶段）结束！"+"本轮识别类别ID数："+(presenceNum+missingNum)+", 其中存在的类别ID数："+presenceNum+", 缺失的类别ID数："+missingNum+"\n\n";

        if(CidMap.size() == 0) {
            flag = false;
            output+="没有标签回应\n";
        }

        //System.out.println(" ");System.out.println("virtual list");printList(virtualList);
        logger.info("in this round, read "+readCidNumInOneRound+" cids");
        return readCidNumInOneRound;
    }

    public int randomIdentificationPhaseNoRemoving() {
        logger.debug("\n");
        logger.debug("in random identification phase");
//        long time = System.currentTimeMillis();
//        Random r = new Random(time);
//        int random = r.nextInt(unReadTags);
//
//        int frameSize = Math.max(unReadTags,20); // the size of frame is at least 20
        int random = (int) (100 * Math.random());
        int frameSize =f1;

        allocateRandomly(frameSize,random);
        //printSlotToVirtualList();
        selectRandomly(frameSize, random);
        //printCidMap();

        //removeMissing();
        //System.out.println("virtualList:");printList(virtualList);
        //System.out.println(" ");

        //System.out.println("actualList: ");printList(actualList);
        //System.out.println(" ");
        //identify
        indicator.clear();
        int readCidNumInOneRound = 0;
        int i = 0;

        logger.debug("CidMap:");
        //printCidMap();
        for(Integer slot : CidMap.keySet()){
            String[] strs = decode(CidMap.get(slot));
            int l = strs.length;


            if(l == 1) {
                for (Tag tag : slotToVirtualList.get(slot)) {
                    virtualList.remove(tag);
                    if (actualList.contains(tag)) actualList.remove(tag);
                }
                readCidNumInOneRound+=1;
                totalPresentCids.add(strs[0]);
                output+=strs[0]+"\n";


                indicator.put(slot, -1);

                logger.debug("in slot NO."+slot+" code = "+CidMap.get(slot));
                logger.debug("identify one tag cid: "+strs[0]);
            } else if(l == 2){
                for(Tag tag : slotToVirtualList.get(slot)){
                    virtualList.remove(tag);
                    if(actualList.contains(slot)) actualList.remove(slot);

                }

                readCidNumInOneRound+=2;
                totalPresentCids.add(strs[0]);
                totalPresentCids.add(strs[1]);
                output+=strs[0]+"\n";
                output+=strs[1]+"\n";

                indicator.put(slot, -1);

                logger.debug("in slot NO."+slot+" code = "+CidMap.get(slot));
                logger.debug("identify two tag cid: "+strs[0] + " "+ strs[1]);
            } else {
                flag = true;
                logger.info("ji xu xun huan");
                indicator.put(slot, i);
                i++;

                logger.debug("in slot NO."+slot+" code = "+CidMap.get(slot));
                logger.debug("identify none tag cid");
            }
        }

        //System.out.println(" ");System.out.println("virtual list");printList(virtualList);
        logger.info("in this round, read "+readCidNumInOneRound+" cids");
        return readCidNumInOneRound;
    }

    /**
     * The reader allocates slot based on  x index, tags belonging to the same slot is divided to one or two parts
     */
    public void allocateBasedOnXIndex() {

        Map<Integer,List<Tag>> newSlotToVirtualList = new HashMap<>();

        /*
        重写：hxq, 2022-1-28
         */
        for (Tag tag : virtualList) {
            int j = indicator.get(tag.getSlotSelected());
            if(j != -1) {
                int xindex = location.get(j);
                int newSlot = tag.selectSlotBasedOnXIndex(j, xindex);
                if (!newSlotToVirtualList.containsKey(newSlot)) {
                    List<Tag> newTagList = new ArrayList<>();
                    newTagList.add(tag);
                    newSlotToVirtualList.put(newSlot,newTagList);
                } else {
                    newSlotToVirtualList.get(newSlot).add(tag);
                }
            }
        }
        slotToVirtualList = newSlotToVirtualList;


//        for (Integer slot : slotToVirtualList.keySet()) {
//            int j = indicator.get(slot);
//            if (j != -1) {
//                int xindex = location.get(j);
//
//                for(Tag tag : slotToVirtualList.get(slot)) {
//                    int newSlot = tag.selectSlotBasedOnXIndex(j,xindex);
//
//                    logger.debug("tag cid:"+tag.getCategoryID()+" new slot:"+newSlot);
//                    if (!newSlotToVirtualList.containsKey(newSlot)) {
//                        List<Tag> newTagList = new ArrayList<>();
//                        newTagList.add(tag);
//                        newSlotToVirtualList.put(newSlot,newTagList);
//                    } else {
//                        newSlotToVirtualList.get(newSlot).add(tag);
//                    }
//                }
//            }
//        }
//
//        slotToVirtualList = newSlotToVirtualList;
        logger.debug("\n");
        logger.debug("The reader allocates slot based on  x index, tags belonging to the same slot is divided to one or two parts");
        logger.debug("after allocating, new slotToVirtualList:");
        //printSlotToVirtualList();
        //System.out.println(" ");printSlotToVirtualList();
    }

    public void allocateBasedOnXIndexNoRemove() {

        for (Integer slot : slotToVirtualList.keySet()) {
            if (CidMap.containsKey(slot)) {
                int j = indicator.get(slot);
                if (j!=-1) {
                    int xindex = location.get(j);
                    for (Tag tag: slotToVirtualList.get(slot)) {
                        tag.selectSlotBasedOnXIndex(j,xindex);
                    }
                }
            } else {
                long time = System.currentTimeMillis();
                Random r = new Random(time);
                int random = r.nextInt();
                for (Tag tag: slotToVirtualList.get(slot)){
                    tag.selectSlotPseudoRandom(f2,random);
                }
            }
        }

        slotToVirtualList.clear();

        for (Tag tag : virtualList) {
            int newSlot = tag.getSlotSelected();
            if (!slotToVirtualList.containsKey(newSlot)) {
                List<Tag> newTagList = new ArrayList<>();
                newTagList.add(tag);
                slotToVirtualList.put(newSlot,newTagList);
            } else {
                slotToVirtualList.get(newSlot).add(tag);
            }
        }

        logger.debug("\n");
        logger.debug("The reader allocates slot based on  x index, tags belonging to the same slot is divided to one or two parts");
        logger.debug("after allocating, new slotToVirtualList:");
        //printSlotToVirtualList();
        //System.out.println(" ");printSlotToVirtualList();
    }

    /**
     * Tags in actual tag list select slots based on x index and respond to the reader
     */
    public void responseBasedOnXIndex() {
        CidMap.clear();

        logger.debug("\n");
        logger.debug("vector indicator:");
        //printIndicator();
        logger.debug("vector location");
        //printLocation();
        logger.debug("structure d");
        //printStructureD();
        logger.debug("Tags in actual tag list select slots based on x index and respond to the reader");
        for (Tag tag : actualList) {
            int slot = tag.getSlotSelected();

            logger.debug("tag cid:"+tag.getCategoryID()+" new slot:"+slot);
//            int j = indicator.get(slot);
//            if (j != -1) {
//                int xindex = location.get(j);
                String partialCid = tag.getPartialCid();
                if (!CidMap.containsKey(slot)) {
                    CidMap.put(slot, partialCid);
                } else {
                    String code = CidMap.get(slot);
                    String newCode = encode(code, partialCid);
                    CidMap.put(slot, newCode);
                }
            }


            logger.debug("after selecting, new CidMap: ");
            //printCidMap();
        }


    /**
     * The rearranged identification phase includes 5 parts:
     * First, construct location vector and structure d in preparation for the following part
     * Second, the reader allocates slots based on x index
     * Third, tags in actual list select and respond
     * Fourth, the reader receive replies, and remove missing tags
     * Fifth, identify cid
     * @return
     */
    public int rearrangedIdentificationPhase() {
        Set<String> missingCids = new HashSet<>();
        String presenceOutput = "";
        String missingOutput = "";
        int presenceNum = 0;
        int missingNum = 0;

        logger.debug("\n");
        logger.debug("The reader allocates slot based on  x index, tags belonging to the same slot is divided to one or two parts");
        logger.debug("in rearranged identification phase");

        constructLocationAndStructureD();

        allocateBasedOnXIndex();

        responseBasedOnXIndex();

        missingNum += CLS.removeMissing(slotToVirtualList,CidMap,virtualList,logger,totalMissingCids,missingOutput);

        //System.out.println(" ");System.out.println("Virtual list");printList(virtualList);
        //System.out.println(" ");System.out.println("actual list");printList(actualList);
        // identify

        // 输出识别开始
        round ++;
        output += "第"+round+"轮（重新分配阶段）开始！\n";

        indicator.clear();
        int i = 0;
        int readCidNum = 0;
        System.out.println("in rearranged identification phase");

        logger.info("cid map size = "+CidMap.size());
        for (Integer slot : CidMap.keySet()) {
            String[] strs = decode(CidMap.get(slot));
            int l = strs.length;

            if (l == 1) {
                readCidNum++;
                String cid1 = combine(slot, strs[0]);
                totalPresentCids.add(cid1);
                presenceOutput+="类别"+cid1+"存在\n";
                presenceNum += 1;

                indicator.put(slot,-1);

                logger.debug("in slot NO." + slot + " code = " + CidMap.get(slot));
                logger.debug("identify one tag cid: " + strs[0]);

                for (Tag tag : slotToVirtualList.get(slot)) {
                    virtualList.remove(tag);
                    if (actualList.contains(tag)) actualList.remove(tag);
                    if(tag.getCategoryID() != cid1) {
                        missingCids.add(cid1);
                    }
                }


            } else if (l == 2) {
                readCidNum += 2;
                String cid1 = combine(slot, strs[0]);
                String cid2 = combine(slot, strs[1]);

                totalPresentCids.add(cid1);
                totalPresentCids.add(cid2);

                presenceOutput+="类别"+cid1+"存在\n";
                presenceOutput+="类别"+cid2+"存在\n";

                presenceNum += 2;
                for (Tag tag : slotToVirtualList.get(slot)) {
                    virtualList.remove(tag);
                    if (actualList.contains(slot)) actualList.remove(slot);
                    if(tag.getCategoryID()!=cid1 && tag.getCategoryID()!=cid2) missingCids.add(tag.getCategoryID());

                }


                indicator.put(slot,-1);

                logger.debug("in slot NO." + slot + " code = " + CidMap.get(slot));
                logger.debug("two partial tag cid: " + strs[0] + " " + strs[1]);
                logger.debug("identify two tag cid: " + cid1 + " " + cid2);
            } else {

                indicator.put(slot,i);
                i++;
                logger.debug("in slot NO." + slot + " code = " + CidMap.get(slot));
                logger.debug("identify none tag cid");
            }
        }


        //存在的类别ID存在presenceOutput里
        // 缺失的类别ID存在missingOutput里
        for(String cid : missingCids) {
            missingOutput+="类别"+cid+"缺失！\n";
            totalMissingCids.add(cid);
        }

        // 一轮识别结束后，整合存在的类别和缺失的类别
        output+=presenceOutput;
        output+=missingOutput;

        // 一轮结束后，整合存在的类别数目和缺失的类别数目
        missingNum = missingCids.size();
        totalMissing += missingNum;
        totalPresence += presenceNum;

        // 输出本轮结果
        output+="第"+round+"轮（重新分配阶段）结束！"+"本轮识别类别ID数："+(presenceNum+missingNum)+", 其中存在的类别ID数："+presenceNum+", 缺失的类别ID数："+missingNum+"\n\n";


        if(CidMap.size() == 0)  {
            flag = false;
            output+="没有标签回应\n";
        } // 退出条件，如果没有标签回应，那所有存在的标签都回应完，剩余的都是缺失的

        logger.info("in this round, read "+readCidNum+" cids");
        return readCidNum;

    }

    public int rearrangedIdentificationPhaseNotRemove() {
        logger.debug("\n");
        logger.debug("The reader allocates slot based on  x index, tags belonging to the same slot is divided to one or two parts");
        logger.debug("in rearranged identification phase");

        constructLocationAndStructureD();

        allocateBasedOnXIndexNoRemove();

        responseBasedOnXIndex();

        //printCidMap();
        int readCidNumInOneRound = 0;
        int i = 0;
        indicator.clear();

        for (Integer slot : CidMap.keySet()) {
            String[] strs = decode(CidMap.get(slot)) ;
            int l = strs.length;

            if(l == 1) {
                for (Tag tag : slotToVirtualList.get(slot)) {
                    virtualList.remove(tag);
                    if (actualList.contains(tag)) actualList.remove(tag);
                }

                readCidNumInOneRound++;
                String cid1 = combine(slot,strs[0]);
                totalPresentCids.add(cid1);
                indicator.put(slot,-1);

                logger.debug("in slot NO."+slot+" code = "+CidMap.get(slot));
                logger.debug("identify one tag cid: "+strs[0]);
            } else if(l == 2){
                for(Tag tag : slotToVirtualList.get(slot)){
                    virtualList.remove(tag);
                    if(actualList.contains(slot)) actualList.remove(slot);
                }

                readCidNumInOneRound+=2;
                String cid1 = combine(slot,strs[0]);
                String cid2 = combine(slot,strs[1]);

                totalPresentCids.add(cid1);
                totalPresentCids.add(cid2);

                indicator.put(slot,-1);

                logger.debug("in slot NO."+slot+" code = "+CidMap.get(slot));
                logger.debug("two partial tag cid: "+strs[0] + " "+ strs[1]);
                logger.debug("identify two tag cid: "+cid1 + " "+ cid2);
            } else{
                flag = true;
                logger.info("ji xu xun huan");

                indicator.put(slot,i);
                i++;

                logger.debug("in slot NO."+slot+" code = "+CidMap.get(slot));
                logger.debug("identify none tag cid");
            }
        }

        // 新增，更新slot to virtual list
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

        logger.info("in this round, read "+readCidNumInOneRound+" cids");
    
        return readCidNumInOneRound;

    }



    /**
     * The reader keeps identify cids, repeat random identification phase and rearranged identification phase, until all tags are identified.
     */
    public double identifyAll() {
        int repeated = 0;
        int round = 1;
        int num;
        int cidnum = 0;

        // 第一阶段：random identification phase
        logger.info("round = "+round);
        //output+="第 "+round+" 轮开始（随机分配阶段）！\n";
        num = randomIdentificationPhase();
        cidnum+=num;
        //output+="在第 "+round+" 轮（随机分配阶段），共识别 "+num+" 个类别ID\n\n";
        round ++;
        if(num == 0) repeated++;



        //第二阶段：重复进行rearranged identification phase
        while (flag) {
           // output+="第 "+round+" 轮开始（重新分配阶段）！\n";

            flag = true;
            logger.info("round = "+round);
            num = rearrangedIdentificationPhase();



            cidnum+=num;
           // output+="在第 "+round+" 轮（重新分配阶段），共识别 "+num+" 个类别ID\n\n";

            round++;
            if (num == 0) repeated ++;

            if (repeated >= 30){
                System.out.println("round = " + round);
                System.out.println("repeated round > 30, stop!");

                output+="没有识别类别ID的轮次超过30, 停止！\n";
                output+="需要识别的类别ID数目："+(totalCidNeedToIdentify)+", 识别的类别ID数量："+(totalPresence+totalMissing)+", 存在的类别ID数量："+totalPresence+", 缺失的类别ID数目："+totalMissing+", 识别率："+(totalPresence+totalMissing)*1.0/(unReadCidNum+cidnum)+", 需要时间约： "+time*1.0/1000 + " s\n";
                output+="模拟结束！\n";

                analysis+="需要识别的类别ID数目："+(totalCidNeedToIdentify)+", 识别的类别ID数量："+(unReadCidNum+cidnum)+", 存在的类别ID数量："+cidnum+", 缺失的类别ID数目："+unReadCidNum+", 识别率：100%"+", 需要时间约： "+time*1.0/1000 + " s\n";
                break;
            }

        }
        System.out.println("识别完成");
        if(repeated < 30) {
            // 已经没有标签回应了，virtual list的所有标签都是缺失的
            Set<String> missingCids = new HashSet<>();
            for(Tag tag : virtualList) {
                missingCids.add(tag.getCategoryID());
            }

            // 输出缺失的类别ID，和缺失的数量,
            String missingOutput = "";
            int missingNum = missingCids.size();
            for(String cid : missingCids) {
                missingOutput += "类别"+cid+"缺失\n";
            }
            output+=missingOutput;
            totalMissing += missingNum;



            output+="识别结束！\n";
            output+="需要识别的类别ID数目："+(totalCidNeedToIdentify)+", 识别的类别ID数量："+(totalCidNeedToIdentify)+", 识别率：100%"+", 需要时间约： "+time*1.0/1000 + " s\n";
            //分析结果，全部存在，全部缺失，部分存在部分缺失


            // 1 具体cid
            Set<String> pset = new HashSet<>();
            Set<String> mset = new HashSet<>();
            Set<String >pmset = new HashSet<>();

            for(String cid : totalPresentCids) {
                if(totalMissingCids.contains(cid)) {
                    pmset.add(cid);
                }
                else {
                    pset.add(cid);
                }
            }

            for(String cid : totalMissingCids) {
                if(!totalPresentCids.contains(cid)) {
                    mset.add(cid);
                }
            }

            // 2 数目
            int pm = pmset.size();
            int p = pset.size();
            int m = mset.size();

            System.out.println(pset.size());
            System.out.println(mset.size());
            System.out.println(pmset.size());

            output+="全部标签都存在的类别ID数量："+p+"分别是：\n";
            for(String cid : pset) {
                output+="类别"+cid+"\n";
            }
            output+="全部标签都缺失的类别ID数量："+m+"分别是：\n";
            for(String cid : mset) {
                output+="类别"+cid+"\n";
            }
            output+="部分标签存在部分标签缺失的类别ID数量："+pm+"分别是：\n";
            for(String cid : pmset) {
                output+="类别"+cid+"\n";
            }

            output+="模拟结束！\n";
            output+="需要识别的类别ID数目："+(totalCidNeedToIdentify)+", 识别的类别ID数量："+(p+m+pm)+", 识别率："+(p+m+pm)*1.0/totalCidNeedToIdentify+", 需要时间约： "+time*1.0/1000 + " s\n";
            output+="全部标签都存在的类别ID数量："+p+", 全部标签都缺失的类别ID数量："+m+", 部分存在部分缺失的类别ID数量："+pm+"\n";

            analysis+="需要识别的类别ID数目："+(totalCidNeedToIdentify)+", 识别的类别ID数量："+(unReadCidNum+cidnum)+", 存在的类别ID数量："+cidnum+", 缺失的类别ID数目："+unReadCidNum+", 识别率："+(p+m+pm)*1.0/totalCidNeedToIdentify+", 需要时间约： "+time*1.0/1000 + " s\n";

        }

        double totaltime = time();
        return totaltime;
    }


    public double ecipNoRemove() {
        int repeated = 0;
        int round = 1;
        int num=0;
        int cidnum = 0;

        // 第一阶段：random identification phase
        logger.info("round = "+round);
        num = randomIdentificationPhaseNoRemoving();
        cidnum+=num;
        round ++;
        if(num == 0) repeated++;

        //第二阶段：重复进行rearranged identification phase
        while (flag) {
            flag = false;

            logger.info("round = "+round);
            num = rearrangedIdentificationPhaseNotRemove();
            cidnum+=num;
            round++;
            if (num == 0) repeated ++;

            if (repeated > 30){
                System.out.println("round = " + round);
                System.out.println("repeated round > 30, stop!");
                break;
            }

        }

        double totaltime = time();
        System.out.println("需要时间约：: "+totaltime*1.0/1000 + "s");
        System.out.println("识别的cid数目 = "+cidnum);
        return totaltime;
    }

    // 计算时间
    public double time() {
        double time = 0;
        // 计算时间
        double d = unReadCidNum /f1;
        // the time of random allocation phase T_p1
        double t1 = f1*Math.exp(-d)*(-0.8)+f1*1.2;
        time += t1;
        //oneRoundTime += t1;

        // the time of broadcasting indicator vector in the first round:T_i
        double ti = f1 * (d*Math.exp(-d)+d)*(2.8)/96;
        time += ti;

        // 计算rearranged identification phase的时间 T_cs
        double tcs = 2.4*(unReadCidNum -f2+f2*Math.exp(-unReadCidNum *1.0/f2));
        time+=tcs;
        return time;
    }

    /**
     * overlap category ID
     * @param s1
     * @param s2
     * @return overlapped CID
     */

    private String encode(String s1, String s2){
        //System.out.println("s1 = " + s1+ "s2 =" + s2);
        int i = 0;
        StringBuilder stringBuilder = new StringBuilder();
        while(i < s1.length()){
            if(s1.charAt(i) == s2.charAt(i)){
                stringBuilder.append(s1.charAt(i));
            }else{
                // If the bits at the same position in strings from different tags are not the same, the reader will decode this bit as ‘X’
                stringBuilder.append('X');
            }

            i++;
        }
        return stringBuilder.toString();
    }


    /**
     * exploit Manchester coding scheme to decode the aggregated signals received in collision slots.
     * @param data:
     */
    protected String[] decode(String data){
        // empty slot
        if (data == null){
            return null;
        }
        // category-compatible slot
        int index1 = data.indexOf('X');
        if (index1 == -1){
            // the reader receives the data with no collision bit ‘X’, it identifies a CID
            return new String[]{data};
        }
        int index2 = data.lastIndexOf('X');
        if (index1 == index2){
            // the reader receives the data with only one collision bit ‘X’, it identifies two CIDs. One is the collision bit ‘X’ is set to ‘0’, the other is set to ‘1’.
            String s1 = data.replace('X', '0');
            String s2 = data.replace('X', '1');
            return new String[]{s1, s2};
        }
        // category-collision slot
        return new String[]{};
    }



    /**
     * construct location vector and structure d
     *Each element corresponds to an unidentified category-
     * collision slot and indicates the index X index of the first
     * collision bit ‘X’ in this slot.
     */
    protected void constructLocationAndStructureD() {

        Vector<Integer> newLocation = new Vector<>();
        Vector<String > newStructureD = new Vector<>();
        logger.info("in construct location and structure d ");
        logger.info("location size = "+location.size());
        logger.info("cid map size = "+CidMap.size());

        //System.out.println("in constructAndStructureD()");
//        System.out.println(" ");
//        System.out.println("indicator");
//        printIndicator();
//        System.out.println(" ");
//        System.out.println("location");
//        printLocation();
//        System.out.println(" ");
//        System.out.println("cid map");
//        printCidMap();
//        System.out.println(" ");
        for (Integer slotID : indicator.keySet()) {


            if (indicator.get(slotID) != -1) {
                int i = indicator.get(slotID);//第i个冲突时隙

                //System.out.println("slot:"+slotID);
                // category-collision
                String data = CidMap.get(slotID);
                //System.out.println("cid map data:"+data);
                int xindex;
                String strBeforeX;
                if(location.isEmpty()) { // 第1次rearranged identification phase，data和cid长度相等
                    xindex = data.indexOf('X');
                    newLocation.add(xindex);
                    strBeforeX = data.substring(0, data.indexOf('X'));
                    //System.out.println("str before x:"+strBeforeX);
                    newStructureD.add(strBeforeX);
                } else { // 之后的rearranged identification phase，此时data比cid长度短，计算xindex时要注意
                    // 保留上一轮的location vector, structure D
                    logger.info("location index = "+slotID/2);
                    logger.info("cid map index = "+slotID);
                    xindex = location.get(slotID/2)+CidMap.get(slotID).indexOf('X')+1;
                    //System.out.println("i :"+i);
                    //System.out.println("new xindex:"+xindex);
                    newLocation.add(xindex);
                    //System.out.println("");
                    strBeforeX = d.get(slotID/2)+CidMap.get(slotID).substring(0,CidMap.get(slotID).indexOf('X'))+((slotID)%2);
                    //System.out.println("str before x:"+strBeforeX);
                    newStructureD.add(strBeforeX);

                }



            }
        }

        location = newLocation;
        d = newStructureD;

        //System.out.println("location vector:");
        //printLocation();
        //System.out.println("structure d");
        //printStructureD();

    }

    protected void constructLocationAndStructureDNoRemove() {
        location.clear();
        d.clear();
        for (Integer slotID : indicator.keySet()) {

            if (indicator.get(slotID) != -1) {
               if (CidMap.containsKey(slotID)) {
                   // category-collision
                   String data = CidMap.get(slotID);
                   int xindex = data.indexOf('X');
                   location.add(xindex);
                   String strBeforeX = data.substring(0,xindex);
                   d.add(strBeforeX);
               }
            }
        }
    }


    /**
     * combine CID
     * @param slotID
     * @param after string after x index
     * @return CID
     */
    protected String combine(int slotID, String after) {

        int j = slotID/2;
        String before = d.get(j);
        String x = String.valueOf(slotID%2);
        //System.out.println("j = " + j + "before = " + before + "x=" + x);
        //System.out.println();

        return before+x+after;


    }
    

    public void printCidMap() {
        for (Integer slotId : CidMap.keySet()) {
            System.out.println("slotId = " + slotId + " code = " + CidMap.get(slotId));
        }
    }

    public void printLocation() {
        for (int i = 0; i < location.size(); i++) {
            System.out.println("the "+ i + "-th collision slot : xindex = " + location.get(i));
        }
    }
    public void printStructureD() {
        for (int i = 0; i < d.size(); ++i) {
            System.out.println("the " + i + "-th collision slot : code before x = "+ d.get(i));
        }
    }

    public void printIndicator() {
        for (Integer slotId : indicator.keySet()) {
            System.out.println("slotId = " + slotId + " indicator = " + indicator.get(slotId));
        }
    }

//    public void setTagList(List<Tag> tagList) {
//        this.tagList = tagList;
//    }


}