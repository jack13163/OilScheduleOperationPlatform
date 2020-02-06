//  LIRCMOP14.java
// Author: Wenji Li
// Email: wenji_li@126.com


package org.uma.jmetal.problem.LIRCMOP;

import org.uma.jmetal.core.Problem;
import org.uma.jmetal.core.Solution;
import org.uma.jmetal.core.Variable;
import org.uma.jmetal.encodings.solutiontype.BinaryRealSolutionType;
import org.uma.jmetal.encodings.solutiontype.RealSolutionType;
import org.uma.jmetal.util.JMException;

/**
 * Class representing problem LIRCMOP14
 */
public class LIRCMOP14 extends Problem{

 /**
  * Creates a default LIRCMOP14 problem (12 variables and 3 objectives)
  * @param solutionType The solution type must "Real" or "BinaryReal".
  */
  public LIRCMOP14(String solutionType) throws ClassNotFoundException {
    this(solutionType, 30, 3);
  } // LIRCMOP14

 /**
  * Creates a new instance of LIRCMOP14
  * @param numberOfVariables Number of variables
  * @param numberOfObjectives Number of objective functions
  * @param solutionType The solution type must "Real" or "BinaryReal".
  */
  public LIRCMOP14(String  solutionType,
                   Integer numberOfVariables,
                   Integer numberOfObjectives) {
    numberOfVariables_  = numberOfVariables;
    numberOfObjectives_ = numberOfObjectives;
    numberOfConstraints_= 3;
    problemName_        = "LIRCMOP14";
        
    upperLimit_ = new double[numberOfVariables_];
    lowerLimit_ = new double[numberOfVariables_];        
    for (int var = 0; var < numberOfVariables_; var++){
      lowerLimit_[var] = 0.0;
      upperLimit_[var] = 1.0;
    } //for
        
    if (solutionType.compareTo("BinaryReal") == 0)
    	solutionType_ = new BinaryRealSolutionType(this) ;
    else if (solutionType.compareTo("Real") == 0)
    	solutionType_ = new RealSolutionType(this) ;
    else {
    	System.out.println("Error: solution type " + solutionType + " invalid") ;
    	System.exit(-1) ;
    }            
  } //LIRCMOP14
        
  /** 
  * Evaluates a solution 
  * @param solution The solution to evaluate
   * @throws JMException 
  */    
  public void evaluate(Solution solution) throws JMException {
    Variable[] gen  = solution.getDecisionVariables();

    double [] x = new double[numberOfVariables_];
    double [] f = new double[numberOfObjectives_];

    for (int i = 0; i < numberOfVariables_; i++)
      x[i] = gen[i].getValue();

    double g = 0.0;
    for (int i = numberOfObjectives_ - 1; i < numberOfVariables_; i++)
      g += 10 * (x[i] - 0.5)*(x[i] - 0.5);

    for (int i = 0; i < numberOfObjectives_; i++)
      f[i] = 1.7057 + g;
        
    for (int i = 0; i < numberOfObjectives_; i++){
      for (int j = 0; j < numberOfObjectives_ - (i + 1); j++)            
        f[i] *= Math.cos(x[j]*0.5*Math.PI);                
        if (i != 0){
          int aux = numberOfObjectives_ - (i + 1);
          f[i] *= Math.sin(x[aux]*0.5*Math.PI);
        } //if 
    } // for
        
    for (int i = 0; i < numberOfObjectives_; i++)
      solution.setObjective(i,f[i]);        
  }

    public void evaluateConstraints(Solution solution) throws JMException {

        double[] constraint = new double[numberOfConstraints_];

        double f = 0;
        for(int i = 0; i < numberOfObjectives_; i++){
            f += Math.pow(solution.getObjective(i),2);
        }
        constraint[0] = (f - 3 * 3)*(f - 2 * 2);
        constraint[1] = (f - 1.9 * 1.9)* (f - 1.8 * 1.8);
        constraint[2] = (f - 1.75 * 1.75)* (f - 1.6 * 1.6);

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
} //evaluate
