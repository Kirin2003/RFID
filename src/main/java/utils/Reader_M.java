package utils;

import base.Location;
import base.Tag;

import java.util.ArrayList;
import java.util.List;

public class Reader_M {
    protected int ID = 0;
    protected Location location = new Location();
    protected double readingRadius;
    private List<Tag> coverExpectedTagList = new ArrayList<>();
    public int realReply;
    public List<Tag> coverUnknowTagList = new ArrayList<>();


    public Reader_M(){

    }

    public Reader_M(int ID){
        this.ID = ID;
    }

    public Reader_M(int ID, Location location, double readingRadius){
        this.ID = ID;
        this.location = location;
        this.readingRadius = readingRadius;
    }

    /**
     * Returns the present tags under the readers' coverage region
     * @param tagList
     * @return
     */
    public List<Tag> getReaderMOwnTagList(List<Tag> tagList){
        List<Tag> presentTagList = new ArrayList<>();
        double R = this.getReadingRadius();
        double readerX = this.getLocation().getX();
        double readerY = this.getLocation().getY();

        for(Tag tag:tagList){
            double tagX = tag.getLocation().getX();
            double tagY = tag.getLocation().getY();
            if(Math.pow(R,2) > Math.pow(readerX-tagX,2)+Math.pow(readerY-tagY,2)){
                presentTagList.add(tag);
                tag.presentTime++;
                tag.readerIdList.add(this.getID());

            }
        }
        return presentTagList;
    }



    public int getID() {
        return ID;
    }
    public void setID(int iD) {
        ID = iD;
    }
    public Location getLocation() {
        return location;
    }
    public void setLocation(Location location) {
        this.location = location;
    }
    public double getReadingRadius() {
        return readingRadius;
    }
    public void setReadingRadius(double readingRadius) {
        this.readingRadius = readingRadius;
    }
    public List<Tag> getCoverExpectedTagList() {
        return coverExpectedTagList;
    }
    public void setCoverExpectedTagList(List<Tag> coverExpectedTagList) {
        this.coverExpectedTagList = coverExpectedTagList;
    }
}
