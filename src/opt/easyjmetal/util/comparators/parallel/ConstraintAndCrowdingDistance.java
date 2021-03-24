//  ConstraintDominanceComparator.java
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

package opt.easyjmetal.util.comparators.parallel;

import opt.easyjmetal.core.Solution;

import java.util.Comparator;

/**
 * 借鉴自DominanceComparator_M_Add_One。
 * 添加两个目标，并根据这两个目标进行解的支配关系比较。这两个目标分别为：
 *    1.拥挤距离；
 *    2.约束违背值。
 */
public class ConstraintAndCrowdingDistance implements Comparator {

    public ConstraintAndCrowdingDistance() {
    }

    /**
     * Compares two solutions.
     *
     * @param object1 Object representing the first <code>Solution</code>.
     * @param object2 Object representing the second <code>Solution</code>.
     * @return -1, or 0, or 1 if solution1 dominates solution2, both are
     * non-dominated, or solution1  is dominated by solution22, respectively.
     */
    @Override
    public int compare(Object object1, Object object2) {
        if (object1 == null) {
            return 1;
        } else if (object2 == null) {
            return -1;
        }

        Solution solution1 = (Solution) object1;
        Solution solution2 = (Solution) object2;

        int dominate1 = 0;
        int dominate2 = 0;

        int dominateCount;
        double[] convertedSolution1 = new double[2];
        double[] convertedSolution2 = new double[2];

        convertedSolution1[0] = Math.abs(solution1.getOverallConstraintViolation());
        convertedSolution2[0] = Math.abs(solution2.getOverallConstraintViolation());
        convertedSolution1[1] = Math.abs(solution1.getCrowdingDistance());
        convertedSolution2[1] = Math.abs(solution2.getCrowdingDistance());

        double value1, value2;
        for (int i = 0; i < 2; i++) {
            value1 = convertedSolution1[i];
            value2 = convertedSolution2[i];
            if (value1 < value2) {
                dominateCount = -1;
            } else if (value1 > value2) {
                dominateCount = 1;
            } else {
                dominateCount = 0;
            }

            if (dominateCount == -1) {
                dominate1 = 1;
            }

            if (dominateCount == 1) {
                dominate2 = 1;
            }
        }

        if (dominate1 == dominate2) {
            // No one dominate the other
            return 0;
        }
        if (dominate1 == 1) {
            // solution1 dominate
            return -1;
        }
        // solution2 dominate
        return 1;
    }
}
