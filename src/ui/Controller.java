package ui;
import ECIP.*;

import java.awt.event.ActionListener;
import java.util.*;
import domain.ResultInfo;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class Controller {
    ResultInfo r = new ResultInfo();
    MainInterface ui = new MainInterface();
    Properties properties = new Properties();

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
            public void actionPerformed(ActionEvent actionEvent) {
                analysis();
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
        ui.loadFileMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                loadFile();
            }
        });
        ui.openFileMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                openFile();
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
                saveFile();

            }
        });
    }

    // 新建配置表格
    public void load() {

        new Property(r).jFrame.setVisible(true);

    }

//    // 保存配置按钮
//    public void save() {
//        ui.saveButton.addActionListener(new AbstractAction() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                ui.chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
//                ui.chooser.showSaveDialog(jFrame);
//                File dir = ui.chooser.getSelectedFile();
//                try(FileWriter fileWriter = new FileWriter(new File(dir, "new.properties"), true)){
//                    fileWriter.write("cidLength="+ResultInfo.cidLength);
//                    fileWriter.write("lossRate="+ResultInfo.missingRate);
//                    fileWriter.write("tagNum="+ResultInfo.tagNum);
//                    fileWriter.write("slotNum="+ResultInfo.f);
//                    fileWriter.write("isRandomAllocated="+ResultInfo.isRandomAllocated);
//                    fileWriter.write("tagNumPerCid="+ResultInfo.tagNumPerCid);
//                } catch (IOException ioException) {
//                    ioException.printStackTrace();
//                }
//            }
//        });
//    }


    // 新建配置文件
    public void loadFile() {

    }

    // 打开配置文件
    public void openFile() {

    }

    //选择算法
    public void chooseAlgorithms() {
        logger.debug(" enter choiceButton()");
        String s = (String) JOptionPane.showInputDialog(ui.jFrame, "", "选择算法", JOptionPane.DEFAULT_OPTION, null, new String[]{"CIP", "ECIP", "ECIP with CLS", "ECIP with DLS"}, "CLS");
        System.out.println(s);
        ResultInfo.Algorithms a = ResultInfo.Algorithms.Cip;
        if(s == "CIP") {
            a = ResultInfo.Algorithms.Cip;
        } else if(s == "ECIP") {
            a = ResultInfo.Algorithms.Ecip;
        } else if (s == "ECIP with CLS") {
            a = ResultInfo.Algorithms.ECIPwithCLS;
        } else if (s == "ECIP with DLS"){
            a = ResultInfo.Algorithms.ECIPwithDLS;
        }

        r.setA(a);

    }


    // 构造identifyTool
    public void initTool() {

            //等待修改 hxq
            Vector<String> tids = null;
            Vector<String> cids = null;

            if (r.isRandomAllocated) {
                tagList = TagListGenerator.tagListFactory2(r.getTagLength(), r.getCidLength(), r.getTagNum(), r.getTagNumPerCid());
            } else {
                tagList = TagListGenerator.tagListFactory3(r.getTagLength(), r.getCidLength(), r.getTagNum(), tids, cids);
            }

            if (r.getMissingRate() > 0) {
                actualList = TagListGenerator.highMissingListFactory(tagList, r.getMissingRate());
            }



        switch (r.getA()) {
            case Cip:
                identifyTool = new CIP(tagList, r.getUnReadCidNum(), r.getF());
                break;
            case Ecip:
                identifyTool = new ECIP(tagList, r.getUnReadCidNum(), r.getF());
                break;
            case ECIPwithCLS:
                identifyTool = new ECIPwithCLS(tagList, actualList, r.getUnReadCidNum(), r.getF());
                break;
            case ECIPwithDLS:
            default:
                identifyTool = new CIP(tagList, r.getUnReadCidNum(), r.getF());
                break;
        }



//
//
        r.algorithmsChanged = false;
        r.propertiesChanged = false;
    }


    // 开始模拟
    public void start() {
        initTool();


        // 控制台区域显示文字
        ui.controlText.setText("模拟开始！");

        //开始模拟
        identifyTool.identifyAll();

//        System.out.println(identifyTool.output);
        ui.controlText.setText(identifyTool.output);

    }



    // 清空控制台
    public void clear() {
        ui.controlText.setText("");


    }

    // 结果分析按钮
    public void analysis() {
        ui.controlText.setText(identifyTool.analysis);

    }

    // 保存记录
    public void saveFile() {

    }



}
