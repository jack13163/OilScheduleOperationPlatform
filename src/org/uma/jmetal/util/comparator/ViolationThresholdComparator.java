//  OverallConstraintViolationComparator.java
//
//  Author:
//       Antonio J. Nebro <antonio@lcc.uma.es>
//       Juan J. Durillo <durillo@lcc.uma.es>
//
//  Copyright (c) 2011 Antonio J. Nebro, Juan J. Durillo
//
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU Lesser General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU Lesser General Public License for more details.
// 
//  You should have received a copy of the GNU Lesser General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.
package org.uma.jmetal.util.comparator;

import org.uma.jmetal.core.Solution;
import org.uma.jmetal.core.SolutionSet;

import java.util.Comparator;

// This class implements the ViolationThreshold Comparator 
public class ViolationThresholdComparator
  implements IConstraintViolationComparator {
   
    
  // threshold used for the comparations
  private double threshold_ = 0.0;
 /** 
  * Compares two solutions.
  * @param o1 Object representing the first <code>Solution</code>.
  * @param o2 Object representing the second <code>Solution</code>.
  * @return -1, or 0, or 1 if o1 is less than, equal, or greater than o2,
  * respectively.
  */
  public int compare(Object o1, Object o2) {    
    double overall1, overall2;
    overall1 = ((Solution) o1).getNumberOfViolatedConstraint() * 
                ((Solution)o1).getOverallConstraintViolation();
    overall2 = ((Solution) o2).getNumberOfViolatedConstraint() *
                ((Solution)o2).getOverallConstraintViolation();
        
    if ((overall1 < 0) && (overall2 < 0)) {
      if (overall1 > overall2){
        return -1;
      } else if (overall2 > overall1){
        return 1;
      } else {
        return 0;
      }
    } else if ((overall1 == 0) && (overall2 < 0)) {
      return -1;
    } else if ((overall1 < 0) && (overall2 == 0)) {        
      return 1;
    } else {
      return 0;        
    }
  } // compare    
  
  /**
   * Returns true if solutions s1 and/or s2 have an overall constraint
   * violation < 0
   */
  public boolean needToCompare(Solution o1, Solution o2) {
    boolean needToCompare ;
    double overall1, overall2;
    overall1 = Math.abs(o1.getNumberOfViolatedConstraint() *
                o1.getOverallConstraintViolation());
    overall2 = Math.abs(o2.getNumberOfViolatedConstraint() *
                o2.getOverallConstraintViolation());

    needToCompare = (overall1 > this.threshold_) || (overall2 > this.threshold_);
    
    return needToCompare ;
  }
  
  
  /**
   * Computes the feasibility ratio
   * Return the ratio of feasible solutions
   */
  public double feasibilityRatio(SolutionSet solutionSet) {
      double aux = 0.0;
      for (int i = 0; i < solutionSet.size(); i++) {
          if (solutionSet.get(i).getOverallConstraintViolation() < 0) {
              aux = aux+1.0;
          }
      }
      return aux / (double)solutionSet.size();
  } // feasibilityRatio
  
  /**
   * Computes the feasibility ratio
   * Return the ratio of feasible solutions
   */
  public double meanOveralViolation(SolutionSet solutionSet) {
      double aux = 0.0;
      for (int i = 0; i < solutionSet.size(); i++) {
          aux += Math.abs(solutionSet.get(i).getNumberOfViolatedConstraint() * 
                          solutionSet.get(i).getOverallConstraintViolation());
      }
      return aux / (double)solutionSet.size();
  } // meanOveralViolation
  
  
  /**
   * Updates the threshold value using the population
   */
  public void updateThreshold(SolutionSet set) {
      threshold_ = feasibilityRatio(set) * meanOveralViolation(set);
               
  } // updateThreshold

    /**
     * This class implements a <code>Comparator</code> (a method for comparing
     * <code>Solution</code> objects) based on the crowding distance, as in NSGA-II.
     */
    public static class CrowdingDistanceComparator implements Comparator {

     /**
      * Compares two solutions.
      * @param o1 Object representing the first <code>Solution</code>.
      * @param o2 Object representing the second <code>Solution</code>.
      * @return -1, or 0, or 1 if o1 is less than, equal, or greater than o2,
      * respectively.
      */
      public int compare(Object o1, Object o2) {
        if (o1==null)
          return 1;
        else if (o2 == null)
          return -1;

        double distance1 = ((Solution)o1).getCrowdingDistance();
        double distance2 = ((Solution)o2).getCrowdingDistance();
        if (distance1 >  distance2)
          return -1;
        if (distance1 < distance2)
          return 1;
        return 0;
      } // compare
    } // CrowdingDistanceComparator

    /**
     * This class implements a <code>Comparator</code> (a method for comparing
     * <code>Solution</code> objects) based on a constraint violation test +
     * dominance checking, as in NSGA-II.
     */
    public static class DominanceComparator implements Comparator {
      IConstraintViolationComparator violationConstraintComparator_ ;

      /**
       * Constructor
       */
      public DominanceComparator() {
        //violationConstraintComparator_ = new DiversityComparator();
        violationConstraintComparator_ = new OverallConstraintViolationComparator();
      }

      /**
       * Constructor
       * @param comparator
       */
      public DominanceComparator(IConstraintViolationComparator comparator) {
        violationConstraintComparator_ = comparator ;
      }

     /**
      * Compares two solutions.
      * @param object1 Object representing the first <code>Solution</code>.
      * @param object2 Object representing the second <code>Solution</code>.
      * @return -1, or 0, or 1 if solution1 dominates solution2, both are
      * non-dominated, or solution1  is dominated by solution22, respectively.
      */
      public int compare(Object object1, Object object2) {
        if (object1==null)
          return 1;
        else if (object2 == null)
          return -1;

        Solution solution1 = (Solution)object1;
        Solution solution2 = (Solution)object2;

        int dominate1 ; // dominate1 indicates if some objective of solution1
                        // dominates the same objective in solution2. dominate2
        int dominate2 ; // is the complementary of dominate1.

        dominate1 = 0 ;
        dominate2 = 0 ;

        int flag; //stores the result of the comparison

        // Test to determine whether at least a solution violates some constraint
        if (violationConstraintComparator_.needToCompare(solution1, solution2))
          return violationConstraintComparator_.compare(solution1, solution2) ;
        /*
        if (solution1.getOverallConstraintViolation()!=
            solution2.getOverallConstraintViolation() &&
           (solution1.getOverallConstraintViolation() < 0) ||
           (solution2.getOverallConstraintViolation() < 0)){
          return (overallConstraintViolationComparator_.compare(solution1,solution2));
        }
       */

        // Equal number of violated constraints. Applying a dominance Test then
        double value1, value2;
        for (int i = 0; i < solution1.getNumberOfObjectives(); i++) {
          value1 = solution1.getObjective(i);
          value2 = solution2.getObjective(i);
          if (value1 < value2) {
            flag = -1;
          } else if (value1 > value2) {
            flag = 1;
          } else {
            flag = 0;
          }

          if (flag == -1) {
            dominate1 = 1;
          }

          if (flag == 1) {
            dominate2 = 1;
          }
        }

        if (dominate1 == dominate2) {
          return 0; //No one dominate the other
        }
        if (dominate1 == 1) {
          return -1; // solution1 dominate
        }
        return 1;    // solution2 dominate
      } // compare
    } // DominanceComparator

    /**
     * This class implements a <code>Comparator</code> (a method for comparing
     * <code>Solution</code> objects) based on the fitness value returned by the
     * method <code>getFitness</code>.
     */
    public static class FitnessComparator implements Comparator {

     /**
      * Compares two solutions.
      * @param o1 Object representing the first <code>Solution</code>.
      * @param o2 Object representing the second <code>Solution</code>.
      * @return -1, or 0, or 1 if o1 is less than, equal, or greater than o2,
      * respectively.
      */
      public int compare(Object o1, Object o2) {
        if (o1==null)
          return 1;
        else if (o2 == null)
          return -1;

        double fitness1 = ((Solution)o1).getFitness();
        double fitness2 = ((Solution)o2).getFitness();
        if (fitness1 <  fitness2) {
          return -1;
        }

        if (fitness1 >  fitness2) {
          return 1;
        }

        return 0;
      } // compare
    } // FitnessComparator

    /**
     * This class implements a <code>Comparator</code> (a method for comparing
     * <code>Solution</code> objects) based on a objective values.
     */
    public static class ObjectiveComparator implements Comparator {

      /**
       * Stores the index of the objective to compare
       */
      private int nObj;
      private boolean ascendingOrder_;

      /**
       * Constructor.
       *
       * @param nObj The index of the objective to compare
       */
      public ObjectiveComparator(int nObj) {
        this.nObj = nObj;
        ascendingOrder_ = true;
      } // ObjectiveComparator

      public ObjectiveComparator(int nObj, boolean descendingOrder) {
        this.nObj = nObj;
        ascendingOrder_ = !descendingOrder;
      } // ObjectiveComparator

      /**
       * Compares two solutions.
       *
       * @param o1 Object representing the first <code>Solution</code>.
       * @param o2 Object representing the second <code>Solution</code>.
       * @return -1, or 0, or 1 if o1 is less than, equal, or greater than o2,
       *         respectively.
       */
      public int compare(Object o1, Object o2) {
        if (o1 == null)
          return 1;
        else if (o2 == null)
          return -1;

        double objetive1 = ((Solution) o1).getObjective(this.nObj);
        double objetive2 = ((Solution) o2).getObjective(this.nObj);
        if (ascendingOrder_) {
          if (objetive1 < objetive2) {
            return -1;
          } else if (objetive1 > objetive2) {
            return 1;
          } else {
            return 0;
          }
        } else {
          if (objetive1 < objetive2) {
            return 1;
          } else if (objetive1 > objetive2) {
            return -1;
          } else {
            return 0;
          }
        }
      } // compare
    } // ObjectiveComparator
} // ViolationThresholdComparator