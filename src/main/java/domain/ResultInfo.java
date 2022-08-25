package domain;

public class ResultInfo {
    /** 标签ID长度 */
    public  Integer tagIDLength = 96;
    /** 类别ID长度 */
    public  Integer categoryIDLength = 32;
    /** 缺失率 */
    public  Double missingRate = 0.8;
    /** 标签数目 */
    public  Integer tagNum = 10000;

    /** 是否随机分配*/
    public  Boolean isRandomAllocated = true;
    /** 标签/类别ID */
    public  Integer tagNumPerCid = 100;

    /**算法重复模拟次数*/
    public int instanceNumber = 1;

    /** 意外标签占的比例*/
    public Double unknownRate = 0.1;

    /** 仓库长度*/
    public Integer repository_leng = 4000;

    /** 仓库宽度*/
    public Integer repository_wid = 2000;

    /** 每行的阅读器数目*/
    public Integer readerInRow = 5;

    /** 每列的阅读器数目*/
    public Integer readerInCol = 2;

    /** 标签随机分布/均匀分布*/
    public boolean isTagRandomlyDistributed = true;


    public Integer mostMissingTagNum = 80;

    public String preciousCid = "10101010101010101010101010101010";


    public int getExpectedTagNum(){
        return (int)(Math.ceil(tagNum * (1-unknownRate)));
    }
    public int getUnknownTagNumber(){
        return (int)(Math.ceil(tagNum * unknownRate));
    }
    public int getActualTagNum(){
        return (int)(Math.ceil(tagNum * (1-unknownRate) * (1-missingRate)));
    }

    public int getMissingTagNum() {
        return (int)(Math.ceil(tagNum * (1-unknownRate)*missingRate));
    }

    public enum Algorithms{
        Cip,
        Ecip,
        ECLS,
        EDLS;
    }

    public Algorithms a = Algorithms.Cip;

    public boolean propertiesChanged = true;

    public boolean warningChanged = true;

    public Algorithms getA() {
        return a;
    }

    public Integer getTagIDLength() {
        return tagIDLength;
    }

    public Integer getCategoryIDLength() {
        return categoryIDLength;
    }

    public Double getMissingRate() {
        return missingRate;
    }

    public Integer getTagNum() {
        return tagNum;
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
                "tagLength=" + tagIDLength +
                ", cidLength=" + categoryIDLength +
                ", missingRate=" + missingRate +
                ", tagNum=" + tagNum +

                ", isRandomAllocated=" + isRandomAllocated +
                ", tagNumPerCid=" + tagNumPerCid +
                ", a=" + a +
                '}';
    }
}
