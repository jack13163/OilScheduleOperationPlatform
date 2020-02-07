//  NCMOP1.java
//
//  Author:
//  wenji li Email: wenji_li@126.com

package opt.easyjmetal.problem.LIRCMOP;

import opt.easyjmetal.core.Problem;
import opt.easyjmetal.core.Solution;
import opt.easyjmetal.core.Variable;
import opt.easyjmetal.encodings.solutiontype.BinaryRealSolutionType;
import opt.easyjmetal.encodings.solutiontype.RealSolutionType;
import opt.easyjmetal.util.Permutation;
import opt.easyjmetal.util.JMException;

/**
 * Class representing problem LIRCMOP6
 */
public class LIRCMOP6 extends Problem {
	/**
	 * Creates an LIRCMOP6 problem (30 variables and 2 objectives)
	 *
	 * @param solutionType
	 *            The solution type must "Real" or "BinaryReal".
	 */
	public LIRCMOP6(String solutionType) throws ClassNotFoundException {
		this(solutionType, 30, 2);
	} // LIRCMOP6

	/**
	 * Creates an LIRCMOP6 problem instance
	 *
	 * @param numberOfVariables
	 *            Number of variables
	 * @param numberOfObjectives
	 *            Number of objective functions
	 * @param solutionType
	 *            The solution type must "Real" or "BinaryReal".
	 */
	public LIRCMOP6(String solutionType, Integer numberOfVariables,
                    Integer numberOfObjectives) throws ClassNotFoundException {
		numberOfVariables_ = numberOfVariables.intValue();
		numberOfObjectives_ = numberOfObjectives.intValue();
		numberOfConstraints_ = 2;
		problemName_ = "LIRCMOP6";

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

		f[0] = x[0] + 10 * sum1 + gx ;
		f[1] = 1.0 - Math.pow(x[0],2) + 10 * sum2 + gx;

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

		double r = 0.1, theta = -0.25 * Math.PI;
		double[] a_array = new double[]{2.0,2.0};
        double[] b_array = new double[]{8.0,8.0};
		double[] xOffset = new double[]{1.8,2.8};
		double[] yOffset = new double[]{1.8,2.8};
		double f1 = solution.getObjective(0);
		double f2 = solution.getObjective(1);
		double[] constraint = new double[numberOfConstraints_];
		for(int i = 0; i < xOffset.length; i++){
			constraint[i] = Math.pow(((f1 - xOffset[i]) * Math.cos(theta) - (f2 - yOffset[i]) * Math.sin(theta))/a_array[i] ,2)
					+ Math.pow(((f1 - xOffset[i]) * Math.sin(theta) + (f2 - yOffset[i]) * Math.cos(theta))/b_array[i],2)
					- r;
		}

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
