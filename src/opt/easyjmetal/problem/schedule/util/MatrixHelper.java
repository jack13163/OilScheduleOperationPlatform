package opt.easyjmetal.problem.schedule.util;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;

import java.util.List;

public class MatrixHelper {

    public static void main(String[] args) {
        DenseMatrix64F L = new DenseMatrix64F(3, 3); // 初始化一个矩阵，并进行下面的赋值
        L.set(0, 0, 4.0);
        L.set(0, 1, 13.0);
        L.set(0, 2, -16.0);
        L.set(1, 0, 12.0);
        L.set(1, 1, 37.0);
        L.set(1, 2, -43.0);
        L.set(2, 0, -16.0);
        L.set(2, 1, -43.0);
        L.set(2, 2, 98.0);
        System.out.println("data为:");
        System.out.println(L);

        DenseMatrix64F mu_0 = getRowMax(L); // 获取行最大值
        System.out.println("矩阵每一行的最大值为:" + mu_0);

        DenseMatrix64F mu_1 = getColMax(L); // 获取列最大值
        System.out.println("矩阵每一列的最大值为:" + mu_1);

        DenseMatrix64F mu_2 = getRowMin(L); // 获取行最小值
        System.out.println("矩阵每一行的最小值为:" + mu_2);

        DenseMatrix64F mu_3 = getColMin(L); // 获取列最小值
        System.out.println("矩阵每一列的最小值为:" + mu_3);
    }

    /**
     * max of the data row
     *
     * @param data
     * @return
     */
    public static DenseMatrix64F getRowMax(DenseMatrix64F data) {

        if (data.numCols > 1) {
            // 将矩阵转换为行向量
            DenseMatrix64F[] dataVectors = new DenseMatrix64F[data.numRows];
            CommonOps.rowsToVector(data, dataVectors);

            DenseMatrix64F max = new DenseMatrix64F(dataVectors.length, 1);// initialized to 0
            for (int i = 0; i < dataVectors.length; i++) {
                DenseMatrix64F vec = dataVectors[i];
                max.set(i, CommonOps.elementMax(vec)); // 获取每一行数据的最大值
            }

            return max;
        } else {
            return data;
        }
    }

    /**
     * max of the data col
     *
     * @param data
     * @return
     */
    public static DenseMatrix64F getColMax(DenseMatrix64F data) {

        if (data.numRows > 1) {
            // 将矩阵转换为列向量
            DenseMatrix64F[] dataVectors = new DenseMatrix64F[data.numCols];
            CommonOps.columnsToVector(data, dataVectors);

            DenseMatrix64F max = new DenseMatrix64F(1, dataVectors.length);// initialized to 0
            for (int i = 0; i < dataVectors.length; i++) {
                DenseMatrix64F vec = dataVectors[i];
                max.set(i, CommonOps.elementMax(vec)); // 获取每一列数据的最大值
            }

            return max;
        } else {
            return data;
        }
    }

    /**
     * min of the data row
     *
     * @param data
     * @return
     */
    public static DenseMatrix64F getRowMin(DenseMatrix64F data) {

        if (data.numCols > 1) {
            // 将矩阵转换为行向量
            DenseMatrix64F[] dataVectors = new DenseMatrix64F[data.numRows];
            CommonOps.rowsToVector(data, dataVectors);

            DenseMatrix64F max = new DenseMatrix64F(dataVectors.length, 1);// initialized to 0
            for (int i = 0; i < dataVectors.length; i++) {
                DenseMatrix64F vec = dataVectors[i];
                max.set(i, CommonOps.elementMin(vec)); // 获取每一行数据的最小值
            }

            return max;
        } else {
            return data;
        }
    }

    /**
     * min of the data col
     *
     * @param data
     * @return
     */
    public static DenseMatrix64F getColMin(DenseMatrix64F data) {

        // 将矩阵转换为列向量
        if (data.numRows > 1) {
            DenseMatrix64F[] dataVectors = new DenseMatrix64F[data.numCols];
            CommonOps.columnsToVector(data, dataVectors);

            DenseMatrix64F max = new DenseMatrix64F(1, dataVectors.length);// initialized to 0
            for (int i = 0; i < dataVectors.length; i++) {
                DenseMatrix64F vec = dataVectors[i];
                max.set(i, CommonOps.elementMin(vec)); // 获取每一列数据的最小值
            }

            return max;
        } else {
            return data;
        }
    }

    /**
     * min of the data row
     *
     * @param data
     * @return
     */
    public static double[] getColMax(List<DenseMatrix64F> data) {

        if (data.isEmpty()) {
            return null;
        }

        DenseMatrix64F result = new DenseMatrix64F(data.size(), data.get(0).numCols);
        // 将矩阵转换为行向量
        for (int i = 0; i < data.size(); i++) {

            if (data.get(i) != null) {
                DenseMatrix64F vec = getColMax(data.get(i));

                for (int j = 0; j < vec.numCols; j++) {
                    result.set(i, j, vec.get(j)); // 获取每一行数据的最小值
                }
            }
        }

        return getColMax(result).data;
    }

    /**
     * min of the data col
     *
     * @param data
     * @return
     */
    public static double[] getColMin(List<DenseMatrix64F> data) {

        if (data.isEmpty()) {
            return null;
        }

        DenseMatrix64F result = new DenseMatrix64F(data.size(), data.get(0).numCols);
        // 将矩阵转换为行向量
        for (int i = 0; i < data.size(); i++) {

            if (data.get(i) != null) {
                DenseMatrix64F vec = getColMin(data.get(i));

                for (int j = 0; j < vec.numCols; j++) {
                    result.set(i, j, vec.get(j)); // 获取每一行数据的最小值
                }
            }
        }

        return getColMin(result).data;
    }
}