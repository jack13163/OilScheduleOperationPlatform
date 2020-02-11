package opt.jmetal.problem.oil.chart.util;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.SpiderWebPlot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.RectangleEdge;

import javax.swing.*;
import java.io.File;

public class SpiderChart {
    public static void main(String args[]) {
        JFrame frame = new JFrame();
        frame.add(erstelleSpinnenDiagramm());

        // 设置窗口大小为最佳大小
        frame.pack();
        frame.setVisible(true);

        // 关闭窗口的行为设置为退出应用程序
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    public static JPanel erstelleSpinnenDiagramm() {
        // 创建雷达图数据
        String title = "实验对比雷达图";
        String[] series = new String[]{"chargeTime", "tankMix", "pipeMix", "energyCost", "tankUsed"};
        String[] legend = new String[]{"伍老师", "SPEA", "NSGAII"};
        double[][] data = new double[][]{
                new double[]{23, 223, 200, 1304.24, 11},
                new double[]{23, 212, 189, 988, 10},
                new double[]{23, 200, 195, 1110, 11},
        };
        DefaultCategoryDataset dataset = createDataset(series, legend, data);
        SpiderWebPlot mySpiderWebPlot = new SpiderWebPlot(dataset);

        JFreeChart jfreechart = new JFreeChart(title, TextTitle.DEFAULT_FONT, mySpiderWebPlot, false);

        // 设置legend图例
        LegendTitle legendtitle = new LegendTitle(mySpiderWebPlot);
        legendtitle.setPosition(RectangleEdge.BOTTOM);
        jfreechart.addSubtitle(legendtitle);

        ChartPanel chartpanel = new ChartPanel(jfreechart);

        // 设置默认保存路径
        chartpanel.setDefaultDirectoryForSaveAs(new File("data/"));

        return chartpanel;
    }

    /**
     * 创建数据集
     *
     * @param series
     * @param legend
     * @param data
     * @return
     */
    public static DefaultCategoryDataset createDataset(String[] series, String[] legend, double[][] data) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                dataset.addValue(data[i][j], legend[i], series[j]);
            }
        }
        return dataset;
    }
}
