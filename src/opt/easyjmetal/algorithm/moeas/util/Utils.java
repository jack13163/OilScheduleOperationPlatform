//  Utils.java
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

package opt.easyjmetal.algorithm.moeas.util;

import opt.easyjmetal.core.Solution;
import opt.easyjmetal.core.SolutionSet;
import opt.easyjmetal.util.Distance;
import opt.easyjmetal.util.ENS_FirstRank;
import opt.easyjmetal.util.Ranking;
import opt.easyjmetal.util.comparators.CrowdingComparator;

/**
 * 帮助类
 */
public class Utils {
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
        Distance distance = new Distance();
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
                    distance.crowdingDistanceAssignment(firstRankSolutions, objectiveNo);
                    firstRankSolutions.sort(new CrowdingComparator());
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
            Ranking ranking = new Ranking(feasible_solutions);
            externalArchive = externalArchive.union(ranking.getSubfront(0));
        }
        return externalArchive;
    }
}