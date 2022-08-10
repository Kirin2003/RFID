package utils;

import base.Location;
import base.Tag;
import base.TagListGenerator;
import base.TagRepository;
import jdk.javadoc.doclet.Taglet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * A class to simulate the environment
 */
public class Environment {
    private double length;
    private double width;

    private int readerX = 0;
    private int readerY = 0;
    private List<Reader_M> readerList = new ArrayList<>();

    private List<Tag> allTagList; // 期望标签+意外标签 expected tag + unexpected tag
    private List<Tag> expectedTagList; // 期望标签, 存在标签+缺失标签, present tag + missing tag
    private List<Tag> actualTagList; // 存在标签
    private int type = 0;

    private Logger logger = LogManager.getLogger(Environment.class);

    public Environment(List<Tag> allTagList, List<Tag> expectedTagList, List<Tag> actualTagList){
        this.allTagList = allTagList;
        this.expectedTagList = expectedTagList;
        this.actualTagList = actualTagList;
    }

    /**
     * Divide the environment of length * width into readersInRow * readersInColumn small rectangles equally, place a reader in the center of each rectangle,
     * total readersInRow * readersInColumn readers The reading range of the reader is a circle, and the diameter is the diagonal length of a small rectangle,
     * which just covers the rectangle. Place expectedTagNumber tags in the environment class, of which there are actually untagNumber
     * Initialize the readers and tags in the environment, ID, random sequence, location, and nothing more. Other initializations that
     * need to be used, as well as label coverage are all written separately
     * @param length Area length
     * @param width Area width
     * @param readersInRow Number of readers in a row
     * @param readersInColumn Number of readers in a column
     */
    public void createType1(double length, double width, int readersInRow, int readersInColumn){
        this.length = length;
        this.width = width;
        this.type = 0;
        this.setReaderX(readersInRow);
        this.setReaderY(readersInColumn);

        //Random allocation of tags
        for (Tag tag : allTagList) {
            double xx = Math.random() * length;
            double yy = Math.random() * width;
            Location location = new Location(xx, yy);
            tag.setLocation(location);
        }

        //Set up the reader
        setReaders(readersInRow, readersInColumn);

        // 初始化阅读器半径范围内的标签
        readerMInit();
    }

    /**
     * Similar to createType1, the only difference is that the tags are
     * expected to be evenly distributed instead of randomly distributed
     * @param length Area length
     * @param width Area width
     * @param readersInRow Number of readers in a row
     * @param readersInColumn Number of readers in a column
     */
    public void createType2(double length, double width, int readersInRow, int readersInColumn, int expectedTagNumber, int tagNum){
        this.length = length;
        this.width = width;
        this.type = 1;
        this.setReaderX(readersInRow);
        this.setReaderY(readersInColumn);

        //Tags are evenly distributed, particle size 10*10
        int M = 10;
        int N = 10;

        double lengthDivide = length / (double) M;
        double widthDivide = width / (double) N;

        int equalTagNumber = expectedTagNumber / (M * N);
        int rest = expectedTagNumber % (M * N);

        //Distribute parts evenly first
        for (int i = 0; i < M; i++) {
            for (int j = 0; j < N; j++) {
                for (int number = 0; number < equalTagNumber; number++) {
                    double xx = Math.random() * lengthDivide + (double) i * lengthDivide;
                    double yy = Math.random() * widthDivide + (double) j * widthDivide;
                    Location location = new Location(xx, yy);
                    if (xx > length || yy > width) {
                        System.out.println("TIP-ERROR　：　" + xx + "  " + yy);
                    }
                    expectedTagList.get(i * N * equalTagNumber + j * equalTagNumber + number).setLocation(location);
                }
            }
        }

        //Randomly distribute the rest
        for (int i = 0; i < rest; i++) {
            double xx = Math.random() * length;
            double yy = Math.random() * width;
            Location location = new Location(xx, yy);
            expectedTagList.get(M * N * equalTagNumber + i).setLocation(location);
        }

        // Randomly distribute the unknown tag list
        List<Tag> unknownTagList = TagRepository.less(allTagList,expectedTagList);
        for (Tag tag : unknownTagList) {
            double xx = Math.random() * length;
            double yy = Math.random() * width;
            Location location = new Location(xx, yy);
            tag.setLocation(location);
        }

        //Set up the reader
        setReaders(readersInRow, readersInColumn);

        // 初始化阅读器半径范围内的标签
        readerMInit();
    }

    /**
     * 初始化阅读器半径范围内的标签
     */
    public void readerMInit()
    {
        List<Reader_M> readerMList = getReaderList();

        //  设置期望列表
        for(Reader_M reader : readerMList)
        {
            reader.expectedTagList.addAll(getExpectedTagList());
            reader.coverActualTagList = reader.getReaderMOwnTagList(actualTagList);
            reader.coveredAllTagList = reader.getReaderMOwnTagList(allTagList);
            reader.realReply = reader.coverActualTagList.size();
        }

//        //  设置,覆盖范围,针对expectedTagList，实际可能不存在
//        for(Reader_M reader : readerMList)
//        {
//            int realReply =0;
//            for(Tag tag : getExpectedTagList())
//            {
//                double x = reader.getLocation().getX() - tag.getLocation().getX();
//                double y = reader.getLocation().getY() - tag.getLocation().getY();
//                if(reader.getCoverActualTagList()== null) { reader.setCoverActualTagList(new ArrayList<Tag>());  }
//                if(x * x + y * y < reader.getReadingRadius() * reader.getReadingRadius())
//                {
//                    reader.coverActualTagList.add(tag);
//                    if(getActualTagList().contains(tag)) { realReply++; }
//                }
//            }
//            reader.realReply = realReply;
//        }
    }

