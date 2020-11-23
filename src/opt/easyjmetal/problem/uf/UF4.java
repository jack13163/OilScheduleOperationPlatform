package opt.easyjmetal.problem.uf;

import opt.easyjmetal.core.Problem;
import opt.easyjmetal.core.Solution;
import opt.easyjmetal.util.JMException;

public class UF4 extends Problem {

    public UF4() {
        this(30);
    }

    public UF4(Integer numberOfVariables) {
        numberOfVariables_ = numberOfVariables;
        numberOfConstraints_ = 0;
        numberOfObjectives_ = 2;
        problemName_ = "UF4";

        lowerLimit_ = new double[numberOfVariables_];
        upperLimit_ = new double[numberOfVariables_];
        for (int i = 0; i < numberOfVariables_; i++) {
            if (i == 0) {
                lowerLimit_[i] = 0.0;
                upperLimit_[i] = 1.0;
            } else {
                lowerLimit_[i] = -2.0;
                upperLimit_[i] = 2.0;
            }
        }
    }

    @Override
    public void evaluate(Solution solution) throws JMException {
        double[] x = new double[getNumberOfVariables()];
        for (int i = 0; i < numberOfVariables_; i++) {
            x[i] = solution.getDecisionVariables()[i].getValue();
        }

        int count1, count2;
        double sum1, sum2, yj, hj;
        sum1 = sum2 = 0.0;
        count1 = count2 = 0;

        for (int j = 2; j <= getNumberOfVariables(); j++) {
            yj = x[j - 1] - Math.sin(6.0 * Math.PI * x[0] + j * Math.PI / getNumberOfVariables());
            hj = Math.abs(yj) / (1.0 + Math.exp(2.0 * Math.abs(yj)));
            if (j % 2 == 0) {
                sum2 += hj;
                count2++;
            } else {
                sum1 += hj;
                count1++;
            }
        }

        solution.setObjective(0, x[0] + 2.0 * sum1 / (double) count1);
        solution.setObjective(1, 1.0 - x[0] * x[0] + 2.0 * sum2 / (double) count2);
    }
}
