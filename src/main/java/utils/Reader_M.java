package utils;

import base.Location;
import base.Tag;

import java.util.ArrayList;
import java.util.List;

public class Reader_M {
    protected int ID = 0;//阅读器id
    protected Location location = new Location(); // 位置坐标
    protected double readingRadius; // 阅读器作用半径,
    private List<Tag> coverActualTagList = new ArrayList<>(); // 覆盖范围内的存在标签, 主要用于识别存在和缺失标签
    public int realReply;
    public List<Tag> coveredAllTagList = new ArrayList<>(); // 覆盖范围内的全部标签, 主要用于意外标签去除阶段
    public List<Tag> expectedTagList = new ArrayList<>(); // 期望标签, 其实是环境中全部的期望标签, 而不是覆盖范围内的期望标签, 因为阅读器对期望标签的分布并没有多余的知识


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
    public List<Tag> getCoverActualTagList() {
        return coverActualTagList;
    }
    public void setCoverActualTagList(List<Tag> coverActualTagList) {
        this.coverActualTagList = coverActualTagList;
    }
}
