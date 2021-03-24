//  MoeadUtils.java
//
//  Author:
//       Antonio J. Nebro <antonio@lcc.uma.es>
//       Juan J. Durillo <durillo@lcc.uma.es>
//
//  Copyright (c) 2011 Antonio J. Nebro, Juan J. Durillo
//
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU Lesser General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU Lesser General Public License for more details.
//
//  You should have received a copy of the GNU Lesser General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.

package opt.easyjmetal.util.solution;

import opt.easyjmetal.core.Problem;
import opt.easyjmetal.core.Solution;
import opt.easyjmetal.core.SolutionSet;
import opt.easyjmetal.core.Variable;
import opt.easyjmetal.util.JMException;
import opt.easyjmetal.util.comparators.line.CrowdingDistanceComparator;
import opt.easyjmetal.util.distance.Distance;
import opt.easyjmetal.util.permutation.PseudoRandom;
import opt.easyjmetal.util.ranking.impl.RankingByCDP;
import opt.easyjmetal.util.ranking.ENS_FirstRank;

/**
 * Utilities methods to used by MOEA/D
 */
public class MoeadUtils {

    public static double distVector(double[] vector1, double[] vector2) {
        int dim = vector1.length;
        double sum = 0;
        for (int n = 0; n < dim; n++) {
            sum += (vector1[n] - vector2[n]) * (vector1[n] - vector2[n]);
        }
        return Math.sqrt(sum);
    }

    public static void minFastSort(double x[], int idx[], int n, int m) {
        for (int i = 0; i < m; i++) {
            for (int j = i + 1; j < n; j++) {
                if (x[i] > x[j]) {
                    double temp = x[i];
                    x[i] = x[j];
                    x[j] = temp;
                    int id = idx[i];
                    idx[i] = idx[j];
                    idx[j] = id;
                }
            }
        }
    }

    public static int[] returnSortedIndex(double x[], int flag) {
        if (x == null || x.length == 0) {
            return null;
        } else {
            int arrayLength = x.length;
            int[] result = new int[arrayLength];

            // Initialize the result
            for (int i = 0; i < arrayLength; i++) {
                result[i] = i;
            }

            // bubble sort
            if (flag == 1) { // ascending order
                for (int i = 0; i < arrayLength; i++) {
                    for (int j = i + 1; j < arrayLength; j++) {
                        if (x[i] > x[j]) {
                            double temp = x[i];
                            x[i] = x[j];
                            x[j] = temp;

                            int tempIndex = result[i];
                            result[i] = result[j];
                            result[j] = tempIndex;
                        }
                    }
                }
            } else if (flag == -1) { //descending order
                for (int i = 0; i < arrayLength; i++) {
                    for (int j = i + 1; j < arrayLength; j++) {
                        if (x[i] < x[j]) {
                            double temp = x[i];
                            x[i] = x[j];
                            x[j] = temp;

                            int tempIndex = result[i];
                            result[i] = result[j];
                            result[j] = tempIndex;
                        }
                    }
                }

            } else {
                System.out.println("Unknown parameter");
            }
            return result;
        }

    }

    /**
     * 快速排序
     * @param array
     * @param idx
     * @param from
     * @param to
     */
    public static void quickSort(double[] array, int[] idx, int from, int to) {
        if (from < to) {
            double temp = array[to];
            int tempIdx = idx[to];
            int i = from - 1;
            for (int j = from; j < to; j++) {
                if (array[j] <= temp) {
                    i++;
                    double tempValue = array[j];
                    array[j] = array[i];
                    array[i] = tempValue;
                    int tempIndex = idx[j];
                    idx[j] = idx[i];
                    idx[i] = tempIndex;
                }
            }
            array[to] = array[i + 1];
            array[i + 1] = temp;
            idx[to] = idx[i + 1];
            idx[i + 1] = tempIdx;
            quickSort(array, idx, from, i);
            quickSort(array, idx, i + 1, to);
        }
    }

    public static void randomPermutation(int[] perm, int size) {
        int[] index = new int[size];
        boolean[] flag = new boolean[size];

        for (int n = 0; n < size; n++) {
            index[n] = n;
            flag[n] = true;
        }

        int num = 0;
        while (num < size) {
            int start = PseudoRandom.randInt(0, size - 1);
            // int start = int(size*nd_uni(&rnd_uni_init));
            while (true) {
                if (flag[start]) {
                    perm[num] = index[start];
                    flag[start] = false;
                    num++;
                    break;
                }
                if (start == (size - 1)) {
                    start = 0;
                } else {
                    start++;
                }
            }
        }
    }

