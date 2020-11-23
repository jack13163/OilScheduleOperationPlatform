package opt.easyjmetal.problem.cdtlz;

import opt.easyjmetal.core.Solution;
import opt.easyjmetal.problem.dtlz.DTLZ2;
import opt.easyjmetal.util.JMException;

import java.util.HashMap;
import java.util.Map;

/**
 * Problem ConvexC2-DTLZ2, defined in: Jain, H. and K. Deb. "An Evolutionary
 * Many-Objective Optimization Algorithm Using Reference-Point-Based
 * Nondominated Sorting Approach, Part II: Handling Constraints and Extending to
 * an Adaptive Approach." EEE Transactions on Evolutionary Computation,
 * 18(4):602-622, 2014.
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class ConvexC2_DTLZ2 extends DTLZ2 {

    private static Map<Integer, Double> rValue;

    static {
        rValue = new HashMap<Integer, Double>();
        rValue.put(3, 0.225);
        rValue.put(5, 0.225);
        rValue.put(8, 0.26);
        rValue.put(10, 0.26);
        rValue.put(15, 0.27);
    }

    /**
     * Constructor
     *
     * @param numberOfVariables
     * @param numberOfObjectives
     */
    public ConvexC2_DTLZ2(int numberOfVariables, int numberOfObjectives) {
        super(numberOfVariables, numberOfObjectives);

        numberOfConstraints_ = 1;
        problemName_ = "ConvexC2_DTLZ2" + "-" + numberOfObjectives;
    }

    @Override
    public void evaluate(Solution solution) throws JMException {
        super.evaluate(solution);
        this.evaluateConstraints(solution);
    }

    @Override
    public void evaluateConstraints(Solution solution) {
        double[] constraint = new double[getNumberOfConstraints()];

        double sum = 0;
        for (int i = 0; i < getNumberOfObjectives(); i++) {
            sum += solution.getObjective(i);
        }

        double lambda = sum / getNumberOfObjectives();

        sum = 0;
        for (int i = 0; i < getNumberOfObjectives(); i++) {
            sum += Math.pow(solution.getObjective(i) - lambda, 2.0);
        }

        constraint[0] = sum - Math.pow(rValue.get(getNumberOfObjectives()), 2.0);

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
