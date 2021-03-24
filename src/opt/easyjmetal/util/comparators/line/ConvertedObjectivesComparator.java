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

public class ConvertedObjectivesComparator implements Comparator {

    public ConvertedObjectivesComparator() {
    }

    @Override
    public int compare(Object object1, Object object2) {
        if (object1 == null) {
            return 1;
        } else if (object2 == null) {
            return -1;
        }

        Solution solution1 = (Solution) object1;
        Solution solution2 = (Solution) object2;
        // dominate1 indicates if some objective of solution1 dominates the same objective in solution2.
        int dominate1;
        // dominate2 is the complementary of dominate1.
        int dominate2;

        dominate1 = 0;
        dominate2 = 0;

        int flag;

        // Equal number of violated constraints. Applying a dominance Test then
        double value1, value2;
        for (int i = 0; i < solution1.getNumberOfObjectives(); i++) {
            value1 = solution1.getConvertedObjective(i);
            value2 = solution2.getConvertedObjective(i);
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
            //No one dominate the other
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
