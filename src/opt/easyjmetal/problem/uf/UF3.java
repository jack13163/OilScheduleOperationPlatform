package opt.easyjmetal.problem.uf;

import opt.easyjmetal.core.Problem;
import opt.easyjmetal.core.Solution;
import opt.easyjmetal.util.JMException;

public class UF3 extends Problem {

    public UF3() {
        this(30);
    }

    public UF3(int numberOfVariables) {
        numberOfVariables_ = numberOfVariables;
        numberOfConstraints_ = 0;
        numberOfObjectives_ = 2;
        problemName_ = "UF3";

        lowerLimit_ = new double[numberOfVariables_];
        upperLimit_ = new double[numberOfVariables_];
        for (int i = 0; i < numberOfVariables_; i++) {
            lowerLimit_[i] = 0.0;
            upperLimit_[i] = 1.0;
        }
    }

    @Override
    public void evaluate(Solution solution) throws JMException {
        double[] x = new double[getNumberOfVariables()];
        for (int i = 0; i < numberOfVariables_; i++) {
            x[i] = solution.getDecisionVariables()[i].getValue();
        }

        int count1, count2;
        double sum1, sum2, prod1, prod2, yj, pj;
        sum1 = sum2 = 0.0;
        count1 = count2 = 0;
        prod1 = prod2 = 1.0;


        for (int j = 2; j <= getNumberOfVariables(); j++) {
            yj = x[j - 1] - Math.pow(x[0], 0.5 * (1.0 + 3.0 * (j - 2.0) / (getNumberOfVariables() - 2.0)));
            pj = Math.cos(20.0 * yj * Math.PI / Math.sqrt(j));
            if (j % 2 == 0) {
                sum2 += yj * yj;
                prod2 *= pj;
                count2++;
            } else {
                sum1 += yj * yj;
                prod1 *= pj;
                count1++;
            }
        }

        solution.setObjective(0, x[0] + 2.0 * (4.0 * sum1 - 2.0 * prod1 + 2.0) / (double) count1);
        solution.setObjective(1, 1.0 - Math.sqrt(x[0]) + 2.0 * (4.0 * sum2 - 2.0 * prod2 + 2.0) / (double) count2);
    }
}