    public static double innerproduct(double[] vec1, double[] vec2) {
        double sum = 0;

        for (int i = 0; i < vec1.length; i++) {
            sum += vec1[i] * vec2[i];
        }

        return sum;
    }

    public static double norm_vector(double[] z, int numberObjectives) {
        double sum = 0;

        for (int i = 0; i < numberObjectives; i++) {
            sum += z[i] * z[i];
        }

        return Math.sqrt(sum);
    }

    public static void repairSolution(Solution solution, Problem problem_) throws JMException {
        Variable[] x = solution.getDecisionVariables();
        double a = x[0].getValue();
        double b = x[1].getValue();
        double e = x[3].getValue();
        double l = x[5].getValue();
        double rule_1 = Math.pow((a + b), 2) - Math.pow(l, 2) - Math.pow(e, 2);
        double rule_2 = Math.pow((a - b), 2) - Math.pow((l - 100), 2) - Math.pow(e, 2);

        while (rule_1 <= 0 || rule_2 >= 0) {
            try {
                Solution tempSolution = new Solution(problem_);
                x = tempSolution.getDecisionVariables();
                a = x[0].getValue();
                b = x[1].getValue();
                e = x[3].getValue();
                l = x[5].getValue();
                rule_1 = Math.pow((a + b), 2) - Math.pow(l, 2) - Math.pow(e, 2);
                rule_2 = Math.pow((a - b), 2) - Math.pow((l - 100), 2) - Math.pow(e, 2);
            } catch (ClassNotFoundException exception) {
                exception.printStackTrace();
            }
        }
        solution.setDecisionVariables(x);
    }

    /**
     * 更新储备集
     *
     * @param pop
     * @param popSize
     * @param externalArchive
     */
    public static void updateExternalArchive(SolutionSet pop, int popSize, SolutionSet externalArchive) {
        SolutionSet feasible_solutions = new SolutionSet(popSize);
        int objectiveNo = pop.get(0).getNumberOfObjectives();
        for (int i = 0; i < popSize; i++) {
            if (pop.get(i).getOverallConstraintViolation() == 0.0) {
                feasible_solutions.add(new Solution(pop.get(i)));
            }
        }

        if (feasible_solutions.size() > 0) {
            SolutionSet union = feasible_solutions.union(externalArchive);
            ENS_FirstRank ranking = new ENS_FirstRank(union);
            SolutionSet firstRankSolutions = ranking.getFirstfront();

            if (firstRankSolutions.size() <= popSize) {
                externalArchive.clear();
                for (int i = 0; i < firstRankSolutions.size(); i++) {
                    externalArchive.add(new Solution(firstRankSolutions.get(i)));
                }
            } else {
                // delete the element of the set until N <= popSize
                while (firstRankSolutions.size() > popSize) {
                    Distance.crowdingDistanceAssignment(firstRankSolutions, objectiveNo);
                    firstRankSolutions.sort(new CrowdingDistanceComparator());
                    firstRankSolutions.remove(firstRankSolutions.size() - 1);
                }

                externalArchive.clear();
                for (int i = 0; i < popSize; i++) {
                    externalArchive.add(new Solution(firstRankSolutions.get(i)));
                }
            }
        }
    }

    /**
     * 初始化储备集
     *
     * @param pop
     * @param popSize
     * @param externalArchive
     * @return
     */
    public static SolutionSet initializeExternalArchive(SolutionSet pop, int popSize, SolutionSet externalArchive) {
        SolutionSet feasible_solutions = new SolutionSet(popSize);
        for (int i = 0; i < popSize; i++) {
            if (pop.get(i).getOverallConstraintViolation() == 0.0) {
                feasible_solutions.add(new Solution(pop.get(i)));
            }
        }

        if (feasible_solutions.size() > 0) {
            // 执行非支配排序获取非支配解集
            RankingByCDP ranking = new RankingByCDP(feasible_solutions);
            externalArchive = externalArchive.union(ranking.getSubfront(0));
        }
        return externalArchive;
    }
}
