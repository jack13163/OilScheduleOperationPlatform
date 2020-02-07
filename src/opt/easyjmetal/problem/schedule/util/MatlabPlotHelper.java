package opt.easyjmetal.problem.schedule.util;

import com.mathworks.toolbox.javabuilder.MWArray;
import com.mathworks.toolbox.javabuilder.MWClassID;
import com.mathworks.toolbox.javabuilder.MWException;
import com.mathworks.toolbox.javabuilder.MWNumericArray;
import plot.PlotUtil;

public class MatlabPlotHelper {
    private static MatlabPlotHelper _instance;
    private PlotUtil plotUtil = null;

    // 之所以采用多线程的方式绘图是因为如果不使用waitForFigures阻塞当前线程，那么绘图将一闪而过，看不到
    private MatlabPlotHelper() {
        try {
            plotUtil = new PlotUtil();
        } catch (MWException e) {
            System.out.println("创建绘图对象失败");
        }
    }

    public static MatlabPlotHelper getInstance() {
        if (_instance == null) {
            _instance = new MatlabPlotHelper();
        }
        return _instance;
    }

    /**
     * 绘制雷达图
     *
     * @param data
     * @param lim
     * @param label
     * @param legend
     */
    public void radar(double[][] data, double[][] lim, String label, String legend) {
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {

                    MWNumericArray B = MWNumericArray.newSparse(data, MWClassID.DOUBLE);
                    MWNumericArray C = MWNumericArray.newSparse(lim, MWClassID.DOUBLE);
                    plotUtil.drawRadar(B, C, label, legend);
                    plotUtil.waitForFigures();

                    MWArray.disposeArray(B);
                    MWArray.disposeArray(C);
                } catch (Exception e) {
                    System.out.println("Matlab plot module error.");
                    System.exit(1);
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * 普通绘图，绘制单条线
     *
     * @param x
     * @param y
     */
    public void plot(double[] x, double[] y) {

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    MWNumericArray B = MWNumericArray.newSparse(x, MWClassID.DOUBLE);
                    MWNumericArray C = MWNumericArray.newSparse(y, MWClassID.DOUBLE);
                    plotUtil.drawPlotSimple(B, C);
                    plotUtil.waitForFigures();

                    MWArray.disposeArray(B);
                    MWArray.disposeArray(C);
                } catch (Exception e) {
                    System.out.println("Matlab plot module error.");
                    System.exit(1);
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * 绘制多条线
     *
     * @param x
     * @param y
     */
    public void plot2(double[][] x, double[][] y, String labels) {
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    MWNumericArray B = MWNumericArray.newSparse(x, MWClassID.DOUBLE);
                    MWNumericArray C = MWNumericArray.newSparse(y, MWClassID.DOUBLE);

                    plotUtil.drawPlot(B, C, labels);
                    plotUtil.waitForFigures();

                    MWArray.disposeArray(B);
                    MWArray.disposeArray(C);
                } catch (Exception e) {
                    System.out.println("Matlab plot module error.");
                    System.exit(1);
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * 绘制甘特图
     *
     * @param labels
     * @param data
     */
    public synchronized void gante(String labels, double[][] data) {

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    // java数组转换成matlab矩阵
                    MWNumericArray A = MWNumericArray.newSparse(data, MWClassID.DOUBLE);
                    plotUtil.printgante(A, labels);
                    plotUtil.waitForFigures();// 等待绘图完成

                    MWArray.disposeArray(A);
                } catch (Exception e) {
                    System.out.println("Matlab plot module error.");
                    System.exit(1);
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }
}
