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

package opt.easyjmetal.util.comparators.line;

import opt.easyjmetal.core.Solution;
import java.util.Comparator;

/**
 * This class implements a <code>Comparator</code> (a method for comparing
 * <code>Solution</code> objects) based on a constraint violation test +
 * dominance checking, as in NSGA-II.
 */
public class GridDominanceComparator implements Comparator {

    private double[] ub_;
    private double[] lb_;
    private  double divide_;

    public GridDominanceComparator(double[] ub, double[] lb, double divide) {
        ub_ = ub;
        lb_ = lb;
        divide_ = divide;
    }

    /**
     * Compares two solutions.
     * @param object1 Object representing the first <code>Solution</code>.
     * @param object2 Object representing the second <code>Solution</code>.
     * @return -1, or 0, or 1 if solution1 dominates solution2, both are
     * non-dominated, or solution1  is dominated by solution22, respectively.
     */
    @Override
    public int compare(Object object1, Object object2) {
        if (object1==null) {
            return 1;
        } else if (object2 == null) {
            return -1;
        }

        Solution a = (Solution)object1;
        Solution b = (Solution)object2;

        double width;
        int[] flag1 = new int[1];
        int[] flag2 = new int[1];

        for (int i = 0; i < a.getNumberOfObjectives(); i++) {
            width = (ub_[i] - lb_[i]) / divide_;

            if ((int) Math.floor((a.getObjective(i) - lb_[i]) / width) < (int) Math
                    .floor((b.getObjective(i) - lb_[i]) / width)) {
                flag1[0] = 1;
            } else {
                if ((int) Math.floor((a.getObjective(i) - lb_[i])
                        / width) > (int) Math
                        .floor((b.getObjective(i) - lb_[i]) / width)) {
                    flag2[0] = 1;
                }
            }
        }
        if (flag1[0] == 1 && flag2[0] == 0) {
            return -1;
        } else {
            if (flag1[0] == 0 && flag2[0] == 1) {
                return 1;
            } else {
                if (flag1[0] == 1 && flag2[0] == 1) {
                    return 0;
                } else {
                    return 0;
                }
            }
        }
    }
}
