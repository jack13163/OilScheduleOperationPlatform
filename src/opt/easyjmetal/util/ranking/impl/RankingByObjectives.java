//  RankingByCDP.java
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

package opt.easyjmetal.util.ranking.impl;

import opt.easyjmetal.core.Solution;
import opt.easyjmetal.core.SolutionSet;
import opt.easyjmetal.util.comparators.line.ObjectiveComparator;
import opt.easyjmetal.util.permutation.RandomGenerator;
import opt.easyjmetal.util.ranking.AbstractRanking;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 随机分层排序算法
 * Jan, Muhammad Asif; Khanum, Rashida Adeeb (2013).
 * A study of two penalty-parameterless constraint handling techniques in the framework of MOEA/D.
 * Applied Soft Computing, 13(1), 128–148. doi:10.1016/j.asoc.2012.07.027
 */
public class RankingByObjectives extends AbstractRanking {

    private final List<Comparator> comparatorList = new ArrayList<>();
    private int numberOfObjectives_;
    private int numberToSelect_;

    public RankingByObjectives(SolutionSet solutionSet,
                               int numberOfObjectives,
                               int numberToSelect) {
        super(solutionSet);
        this.numberOfObjectives_ = numberOfObjectives;
        this.numberToSelect_ = numberToSelect;
        for (int i = 0; i < numberOfObjectives; i++) {
            comparatorList.add(new ObjectiveComparator(i));
        }
        ranking();
    }

    /**
     * 按照各个目标随机排序
     *
     * @return
     */
    @Override
    public void ranking() {
        for (int i = 0; i < Math.ceil(1.0 * numberToSelect_ / 2); i++) {
            for (int j = 0; j < numberToSelect_ - 1; j++) {
                // 随机从比较器中选择一个
                int indexOfComparator = RandomGenerator.generateRandomInteger(0, this.numberOfObjectives_ - 1, Math.random());
                Solution solution1 = solutionSet_.get(j);
                Solution solution2 = solutionSet_.get(j + 1);
                Comparator comparator = this.comparatorList.get(indexOfComparator);

                int res = comparator.compare(solution1, solution2);
                // 比较前后两个解的优劣
                if (res < 0) {
                    solutionSet_.swap(j, j + 1);
                }
            }
        }
        for (int i = 0; i < numberToSelect_; i++) {
            Solution copySolution = new Solution(solutionSet_.get(i));
            result.add(copySolution);
        }
    }
}
