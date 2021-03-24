package opt.easyjmetal.problem.schedule.util.kmeans;

import opt.easyjmetal.util.fileinput.ParetoFrontUtil;
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
        // ��������
        int MaxIterationCount = 100;
        int MaxK = 2;

        // ����PF
        String[] algorithmNames = {"NSGAII_CDP", "ISDEPLUS_CDP", "NSGAIII_CDP", "MOEAD_CDP", "MOEAD_IEpsilon", "MOEAD_Epsilon", "MOEAD_SR", "C_MOEAD", "PPS_MOEAD"};
        String[] problemNames = {"EDF_PS", "EDF_TSS"};
        int runtimes = 10;
        String basePath = "result/easyjmetal/twopipeline/";

        // ����paretoǰ����
        try {
            ParetoFrontUtil.generateOilScheduleParetoFront(algorithmNames, problemNames, runtimes, basePath);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // ��ȡ����
        double[][] data = VectorFileUtils.readDoubleValues(basePath + "oil.pf");

        // ��׼��
        INDArray indArray = Nd4j.create(data);                  // ������������
        INDArray max = Nd4j.max(indArray, 0);          // dimension = 0ʱ������ȡ�����Сֵ
        INDArray min = Nd4j.min(indArray, 0);
        INDArray normArray = indArray.sub(Nd4j.repeat(min, data.length))
                .div(Nd4j.repeat(max, data.length).sub(Nd4j.repeat(min, data.length)));

        // ���ݱ�׼�����������������KMeans�ĵ����
        List<Point> points = Point.toPoints(normArray);
        String[] serizes = new String[]{"Calinski Harabaz"};
        double[][] x = new double[1][MaxK - 2];
        double[][] y = new double[1][MaxK - 2];
        for (int k = 2; k < MaxK; k++) {
            // ����һ��KMeans������󣬲����ֱ��� ���վ��������������������������뺯��
            KMeansClustering kMeansClustering = KMeansClustering.setup(k, MaxIterationCount, Distance.EUCLIDEAN, false);

            // ����kmeans�����㷨
            ClusterSet clusterSet = kMeansClustering.applyTo(points);

            // ��������ϵ������������Ч��
            x[0][k - 2] = k;
            y[0][k - 2] = ClusterEvaluation.CalinskiHarabaz(points, clusterSet);
            System.out.println("k = " + k + "ʱ��Calinski Harabaz: " + y[0][k - 2]);
        }

        // ����1������CategoryDataset����׼�����ݣ�
        XYDataset dataset = createDataset(serizes, x, y);
        // ����2������Dataset ����JFreeChart�����Լ�����Ӧ������
        JFreeChart freeChart = createChart(dataset);
        org.jfree.chart.ChartFrame frame = new org.jfree.chart.ChartFrame("Silhouette Coefficient", freeChart);
        frame.pack();
        frame.setVisible(true);
        // ����3����JFreeChart����������ļ���Servlet�������
        saveAsFile(freeChart, "data/line.jpg", 600, 400);
    }

    // ����Ϊ�ļ�
    public static void saveAsFile(JFreeChart chart, String outputPath, int weight, int height) {
        FileOutputStream out = null;
        try {
            File outFile = new File(outputPath);
            if (!outFile.getParentFile().exists()) {
                outFile.getParentFile().mkdirs();
            }
            out = new FileOutputStream(outputPath);
            // ����ΪJPEG
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

    // ����CategoryDataset����JFreeChart����
    public static JFreeChart createChart(XYDataset xyDataset) {
        // ����JFreeChart����ChartFactory.createLineChart
        JFreeChart jfreechart = ChartFactory.createXYLineChart("", // ����
                "number of clusters", // categoryAxisLabel ��category�ᣬ���ᣬX���ǩ��
                "Silhouette Coefficient", // valueAxisLabel��value�ᣬ���ᣬY��ı�ǩ��
                xyDataset, // dataset
                PlotOrientation.VERTICAL, true, // legend
                false, // tooltips
                false); // URLs
        // ʹ��CategoryPlot���ø��ֲ������������ÿ���ʡ�ԡ�
        XYPlot plot = (XYPlot) jfreechart.getPlot();

        NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
        //����ʱ������ʱ������ʾ��ʽ��1����һ�����
        domainAxis.setTickUnit(new NumberTickUnit(7));

        // ����ɫ ͸����
        plot.setBackgroundAlpha(0.5f);
        // ǰ��ɫ ͸����
        plot.setForegroundAlpha(0.5f);
        return jfreechart;
    }

    private static XYDataset createDataset(String[] seriesNames, double[][] x, double[][] y) {
        if (seriesNames.length != x.length && seriesNames.length != y.length) {
            return null;
        }
        // ��XYSeriesCollection�����XYSeries ����
        XYSeriesCollection xyseriescollection = new XYSeriesCollection();

        for (int i = 0; i < seriesNames.length; i++) {
            XYSeries xyseries = new XYSeries(seriesNames[i]); //�Ȳ���XYSeries ����
            for (int j = 0; j < x[i].length; j++) {
                xyseries.add(x[i][j], y[i][j]);
            }
            xyseriescollection.addSeries(xyseries);
        }

        return xyseriescollection;
    }
}
