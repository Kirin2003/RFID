package protocals;
import base.*;
import org.apache.logging.log4j.Logger;
import utils.Environment;
import utils.Reader_M;
import utils.Recorder;


import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

public abstract class IdentifyTool implements ISubject {
    Logger logger;
    Recorder recorder; //记录器

    Environment environment; // 环境
    double falsePositiveRatio; // 假阳性误报率, 即意外标签通过成员检查的概率
    int instanceNum; // 模拟次数


    protected int warningNum = 1000000; // 预警数目
    protected String warningCid = ""; // 预警类别id
    protected boolean isWarning = true; // 是否需要弹出警告框，只警告一次

    protected Vector<IObserver> iObservers = new Vector<>();

    public IdentifyTool(Logger logger, Recorder recorder, Environment environment) {
        this.logger = logger;
        this.recorder = recorder;
        this.environment = environment;

    }

    public abstract void execute();

    public abstract void unexpectedTagElimination(int numberOfHashFunctions, double falsePositiveRatio, List<Tag> allTagList) ;

    public abstract void identify(double missRate, Reader_M reader_m);

    @Override
    public boolean add(IObserver iObserver) {
        if (iObserver != null && !iObservers.contains(iObserver)) {

            return iObservers.add(iObserver);
        }
        return false;
    }

    @Override
    public boolean remove(IObserver iObserver) {
        return iObservers.remove(iObserver);
    }

    @Override
    public void notifyAllObservers(String warningMessage) {
        logger.debug("notify all observers()");
        for(IObserver iObserver : iObservers) {
            iObserver.update(this, warningMessage);
        }
    }


    public void changeMissingCids(String missingCid) {
        recorder.missingCids.add(missingCid);
        invoke();
    }


    protected void invoke() {
        logger.debug("invoke()");
        if(!isWarning) {
            logger.debug("return");
            return;
        }

        int missingCidNum = recorder.missingCids.size();
        logger.debug("missing cid num:"+missingCidNum);
        String warningMessage = "";
        if(missingCidNum >= warningNum && missingCidNum <= warningNum +5) {
            warningMessage+="预警！缺失数量超过"+warningNum+"\n";
            warningMessage+="识别时间："+String.format("%.4f", recorder.totalExecutionTime*1.0/1000)+"s\n";
        } else if (recorder.missingCids.contains(warningCid) ) {
            warningMessage += "预警！类别："+warningCid+"缺失\n";
            warningMessage+="识别时间："+String.format("%.4f",recorder.totalExecutionTime*1.0/1000)+"s\n";
        }

        if(warningMessage.length()>0) {
            notifyAllObservers(warningMessage);
            logger.debug("弹出警告框");
            isWarning = false;
        }

    }
}
