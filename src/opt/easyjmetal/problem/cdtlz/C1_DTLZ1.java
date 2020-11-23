package opt.easyjmetal.problem.cdtlz;

import opt.easyjmetal.core.Solution;
import opt.easyjmetal.problem.dtlz.DTLZ1;
import opt.easyjmetal.util.JMException;

/**
 * Problem C1-DTLZ1, defined in: Jain, H. and K. Deb. "An Evolutionary
 * Many-Objective Optimization Algorithm Using Reference-Point-Based
 * Nondominated Sorting Approach, Part II: Handling Constraints and Extending to
 * an Adaptive Approach." EEE Transactions on Evolutionary Computation,
 * 18(4):602-622, 2014.
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class C1_DTLZ1 extends DTLZ1 {

    public C1_DTLZ1(int numberOfVariables, int numberOfObjectives) {
        super(numberOfVariables, numberOfObjectives);

        numberOfConstraints_ = 1;
        problemName_ = "C1_DTLZ1";
    }

    @Override
    public void evaluate(Solution solution) throws JMException {
        super.evaluate(solution);
        this.evaluateConstraints(solution);
    }

    @Override
    public void evaluateConstraints(Solution solution) {
        double[] constraint = new double[this.getNumberOfConstraints()];

        double sum = 0;
        for (int i = 0; i < getNumberOfObjectives() - 2; i++) {
            sum += solution.getObjective(i) / 0.5;
        }

        constraint[0] = 1.0 - solution.getObjective(getNumberOfObjectives() - 1) - sum;

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
