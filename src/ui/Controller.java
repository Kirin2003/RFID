package ui;
import ECIP.*;

import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import domain.ResultInfo;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class Controller implements IObserver{
    ResultInfo r = new ResultInfo();
    MainInterface ui = new MainInterface();

    IdentifyTool identifyTool = null;

    List<Tag> tagList = null;
    List<Tag> actualList = null;

    Logger logger = Logger.getLogger("controller");

    public void init() throws Exception {
        logger.setLevel(Level.DEBUG);
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

        ui.analysisButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                analysis();
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
                } catch (IOException e) {
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
        ui.analysisMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                analysis();
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
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // 新建配置表格
    public void load() {
        Property property = new Property(r);
        property.jFrame.setVisible(true);
    }

    // 预警设置
    public void warn(){
        Warning warning = new Warning(r);
        warning.pack();
        warning.setLocationRelativeTo(ui.jFrame);
        warning.setVisible(true);
    }

    public void advice() throws IOException {
        // 检查是否输入配置,如果没有输入，提示用户输入；如果有输入，打印配置参数
        if(!r.propertiesChanged) {
            JOptionPane.showMessageDialog(ui.jFrame, "请输入配置");
            return ;

        }

        String advice = "";
        advice+="参数配置如下：\n";
        advice+="标签ID长度："+96+" 类别ID长度："+32+" 缺失率："+r.missingRate+" 标签数目："+r.tagNum+" 平均每个类别的标签数量："+r.tagNumPerCid+"\n\n";

        // 估算三个算法的时间，打印时间对比图
        // CIP 和 ECIP 直接整体估计, ECLS需要创建对象仿真估计
        int virtualCidNum = r.getVirtualCidNum();
        int actualCidNum = r.getActualCidNum();
        tagList = TagListGenerator.tagListFactory2(r.getTagLength(), r.getCidLength(), r.getTagNum(), r.getTagNumPerCid());
        actualList = TagListGenerator.highMissingListFactory2(tagList, virtualCidNum, r.getMissingRate());


        // 将毫秒转化为秒
        double CIPtime = CIP.time(actualCidNum)*1.0/1000;
        double ECIPtime = ECIP.time(actualCidNum)*1.0/1000;
        IdentifyTool identifyTool = new ECLS(tagList, actualList,virtualCidNum,actualCidNum,r.f, r.tagLength, r.cidLength,r.mostMissingTagNum,r.preciousCid);
        identifyTool.identifyAll();
        double ECLStime = identifyTool.getTime()*1.0/1000;



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

        advice+="估算时间如下：\n";
        advice += "CIP:"+String.format("%.4f",CIPtime)+"s\n";
        advice += "ECIP:"+String.format("%.4f",ECIPtime)+"s\n";
        advice += "ECLS:"+String.format("%.4f",ECLStime)+"s\n\n";

        advice+="推荐算法："+algorithm+"\n";

        JPython.Graphic(CIPtime, ECIPtime, ECLStime,advice);
        
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
            fileWriter.write("cidLength="+r.getCidLength());
            fileWriter.write("lossRate="+r.getMissingRate());
            fileWriter.write("tagNum="+r.getTagNum());
            fileWriter.write("slotNum="+r.getF());
            fileWriter.write("isRandomAllocated="+r.getRandomAllocated());
            fileWriter.write("tagNumPerCid="+r.getTagNumPerCid());
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    //选择算法
    public void chooseAlgorithms() {
        logger.debug(" enter choiceButton()");
        String s = (String) JOptionPane.showInputDialog(ui.jFrame, "", "选择算法", JOptionPane.DEFAULT_OPTION, null, new String[]{"CIP", "ECIP", "ECLS", "EDLS"}, "CLS");
        System.out.println(s);
        ResultInfo.Algorithms a = ResultInfo.Algorithms.Cip;
        if(s == "CIP") {
            a = ResultInfo.Algorithms.Cip;
        } else if(s == "ECIP") {
            a = ResultInfo.Algorithms.Ecip;
        } else if (s == "ECLS") {
            a = ResultInfo.Algorithms.ECLS;
        } else if (s == "EDLS"){
            a = ResultInfo.Algorithms.EDLS;
        }
        r.setA(a);
    }

    // 构造identifyTool
    public void initTool() {


        int virtualCidNum = r.getVirtualCidNum();
        int actualCidNum = r.getActualCidNum();

        tagList = TagListGenerator.tagListFactory2(r.getTagLength(), r.getCidLength(), r.getTagNum(), r.getTagNumPerCid());

        actualList = TagListGenerator.highMissingListFactory2(tagList, virtualCidNum, r.getMissingRate());




        switch (r.getA()) {
            case Cip:

                identifyTool = new CIP(tagList, actualList, virtualCidNum, actualCidNum,r.f, r.tagLength,r.cidLength,r.mostMissingTagNum,r.preciousCid);
                break;
            case Ecip:
                identifyTool = new ECIP(tagList, actualList, virtualCidNum, actualCidNum,r.f, r.tagLength,r.cidLength, r.mostMissingTagNum,r.preciousCid);

                break;
            case ECLS:
                identifyTool = new ECLS(tagList, actualList,virtualCidNum,actualCidNum,r.f, r.tagLength, r.cidLength,r.mostMissingTagNum,r.preciousCid);
                break;
            case EDLS:
            default:
                identifyTool = new CIP(tagList, actualList, virtualCidNum, actualCidNum,r.f, r.tagLength,r.cidLength,r.mostMissingTagNum,r.preciousCid);
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
        output+="标签ID长度："+r.tagLength+" 类别ID长度："+r.cidLength+" 缺失率："+r.missingRate+" 标签数目："+r.tagNum+"\n是否随机分配类别ID："+r.isRandomAllocated+" 平均每个类别的标签数量："+r.tagNumPerCid+"\n\n";

        output+="选择算法：\n";
        output+=r.a.toString()+"\n\n";
        //output+="基于以上配置，最优时隙为："+r.f+"\n\n";
        output+="模拟结果如下：\n\n";

        ui.controlText.setText(output);
        ui.controlText.setSelectedTextColor(Color.BLUE);

        //开始模拟


        identifyTool.identifyAll();
        output+=identifyTool.getOutput();

        ui.controlText.setText(output);
        System.out.println("output");
        System.out.println(output);



    }

    // 清空控制台
    public void clear() {
        ui.controlText.setText("");
    }

    // 结果分析按钮
    public void analysis() {
        ui.controlText.setText(identifyTool.getAnalysis());
    }

    @Override
    public void update(ISubject subject, String message) {
        JOptionPane.showMessageDialog(ui.jFrame, message);
    }
}
