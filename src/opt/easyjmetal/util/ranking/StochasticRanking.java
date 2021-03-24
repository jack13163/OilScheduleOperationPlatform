//  Ranking.java
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

package opt.easyjmetal.util.ranking;

import opt.easyjmetal.core.Solution;
import opt.easyjmetal.core.SolutionSet;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 随机分层排序算法
 * Jan, Muhammad Asif; Khanum, Rashida Adeeb (2013).
 * A study of two penalty-parameterless constraint handling techniques in the framework of MOEA/D.
 * Applied Soft Computing, 13(1), 128–148. doi:10.1016/j.asoc.2012.07.027
 */
public class StochasticRanking {

    private final List<Comparator> comparatorList = new ArrayList<>();

    public StochasticRanking(Comparator... comparators) throws Exception {
        if (comparators.length < 1) {
            throw new Exception("请至少输入一种比较器");
        }
        for (int i = 0; i < comparators.length; i++) {
            comparatorList.add(comparators[i]);
        }
    }

    /**
     * Stochastic ranking
     *
     * @param numberOfSolution
     * @return
     */
    public SolutionSet ranking(SolutionSet solutionSet, int numberOfSolution) throws Exception {
        SolutionSet result = new SolutionSet();
        for (int i = 0; i < Math.ceil(1.0 * numberOfSolution / 2); i++) {
            for (int j = 0; j < numberOfSolution - 1; j++) {
                // 随机从比较器中选择一个
                int indexOfComparator = generateRandomInteger(0, comparatorList.size() - 1, Math.random());
                Solution solution1 = solutionSet.get(j);
                Solution solution2 = solutionSet.get(j + 1);
                Comparator comparator = this.comparatorList.get(indexOfComparator);

                int res = comparator.compare(solution1, solution2);
                // 比较前后两个解的优劣
                if (res < 0) {
                    solutionSet.swap(j, j + 1);
                }
            }
        }
        for (int i = 0; i < numberOfSolution; i++) {
            Solution copySolution = new Solution(solutionSet.get(i));
            result.add(copySolution);
        }
        return result;
    }

    /**
     * 生成一个指定范围内的整数，输入为0~1之间的数
     *
     * @param min
     * @param max
     * @param feasibleRate
     * @return
     */
    private int generateRandomInteger(int min, int max, double feasibleRate) {
        if (feasibleRate == 1) {
            return max;
        }
        return (int) (feasibleRate * (max - min + 1) + min);
    }
}
