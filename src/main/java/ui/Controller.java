package ui;

import base.TagListGenerator;
import org.apache.logging.log4j.LogManager;
import protocals.*;
import base.*;
import domain.ResultInfo;
import org.apache.logging.log4j.Logger;
import utils.Environment;
import utils.Reader_M;
import utils.Recorder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class Controller implements IObserver{
    private LineChart lineChart = new LineChart();
    private LineChart2 lineChart2 = new LineChart2();
    private BarChart barChart = new BarChart();
    ResultInfo r = new ResultInfo();
    MainInterface ui = new MainInterface();

    IdentifyTool identifyTool = null;

    Logger logger = LogManager.getLogger(Controller.class);

    public void init() throws Exception {
        ui.init();
        assembleFunction();
        assembleMenu();
    }

    public void assembleFunction() {
        ui.loadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                load();
            }
        });

        ui.choiceButton.addActionListener(new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                chooseAlgorithms();
            }

        });

        ui.startButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                start();

            }
        });

        ui.clearButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clear();

            }
        });

        ui.saveFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                save();
            }
        });

        ui.warnButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                warn();
            }
        });

        ui.adviceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    advice();
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void assembleMenu() {
        ui.loadMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                load();
            }
        });
        ui.choiceMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                chooseAlgorithms();
            }
        });
        ui.startMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                start();
            }
        });
        ui.clearMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                clear();
            }
        });
        ui.saveFileMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                save();
            }
        });
        ui.warnMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                warn();
            }
        });
        ui.adviceMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    advice();
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // 新建配置表格
    public void load() {
        ui.Property property = new ui.Property(r);
        property.jFrame.setVisible(true);
    }

    // 预警设置
    public void warn(){
        Warning warning = new Warning(r);
        warning.pack();
        warning.setLocationRelativeTo(ui.jFrame);
        warning.setVisible(true);
    }

    public void advice() throws IOException, InterruptedException {
        // 检查是否输入配置,如果没有输入，提示用户输入；如果有输入，打印配置参数
        if(!r.propertiesChanged) {
            JOptionPane.showMessageDialog(ui.jFrame, "请输入配置");
            return;
        }
        String s = (String) JOptionPane.showInputDialog(ui.jFrame, "", "图像选择", JOptionPane.DEFAULT_OPTION, null, new String[]{"各算法估算时间对比柱状图", "各算法执行时间随识别轮次变化折线图", "各算法缺失率随识别轮次变化折线图"}, "各算法估算时间对比柱状图");

        String advice = "";
        advice+="参数配置如下：\n";
        advice+="标签ID长度："+96+" 类别ID长度："+32+" 缺失率："+r.missingRate+" 标签数目："+r.tagNum+" 平均每个类别的标签数量："+r.tagNumPerCid+"\n\n";

        // 估算四个算法的时间，打印时间对比图

        TagRepository tagRepository = TagListGenerator.generateTagRepository(r.tagIDLength, r.categoryIDLength, r.tagNum, r.tagNumPerCid,r.getUnknownTagNumber(), r.getMissingTagNum());
        List<Tag> allTagList = tagRepository.getAllTagList();
        List<Tag> expectedTagList = tagRepository.getExpectedTagList();
        List<Tag> tagList = tagRepository.getActucaltagList();

        //Single Reader and Multi reader codes are almost same, we only give one reader for the environment
        Environment environment = new Environment(allTagList, expectedTagList, tagList,expectedTagList.size()/r.tagNumPerCid);
        if(r.isTagRandomlyDistributed) {
            environment.createType1(r.repository_leng, r.repository_wid, r.readerInRow, r.readerInCol);
        } else {
            environment.createType2(r.repository_leng, r.repository_wid, r.readerInRow, r.readerInCol);
        }

        Recorder recorder = new Recorder();

        environment.reset();
        for(Reader_M reader_m : environment.getReaderList()) {
            reader_m.recorder = new Recorder();
        }

        // 将毫秒转化为秒
        IdentifyTool cip = new CIP(logger, recorder, environment);
        cip.execute();
        double CIPtime = cip.recorder.totalExecutionTime * 1.0 / 1000;
        Recorder ciprecorder = cip.environment.getReaderList().get(0).recorder;
        List<Double> executionTime1 = ciprecorder.executionTimeList;
        List<Double> missingRateList1 = ciprecorder.missingRateList;
        environment.reset();
        for(Reader_M reader_m : environment.getReaderList()) {
            reader_m.recorder = new Recorder();
        }

        IdentifyTool ecip = new ECIP(logger,recorder,environment);
        ecip.execute();
        double ECIPtime = ecip.recorder.totalExecutionTime*1.0/1000;
        Recorder eciprecorder = ecip.environment.getReaderList().get(0).recorder;
        List<Double> executionTime2 = eciprecorder.executionTimeList;
        List<Double> missingRateList2 = eciprecorder.missingRateList;
        environment.reset();
        for(Reader_M reader_m : environment.getReaderList()) {
            reader_m.recorder = new Recorder();
        }

        IdentifyTool ecls = new ECLS(logger,recorder,environment);
        ecls.execute();
        double ECLStime = ecls.recorder.totalExecutionTime*1.0/1000;
        Recorder eclsrecorder = ecls.environment.getReaderList().get(0).recorder;
        List<Double> executionTime3 = eclsrecorder.executionTimeList;
        List<Double> missingRateList3 = eclsrecorder.missingRateList;
        environment.reset();
        for(Reader_M reader_m : environment.getReaderList()) {
            reader_m.recorder = new Recorder();
        }

        IdentifyTool edls = new ECLS(logger,recorder,environment);
        edls.execute();
        double EDLStime = edls.recorder.totalExecutionTime*1.0/1000;
        Recorder edlsrecorder = edls.environment.getReaderList().get(0).recorder;
        List<Double> executionTime4 = edlsrecorder.executionTimeList;
        List<Double> missingRateList4 = edlsrecorder.missingRateList;
        environment.reset();
        for(Reader_M reader_m : environment.getReaderList()) {
            reader_m.recorder = new Recorder();
        }

        if ("各算法估算时间对比柱状图".equals(s))
        {
            barChart.initUI(CIPtime, ECIPtime, ECLStime, EDLStime);
        }
        else if("各算法执行时间随识别轮次变化折线图".equals(s))
        {
            lineChart2.initUI(executionTime1, executionTime2, executionTime3, executionTime4, "各算法执行时间随识别轮次变化折线图", "执行时间");
        }
        else if("各算法缺失率随识别轮次变化折线图".equals(s))
        {
            lineChart2.initUI(missingRateList1, missingRateList2, missingRateList3, missingRateList4, "各算法缺失率随识别轮次变化折线图", "缺失率");
        }
        // 推荐时间最短的
        double minTime = ECIPtime;
        String algorithm = "ECIP";
        if(CIPtime < minTime) {
            minTime = CIPtime;
            algorithm = "CIP";
        }
        if(ECLStime < minTime) {
            minTime = ECLStime;
            algorithm = "ECLS";
        }
        if(EDLStime < minTime) {
            minTime = EDLStime;
            algorithm = "EDLS";
        }

        advice+="估算时间：\n";
        advice += "CIP:"+String.format("%.4f", CIPtime)+"s\n";
        advice += "ECIP:"+String.format("%.4f", ECIPtime)+"s\n";
        advice += "ECLS:"+String.format("%.4f", ECLStime)+"s\n";
        advice += "EDLS:"+String.format("%.4f", EDLStime)+"s\n\n";

        advice += "估算识别准确率：\n";
        advice += "CIP：100%\n";
        advice += "ECIP：100%\n";
        advice += "ECLS：100%\n";
        advice += "EDLS：100%\n\n";

        advice+="综合考虑时间和准确率，推荐算法："+algorithm+"\n";
        ui.controlText.setText(advice);
    }

    // 保存配置按钮
    public void save() {
        // 设置文件过滤器
        MyFilter myFilter = new MyFilter();
        myFilter.addExtension("properties");
        myFilter.addExtension("txt");
        myFilter.setDescription("properties or txt");
        ui.chooser.setFileFilter(myFilter);

        ui.chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        ui.chooser.showSaveDialog(ui.jFrame);
        File dir = ui.chooser.getSelectedFile();
        try(FileWriter fileWriter = new FileWriter(new File(dir, "new.properties"), true)){
            fileWriter.write("cidLength="+r.getCategoryIDLength());
            fileWriter.write("lossRate="+r.getMissingRate());
            fileWriter.write("tagNum="+r.getTagNum());
            fileWriter.write("isRandomAllocated="+r.getRandomAllocated());
            fileWriter.write("tagNumPerCid="+r.getTagNumPerCid());
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    //选择算法
    public void chooseAlgorithms() {
        String s = (String) JOptionPane.showInputDialog(ui.jFrame, "", "选择算法", JOptionPane.DEFAULT_OPTION, null, new String[]{"CIP", "ECIP", "ECLS", "EDLS"}, "CLS");
        ResultInfo.Algorithms a = ResultInfo.Algorithms.Cip;
        if("CIP".equals(s)) {
            a = ResultInfo.Algorithms.Cip;
        } else if("ECIP".equals(s)) {
            a = ResultInfo.Algorithms.Ecip;
        } else if ("ECLS".equals(s)) {
            a = ResultInfo.Algorithms.ECLS;
        } else if ("EDLS".equals(s)){
            a = ResultInfo.Algorithms.EDLS;
        }
        r.setA(a);
    }

    // 构造identifyTool
    public void initTool() {
        TagRepository tagRepository = TagListGenerator.generateTagRepository(r.tagIDLength, r.categoryIDLength, r.tagNum, r.tagNumPerCid,r.getUnknownTagNumber(), r.getMissingTagNum());
        List<Tag> allTagList = tagRepository.getAllTagList();
        List<Tag> expectedTagList = tagRepository.getExpectedTagList();
        List<Tag> actualTagList = tagRepository.getActucaltagList();


        //Single Reader and Multi reader codes are almost same, we only give one reader for the environment
        Environment environment = new Environment(allTagList, expectedTagList, actualTagList,expectedTagList.size()/r.tagNumPerCid);

        environment.createType1(4000, 1600, 2, 5);
        Recorder recorder = new Recorder();

        environment.reset();
        for(Reader_M reader : environment.getReaderList()) {
            reader.recorder = new Recorder();
        }

        switch (r.getA()) {
            case Cip:
                identifyTool = new CIP(logger,recorder,environment);
                break;
            case Ecip:
                identifyTool = new ECIP(logger,recorder,environment);
                break;
            case ECLS:
                identifyTool = new ECLS(logger,recorder,environment);
                break;
            case EDLS:
            default:
                identifyTool = new EDLS(logger,recorder,environment);
                break;
        }

    }

    // 开始模拟
    public void start() {
        initTool();

        // 注册观察者
        identifyTool.add(this);

        String output = "";

        // 控制台区域显示文字
        output += "模拟开始！\n\n";
        output+="参数配置如下：\n";
        output+="标签ID长度："+r.tagIDLength+" 类别ID长度："+r.categoryIDLength+" 缺失率："+r.missingRate+" 标签数目："+r.tagNum+"\n是否随机分配类别ID："+r.isRandomAllocated+" 平均每个类别的标签数量："+r.tagNumPerCid+"\n\n";

        output+="选择算法：\n";
        output+=r.a.toString()+"\n\n";
        output+="模拟结果如下：\n\n";

        //开始模拟
        identifyTool.execute();
        output += identifyTool.recorder.getAnalysis().get(0);
        output += identifyTool.recorder.getAnalysis().get(1);

        // 预警
        String warningCid = r.preciousCid;
        int warningNum = r.mostMissingCidNum;
        if(identifyTool.recorder.missingCids.size() > warningNum) {
            output += "预警！缺失数量超过"+warningNum+"\n\n";
        }
        if(identifyTool.recorder.missingCids.contains(warningCid)) {
            output += "预警！类别："+warningCid+"缺失\n\n";
        }
        ui.controlText.setForeground(Color.BLACK);
        ui.controlText.append(output);
        ui.controlText.setSelectedTextColor(Color.BLUE);

        Recorder recorder = identifyTool.environment.getReaderList().get(0).recorder;
        lineChart.initUI(r.a.toString(), recorder.executionTimeList, recorder.missingRateList);
    }

    // 清空控制台
    public void clear() {
        ui.controlText.setText("");
    }

    // 结果分析按钮
    public void analysis() {
        String result = identifyTool.recorder.getAnalysis().get(0) + identifyTool.recorder.getAnalysis().get(1);
        ui.controlText.setText(result);
    }

    @Override
    public void update(ISubject subject, String message) {
        JOptionPane.showMessageDialog(ui.jFrame, message);
    }
}