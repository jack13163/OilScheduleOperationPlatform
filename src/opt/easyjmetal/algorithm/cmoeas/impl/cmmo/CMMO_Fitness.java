package opt.easyjmetal.algorithm.cmoeas.impl.cmmo;

import opt.easyjmetal.algorithm.common.MatlabUtilityFunctionsWrapper;
import opt.easyjmetal.core.SolutionSet;

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
        double[] D = MatlabUtilityFunctionsWrapper.di(solutionSet.writeObjectivesToMatrix());

        // 计算适应度值
        for (int i = 0; i < solutionSet.size(); i++) {
            solutionSet.get(i).setFitness(R[i] + D[i]);
        }
    }
}