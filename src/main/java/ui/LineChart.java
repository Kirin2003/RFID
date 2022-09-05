package ui;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zxd3099
 * @create 2022-09-04-16:21
 */
public class LineChart extends JFrame
{
    public void initUI(String algorithm, List<Double> data1, List<Double> data2)
    {
        JFrame jFrame = new JFrame();
        JPanel panel = new JPanel();
        StandardChartTheme mChartTheme = new StandardChartTheme("CN");
        mChartTheme.setExtraLargeFont(new Font("黑体", Font.BOLD, 20));
        mChartTheme.setLargeFont(new Font("宋体", Font.CENTER_BASELINE, 15));
        mChartTheme.setRegularFont(new Font("宋体", Font.CENTER_BASELINE, 15));
        ChartFactory.setChartTheme(mChartTheme);

        XYDataset dataset1 = createDataset(algorithm, data1);
        JFreeChart chart1 = createChart(dataset1, "算法执行时间随识别轮次变化折线图", "执行时间");
        ChartPanel chartPanel1 = new ChartPanel(chart1);
        chartPanel1.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        chartPanel1.setBackground(Color.white);

        XYDataset dataset2 = createDataset(algorithm, data2);
        JFreeChart chart2 = createChart(dataset2, "各算法缺失率随识别轮次变化折线图", "缺失率");
        ChartPanel chartPanel2 = new ChartPanel(chart2);
        chartPanel2.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        chartPanel2.setBackground(Color.white);

        panel.add(chartPanel1);
        panel.add(chartPanel2);
        jFrame.add(panel);
        jFrame.pack();
        jFrame.setTitle("Line chart");
        jFrame.setLocationRelativeTo(null);
        jFrame.setVisible(true);
    }

    private XYDataset createDataset(String algorithm, List<Double> data)
    {
        XYSeries series = new XYSeries(algorithm);
        for (int i = 1; i <= data.size(); i++)
        {
            series.add(i, data.get(i - 1));
        }
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);

        return dataset;
    }

    private JFreeChart createChart(XYDataset dataset, String title, String yaxis)
    {
        JFreeChart chart = ChartFactory.createXYLineChart(
                title,
                "轮次",
                yaxis,
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        XYPlot plot = chart.getXYPlot();

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, Color.RED);
        renderer.setSeriesStroke(0, new BasicStroke(2.0f));

        plot.setRenderer(renderer);
        plot.setBackgroundPaint(Color.white);

        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.BLACK);

        plot.setDomainGridlinesVisible(true);
        plot.setDomainGridlinePaint(Color.BLACK);

        chart.setTitle(new TextTitle(title,
                new Font("Serif", java.awt.Font.BOLD, 18)));

        return chart;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
        {
            LineChart ex = new LineChart();
            List<Double> list = new ArrayList<>();
            list.add(1.234); list.add(2.345); list.add(4.556);
            list.add(1.234); list.add(2.345); list.add(4.556);
            ex.initUI("ECIP", list, list);
        });
    }
}
