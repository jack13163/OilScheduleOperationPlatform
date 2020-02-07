package opt.jmetal.util.comparator.impl;

import opt.jmetal.solution.Solution;
import opt.jmetal.util.solutionattribute.impl.OverallConstraintViolation;
import opt.jmetal.util.comparator.ConstraintViolationComparator;

/**
 * This class implements a <code>Comparator</code> (a method for comparing
 * <code>Solution</code> objects) based on the overall constraint violation of
 * the solutions, as done in NSGA-II. Deb的三条准则【可以优化的地方】
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
@SuppressWarnings("serial")
public class OverallConstraintViolationComparator<S extends Solution<?>> implements ConstraintViolationComparator<S> {
    private OverallConstraintViolation<S> overallConstraintViolation;

    /**
     * Constructor
     */
    public OverallConstraintViolationComparator() {
        overallConstraintViolation = new OverallConstraintViolation<S>();
    }

    /**
     * Compares two solutions. If the solutions has no constraints the method return
     * 0
     *
     * @param solution1 Object representing the first <code>Solution</code>.
     * @param solution2 Object representing the second <code>Solution</code>.
     * @return -1, or 0, or 1 if o1 is less than, equal, or greater than o2,
     * respectively.
     */
    public int compare(S solution1, S solution2) {
        double violationDegreeSolution1;
        double violationDegreeSolution2;

        // 判断是否含有约束的依据
        if (overallConstraintViolation.getAttribute(solution1) == null) {
            return 0;
        }
        try {
            // violationDegree <= 0，其中，等于0代表不违背约束，小于0代表违背约束
            violationDegreeSolution1 = overallConstraintViolation.getAttribute(solution1);
            violationDegreeSolution2 = overallConstraintViolation.getAttribute(solution2);

            // 【用于约束多目标优化的Deb的三个原则】
            if ((violationDegreeSolution1 < 0) && (violationDegreeSolution2 < 0)) {
                if (violationDegreeSolution1 > violationDegreeSolution2) {
                    return -1;
                } else if (violationDegreeSolution2 > violationDegreeSolution1) {
                    return 1;
                } else {
                    return 0;
                }
            } else if ((violationDegreeSolution1 == 0) && (violationDegreeSolution2 < 0)) {
                return -1;
            } else if ((violationDegreeSolution1 < 0) && (violationDegreeSolution2 == 0)) {
                return 1;
            } else {
                return 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}