    /**
     * Displays information about the creation of environment, but it is suitable for output in the algorithm
     * verification file during the verification phase.
     * TODO 修改!
     */
    public void display(){
        logger.warn("==========================env setting=============================");
        // 输出环境信息/Output environment information
        logger.info("[length] : " + this.getLength() + "      [width] : " + this.getWidth());
        logger.warn("[reader]-[number] : " + this.getReaderList().size());

        for (Reader_M reader : readerList) {
            logger.info("    [ID] :" + reader.getID() + "    [Location] :(" + reader.getLocation().getX() + ","
                    + reader.getLocation().getY() + ")" + "    [readingRadius] :" + reader.getReadingRadius());
        }

        logger.warn("[tag]- [number] :" + "    [expected tag] :" + this.getExpectedTagList().size()
                + "    [present tag] :" + this.getActualTagList().size() + "    [absent tag] :"
                + (this.getExpectedTagList().size() - this.getActualTagList().size()));

        logger.debug("[expected info] :");
        for (Tag tag : this.getExpectedTagList()) {
            logger.debug("    [ID] :" + tag.getTagID() + "    [Location] :(" + tag.getLocation().getX() + ","
                    + tag.getLocation().getY() + ")" + "    [pseudoRanList] :" + tag.getPseudoRandomList().get(0));
        }

        logger.debug("[present info] :");
        for (Tag tag : this.getActualTagList()) {
            logger.debug("     [id] :" + tag.getTagID());
        }
        logger.debug("[absent info] :");
        for (Tag tag : expectedTagList) {
            if (actualTagList.contains(tag) == false) {
                logger.debug("     [id] :" + tag.getTagID());
            }
        }

        logger.debug("[reader-tag relation] :");
        for (Reader_M reader : readerList) {
            //
            List<Tag> pTagList = new ArrayList<>();
            for (Tag tag : expectedTagList) {
                if (Base_M.inCircle(reader, tag) && actualTagList.contains(tag)) {
                    pTagList.add(tag);
                }
            }
            logger.warn("     reader-" + reader.getID() + " : " + pTagList.size());
            logger.debug("      -->" + Base_M.getListIDInfo(pTagList));
        }

        logger.info("[tag-reader relation] :");
        for (Tag tag : this.getActualTagList()) {
            List<Reader_M> pRL = new ArrayList<>();
            for (Reader_M reader : readerList) {
                if (Base_M.inCircle(reader, tag) && actualTagList.contains(tag)) {
                    pRL.add(reader);
                }
            }
            logger.info("    tag-" + tag.getTagID() + " : " + pRL.size());
            logger.debug("      -->" + Base_M.getListIDInof(pRL));
        }
    }

    /**
     * Clears the original reader list and re-allocates the reader list
     * @param x
     * @param y
     */
    public void setReaders(int x, int y){
        readerList = new ArrayList<>();

        double readerRadius = Math.sqrt(length * length / x / x + width * width / y / y) / 2;

        for (int yy = 0; yy < y; yy++){
            for (int xx = 0; xx < x; xx++){
                Reader_M reader = new Reader_M(yy * x + xx + 1);
                reader.setReadingRadius(readerRadius);

                double locationX = xx * this.length / x + this.length / x / 2;
                double locationY = yy * this.width / y + this.width / y / 2;
                Location location = new Location(locationX, locationY);
                reader.setLocation(location);

                readerList.add(reader);
            }
        }
    }

    /**
     * The environment is reset. During the algorithm verification process, some algorithms will change some settings,
     * such as the active status of tags
     */
    public void reset() {
        for (Tag tag : expectedTagList) {
            tag.setActive(true);
        }
    }


    //Sets and gets
    public double getLength() {
        return length;
    }
    public List<Reader_M> getReaderList() {
        return readerList;
    }
    public void setReaderList(List<Reader_M> readerList) {
        this.readerList = readerList;
    }
    public List<Tag> getExpectedTagList() {
        return expectedTagList;
    }
    public void setExpectedTagList(List<Tag> expectedTagList) {
        this.expectedTagList = expectedTagList;
    }
    public List<Tag> getActualTagList() {
        return actualTagList;
    }
    public void setActualTagList(List<Tag> actualTagList) {
        this.actualTagList = actualTagList;
    }
    public void setLength(double length) {
        this.length = length;
    }
    public double getWidth() {
        return width;
    }
    public void setWidth(double width) {
        this.width = width;
    }
    public Logger getLogger() {
        return logger;
    }
    public void setLogger(Logger logger) {
        this.logger = logger;
    }
    public int getReaderX() {
        return readerX;
    }
    public void setReaderX(int readerx) {
        this.readerX = readerx;
    }
    public int getReaderY() {
        return readerY;
    }
    public void setReaderY(int readery) {
        this.readerY = readery;
    }
    public List<Tag> getAllTagList() {
        return allTagList;
    }
    public void setAllTagList(List<Tag> allTagList) {
        this.allTagList = allTagList;
    }
}
