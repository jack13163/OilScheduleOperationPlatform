package opt.easyjmetal.problem.schedule.util;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class Boxplot {

    private static final int COLS = 9;      // 列数
    private static final int VISIBLE = 9;   // 可见的列数
    private static final int ROWS = 2;      // 系列个数
    private static final int VALUES = 10;   // 离散数据的格式
    private static final Random rnd = new Random();
    private List<String> columns;
    private List<List<List<Double>>> data;
    private DefaultBoxAndWhiskerCategoryDataset dataset;
    private CategoryPlot plot;
    private ChartPanel chartPanel;
    private int start = 0;

    public Boxplot() {
        createData();
        createDataset(start);
        createChartPanel();
    }

    private void createData() {
        columns = new ArrayList<String>(COLS);
        data = new ArrayList<List<List<Double>>>();
        for (int i = 0; i < COLS; i++) {
            String name = "Category" + String.valueOf(i + 1);
            columns.add(name);
            List<List<Double>> list = new ArrayList<List<Double>>();
            for (int j = 0; j < ROWS; j++) {
                list.add(createValues());
            }
            data.add(list);
        }
    }

    private List<Double> createValues() {
        List<Double> list = new ArrayList<Double>();
        for (int i = 0; i < VALUES; i++) {
            list.add(rnd.nextGaussian());
        }
        return list;
    }

    private void createDataset(int start) {
        dataset = new DefaultBoxAndWhiskerCategoryDataset();
        for (int i = start; i < start + VISIBLE; i++) {
            List<List<Double>> list = data.get(i);
            int row = 0;
            for (List<Double> values : list) {
                String category = columns.get(i);
                dataset.add(values, "s" + row++, category);
            }
        }
    }

    private void createChartPanel() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis("Run Time");
        BoxAndWhiskerRenderer renderer = new BoxAndWhiskerRenderer();
        plot = new CategoryPlot(dataset, xAxis, yAxis, renderer);
        JFreeChart chart = new JFreeChart(plot);
        chartPanel = new ChartPanel(chart);
    }

    public ChartPanel getChartPanel() {
        return chartPanel;
    }

    public static void main(String[] args) throws IOException {
        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                JFrame frame = new JFrame();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                Boxplot demo = new Boxplot();
                frame.add(demo.getChartPanel(), BorderLayout.CENTER);
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }
        });
    }
}