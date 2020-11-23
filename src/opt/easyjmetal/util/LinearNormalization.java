package opt.easyjmetal.util;

/**
 * 最大最小归一化【按列】
 */
public class LinearNormalization {
    /**
     * 线性归一化 公式：X(norm) = (X - min) / (max - min)
     *
     * @param points 原始数据
     * @return 归一化后的数据
     */
    public static double[][] normalize4Scale(double[][] points) {
        if (points == null || points.length < 1) {
            return points;
        }
        double[][] p = new double[points.length][points[0].length];
        double[] matrixJ;
        double maxV;
        double minV;
        for (int j = 0; j < points[0].length; j++) {
            matrixJ = getMatrixCol(points, j);// 获取某一列
            maxV = maxV(matrixJ);
            minV = minV(matrixJ);
            for (int i = 0; i < points.length; i++) {
                p[i][j] = maxV == minV ? minV : (points[i][j] - minV) / (maxV - minV);
            }
        }
        return p;
    }

    /**
     * 按照升序排列，并返回下标
     *
     * @param arr
     * @return
     */
    public static int[] sortArray(double[] arr) {
        double temp;
        int index;
        int k = arr.length;
        int[] Index = new int[k];
        for (int i = 0; i < k; i++) {
            Index[i] = i;
        }

        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr.length - i - 1; j++) {
                if (arr[j] > arr[j + 1])//升序
                {
                    temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;

                    index = Index[j];
                    Index[j] = Index[j + 1];
                    Index[j + 1] = index;
                }
            }
        }
        return Index;
    }

    /**
     * 按行求和
     *
     * @param points 原始数据
     * @return 求和后的数据
     */
    public static double[] sumByRow(double[][] points) {
        if (points == null || points.length < 1) {
            return null;
        }
        double[] sumValue = new double[points.length];
        double[] matrixJ;
        for (int j = 0; j < points.length; j++) {
            matrixJ = getMatrixRow(points, j);// 获取某一行
            sumValue[j] = sumV(matrixJ);
        }
        return sumValue;
    }

    /**
     * 获取矩阵的某一列
     *
     * @param points points
     * @param column column
     * @return double[]
     */
    public static double[] getMatrixCol(double[][] points, int column) {
        double[] matrixJ = new double[points.length];
        for (int i = 0; i < points.length; i++) {
            matrixJ[i] = points[i][column];
        }
        return matrixJ;
    }

    /**
     * 获取矩阵的某一行
     *
     * @param points points
     * @param row    row
     * @return double[]
     */
    public static double[] getMatrixRow(double[][] points, int row) {
        double[] matrixJ = new double[points[0].length];
        for (int i = 0; i < points[0].length; i++) {
            matrixJ[i] = points[row][i];
        }
        return matrixJ;
    }

    /**
     * 获取数组中的元素的和
     *
     * @param matrixJ matrixJ
     * @return v
     */
    public static double sumV(double[] matrixJ) {
        double v = 0;
        for (int i = 0; i < matrixJ.length; i++) {
            v += matrixJ[i];
        }
        return v;
    }

    /**
     * 获取数组中的最小值
     *
     * @param matrixJ matrixJ
     * @return v
     */
    public static double minV(double[] matrixJ) {
        double v = matrixJ[0];
        for (int i = 0; i < matrixJ.length; i++) {
            if (matrixJ[i] < v) {
                v = matrixJ[i];
            }
        }
        return v;
    }

    /**
     * 获取数组中的最大值
     *
     * @param matrixJ matrixJ
     * @return v
     */
    public static double maxV(double[] matrixJ) {
        double v = matrixJ[0];
        for (int i = 0; i < matrixJ.length; i++) {
            if (matrixJ[i] > v) {
                v = matrixJ[i];
            }
        }
        return v;
    }
}
