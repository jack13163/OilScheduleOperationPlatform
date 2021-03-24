package opt.easyjmetal.util.comparators.line;

import opt.easyjmetal.core.Solution;
import opt.easyjmetal.util.comparators.IConstraintViolationComparator;

import java.util.Comparator;

/**
 * CDP±È½ÏÆ÷
 */
public class ConstraintDominanceComparator implements Comparator {
    IConstraintViolationComparator violationConstraintComparator_;

    public ConstraintDominanceComparator() {
        violationConstraintComparator_ = new OverallConstraintViolationComparator();
    }

    public ConstraintDominanceComparator(IConstraintViolationComparator comparator) {
        violationConstraintComparator_ = comparator;
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
        if (object1 == null)
            return 1;
        else if (object2 == null)
            return -1;

        Solution solution1 = (Solution) object1;
        Solution solution2 = (Solution) object2;

        int dominate1; // dominate1 indicates if some objective of solution1
        // dominates the same objective in solution2. dominate2
        int dominate2; // is the complementary of dominate1.

        dominate1 = 0;
        dominate2 = 0;

        int flag; //stores the result of the comparison

        // Test to determine whether at least a solution violates some constraint
        if (violationConstraintComparator_.needToCompare(solution1, solution2)) {
            return violationConstraintComparator_.compare(solution1, solution2);
        }

        // Equal number of violated constraints. Applying a dominance Test then
        double value1, value2;
        for (int i = 0; i < solution1.getNumberOfObjectives(); i++) {
            value1 = solution1.getObjective(i);
            value2 = solution2.getObjective(i);
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
