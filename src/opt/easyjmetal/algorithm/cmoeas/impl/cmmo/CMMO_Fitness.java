package opt.easyjmetal.algorithm.cmoeas.impl.cmmo;

import opt.easyjmetal.core.SolutionSet;
import opt.easyjmetal.util.LinearNormalization;

public class CMMO_Fitness {

    /**
     * 计算解集的适应度值
     *
     * @param solutionSet
     */
    public static void computeFitnessValue(SolutionSet solutionSet) {
        // FunctionValue为目标函数值 ，N为种群规模，M为目标函数的数量
        int N = solutionSet.size();
        double CV = 0;
        int[][] Dominate = new int[N][N];
        for (int i = 0; i < N - 1; i++) {
            for (int j = i + 1; j < N; j++) {
                if (Math.max(0, -solutionSet.get(i).getOverallConstraintViolation())
                        < Math.max(0, -solutionSet.get(j).getOverallConstraintViolation())) {
                    Dominate[i][j] = 1;
                } else if (Math.max(0, -solutionSet.get(i).getOverallConstraintViolation())
                        > Math.max(0, -solutionSet.get(j).getOverallConstraintViolation())) {
                    Dominate[j][i] = 1;
                } else {
                    if (solutionSet.get(i).isDominated(solutionSet.get(j)) > 0) {
                        Dominate[i][j] = 1;
                    } else if (solutionSet.get(i).isDominated(solutionSet.get(j)) > 0) {
                        Dominate[j][i] = 1;
                    }
                }
            }
        }
        // Calculate S(i)
        double[] S = new double[Dominate.length];
        for (int i = 0; i < Dominate.length; i++) {
            for (int j = 0; j < Dominate[i].length; j++) {
                S[i] += Dominate[i][j];
            }
        }

        // Calculate R(i)
        double[] R = new double[N];
        for (int i = 0; i < N; i++) {
            // R(i) = sum(S(Dominate(:,i)));
            for (int j = 0; j < N; j++) {
                if(Dominate[j][i] > 0) {
                    R[i] += S[j];
                }
            }
        }

        // Calculate D(i)


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
                    if (normalizedDistance[indexs[i]][k] < normalizedDistance[indexs[j]][k]) {
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
