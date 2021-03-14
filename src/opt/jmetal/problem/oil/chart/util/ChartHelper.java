package opt.jmetal.problem.oil.chart.util;

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
     * ����chart����ʾ
     *
     * @param problems
     * @param algorithms
     * @param metrics
     * @param runId
     * @return
     */
    public static JFrame createLineChart(String problems, String algorithms, String metrics, int runId) {

        // 1.�ж������Ƿ�Ϸ�
        List<String> problemList = Arrays.asList(problems.split(","));
        List<String> algorithmList = Arrays.asList(algorithms.split(","));
        List<String> metricList = Arrays.asList(metrics.split(","));
        if (problemList.isEmpty() || algorithmList.isEmpty() || metricList.isEmpty()) {
            return null;
        }
        if (problemList.size() > 1 && algorithmList.size() > 1) {
            return null;
        }

        // 2.׼�����ݼ�
        XYDataset dataset = null;
        String title = "";
        String xlabel = "��������";
        String ylabel = metricList.get(0);
        if (problemList.size() == 1) {
            title = "����" + problemList.get(0) + "���Ե�����µ��㷨�Ա�";
            dataset = DataSetHelper.createAlgorithmsCompareDataset(problemList.get(0), algorithmList, metricList.get(0),
                    runId);
        } else if (algorithmList.size() == 1) {
            title = "����" + algorithmList.get(0) + "�㷨������µĲ��ԶԱ�";
            dataset = DataSetHelper.createProblemsCompareDataset(problemList, algorithmList.get(0), metricList.get(0),
                    runId);
        }

        // 3.��������
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
     * ����XYDataset����JFreeChart����
     *
     * @param title
     * @param xlabel
     * @param ylabel
     * @param dataset
     * @return
     */
    public static JFreeChart createChart(String title, String xlabel, String ylabel, XYDataset dataset) {
        Font titleFont = new Font("����", Font.BOLD, 20);
        Font numFont = new Font("Times New Roman", 10, 16);
        Font labelFont = new Font("����", 10, 16); // �趨���塢���͡��ֺ�

        // ����JFreeChart����
        JFreeChart jfreechart = ChartFactory.createXYLineChart(title, xlabel, ylabel, dataset, PlotOrientation.VERTICAL,
                true, true, false);

        // ���ñ���ɫΪ��ɫ
        jfreechart.setBackgroundPaint(Color.white);

        // ���ñ�������
        jfreechart.getTitle().setFont(titleFont);

        // �������ݼ�
        XYPlot plot = (XYPlot) jfreechart.getPlot();
        plot.setDataset(dataset);

        // ���ñ���ɫ�ͱ���ɫ��͸����
        plot.setBackgroundAlpha(0f);
        plot.setForegroundAlpha(0.5f);

        // ������߿��Ƿ�ɼ�
        plot.setOutlineVisible(false);

        // ���ú�������
        ValueAxis categoryAxis = plot.getDomainAxis();
        categoryAxis.setPositiveArrowVisible(true);// ���Ӻ���ļ�ͷ
        categoryAxis.setTickLabelFont(numFont);
        categoryAxis.setLabelFont(labelFont);

        // ������������
        ValueAxis rangeAxis = plot.getRangeAxis();
        rangeAxis.setPositiveArrowVisible(true);// ��������ļ�ͷ
        rangeAxis.setTickLabelFont(numFont);
        rangeAxis.setLabelFont(labelFont);

        // ����������
        plot.setDomainGridlinePaint(Color.gray);  // ���ú��������߻�ɫ
        plot.setDomainGridlinesVisible(true);     // ������ʾ����������
        plot.setRangeGridlinePaint(Color.gray);   // �������������߻�ɫ
        plot.setRangeGridlinesVisible(true);      // ������ʾ����������

        // ����ͼ������
        LegendTitle legendTitle = jfreechart.getLegend();
        legendTitle.setItemFont(labelFont);
        legendTitle.setPosition(RectangleEdge.BOTTOM);

        // ����ÿһ�������ߺ����ݵ�
        XYItemRenderer r = plot.getRenderer();
        if (r instanceof XYLineAndShapeRenderer) {
            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
            // ���ݵ���ʽ����
            renderer.setBaseShapesVisible(true);    // ���ݵ���ʾ���
            renderer.setBaseShapesFilled(true);     // ���ݵ�������Ƿ����
            renderer.setUseFillPaint(true); // ���Ҫ�����ݵ����������Զ������ɫ�������־λ����Ϊ��
            renderer.setLegendItemToolTipGenerator(new StandardXYSeriesLabelGenerator("{0}"));// ����Ƶ�����������ʾ��ϢΪ�������ߵ����֡�

            Color[] colors = new Color[]{
                    Color.RED,
                    Color.GREEN,
                    Color.BLUE,
                    Color.ORANGE,
                    Color.CYAN,
                    Color.lightGray
            };

            for (int i = 0; i < colors.length; i++) {
                // �������߼Ӵ�
                renderer.setSeriesStroke(i, new BasicStroke(1.5F));
                // �������ݵ������ɫ
                renderer.setSeriesFillPaint(i, Color.WHITE);    // �ڶ��������������ݵ�����������ɫΪ��ɫ
                // ������������ɫ
                renderer.setSeriesPaint(i, colors[i]);
            }
        }

        return jfreechart;
    }

    public static void main(String[] args) {
        createLineChart("EDF_PS", "NSGAII", "HV", 0);
    }
}
