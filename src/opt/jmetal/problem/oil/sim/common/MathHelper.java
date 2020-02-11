package opt.jmetal.problem.oil.sim.common;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class MathHelper {
    public static double sum(double[] array) {
        double sum = 0;
        for (int i = 0; i < array.length; i++) {
            sum += array[i];
        }
        return sum;
    }

    /**
     * 四舍五入
     *
     * @param data
     * @param num
     * @return
     */
    public static double precision(Double data, int num) {
        double tmp1 = Math.pow(10, num);
        double tmp2 = Math.pow(0.1, num);
        double result = Math.round(data * tmp1) * tmp2;
        BigDecimal bg = new BigDecimal(result).setScale(2, RoundingMode.HALF_UP);
        return bg.doubleValue();
    }

    /**
     * 字符串数组转为double数组
     *
     * @param array
     * @return
     */
    public static double[][] stringToDouble(List<String[]> array) {
        double[][] result = new double[array.size()][array.get(0).length];
        for (int i = 0; i < array.size(); i++) {
            String[] line = array.get(i);
            for (int j = 0; j < line.length; j++) {
                result[i][j] = Double.parseDouble(line[j]);
            }
        }
        return result;
    }
}
