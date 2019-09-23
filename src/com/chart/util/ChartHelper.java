package com.chart.util;

import java.awt.Color;
import java.awt.Font;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleEdge;

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
			title = "算法对比";
			dataset = DataSetHelper.createAlgorithmsCompareDataset(problemList.get(0), algorithmList, metricList.get(0),
					runId);
		} else if (algorithmList.size() == 1) {
			title = "策略对比";
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
		// 创建JFreeChart对象
		JFreeChart jfreechart = ChartFactory.createXYLineChart(title, xlabel, ylabel, dataset, PlotOrientation.VERTICAL,
				true, true, false);
		jfreechart.setBackgroundPaint(Color.white);// 设置背景色为白色

		Font titleFont = new Font("黑体", Font.BOLD, 18);
		Font font1 = new Font("宋体", 10, 12);
		Font font2 = new Font("宋体", 10, 16); // 设定字体、类型、字号

		jfreechart.getTitle().setFont(titleFont);
		// 使用CategoryPlot设置各种参数
		XYPlot plot = (XYPlot) jfreechart.getPlot();
		plot.setDataset(dataset);
		// 设置背景色和背景色的透明度
		plot.setBackgroundAlpha(0.5f);
		plot.setForegroundAlpha(0.5f);

		// 其它设置可以参考XYPlot类
		ValueAxis categoryAxis = plot.getDomainAxis(); // 横轴上的
		categoryAxis.setPositiveArrowVisible(true);// 增加横轴的箭头
		categoryAxis.setTickLabelFont(font1);
		categoryAxis.setLabelFont(font2);// 相当于横轴或理解为X轴

		ValueAxis rangeAxis = plot.getRangeAxis();
		rangeAxis.setPositiveArrowVisible(true);// 增加纵轴的箭头
		rangeAxis.setTickLabelFont(font1);
		rangeAxis.setLabelFont(font2);// 相当于竖轴理解为Y轴

		LegendTitle legendTitle = jfreechart.getLegend();
		legendTitle.setItemFont(font2);// 设置图例字体样式
		legendTitle.setPosition(RectangleEdge.BOTTOM);

		return jfreechart;
	}

	public static void main(String[] args) {
		createLineChart("EDF_PS", "NSGAII", "HV", 0);
	}
}
