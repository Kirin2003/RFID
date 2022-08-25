package ui;

import domain.ResultInfo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;

/**
 * @author zxd3099
 * @create 2022-01-26-16:44
 */
public class MainInterface {
    public JFrame jFrame = new JFrame("RFID类别识别仿真系统");

    /** ResultInfo */
    protected ResultInfo resultInfo;

    /** 创建保存文件对话框*/
    protected JFileChooser chooser = new JFileChooser(String.valueOf(JFileChooser.SAVE_DIALOG));

    /** 定义菜单条 */
    protected JMenuBar mb = new JMenuBar();

    /** 定义菜单 */
    protected JMenu configuration = new JMenu("配置(C)");
    protected JMenu example = new JMenu("示例(E)");
    protected JMenu algorithm = new JMenu("算法(A)");
    protected JMenu simulation = new JMenu("模拟(S)");
    protected JMenu help = new JMenu("帮助(H)");

    /** 定义一个右键菜单，用于设置程序的外观风格 */
    protected JPopupMenu popout = new JPopupMenu();

    /** 定义一个ButtonGroup对象，用于组合风格按钮，形成单选 */
    ButtonGroup flavorGroup = new ButtonGroup();

    /** 定义五个单选按钮菜单项，用于设置程序风格 */
    protected JRadioButtonMenuItem metalItem = new JRadioButtonMenuItem("Metal 风格",true);
    protected JRadioButtonMenuItem nimbusItem = new JRadioButtonMenuItem("Nimbus 风格",true);
    protected JRadioButtonMenuItem windowsItem = new JRadioButtonMenuItem("Windows 风格",true);
    protected JRadioButtonMenuItem classicItem = new JRadioButtonMenuItem("Windows 经典风格",true);
    protected JRadioButtonMenuItem motifItem = new JRadioButtonMenuItem("Motif 风格",true);

    /** 创建一个横向的Box*/
    Box mainBox = Box.createHorizontalBox();

    /** 创建两个竖向的Box，并添加相应的组件*/
    Box functionBox = Box.createVerticalBox();
    JPanel functionPanel = new JPanel();
    Box viewBox = Box.createVerticalBox();
    JPanel viewPanel = new JPanel();


    /** 创建按钮组件 */
    JButton loadButton = new JButton("新建配置表格");
    JButton choiceButton = new JButton("选择算法");
    JButton adviceButton = new JButton("推荐算法");

    JButton startButton = new JButton("开始模拟");
    JButton endButton = new JButton("结束模拟");//无用
    JButton clearButton = new JButton("清空控制台");
    JButton saveFileButton = new JButton("保存记录");
    JButton analysisButton = new JButton("结果分析");
    JButton warnButton = new JButton("预警设置");

    JLabel speedLabel = new JLabel("动画速度:(慢-快)");
    final JSlider slider = new JSlider(1, 10, 5);

    /** 定义菜单栏 */
    JMenuItem loadMenu = new JMenuItem("新建配置表格");
    JMenuItem choiceMenu = new JMenuItem("选择算法");
    JMenuItem adviceMenu = new JMenuItem("推荐算法");
    JMenuItem startMenu = new JMenuItem("开始模拟");
    JMenuItem clearMenu = new JMenuItem("清空控制台");
    JMenuItem analysisMenu = new JMenuItem("结果分析");
    JMenuItem saveFileMenu = new JMenuItem("保存记录");
    JMenuItem warnMenu = new JMenuItem("预警设置");

    /** 创建文本域组件 */
    public JTextArea controlText = new JTextArea();
    JTextArea AnalogText = new JTextArea();

    JScrollPane viewPane = new JScrollPane(controlText);
    //private Object FlowLayout;

    /** 定义方法，用于改变界面风格 */
    private void changeFlavor(String command) throws Exception{
        switch (command){
            case "Metal 风格":
                UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
                break;
            case "Nimbus 风格":
                UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
                break;
            case "Windows 风格":
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
                break;
            case "Windows 经典风格":
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel");
                break;
            case "Motif 风格":
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
                break;
        }

        // 更新窗口内顶级容器以及所有组件的UI
        SwingUtilities.updateComponentTreeUI(jFrame.getContentPane());
        // 更新菜单条及每部所有组件UI
        SwingUtilities.updateComponentTreeUI(mb);
        // 更新右键菜单及内部所有菜单项的UI
        SwingUtilities.updateComponentTreeUI(popout);
    }

