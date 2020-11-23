package opt.easyjmetal.problem.uf;

import opt.easyjmetal.core.Problem;
import opt.easyjmetal.core.Solution;
import opt.easyjmetal.util.JMException;

public class UF6 extends Problem {
    int n;
    double epsilon;

    public UF6() {
        this(30, 2, 0.1);
    }

    public UF6(Integer numberOfVariables, int N, double epsilon) {
        numberOfVariables_ = numberOfVariables;
        numberOfConstraints_ = 0;
        numberOfObjectives_ = 2;
        problemName_ = "UF6";
        n = N;
        this.epsilon = epsilon;

        lowerLimit_ = new double[numberOfVariables_];
        upperLimit_ = new double[numberOfVariables_];
        for (int i = 0; i < numberOfVariables_; i++) {
            if (i == 0) {
                lowerLimit_[i] = 0.0;
                upperLimit_[i] = 1.0;
            } else {
                lowerLimit_[i] = -1.0;
                upperLimit_[i] = 1.0;
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
        double prod1, prod2;
        double sum1, sum2, yj, hj, pj;
        sum1 = sum2 = 0.0;
        count1 = count2 = 0;
        prod1 = prod2 = 1.0;

        for (int j = 2; j <= getNumberOfVariables(); j++) {
            yj = x[j - 1] - Math.sin(6.0 * Math.PI * x[0] + j * Math.PI / getNumberOfVariables());
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
        hj = 2.0 * (0.5 / n + epsilon) * Math.sin(2.0 * n * Math.PI * x[0]);
        if (hj < 0.0) {
            hj = 0.0;
        }

        solution.setObjective(0, x[0] + hj + 2.0 * (4.0 * sum1 - 2.0 * prod1 + 2.0) / (double) count1);
        solution.setObjective(1, 1.0 - x[0] + hj + 2.0 * (4.0 * sum2 - 2.0 * prod2 + 2.0) / (double) count2);
    }
}
