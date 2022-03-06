package domain;

public class ResultInfo {
    /** 标签ID长度 */
    public  Integer tagLength = 96;
    /** 类别ID长度 */
    public  Integer cidLength = 32;
    /** 缺失率 */
    public  Double missingRate = 0.95;
    /** 标签数目 */
    public  Integer tagNum = 100000;
    /** 时隙数目 */
    public  Integer f =160;
    /** 是否随机分配*/
    public  Boolean isRandomAllocated = true;
    /** 标签/类别ID */
    public  Integer tagNumPerCid = 100;

    public Integer mostMissingTagNum = 100;

    public String preciousCid = "10101010101010101010101010101010";


    public enum Algorithms{
        Cip,
        Ecip,
        ECIPwithCLS,
        ECIPwithDLS;
    }

    public Algorithms a = Algorithms.Cip;

    public boolean propertiesChanged = true;

    public boolean warningChanged = true;

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

    public Integer getVirtualCidNum() {

        // 无缺失时，显然正确
        // 有缺失时，目标是找到印证每个类是缺失还是存在，正确
        return (int)Math.ceil( (tagNum*1.0/tagNumPerCid) );
    }

    public Integer getActualCidNum() {
        return (int)Math.ceil(tagNum *(1-missingRate) /tagNumPerCid);
    }


    public void setA(Algorithms a) {
        this.a = a;

    }

    @Override
    public String toString() {
        return "ResultInfo{" +
                "tagLength=" + tagLength +
                ", cidLength=" + cidLength +
                ", missingRate=" + missingRate +
                ", tagNum=" + tagNum +
                ", f=" + f +
                ", isRandomAllocated=" + isRandomAllocated +
                ", tagNumPerCid=" + tagNumPerCid +
                ", a=" + a +
                '}';
    }
}
