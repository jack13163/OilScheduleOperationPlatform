package opt.easyjmetal.algorithm.common;

import opt.easyjmetal.core.SolutionSet;
import opt.easyjmetal.util.LinearNormalization;

public class ISDEPlus_Fitness {

    /**
     * 计算解集的适应度值
     * @param solutionSet
     */
    public static void computeFitnessValue(SolutionSet solutionSet) {
        // FunctionValue为目标函数值 ，N为种群规模，M为目标函数的数量
        double[][] distance = solutionSet.writeObjectivesToMatrix();
        int numOfObjects = solutionSet.get(0).getNumberOfObjectives();

        // 最大最小归一化
        double[][] normalizedDistance = LinearNormalization.normalize4Scale(distance);

        // 按照个体目标值的和的升序排序
        double[] sumedDistance = LinearNormalization.sumByRow(normalizedDistance);
        int[] indexs = LinearNormalization.sortArray(sumedDistance);

        for (int i = 1; i < solutionSet.size(); i++) {

            // 计算第i个个体与前i-1个个体的SDE距离
            double[] betweenDistances = new double[i];
            for (int j = 0; j < i - 1; j++) {
                for (int k = 0; k < numOfObjects; k++) {
                    // 只计算qi<pj时的值
                    if(normalizedDistance[indexs[i]][k] < normalizedDistance[indexs[j]][k]) {
                        betweenDistances[j] += (normalizedDistance[indexs[i]][k] - normalizedDistance[indexs[j]][k]) * (normalizedDistance[indexs[i]][k] - normalizedDistance[indexs[j]][k]);
                    }
                }
            }

            // 求个体j与种群中其他个体的欧式距离的最小值
            double minDistance = LinearNormalization.minV(betweenDistances);
            solutionSet.get(indexs[i]).setFitness(Math.exp(-minDistance));
        }
    }

}
