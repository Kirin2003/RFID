package domain;

import ECIP.Tag;
import java.util.*;



public class ResultInfo {
    /** 标签ID长度 */
    public  Integer tagLength = 96;
    /** 类别ID长度 */
    public  Integer cidLength = 32;
    /** 缺失率 */
    public  Double missingRate = 0.75;
    /** 标签数目 */
    public  Integer tagNum = 10000;
    /** 时隙数目 */
    public  Integer f =98;
    /** 是否随机分配*/
    public  Boolean isRandomAllocated = true;
    /** 标签/类别ID */
    public  Integer tagNumPerCid = 100;


    public enum Algorithms{
        Cip,
        Ecip,
        ECIPwithCLS,
        ECIPwithDLS;
    }

    public Algorithms a = Algorithms.ECIPwithCLS;

    public boolean propertiesChanged = true;

    public boolean algorithmsChanged = true;



    public Algorithms getA() {
        return a;
    }

    public Integer getTagLength() {
        return tagLength;
    }

    public Integer getCidLength() {
        return cidLength;
    }

    public Double getMissingRate() {
        return missingRate;
    }

    public Integer getTagNum() {
        return tagNum;
    }

    public Integer getF() {
        return f;
    }

    public Boolean getRandomAllocated() {
        return isRandomAllocated;
    }

    public Integer getTagNumPerCid() {
        return tagNumPerCid;
    }

    public Integer getUnReadCidNum() {

        // 无缺失时，显然正确
        // 有缺失时，目标是找到印证每个类是缺失还是存在，正确
        return (int)Math.ceil( (tagNum*1.0/tagNumPerCid) );
    }

    public boolean isPropertiesChanged() {
        return propertiesChanged;
    }

    public boolean isAlgorithmsChanged() {
        return algorithmsChanged;
    }

    public void setTagLength(Integer tagLength) {
        this.tagLength = tagLength;
        propertiesChanged = true;
    }

    public void setCidLength(Integer cidLength) {
        this.cidLength = cidLength;
        propertiesChanged = true;
    }

    public void setMissingRate(Double missingRate) {
        this.missingRate = missingRate;
        propertiesChanged = true;
    }

    public void setTagNum(Integer tagNum) {
        this.tagNum = tagNum;
        propertiesChanged = true;
    }

    public void setF(Integer f) {
        this.f = f;
        propertiesChanged = true;
    }

    public void setRandomAllocated(Boolean randomAllocated) {
        isRandomAllocated = randomAllocated;
        propertiesChanged = true;
    }

    public void setTagNumPerCid(Integer tagNumPerCid) {
        this.tagNumPerCid = tagNumPerCid;
        propertiesChanged = true;
    }

    public void setA(Algorithms a) {
        this.a = a;
        algorithmsChanged = true;
    }

    //    public static List<Tag> virtualList = null;
//    public static List<Tag> actualList = null;
//    public static Integer unReadCids = 0;
//    public static Vector<String> tids = null;
//    public static Vector<String> cids = null;




}
