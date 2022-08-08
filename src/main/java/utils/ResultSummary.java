package utils;

import java.util.ArrayList;
import java.util.List;

public class ResultSummary {
    List<String> slotcount= new ArrayList<>();
    List<String> frameSizeCount= new ArrayList<>();
    List<String> filtervectorlength=new ArrayList<String>();
    List<String> zeroSlot=new ArrayList<String>();
    List<String> oneSlot=new ArrayList<String>();
    List<String> twoSlot=new ArrayList<String>();
    List<String> threeSlot=new ArrayList<String>();
    List<String> moreThanThreeSlot=new ArrayList<String>();
    List<String> bloomfilterSizeCount=new ArrayList<String>();
    List<String> recognizedTagNum=new ArrayList<String>();
    List<String> numcounttime=new ArrayList<String>();

    /**
     * Result Analyzing Method
     * @param approachName
     */
    public void analyzeResult(String approachName){
        String slotCountStr="slotcount_"+approachName+"=[";
        for(String s:slotcount){
            slotCountStr+=s+" ";
        }
        slotCountStr+="]";
        System.out.println(slotCountStr);

        String frameSizeCountStr="frameSizeCount_"+approachName+"=[";
        for(String s:frameSizeCount){
            frameSizeCountStr+=s+" ";
        }
        frameSizeCountStr+="]";
        System.out.println(frameSizeCountStr);

        String filtervectorCountStr="filtervectorlength_"+approachName+"=[";
        for(String s:filtervectorlength){
            filtervectorCountStr+=s+" ";
        }
        filtervectorCountStr+="]";
        System.out.println(filtervectorCountStr);

        String zeroSlotStr="zero_"+approachName+"=[";
        for(String s:zeroSlot){
            zeroSlotStr+=s+" ";
        }
        zeroSlotStr+="]";
        System.out.println(zeroSlotStr);

        String oneSlotStr="one_"+approachName+"=[";
        for(String s:oneSlot){
            oneSlotStr+=s+" ";
        }
        oneSlotStr+="]";
        System.out.println(oneSlotStr);

        String twoSlotStr="two_"+approachName+"=[";
        for(String s:twoSlot){
            twoSlotStr+=s+" ";
        }
        twoSlotStr+="]";
        System.out.println(twoSlotStr);

        String threeSlotStr="three_"+approachName+"=[";
        for(String s:threeSlot){
            threeSlotStr+=s+" ";
        }
        threeSlotStr+="]";
        System.out.println(threeSlotStr);

        String moreThanThreeSlotStr="moreThanThree_"+approachName+"=[";
        for(String s:moreThanThreeSlot){
            moreThanThreeSlotStr+=s+" ";
        }
        moreThanThreeSlotStr+="]";
        System.out.println(moreThanThreeSlotStr);


        String bloomfilterSizeCountStr="bloomfilterSize_"+approachName+"=[";
        for(String s:bloomfilterSizeCount){
            bloomfilterSizeCountStr+=s+" ";
        }
        bloomfilterSizeCountStr+="]";
        System.out.println(bloomfilterSizeCountStr);


        String recognizedTagNumStr="recognizedTagNumStr_"+approachName+"=[";
        for(String s:recognizedTagNum){
            recognizedTagNumStr+=s+" ";
        }
        recognizedTagNumStr+="]";
        System.out.println(recognizedTagNumStr);

        String numcounttimeStr="numcounttimeStr_"+approachName+"=[";
        for(String s:numcounttime){
            numcounttimeStr+=s+" ";
        }
        numcounttimeStr+="]";
        System.out.println(numcounttimeStr);

    }

    //Sets and Gets
    public List<String> getSlotcount() {
        return slotcount;
    }
    public void setSlotcount(List<String> slotcount) {
        this.slotcount = slotcount;
    }
    public List<String> getFrameSizeCount() {
        return frameSizeCount;
    }
    public void setFrameSizeCount(List<String> frameSizeCount) {
        this.frameSizeCount = frameSizeCount;
    }
    public List<String> getFiltervectorlength() {
        return filtervectorlength;
    }
    public void setFiltervectorlength(List<String> filtervectorlength) {
        this.filtervectorlength = filtervectorlength;
    }
    public List<String> getOneSlot() {
        return oneSlot;
    }
    public void setOneSlot(List<String> oneSlot) {
        this.oneSlot = oneSlot;
    }
    public List<String> getTwoSlot() {
        return twoSlot;
    }
    public void setTwoSlot(List<String> twoSlot) {
        this.twoSlot = twoSlot;
    }
    public List<String> getThreeSlot() {
        return threeSlot;
    }
    public void setThreeSlot(List<String> threeSlot) {
        this.threeSlot = threeSlot;
    }

    public List<String> getBloomfilterSizeCount() {
        return bloomfilterSizeCount;
    }
    public void setBloomfilterSizeCount(List<String> bloomfilterSizeCount) {
        this.bloomfilterSizeCount = bloomfilterSizeCount;
    }
    public List<String> getMoreThanThreeSlot() {
        return moreThanThreeSlot;
    }
    public void setMoreThanThreeSlot(List<String> moreThanThreeSlot) {
        this.moreThanThreeSlot = moreThanThreeSlot;
    }
    public List<String> getZeroSlot() {
        return zeroSlot;
    }
    public void setZeroSlot(List<String> zeroSlot) {
        this.zeroSlot = zeroSlot;
    }
    public List<String> getRecognizedTagNum() {
        return recognizedTagNum;
    }
    public void setRecognizedTagNum(List<String> recognizedTagNum) {
        this.recognizedTagNum = recognizedTagNum;
    }

    public List<String> getnumcounttime() {
        return numcounttime;
    }
    public void setnumcounttime(List<String> numcounttime) {
        this.numcounttime = numcounttime;
    }
}