    /** 组装菜单 */
    public void assembleMenu(){
        // 每个菜单下添加菜单项
        configuration.add(loadMenu);

        algorithm.add(choiceMenu);
        algorithm.addSeparator();
        algorithm.add(adviceMenu);
        algorithm.addSeparator();

        simulation.add(warnMenu);
        simulation.addSeparator();
        simulation.add(startMenu);
        simulation.addSeparator();
        simulation.add(clearMenu);
        simulation.addSeparator();
        simulation.add(analysisMenu);
        simulation.addSeparator();
        simulation.add(saveFileMenu);
        simulation.addSeparator();

        // 将菜单添加到菜单条
        mb.add(configuration);
        mb.add(example);
        mb.add(algorithm);
        mb.add(simulation);
        mb.add(help);

        // 把菜单条设置给窗口
        jFrame.setJMenuBar(mb);

        // 组合右键菜单，选择风格
        flavorGroup.add(metalItem);
        flavorGroup.add(nimbusItem);
        flavorGroup.add(windowsItem);
        flavorGroup.add(classicItem);
        flavorGroup.add(motifItem);

        //给5个风格菜单创建事件监听器
        ActionListener flavorLister = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String command = e.getActionCommand();
                try {
                    changeFlavor(command);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        };

        //为5个风格菜单项注册监听器
        metalItem.addActionListener(flavorLister);
        nimbusItem.addActionListener(flavorLister);
        windowsItem.addActionListener(flavorLister);
        classicItem.addActionListener(flavorLister);
        motifItem.addActionListener(flavorLister);

        popout.add(metalItem);
        popout.add(nimbusItem);
        popout.add(windowsItem);
        popout.add(classicItem);
        popout.add(motifItem);

        controlText.setComponentPopupMenu(popout);
        AnalogText.setComponentPopupMenu(popout);

        mb.setBackground(Color.ORANGE);
    }

    /** 组装功能区 */
    public void assembleFunction(){

        // 设置背景颜色
        slider.setBackground(Color.WHITE);
        // 设置主刻度间隔
        slider.setMajorTickSpacing(2);
        // 设置次刻度间隔
        slider.setMinorTickSpacing(1);
        // 绘制刻度和标签
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        // 设置滑块方向为水平方向
        slider.setOrientation(SwingConstants.HORIZONTAL);
        // 给指定的刻度值显示自定义标签
        Hashtable<Integer, JComponent> hashtable = new Hashtable<Integer, JComponent>();
        hashtable.put(1, new JLabel("Slow"));   // 0 刻度位置，显示 "Start"
        hashtable.put(5, new JLabel("Middle"));  // 10 刻度位置，显示 "Middle"
        hashtable.put(10, new JLabel("Fast"));    // 20 刻度位置，显示 "End"
        // 将刻度值和自定义标签的对应关系设置到滑块（设置后不再显示默认的刻度值）
        slider.setLabelTable(hashtable);


        loadButton.setPreferredSize(new Dimension(150, 25));
        adviceButton.setPreferredSize(new Dimension(150, 25));
        warnButton.setPreferredSize(new Dimension(150, 25));
        choiceButton.setPreferredSize(new Dimension(150, 25));
        startButton.setPreferredSize(new Dimension(150, 25));
        endButton.setPreferredSize(new Dimension(150, 25));
        clearButton.setPreferredSize(new Dimension(150, 25));
        saveFileButton.setPreferredSize(new Dimension(150, 25));
        analysisButton.setPreferredSize(new Dimension(150, 25));
        speedLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

        JPanel internalPanel = new JPanel();
        internalPanel.setLayout(new FlowLayout());

        internalPanel.setPreferredSize(new Dimension(200, Toolkit.getDefaultToolkit().getScreenSize().height));
        internalPanel.setBackground(Color.WHITE);
        internalPanel.add(Box.createVerticalStrut(70));
        internalPanel.add(loadButton);
        internalPanel.add(Box.createVerticalStrut(70));
        internalPanel.add(adviceButton);
        internalPanel.add(Box.createVerticalStrut(70));
        internalPanel.add(warnButton);
        internalPanel.add(Box.createVerticalStrut(70));
        internalPanel.add(choiceButton);
        internalPanel.add(Box.createVerticalStrut(70));
        internalPanel.add(startButton);
        internalPanel.add(Box.createVerticalStrut(70));
        internalPanel.add(clearButton);
        internalPanel.add(Box.createVerticalStrut(70));
        internalPanel.add(saveFileButton);
        internalPanel.add(Box.createVerticalStrut(70));
        internalPanel.add(analysisButton);
        internalPanel.add(Box.createVerticalStrut(70));
        // 暂时删去动画
        //internalPanel.add(speedLabel);
        //internalPanel.add(slider);
        functionPanel.add(Box.createHorizontalStrut(1));
        functionPanel.add(internalPanel);
        functionPanel.add(Box.createHorizontalStrut(1));
    }

    /** 组装控制和模拟区*/
    public void assembleView(){
        //viewPane.setPreferredSize(new Dimension(Toolkit.getDefaultToolkit().getScreenSize().width-200,Toolkit.getDefaultToolkit().getScreenSize().height));
        viewPane.setPreferredSize(new Dimension(250,500));
        viewPane.setLayout(new ScrollPaneLayout());
        viewPanel.setLayout(new BoxLayout(viewPanel,BoxLayout.PAGE_AXIS));
        viewPanel.add(AnalogText);
        viewPanel.add(Box.createRigidArea(new Dimension(0,5)));

        viewPanel.add(viewPane);
        //controlText.setText("1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n");

    }

    /** 组装视图 */
    public void init() throws Exception{
        assembleMenu();
        assembleFunction();
        assembleView();

        mainBox.add(functionPanel);
        mainBox.add(viewPanel);
        jFrame.add(Box.createVerticalStrut(8));
        jFrame.add(mainBox);
        // 设置关闭窗口时推出程序
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 设置jFrame最佳大小并可见
        jFrame.setVisible(true);
        jFrame.setLocationRelativeTo(null);
        jFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    /** 客户端程序的入口 */
    public static void main(String[] args){
        try {
            new MainInterface().init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
