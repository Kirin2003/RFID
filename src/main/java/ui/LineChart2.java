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
import java.util.List;

/**
 * @author zxd3099
 * @create 2022-09-04-18:41
 */
public class LineChart2 extends JFrame
{
    public void initUI(List<Double> data1, List<Double> data2, List<Double> data3, List<Double> data4, String title, String yaxis)
    {
        JFrame jFrame = new JFrame();
        JPanel panel = new JPanel();
        StandardChartTheme mChartTheme = new StandardChartTheme("CN");
        mChartTheme.setExtraLargeFont(new Font("黑体", Font.BOLD, 20));
        mChartTheme.setLargeFont(new Font("宋体", Font.CENTER_BASELINE, 15));
        mChartTheme.setRegularFont(new Font("宋体", Font.CENTER_BASELINE, 15));
        ChartFactory.setChartTheme(mChartTheme);

        XYDataset dataset = createDataset(data1, data2, data3, data4);
        JFreeChart chart = createChart(dataset, title, yaxis);
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        chartPanel.setBackground(Color.white);

        panel.add(chartPanel);
        jFrame.add(panel);
        jFrame.pack();
        jFrame.setTitle("Line chart");
        jFrame.setLocationRelativeTo(null);
        jFrame.setVisible(true);
    }

    private XYDataset createDataset(List<Double> data1, List<Double> data2, List<Double> data3, List<Double> data4)
    {
        XYSeries series1 = new XYSeries("CIP");
        for (int i = 1; i <= data1.size(); i++)
        {
            series1.add(i, data1.get(i - 1));
        }
        XYSeries series2 = new XYSeries("ECIP");
        for (int i = 1; i <= data2.size(); i++)
        {
            series2.add(i, data2.get(i - 1));
        }
        XYSeries series3 = new XYSeries("ECLS");
        for (int i = 1; i <= data3.size(); i++)
        {
            series3.add(i, data3.get(i - 1));
        }
        XYSeries series4 = new XYSeries("EDLS");
        for (int i = 1; i <= data4.size(); i++)
        {
            series4.add(i, data4.get(i - 1));
        }
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series1);
        dataset.addSeries(series2);
        dataset.addSeries(series3);
        dataset.addSeries(series4);

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

        renderer.setSeriesPaint(1, Color.BLUE);
        renderer.setSeriesStroke(1, new BasicStroke(2.0f));

        renderer.setSeriesPaint(2, Color.YELLOW);
        renderer.setSeriesStroke(2, new BasicStroke(2.0f));

        renderer.setSeriesPaint(3, Color.GREEN);
        renderer.setSeriesStroke(3, new BasicStroke(2.0f));

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
}
