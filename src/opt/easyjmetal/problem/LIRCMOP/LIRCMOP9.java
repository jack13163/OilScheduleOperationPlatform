//  LIRCMOP9.java
//
//  Author:
//  wenji li Email: wenji_li@126.com

package opt.easyjmetal.problem.LIRCMOP;

import opt.easyjmetal.core.Problem;
import opt.easyjmetal.core.Solution;
import opt.easyjmetal.core.Variable;
import opt.easyjmetal.encodings.solutiontype.BinaryRealSolutionType;
import opt.easyjmetal.encodings.solutiontype.RealSolutionType;
import opt.easyjmetal.util.JMException;
import opt.easyjmetal.util.Permutation;

/**
 * Class representing problem LIRCMOP9
 */
public class LIRCMOP9 extends Problem {
	/**
	 * Creates an LIRCMOP9 problem (30 variables and 2 objectives)
	 *
	 * @param solutionType
	 *            The solution type must "Real" or "BinaryReal".
	 */
	public LIRCMOP9(String solutionType) throws ClassNotFoundException {
		this(solutionType, 30, 2);
	} // LIRCMOP9

	/**
	 * Creates an LIRCMOP9 problem instance
	 *
	 * @param numberOfVariables
	 *            Number of variables
	 * @param numberOfObjectives
	 *            Number of objective functions
	 * @param solutionType
	 *            The solution type must "Real" or "BinaryReal".
	 */
	public LIRCMOP9(String solutionType, Integer numberOfVariables,
                    Integer numberOfObjectives) throws ClassNotFoundException {
		numberOfVariables_ = numberOfVariables.intValue();
		numberOfObjectives_ = numberOfObjectives.intValue();
		numberOfConstraints_ = 2;
		problemName_ = "LIRCMOP9";

		lowerLimit_ = new double[numberOfVariables_];
		upperLimit_ = new double[numberOfVariables_];
		for (int var = 0; var < numberOfVariables; var++) {
			lowerLimit_[var] = 0.0;
			upperLimit_[var] = 1.0;
		} // for

		if (solutionType.compareTo("BinaryReal") == 0)
			solutionType_ = new BinaryRealSolutionType(this);
		else if (solutionType.compareTo("Real") == 0)
			solutionType_ = new RealSolutionType(this);
		else {
			System.out.println("Error: solution type " + solutionType
					+ " invalid");
			System.exit(-1);
		}
	}

	/**
	 * Evaluates a solution
	 *
	 * @param solution
	 *            The solution to evaluate
	 * @throws JMException
	 */

	public void evaluate(Solution solution) throws JMException {

		double sum1, sum2, yj;
		int j;
		sum1 = sum2 = 0.0;
		int nx = numberOfVariables_;
		double[] x = new double[numberOfVariables_];
		double[] f = new double[numberOfObjectives_];
		Variable[] gen = solution.getDecisionVariables();

		for (int i = 0; i < numberOfVariables_; i++)
			x[i] = gen[i].getValue();

		for (j = 2; j <= nx; j++) {
			if (j % 2 == 1) {
				yj = x[j-1] - Math.sin(0.5 * j / nx * Math.PI * x[0]);
				sum1 += yj * yj;
			} else {
				yj = x[j-1] - Math.cos(0.5 * j / nx * Math.PI * x[0]);
				sum2 += yj * yj;
			}
		}

		//double gx = 2.0 - Math.exp(- Math.pow((x[1] - 0.2) / 0.004,2)) - 0.8 * Math.exp(- Math.pow((x[1] - 0.6) / 0.4,2));
		double gx = 0.7057;

		f[0] = x[0] * (10 * sum1 + 1) * (1 + gx);
		f[1] = (1.0 - Math.pow(x[0],2)) * (10 * sum2 + 1) * (1 + gx);

		for (int i = 0; i < numberOfObjectives_; i++)
			solution.setObjective(i, f[i]);
	} // evaluate

	/**
	 * Evaluates the constraint overhead of a solution
	 *
	 * @param solution
	 *            The solution
	 * @throws JMException
	 */
	public void evaluateConstraints(Solution solution) throws JMException {

		double f0 = solution.getObjective(0);
		double f1 = solution.getObjective(1);
		double N = 4.0, theta = 0.25 * Math.PI;
		double[] constraint = new double[numberOfConstraints_];
		constraint[0] = f0 * Math.sin(theta) + f1 * Math.cos(theta) - Math.sin(N * Math.PI * (f0 * Math.cos(theta) - f1 * Math.sin(theta))) - 2;
        double xOffset = 1.40, yOffset = 1.40, a = 1.5, b = 6.0, r = 0.1;
		constraint[1] = Math.pow(((f0 - xOffset) * Math.cos(-theta) - (f1 - yOffset) * Math.sin(-theta))/a ,2)
						+ Math.pow(((f0 - xOffset) * Math.sin(-theta) + (f1 - yOffset) * Math.cos(-theta))/b,2)
						- r;
		double total = 0.0;
		int number = 0;
		for(int i = 0 ; i < numberOfConstraints_; i++){
			if (constraint[i] < 0.0){
				total+=constraint[i] ;
				number++;
				solution.setConstraint(i,constraint[i]);
			}
		}

		solution.setOverallConstraintViolation(total);
		solution.setNumberOfViolatedConstraint(number);
	} // evaluateConstraints

}
