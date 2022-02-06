package ECIP;

import java.math.BigInteger;
import java.util.*;

public class Tag {
    private String tagID = null;   //tag ID

    private String categoryID = null;//category ID

    private int slotSelected;

    private boolean active = true;



    private String partialCid;

    public Tag() {
    }

    public Tag(String cid, int slotId) {
        categoryID = cid;
        slotSelected = slotId;
    }

    public Tag(String tagID, String categoryID) {
        this.tagID = tagID;
        this.categoryID = categoryID;
    }
    /**
     * in cip / ecip random identification phase
     * Upon receiving the Query<f,i>, each tag selects a slot based on the random hash function H(CID, r) mod f, and responds its CID in this slot
     * the selected slot is stored in tag's data member: slotSelected
     * @param frameSize the frameSize of each round
     * @param random random seed
     * @return categoryID
     */
    @SuppressWarnings("DuplicatedCode")
    public String selectSlotPseudoRandom(int frameSize, int random){
        // H(CID, r) mod f
        slotSelected = (int) ((Long.parseLong(categoryID, 2) + random) % frameSize);
        return categoryID;
    }

    /**
     * in ecip rearranged identification phase
     * if the char at xindex of tag's categoryID is '0',it will respond in 2*j-th time slot, otherwise (2*j+1)-th
     *  and responds its partialCategoryID in this slot
     * @param j
     * @param xindex
     * @return partial categoryID
     */
    public int selectSlotBasedOnXIndex(int j, int xindex) {
//        int slot;
        //System.out.println("in Tag::selectSlotBasedOnXIndex");
        char x = categoryID.charAt(xindex);
        if (x == '0') {
            slotSelected = 2*j;
        } else {
            slotSelected = 2*j+1;
        }

        //System.out.println("slotSelected = " + slotSelected);

        partialCid = categoryID.substring(xindex+1);
        return slotSelected;


    }



    public void setTagID(String tagID) {
        this.tagID = tagID;
    }

    public void setCategoryID(String categoryID) {
        this.categoryID = categoryID;
    }

    public String getCategoryID() {
        return categoryID;
    }

    public Integer getSlotSelected() {
        return slotSelected;
    }

    public void setSlotSelected(int slotSelected) {
        this.slotSelected = slotSelected;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getPartialCid() {
        return partialCid;
    }

}
