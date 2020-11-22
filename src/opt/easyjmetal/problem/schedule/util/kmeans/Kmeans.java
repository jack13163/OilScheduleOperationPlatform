package opt.easyjmetal.problem.schedule.util.kmeans;

import opt.easyjmetal.algorithm.util.Utils;
import opt.easyjmetal.util.JMException;
import opt.easyjmetal.util.fileinput.VectorFileUtils;
import org.deeplearning4j.clustering.algorithm.Distance;
import org.deeplearning4j.clustering.cluster.ClusterSet;
import org.deeplearning4j.clustering.cluster.Point;
import org.deeplearning4j.clustering.kmeans.KMeansClustering;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class Kmeans {
    public static void main(String[] args) {
        // 参数设置
        int MaxIterationCount = 100;
        int MaxK = 2;

        // 生成PF
        String[] algorithmNames = {"NSGAII_CDP", "ISDEPLUS_CDP", "NSGAIII_CDP", "MOEAD_CDP", "MOEAD_IEpsilon", "MOEAD_Epsilon", "MOEAD_SR", "C_MOEAD", "PPS_MOEAD"};
        String[] problemNames = {"EDF_PS", "EDF_TSS"};
        int runtimes = 10;
        // 生成pareto前沿面
        try {
            Utils.generateOilScheduleParetoFront(algorithmNames, problemNames, runtimes);
        } catch (JMException e) {
            e.printStackTrace();
        }

        // 读取数据
        double[][] data = VectorFileUtils.readDoubleValues("result/easyjmetal/oil.pf");

        // 标准化
        INDArray indArray = Nd4j.create(data);                  // 创建张量矩阵
        INDArray max = Nd4j.max(indArray, 0);          // dimension = 0时，按列取最大最小值
        INDArray min = Nd4j.min(indArray, 0);
        INDArray normArray = indArray.sub(Nd4j.repeat(min, data.length))
                .div(Nd4j.repeat(max, data.length).sub(Nd4j.repeat(min, data.length)));

        // 根据标准化后的张量矩阵生成KMeans的点对象
        List<Point> points = Point.toPoints(normArray);
        String[] serizes = new String[]{"Calinski Harabaz"};
        double[][] x = new double[1][MaxK - 2];
        double[][] y = new double[1][MaxK - 2];
        for (int k = 2; k < MaxK; k++) {
            // 声明一个KMeans聚类对象，参数分别是 最终聚类的类别数量，迭代次数，距离函数
            KMeansClustering kMeansClustering = KMeansClustering.setup(k, MaxIterationCount, Distance.EUCLIDEAN, false);

            // 运行kmeans聚类算法
            ClusterSet clusterSet = kMeansClustering.applyTo(points);

            // 计算轮廓系数，评估聚类效果
            x[0][k - 2] = k;
            y[0][k - 2] = ClusterEvaluation.CalinskiHarabaz(points, clusterSet);
            System.out.println("k = " + k + "时，Calinski Harabaz: " + y[0][k - 2]);
        }

        // 步骤1：创建CategoryDataset对象（准备数据）
        XYDataset dataset = createDataset(serizes, x, y);
        // 步骤2：根据Dataset 生成JFreeChart对象，以及做相应的设置
        JFreeChart freeChart = createChart(dataset);
        org.jfree.chart.ChartFrame frame = new org.jfree.chart.ChartFrame("Silhouette Coefficient", freeChart);
        frame.pack();
        frame.setVisible(true);
        // 步骤3：将JFreeChart对象输出到文件，Servlet输出流等
        saveAsFile(freeChart, "data/line.jpg", 600, 400);
    }

    // 保存为文件
    public static void saveAsFile(JFreeChart chart, String outputPath, int weight, int height) {
        FileOutputStream out = null;
        try {
            File outFile = new File(outputPath);
            if (!outFile.getParentFile().exists()) {
                outFile.getParentFile().mkdirs();
            }
            out = new FileOutputStream(outputPath);
            // 保存为JPEG
            ChartUtilities.writeChartAsJPEG(out, chart, 600, 400);
            out.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    // do nothing
                }
            }
        }
    }

    // 根据CategoryDataset创建JFreeChart对象
    public static JFreeChart createChart(XYDataset xyDataset) {
        // 创建JFreeChart对象：ChartFactory.createLineChart
        JFreeChart jfreechart = ChartFactory.createXYLineChart("", // 标题
                "number of clusters", // categoryAxisLabel （category轴，横轴，X轴标签）
                "Silhouette Coefficient", // valueAxisLabel（value轴，纵轴，Y轴的标签）
                xyDataset, // dataset
                PlotOrientation.VERTICAL, true, // legend
                false, // tooltips
                false); // URLs
        // 使用CategoryPlot设置各种参数。以下设置可以省略。
        XYPlot plot = (XYPlot) jfreechart.getPlot();

        NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
        //设置时间间隔和时间轴显示格式：1个月一个间隔
        domainAxis.setTickUnit(new NumberTickUnit(7));

        // 背景色 透明度
        plot.setBackgroundAlpha(0.5f);
        // 前景色 透明度
        plot.setForegroundAlpha(0.5f);
        return jfreechart;
    }

    private static XYDataset createDataset(String[] seriesNames, double[][] x, double[][] y) {
        if (seriesNames.length != x.length && seriesNames.length != y.length) {
            return null;
        }
        // 往XYSeriesCollection添加入XYSeries 对象
        XYSeriesCollection xyseriescollection = new XYSeriesCollection();

        for (int i = 0; i < seriesNames.length; i++) {
            XYSeries xyseries = new XYSeries(seriesNames[i]); //先产生XYSeries 对象
            for (int j = 0; j < x[i].length; j++) {
                xyseries.add(x[i][j], y[i][j]);
            }
            xyseriescollection.addSeries(xyseries);
        }

        return xyseriescollection;
    }
}
