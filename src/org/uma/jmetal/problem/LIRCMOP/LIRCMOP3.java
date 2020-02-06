//  LIRCMOP3.java
//
//  Author:
//  wenji li Email: wenji_li@126.com

package org.uma.jmetal.problem.LIRCMOP;

import org.uma.jmetal.core.Problem;
import org.uma.jmetal.core.Solution;
import org.uma.jmetal.core.Variable;
import org.uma.jmetal.encodings.solutiontype.BinaryRealSolutionType;
import org.uma.jmetal.encodings.solutiontype.RealSolutionType;
import org.uma.jmetal.util.JMException;

/**
 * Class representing problem LIRCMOP3
 */
public class LIRCMOP3 extends Problem {
	/**
	 * Creates an LIRCMOP3 problem (30 variables and 2 objectives)
	 *
	 * @param solutionType
	 *            The solution type must "Real" or "BinaryReal".
	 */
	public LIRCMOP3(String solutionType) throws ClassNotFoundException {
		this(solutionType, 30, 2);
	} // LIRCMOP3

	/**
	 * Creates an LIRCMOP3 problem instance
	 *
	 * @param numberOfVariables
	 *            Number of variables
	 * @param numberOfObjectives
	 *            Number of objective functions
	 * @param solutionType
	 *            The solution type must "Real" or "BinaryReal".
	 */
	public LIRCMOP3(String solutionType, Integer numberOfVariables,
                    Integer numberOfObjectives) throws ClassNotFoundException {
		numberOfVariables_ = numberOfVariables.intValue();
		numberOfObjectives_ = numberOfObjectives.intValue();
		numberOfConstraints_ = 3;
		problemName_ = "LIRCMOP3";

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
				yj = x[j-1] - Math.sin(0.5 * Math.PI * x[0]);
				sum1 += yj * yj;
			} else {
				yj = x[j-1] - Math.cos(0.5 * Math.PI * x[0]);
				sum2 += yj * yj;
			}
		}
		f[0] = x[0] + sum1 ;
		f[1] = 1.0 - Math.pow(x[0], 2) + sum2;

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

        double[] constraint = new double[3];
		double g_1, g_2, yj;
		int j;
		g_1 = g_2 = 0.0;
		int nx = numberOfVariables_;
		double[] x = new double[numberOfVariables_];
		Variable[] gen = solution.getDecisionVariables();

		for (int i = 0; i < numberOfVariables_; i++)
			x[i] = gen[i].getValue();

		for (j = 2; j <= nx; j++) {
			if (j % 2 == 1) {
				yj = x[j-1] - Math.sin(0.5 * Math.PI * x[0]);
				g_1 += yj * yj;
			} else {
				yj = x[j-1] - Math.cos(0.5 * Math.PI * x[0]);
				g_2 += yj * yj;
			}
		}

		constraint[0] = (0.51 - g_1) * (g_1 - 0.5);
		constraint[1] = (0.51 - g_2) * (g_2 - 0.5);
		constraint[2] = Math.sin(20 * Math.PI * x[0]) - 0.5;

		double total = 0.0;
		int number = 0;
		for(int i = 0; i < constraint.length; i++){
			if (constraint[i] < 0.0) {
				total += constraint[i];
				number++;
				solution.setConstraint(i,constraint[i]);
			}
		}

		solution.setOverallConstraintViolation(total);
		solution.setNumberOfViolatedConstraint(number);
	} // evaluateConstraints

}
