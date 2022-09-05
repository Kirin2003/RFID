package ui;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;

/**
 * @author zxd3099
 * @create 2022-09-04-16:24
 */
public class BarChart extends JFrame
{
    public void initUI(double time1, double time2, double time3, double time4)
    {
        JFrame jFrame = new JFrame();
        JPanel panel = new JPanel();
        StandardChartTheme mChartTheme = new StandardChartTheme("CN");
        mChartTheme.setExtraLargeFont(new Font("黑体", Font.BOLD, 20));
        mChartTheme.setLargeFont(new Font("宋体", Font.CENTER_BASELINE, 15));
        mChartTheme.setRegularFont(new Font("宋体", Font.CENTER_BASELINE, 15));
        ChartFactory.setChartTheme(mChartTheme);

        CategoryDataset dataset = createDataset(time1, time2, time3, time4);

        JFreeChart chart = createChart(dataset);
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        chartPanel.setBackground(Color.white);

        panel.add(chartPanel);
        jFrame.add(panel);
        jFrame.pack();
        jFrame.setTitle("Bar chart");
        jFrame.setLocationRelativeTo(null);
        jFrame.setVisible(true);
    }

    private CategoryDataset createDataset(double time1, double time2, double time3, double time4)
    {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.setValue(time1, "估算时间", "CIP");
        dataset.setValue(time2, "估算时间", "ECIP");
        dataset.setValue(time3, "估算时间", "ECLS");
        dataset.setValue(time4, "估算时间", "EDLS");

        return dataset;
    }

    private JFreeChart createChart(CategoryDataset dataset)
    {
        JFreeChart barChart = ChartFactory.createBarChart(
                "各算法估算时间对比柱状图",
                "算法",
                "估算时间(s)",
                dataset,
                PlotOrientation.VERTICAL,
                false, true, false
        );
        return barChart;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BarChart ex = new BarChart();
            ex.initUI(15, 15, 15, 15);
        });
    }
}
