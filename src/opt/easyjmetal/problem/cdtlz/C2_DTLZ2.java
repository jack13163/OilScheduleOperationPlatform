package opt.easyjmetal.problem.cdtlz;

import opt.easyjmetal.core.Solution;
import opt.easyjmetal.problem.dtlz.DTLZ2;
import opt.easyjmetal.util.JMException;

/**
 * Problem C2-DTLZ2, defined in: Jain, H. and K. Deb. "An Evolutionary
 * Many-Objective Optimization Algorithm Using Reference-Point-Based
 * Nondominated Sorting Approach, Part II: Handling Constraints and Extending to
 * an Adaptive Approach." EEE Transactions on Evolutionary Computation,
 * 18(4):602-622, 2014.
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class C2_DTLZ2 extends DTLZ2 {
    private double rValue;

    public C2_DTLZ2(int numberOfVariables, int numberOfObjectives) {
        super(numberOfVariables, numberOfObjectives);

        numberOfConstraints_ = 1;
        problemName_ = "C2_DTLZ2";

        if (getNumberOfObjectives() == 3) {
            rValue = 0.4;
        } else {
            rValue = 0.5;
        }
    }

    @Override
    public void evaluate(Solution solution) throws JMException {
        super.evaluate(solution);
        this.evaluateConstraints(solution);
    }

    @Override
    public void evaluateConstraints(Solution solution) {
        double[] constraint = new double[getNumberOfConstraints()];

        double sum2 = 0;
        double maxSum1 = Double.MIN_VALUE;
        for (int i = 0; i < getNumberOfObjectives(); i++) {
            double sum1 = Math.pow(solution.getObjective(i) - 1.0, 2.0) - Math.pow(rValue, 2.0);
            for (int j = 0; j < getNumberOfObjectives(); j++) {
                if (i != j) {
                    sum1 += Math.pow(solution.getObjective(j), 2.0);
                }
            }

            maxSum1 = Math.max(maxSum1, sum1);

            sum2 += Math.pow((solution.getObjective(i) - 1.0 / Math.sqrt(getNumberOfObjectives())), 2.0);

        }

        sum2 -= Math.pow(rValue, 2.0);

        constraint[0] = Math.max(maxSum1, sum2);

        double overallConstraintViolation = 0.0;
        int violatedConstraints = 0;
        for (int i = 0; i < getNumberOfConstraints(); i++) {
            if (constraint[i] < 0.0) {
                overallConstraintViolation += constraint[i];
                violatedConstraints++;
            }
        }

        solution.setOverallConstraintViolation(overallConstraintViolation);
        solution.setNumberOfViolatedConstraint(violatedConstraints);
    }
}
