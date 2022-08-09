package base;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Tag {


    public String tagID = null;
    public String categoryID = null;
    protected boolean active = true;
    protected int slotSelected;

    public int presentTime = 0;
    public List<Integer> readerIdList = new ArrayList<>();

    private Location location = new Location();

    protected List<String> pseudoRandomList = new ArrayList<>(); //List of tags which contains all the Tags IDs
    protected int pseudoRanStrLen; //Length of the TagID
    protected int pseudoRanListLen; //Length of the tag List

    public Tag(int pseudoRanStrLen,int pseudoRanListLen) {
        initPseudoRandomList(pseudoRanStrLen, pseudoRanListLen);
    }

    public Tag() {
        initPseudoRandomList(100, 15);
    }

    public Tag(String tagID, String categoryID) {
        initPseudoRandomList(100, 15);
        this.tagID = tagID;
        this.categoryID = categoryID;
    }

    /**
     * Initializing function which fills the pseudoRandomList list with pseudo random tag IDs
     * @param pseudoRanStrLen
     * @param pseudoRanListLen
     */
    public void initPseudoRandomList(int pseudoRanStrLen, int pseudoRanListLen){
        this.pseudoRanStrLen = pseudoRanStrLen;
        this.pseudoRanListLen = pseudoRanListLen;
        for (int i = 0; i < pseudoRanListLen; i++){
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < pseudoRanStrLen; j++)
                sb.append(((int)(10 * Math.random()))%2);
            pseudoRandomList.add(sb.toString());
        }
    }

    /**
     * Fill the map and save the correspondence between the Expected Tag List and The Chosen Slot by Tag
     * @param map Vector Map
     * @param index Index of the tag
     * @param tag Tag
     */
    public void fillMap(Map<Integer, List<Tag>> map, int index, Tag tag){
        if (map != null){
            if (map.containsKey(index)){
                map.get(index).add(tag);
            }else {
                List<Tag> tagList = new ArrayList<>();
                tagList.add(tag);
                map.put(index, tagList);
            }
        }
    }

    /**
     * Fill the map and save the correspondence between the Expected Tag ID and The Chosen Slot by Tag
     * @param map
     * @param index
     * @param tmpStr
     */
    public void fillMap(Map<Integer, List<String>> map, int index, String tmpStr){
        if (map != null){
            if (map.containsKey(index)){
                map.get(index).add(tmpStr);
            }else {
                List<String> strList = new ArrayList<>();
                strList.add(tmpStr);
                map.put(index, strList);
            }
        }
    }

    /**
     * Pseudo hash function
     * @param frameSize
     * @param hashFunID
     * @param randomNum
     * @return
     */
    public int selectSlotPseudoRandom(int frameSize, int hashFunID, int randomNum){
        //Round up log2 (frameSize) and add one, the effect is to add one to the length of the binary representation of frameSize
        int bitStr = (int) Math.ceil((Math.log(frameSize)/Math.log(2))) + 1;
        //startIndex is the remainder after dividing by 100
        int startIndex = randomNum % pseudoRanStrLen;
        //Take a 100-bit binary random number from a list of pseudorandom numbers
        String curStr = pseudoRandomList.get(hashFunID);
        String tmpStr = "";
        //The judgment is that startIndex + bitStr is less than or equal to 100
        if ((startIndex + bitStr) <= curStr.length()){
            //At this time, curStr, that is, a part of the pre-generated 100-bit string of 0-1, is intercepted as tmr
            tmpStr = curStr.substring(startIndex, (startIndex + bitStr));
        } else {
            //If it exceeds, the characters of the bit after startIndex are removed from the front and the back, and added to the back.
            tmpStr = curStr.substring(startIndex) + curStr.substring(0, (bitStr - (curStr.length() - startIndex)));
        }
        //Then parse the generated tmpStr as a binary number into a decimal value, and then calculate its remainder to framesize
        slotSelected = Integer.valueOf(tmpStr, 2) % frameSize;
        return slotSelected;
    }



    /**
     * Newly added hash function used for bloom filter
     * @param frameSize Frame Size
     * @param random Random NUmber
     * @return
     */
    public int hash1(int frameSize, int random){
        int p = random;
        int hash = 0;
        for(int i = 0; i < tagID.length(); i++){
            hash = Math.abs((int) (hash * 13331 + (tagID.charAt(i) == '0' ? 0 : p + 131 * i * tagID.charAt(i))));
            p = Math.abs(p * 131 + i * tagID.charAt(i));
        }
        slotSelected = Math.abs(hash % frameSize);
        return slotSelected;
    }

    /**
     * Similar to hash1, the only difference is this hash function uses category id instead of tag id
     * @param frameSize Frame Size
     * @param random Random NUmber
     * @return
     */
    public int hash2(int frameSize, int random){
        int p = random;
        int hash = 0;
        for(int i = 0; i < categoryID.length(); i++){
            hash = Math.abs((int) (hash * 13331 + (categoryID.charAt(i) == '0' ? 0 : p + 131 * i * categoryID.charAt(i))));
            p = Math.abs(p * 131 + i * categoryID.charAt(i));
        }
        slotSelected = Math.abs(hash % frameSize);
        return slotSelected;
    }

    /**
     * Tag Selects a Slot
     * @param slotID selected slot
     * @return slotID
     */
    public SlotResponse executeSlot(int slotID){
        if (active == true){
            return slotID == slotSelected ? new SlotResponse(0, tagID, this) : null;
        }
        return null;
    }



    //Gets and Sets
    public String getTagID() {
        return tagID;
    }

    public void setTagID(String tagID) {
        this.tagID = tagID;
    }

    public String getCategoryID() {
        return categoryID;
    }

    public void setCategoryID(String categoryID) {
        this.categoryID = categoryID;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getSlotSelected() {
        return slotSelected;
    }

    public void setSlotSelected(int slotSelected) {
        this.slotSelected = slotSelected;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void setPseudoRandomList(List<String> pseudoRandomList) {
        this.pseudoRandomList = pseudoRandomList;
    }

    public List<String> getPseudoRandomList() {
        return pseudoRandomList;
    }
}
