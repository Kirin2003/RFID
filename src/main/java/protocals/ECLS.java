package protocals;

import base.Tag;
import org.apache.logging.log4j.Logger;
import utils.Environment;
import utils.Reader_M;
import utils.Recorder;

import java.util.List;

/**
 * @author Kirin Huang
 * @date 2022/8/8 下午10:19
 */
public class ECLS extends IdentifyTool{

    public ECLS(Logger logger, Recorder recorder, Environment environment) {
        super(logger, recorder, environment);
    }

    @Override
    public void execute() {

    }

    @Override
    public void unexpectedTagElimination() {

    }

    @Override
    public void identify(Reader_M reader_m) {

    }
}
