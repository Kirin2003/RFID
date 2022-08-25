package protocals;

import base.Tag;
import base.TagListGenerator;
import base.TagRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.Environment;
import utils.Recorder;

import java.util.List;

/**
 * @author Kirin Huang
 * @date 2022/8/12 下午4:53
 */
public class ECLS_Main {
    public static void main(String[] args) {
        int instanceNumber = 1;
        int allTagNumber = 5000;
        int unknownTagNumber = 0;
        int expectedTagNum = allTagNumber - unknownTagNumber;
        int missingTagNumber = 4900;
        int tagIDLength = 14;
        int categoryIDLength = 32;
        Logger logger = LogManager.getLogger(ECLS_Main.class);

        logger.error("Total number of tags: [" + allTagNumber + "]");
        logger.error("Total number of expected tags: [" + (allTagNumber - unknownTagNumber) + "]");
        logger.error("Actual number of tags: [" + (allTagNumber - missingTagNumber - unknownTagNumber) + "]");

        Recorder recorder = new Recorder();

        for (int r = 0; r < instanceNumber; r++){
            logger.error("<<<<<<<<<<<<<<<<<<<< Instance: " + r + ">>>>>>>>>>>>>>>>>>>");

            TagRepository tagRepository = TagListGenerator.generateTagRepository(tagIDLength, categoryIDLength, allTagNumber, 10,unknownTagNumber, missingTagNumber);
            List<Tag> allTagList = tagRepository.getAllTagList();
            List<Tag> expectedTagList = tagRepository.getExpectedTagList();
            List<Tag> tagList = tagRepository.getActucaltagList();


            Environment environment = new Environment(allTagList, expectedTagList, tagList,expectedTagNum/10);

            environment.createType1(4000, 1600, 2, 5);

            IdentifyTool edls = new ECLS(logger,recorder,environment);
            edls.execute();
        }
    }
}
