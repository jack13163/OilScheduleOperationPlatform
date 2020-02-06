package com.analysis;

import org.uma.jmetal.util.fileinput.VectorFileUtils;

import java.math.BigDecimal;
import java.util.Arrays;

public class AHPComputeWeight {
    /**
     * @param args
     */
    public static void main(String[] args) {
        /** a为N*N矩阵 */
        double[][] a = VectorFileUtils.readVectors("data/ahp_weight.csv");
        AHPComputeWeight instance = AHPComputeWeight.getInstance();
        double[] weight = instance.weight(a);
        System.out.println(Arrays.toString(weight));
    }

    // 单例
    private static final AHPComputeWeight acw = new AHPComputeWeight();

    // 平均随机一致性指针
    private double[] RI = {0.00, 0.00, 0.58, 0.90, 1.12, 1.21, 1.32, 1.41,
            1.45, 1.49};

    // 随机一致性比率
    private double CR = 0.0;

    // 最大特征值
    private double lamta = 0.0;

    /**
     * 私有构造
     */
    private AHPComputeWeight() {

    }

    /**
     * 返回单例
     *
     * @return
     */
    public static AHPComputeWeight getInstance() {
        return acw;
    }

    /**
     * 计算权重
     *
     * @param a
     * @return
     */
    public double[] weight(double[][] a) {

        int N = a[0].length;
        double[] weight = new double[N];

        // 初始向量Wk
        double[] w0 = new double[N];
        for (int i = 0; i < N; i++) {
            w0[i] = 1.0 / N;
        }

        // 一般向量W（k+1）
        double[] w1 = new double[N];

        // W（k+1）的归一化向量
        double[] w2 = new double[N];

        double sum = 1.0;

        double d = 1.0;

        // 误差
        double delt = 0.00001;

        while (d > delt) {
            d = 0.0;
            sum = 0;

            // 获取向量
            int index = 0;
            for (int j = 0; j < N; j++) {
                double t = 0.0;
                for (int l = 0; l < N; l++)
                    t += a[j][l] * w0[l];
                // w1[j] = a[j][0] * w0[0] + a[j][1] * w0[1] + a[j][2] * w0[2];
                w1[j] = t;
                sum += w1[j];
            }

            // 向量归一化
            for (int k = 0; k < N; k++) {
                w2[k] = w1[k] / sum;

                // 最大差值
                d = Math.max(Math.abs(w2[k] - w0[k]), d);

                // 用于下次迭代使用
                w0[k] = w2[k];
            }
        }

        // 计算矩阵最大特征值lamta，CI，RI
        lamta = 0.0;

        for (int k = 0; k < N; k++) {
            lamta += w1[k] / (N * w0[k]);
        }

        double CI = (lamta - N) / (N - 1);

        if (RI[N - 1] != 0) {
            CR = CI / RI[N - 1];
        }

        // 四舍五入处理
        lamta = round(lamta, 3);
        CI = round(CI, 3);
        CR = round(CR, 3);

        for (int i = 0; i < N; i++) {
            w0[i] = round(w0[i], 4);
            w1[i] = round(w1[i], 4);
            w2[i] = round(w2[i], 4);
        }
        // 控制台打印输出

        System.out.println("lamta=" + lamta);
        System.out.println("CI=" + CI);
        System.out.println("CR=" + CR);

        // 控制台打印权重
        System.out.println("w0[]=");
        for (int i = 0; i < N; i++) {
            System.out.print(w0[i] + " ");
        }
        System.out.println("");

        System.out.println("w1[]=");
        for (int i = 0; i < N; i++) {
            System.out.print(w1[i] + " ");
        }
        System.out.println("");

        System.out.println("w2[]=");
        for (int i = 0; i < N; i++) {
            weight[i] = w2[i];
            System.out.print(w2[i] + " ");
        }
        System.out.println("");

        return weight;
    }

    /**
     * 四舍五入
     *
     * @param v
     * @param scale
     * @return
     */
    public double round(double v, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException(
                    "The scale must be a positive integer or zero");
        }
        BigDecimal b = new BigDecimal(Double.toString(v));
        BigDecimal one = new BigDecimal("1");
        return b.divide(one, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * 返回随机一致性比率
     *
     * @return
     */
    public double getCR() {
        return CR;
    }
}
