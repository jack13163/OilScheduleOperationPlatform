package oil.chart.util;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardXYSeriesLabelGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleEdge;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class ChartHelper {
    /**
     * 创建chart并显示
     *
     * @param problems
     * @param algorithms
     * @param metrics
     * @param runId
     * @return
     */
    public static JFrame createLineChart(String problems, String algorithms, String metrics, int runId) {

        // 1.判断输入是否合法
        List<String> problemList = Arrays.asList(problems.split(","));
        List<String> algorithmList = Arrays.asList(algorithms.split(","));
        List<String> metricList = Arrays.asList(metrics.split(","));
        if (problemList.isEmpty() || algorithmList.isEmpty() || metricList.isEmpty()) {
            return null;
        }
        if (problemList.size() > 1 && algorithmList.size() > 1) {
            return null;
        }

        // 2.准备数据集
        XYDataset dataset = null;
        String title = "";
        String xlabel = "迭代次数";
        String ylabel = metricList.get(0);
        if (problemList.size() == 1) {
            title = "采用" + problemList.get(0) + "策略的情况下的算法对比";
            dataset = DataSetHelper.createAlgorithmsCompareDataset(problemList.get(0), algorithmList, metricList.get(0),
                    runId);
        } else if (algorithmList.size() == 1) {
            title = "采用" + algorithmList.get(0) + "算法的情况下的策略对比";
            dataset = DataSetHelper.createProblemsCompareDataset(problemList, algorithmList.get(0), metricList.get(0),
                    runId);
        }

        // 3.创建窗口
        JFrame frame = new JFrame();
        frame.setSize(800, 500);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        JFreeChart lineChart = createChart(title, xlabel, ylabel, dataset);

        ChartPanel chartPanel = new ChartPanel(lineChart);
        chartPanel.setPreferredSize(new java.awt.Dimension(800, 250));
        chartPanel.setVisible(true);
        frame.add(chartPanel);
        frame.setVisible(true);

        return frame;
    }

    /**
     * 根据XYDataset创建JFreeChart对象
     *
     * @param title
     * @param xlabel
     * @param ylabel
     * @param dataset
     * @return
     */
    public static JFreeChart createChart(String title, String xlabel, String ylabel, XYDataset dataset) {
        Font titleFont = new Font("宋体", Font.BOLD, 20);
        Font numFont = new Font("Times New Roman", 10, 16);
        Font labelFont = new Font("宋体", 10, 16); // 设定字体、类型、字号

        // 创建JFreeChart对象
        JFreeChart jfreechart = ChartFactory.createXYLineChart(title, xlabel, ylabel, dataset, PlotOrientation.VERTICAL,
                true, true, false);

        // 设置背景色为白色
        jfreechart.setBackgroundPaint(Color.white);

        // 设置标题字体
        jfreechart.getTitle().setFont(titleFont);

        // 设置数据集
        XYPlot plot = (XYPlot) jfreechart.getPlot();
        plot.setDataset(dataset);

        // 设置背景色和背景色的透明度
        plot.setBackgroundAlpha(0f);
        plot.setForegroundAlpha(0.5f);

        // 设置外边框是否可见
        plot.setOutlineVisible(false);

        // 设置横轴属性
        ValueAxis categoryAxis = plot.getDomainAxis();
        categoryAxis.setPositiveArrowVisible(true);// 增加横轴的箭头
        categoryAxis.setTickLabelFont(numFont);
        categoryAxis.setLabelFont(labelFont);

        // 设置纵轴属性
        ValueAxis rangeAxis = plot.getRangeAxis();
        rangeAxis.setPositiveArrowVisible(true);// 增加纵轴的箭头
        rangeAxis.setTickLabelFont(numFont);
        rangeAxis.setLabelFont(labelFont);

        // 设置网格线
        plot.setDomainGridlinePaint(Color.gray);  // 设置横向网格线灰色
        plot.setDomainGridlinesVisible(true);     // 设置显示横向网格线
        plot.setRangeGridlinePaint(Color.gray);   // 设置纵向网格线灰色
        plot.setRangeGridlinesVisible(true);      // 设置显示纵向网格线

        // 设置图例属性
        LegendTitle legendTitle = jfreechart.getLegend();
        legendTitle.setItemFont(labelFont);
        legendTitle.setPosition(RectangleEdge.BOTTOM);

        // 设置每一个序列线和数据点
        XYItemRenderer r = plot.getRenderer();
        if (r instanceof XYLineAndShapeRenderer) {
            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
            // 数据点样式设置
            renderer.setBaseShapesVisible(true);    // 数据点显示外框
            renderer.setBaseShapesFilled(true);     // 数据点外框内是否填充
            renderer.setUseFillPaint(true); // 如果要在数据点外框内填充自定义的颜色，这个标志位必须为真
            renderer.setLegendItemToolTipGenerator(new StandardXYSeriesLabelGenerator("{0}"));// 鼠标移到序列线上提示信息为“序列线的名字”

            Color[] colors = new Color[]{
                    Color.RED,
                    Color.GREEN,
                    Color.BLUE,
                    Color.ORANGE,
                    Color.CYAN,
                    Color.lightGray
            };

            for (int i = 0; i < colors.length; i++) {
                // 设置折线加粗
                renderer.setSeriesStroke(i, new BasicStroke(1.5F));
                // 设置数据点填充颜色
                renderer.setSeriesFillPaint(i, Color.WHITE);    // 第二条序列线上数据点外框内填充颜色为白色
                // 设置序列线颜色
                renderer.setSeriesPaint(i, colors[i]);
            }
        }

        return jfreechart;
    }

    public static void main(String[] args) {
        createLineChart("EDF_PS", "NSGAII", "HV", 0);
    }
}
