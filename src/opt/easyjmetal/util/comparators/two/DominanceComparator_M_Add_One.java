//  DominanceComparator.java
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

package opt.easyjmetal.util.comparators.two;

import opt.easyjmetal.core.Solution;

import java.util.Comparator;

/**
 * 目标值+约束值
 */
public class DominanceComparator_M_Add_One implements Comparator {

    public DominanceComparator_M_Add_One() {
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

        int dominate1; // dominate1 indicates if some objective of solution1
        // dominates the same objective in solution2. dominate2
        int dominate2; // is the complementary of dominate1.

        dominate1 = 0;
        dominate2 = 0;

        int flag; //stores the result of the comparison

        int m = solution1.getNumberOfObjectives();

        double[] convertedSolution1 = new double[m + 1];
        double[] convertedSolution2 = new double[m + 1];

        for (int i = 0; i < m; i++) {
            convertedSolution1[i] = solution1.getObjective(i);
            convertedSolution2[i] = solution2.getObjective(i);
        }
        convertedSolution1[m] = Math.abs(solution1.getOverallConstraintViolation());
        convertedSolution2[m] = Math.abs(solution2.getOverallConstraintViolation());

        double value1, value2;
        for (int i = 0; i < m + 1; i++) {
            value1 = convertedSolution1[i];
            value2 = convertedSolution2[i];
            if (value1 < value2) {
                flag = -1;
            } else if (value1 > value2) {
                flag = 1;
            } else {
                flag = 0;
            }

            if (flag == -1) {
                dominate1 = 1;
            }

            if (flag == 1) {
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
