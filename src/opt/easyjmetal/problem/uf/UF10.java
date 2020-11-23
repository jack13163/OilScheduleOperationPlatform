package opt.easyjmetal.problem.uf;

import opt.easyjmetal.core.Problem;
import opt.easyjmetal.core.Solution;
import opt.easyjmetal.util.JMException;

public class UF10 extends Problem {

    public UF10() {
        this(30);
    }

    public UF10(int numberOfVariables) {
        numberOfVariables_ = numberOfVariables;
        numberOfConstraints_ = 0;
        numberOfObjectives_ = 3;
        problemName_ = "UF10";

        lowerLimit_ = new double[numberOfVariables_];
        upperLimit_ = new double[numberOfVariables_];
        for (int i = 0; i < numberOfVariables_; i++) {
            if (i <= 1) {
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

        int count1, count2, count3;
        double sum1, sum2, sum3, yj, hj;
        sum1 = sum2 = sum3 = 0.0;
        count1 = count2 = count3 = 0;

        for (int j = 3; j <= getNumberOfVariables(); j++) {
            yj = x[j - 1] - 2.0 * x[1] * Math.sin(2.0 * Math.PI * x[0] + j * Math.PI / getNumberOfVariables());
            hj = 4.0 * yj * yj - Math.cos(8.0 * Math.PI * yj) + 1.0;
            if (j % 3 == 1) {
                sum1 += hj;
                count1++;
            } else if (j % 3 == 2) {
                sum2 += hj;
                count2++;
            } else {
                sum3 += hj;
                count3++;
            }
        }

        solution.setObjective(0, Math.cos(0.5 * Math.PI * x[0]) * Math.cos(0.5 * Math.PI * x[1]) + 2.0 * sum1 / (double) count1);
        solution.setObjective(1, Math.cos(0.5 * Math.PI * x[0]) * Math.sin(0.5 * Math.PI * x[1]) + 2.0 * sum2 / (double) count2);
        solution.setObjective(2, Math.sin(0.5 * Math.PI * x[0]) + 2.0 * sum3 / (double) count3);
    }
}

