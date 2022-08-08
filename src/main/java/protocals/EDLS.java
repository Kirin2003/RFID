package protocals;

import base.Tag;
import org.apache.logging.log4j.Logger;
import utils.Environment;
import utils.Recorder;

import java.util.List;

/**
 * @author Kirin Huang
 * @date 2022/8/8 下午10:19
 */
public class EDLS extends IdentifyTool{
    public EDLS(Logger logger, Recorder recorder, Environment environment, int warningNum, String warningCid) {
        super(logger, recorder, environment, warningNum, warningCid);
    }

    @Override
    public void execute() {

    }

    @Override
    public void unexpectedTagElimination(int numberOfHashFunctions, double falsePositiveRatio) {

    }

    @Override
    public void identify(List<Tag> expectedTagList) {

    }
}
