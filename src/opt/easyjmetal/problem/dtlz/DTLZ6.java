package opt.easyjmetal.problem.dtlz;

import opt.easyjmetal.core.Problem;
import opt.easyjmetal.core.Solution;
import opt.easyjmetal.util.JMException;
import opt.jmetal.util.JMetalException;

/**
 * Class representing problem DTLZ6
 */
public class DTLZ6 extends Problem {

    public DTLZ6() {
        this(12, 3);
    }

    public DTLZ6(Integer numberOfVariables, Integer numberOfObjectives) throws JMetalException {
        numberOfVariables_ = numberOfVariables;
        numberOfObjectives_ = numberOfObjectives;
        problemName_ = "DTLZ6";

        lowerLimit_ = new double[numberOfVariables_];
        upperLimit_ = new double[numberOfVariables_];
        for (int i = 0; i < numberOfVariables_; i++) {
            lowerLimit_[i] = 0.0;
            upperLimit_[i] = 1.0;
        }
    }

    @Override
    public void evaluate(Solution solution) throws JMException {
        int numberOfVariables = getNumberOfVariables();
        int numberOfObjectives = getNumberOfObjectives();
        double[] theta = new double[numberOfObjectives - 1];

        double[] f = new double[numberOfObjectives];
        double[] x = new double[numberOfVariables];

        int k = getNumberOfVariables() - getNumberOfObjectives() + 1;

        for (int i = 0; i < numberOfVariables; i++) {
            x[i] = solution.getDecisionVariables()[i].getValue();
        }

        double g = 0.0;
        for (int i = numberOfVariables - k; i < numberOfVariables; i++) {
            g += Math.pow(x[i], 0.1);
        }

        double t = Math.PI / (4.0 * (1.0 + g));
        theta[0] = x[0] * Math.PI / 2;
        for (int i = 1; i < (numberOfObjectives - 1); i++) {
            theta[i] = t * (1.0 + 2.0 * g * x[i]);
        }

        for (int i = 0; i < numberOfObjectives; i++) {
            f[i] = 1.0 + g;
        }

        for (int i = 0; i < numberOfObjectives; i++) {
            for (int j = 0; j < numberOfObjectives - (i + 1); j++) {
                f[i] *= Math.cos(theta[j]);
            }
            if (i != 0) {
                int aux = numberOfObjectives - (i + 1);
                f[i] *= Math.sin(theta[aux]);
            }
        }

        for (int i = 0; i < numberOfObjectives; i++) {
            solution.setObjective(i, f[i]);
        }
    }
}
