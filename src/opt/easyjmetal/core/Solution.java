//  Solution.java
//
//  Author:
//       Antonio J. Nebro <antonio@lcc.uma.es>
//       Juan J. Durillo <durillo@lcc.uma.es>
//
//  Description:
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

package opt.easyjmetal.core;


import opt.easyjmetal.encodings.solutiontype.variable.Binary;
import opt.easyjmetal.encodings.solutiontype.variable.BinaryReal;

import java.io.Serializable;

/**
 * Class representing a solution for a problem.
 */
public class Solution implements Serializable {

    private Problem problem_;
    private SolutionType type_;
    private Variable[] variable_;
    private final double[] objective_;
    private final double[] convertedObjective_;
    private final double[] constraint_;
    private int numberOfObjectives_;
    private int numberOfConstraints_;
    private double fitness_;
    private double contribution_;

    /**
     * Used in algorithm AbYSS, this field is intended to be used to know when a
     * <code>Solution</code> is marked.
     */
    private boolean marked_;
    private int rank_;
    private double overallConstraintViolation_;
    private double normalizedConstraintViolation_;
    private int numberOfViolatedConstraints_;

    /**
     * This field is intended to be used to know the location of a solution into
     * a <code>SolutionSet</code>. Used in MOCell
     */
    private int location_;
    private double diversity_;
    private double associateDist_;

    /**
     * This field is intended to be used to know the region of a solution
     * <code>SolutionSet</code>. Used in MST
     */
    private int region_;

    /**
     * Stores the distance to his k-nearest neighbor into a
     * <code>SolutionSet</code>. Used in SPEA2.
     */
    private double kDistance_;

    /**
     * Stores the crowding distance of the the solution in a
     * <code>SolutionSet</code>. Used in NSGA-II.
     */
    private double crowdingDistance_;

    /**
     * Stores the distance between this solution and a <code>SolutionSet</code>.
     * Used in AbySS.
     */
    private double distanceToSolutionSet_;

    /**
     * Constructor.
     */
    public Solution() {
        problem_ = null;
        marked_ = false;
        overallConstraintViolation_ = 0.0;
        normalizedConstraintViolation_ = 0.0;
        numberOfViolatedConstraints_ = 0;
        type_ = null;
        variable_ = null;
        objective_ = null;
        constraint_ = null;

        convertedObjective_ = null;
    }

    /**
     * Constructor
     *
     * @param numberOfObjectives Number of objectives of the solution
     *                           <p>
     *                           This constructor is used mainly to read objective values from
     *                           a file to variables of a SolutionSet to apply quality
     *                           indicators
     */
    public Solution(int numberOfObjectives) {
        numberOfObjectives_ = numberOfObjectives;
        objective_ = new double[numberOfObjectives];
        constraint_ = null;
        convertedObjective_ = new double[numberOfObjectives];
    }

    public Solution(int numberOfObjectives, int numberOfConstraints) {
        numberOfObjectives_ = numberOfObjectives;
        numberOfConstraints_ = numberOfConstraints;
        objective_ = new double[numberOfObjectives];
        constraint_ = new double[numberOfConstraints];
        convertedObjective_ = new double[numberOfObjectives];
    }


    /**
     * Constructor.
     *
     * @param problem The problem to solve
     * @throws ClassNotFoundException
     */
    public Solution(Problem problem) throws ClassNotFoundException {
        problem_ = problem;
        type_ = problem.getSolutionType();
        numberOfObjectives_ = problem.getNumberOfObjectives();
        numberOfConstraints_ = problem.getNumberOfConstraints();
        objective_ = new double[numberOfObjectives_];
        constraint_ = new double[numberOfConstraints_];
        convertedObjective_ = new double[numberOfObjectives_];

        fitness_ = 0.0;
        contribution_ = 0.0;
        kDistance_ = 0.0;
        crowdingDistance_ = 0.0;
        distanceToSolutionSet_ = Double.POSITIVE_INFINITY;

        // variable_ = problem.solutionType_.createVariables() ;
        variable_ = type_.createVariables();
    }

    static public Solution getNewSolution(Problem problem)
            throws ClassNotFoundException {
        return new Solution(problem);
    }

    /**
     * Constructor
     *
     * @param problem The problem to solve
     */
    public Solution(Problem problem, Variable[] variables) {
        problem_ = problem;
        type_ = problem.getSolutionType();
        numberOfObjectives_ = problem.getNumberOfObjectives();
        objective_ = new double[numberOfObjectives_];
        convertedObjective_ = new double[numberOfObjectives_];

        numberOfConstraints_ = problem.getNumberOfConstraints();
        constraint_ = new double[numberOfConstraints_];

        fitness_ = 0.0;
        contribution_ = 0.0;
        kDistance_ = 0.0;
        crowdingDistance_ = 0.0;
        distanceToSolutionSet_ = Double.POSITIVE_INFINITY;
        variable_ = variables;
    }

    /**
     * ���Ƶ�ǰ��
     *
     * @param solution Solution to copy.
     */
    public Solution(Solution solution) {
        problem_ = solution.problem_;
        type_ = solution.type_;

        numberOfObjectives_ = solution.getNumberOfObjectives();
        objective_ = new double[numberOfObjectives_];
        for (int i = 0; i < objective_.length; i++) {
            objective_[i] = solution.getObjective(i);
        }

        convertedObjective_ = new double[numberOfObjectives_];
        for (int i = 0; i < convertedObjective_.length; i++) {
            convertedObjective_[i] = solution.getConvertedObjective(i);
        }

        numberOfConstraints_ = solution.getNumberOfConstraints();
        constraint_ = new double[numberOfConstraints_];

        for (int i = 0; i < constraint_.length; i++) {
            constraint_[i] = solution.getConstraint(i);
        }

        variable_ = type_.copyVariables(solution.variable_);
        overallConstraintViolation_ = solution.getOverallConstraintViolation();
        normalizedConstraintViolation_ = solution.getNormalizedConstraintViolation();
        numberOfViolatedConstraints_ = solution.getNumberOfViolatedConstraint();
        distanceToSolutionSet_ = solution.getDistanceToSolutionSet();
        crowdingDistance_ = solution.getCrowdingDistance();
        kDistance_ = solution.getKDistance();
        fitness_ = solution.getFitness();
        contribution_ = solution.getContribution();
        marked_ = solution.isMarked();
        rank_ = solution.getRank();
        location_ = solution.getLocation();
    }

    public void setDistanceToSolutionSet(double distance) {
        distanceToSolutionSet_ = distance;
    }

    public double getDistanceToSolutionSet() {
        return distanceToSolutionSet_;
    } // getDistanceToSolutionSet

    public void setKDistance(double distance) {
        kDistance_ = distance;
    } // setKDistance

    public double getKDistance() {
        return kDistance_;
    } // getKDistance

    public void setCrowdingDistance(double distance) {
        crowdingDistance_ = distance;
    } // setCrowdingDistance

    public double getCrowdingDistance() {
        return crowdingDistance_;
    }

    public void setFitness(double fitness) {
        fitness_ = fitness;
    }

    public double getFitness() {
        return fitness_;
    }

    public double getContribution() {
        return contribution_;
    }

    public void setContribution(double contribution) {
        this.contribution_ = contribution;
    }

    /**
     * Sets the value of the i-th objective.
     *
     * @param i     The number identifying the objective.
     * @param value The value to be stored.
     */
    public void setObjective(int i, double value) {
        objective_[i] = value;
    } // setObjective

    public void setConvertedObjective(int i, double value) {
        convertedObjective_[i] = value;
    }


    public void setConstraint(int i, double value) {
        constraint_[i] = value;
    }

    /**
     * Returns the value of the i-th objective.
     *
     * @param i The value of the objective.
     */
    public double getObjective(int i) {
        return objective_[i];
    }

    public double getConvertedObjective(int i) {
        return convertedObjective_[i];
    }

    public double getConstraint(int i) {
        return constraint_[i];
    }

    /**
     * Returns the number of objectives.
     *
     * @return The number of objectives.
     */
    public int getNumberOfObjectives() {
        if (objective_ == null) {
            return 0;
        } else {
            return numberOfObjectives_;
        }
    }

    public int getNumberOfConstraints() {
        if (constraint_ == null) {
            return 0;
        } else {
            return numberOfConstraints_;
        }
    }

    /**
     * Returns the number of decision variables of the solution.
     *
     * @return The number of decision variables.
     */
    public int numberOfVariables() {
        return problem_.getNumberOfVariables();
    } // numberOfVariables

    /**
     * Returns a string representing the solution.
     *
     * @return The string.
     */
    @Override
    public String toString() {
        String aux = "";
        for (int i = 0; i < this.numberOfObjectives_; i++) {
            if (i < this.numberOfObjectives_ - 1) {
                aux = aux + this.getObjective(i) + " ";
            } else {
                aux = aux + this.getObjective(i);
            }
        }

        return aux;
    }

    /**
     * Returns the decision variables of the solution.
     *
     * @return the <code>DecisionVariables</code> object representing the
     * decision variables of the solution.
     */
    public Variable[] getDecisionVariables() {
        return variable_;
    } // getDecisionVariables

    /**
     * Sets the decision variables for the solution.
     *
     * @param variables The <code>DecisionVariables</code> object representing the
     *                  decision variables of the solution.
     */
    public void setDecisionVariables(Variable[] variables) {
        variable_ = variables;
    }

    public Problem getProblem() {
        return problem_;
    }

    /**
     * Indicates if the solution is marked.
     *
     * @return true if the method <code>marked</code> has been called and, after
     * that, the method <code>unmarked</code> hasn't been called. False
     * in other case.
     */
    public boolean isMarked() {
        return this.marked_;
    } // isMarked

    /**
     * Establishes the solution as marked.
     */
    public void marked() {
        this.marked_ = true;
    } // marked

    /**
     * Established the solution as unmarked.
     */
    public void unMarked() {
        this.marked_ = false;
    } // unMarked

    /**
     * Sets the rank of a solution.
     *
     * @param value The rank of the solution.
     */
    public void setRank(int value) {
        this.rank_ = value;
    } // setRank

    /**
     * Gets the rank of the solution. <b> REQUIRE </b>: This method has to be
     * invoked after calling <code>setRank()</code>.
     *
     * @return the rank of the solution.
     */
    public int getRank() {
        return this.rank_;
    } // getRank

    /**
     * Sets the overall constraints violated by the solution.
     *
     * @param value The overall constraints violated by the solution.
     */
    public void setOverallConstraintViolation(double value) {
        this.overallConstraintViolation_ = value;
    } // setOverallConstraintViolation

    public void setNormalizedConstraintViolation(double value) {
        this.normalizedConstraintViolation_ = value;
    } // setOverallConstraintViolation

    /**
     * Gets the overall constraint violated by the solution. <b> REQUIRE </b>:
     * This method has to be invoked after calling
     * <code>overallConstraintViolation</code>.
     *
     * @return the overall constraint violation by the solution.
     */
    public double getOverallConstraintViolation() {
        return this.overallConstraintViolation_;
    } // getOverallConstraintViolation

    public double getNormalizedConstraintViolation() {
        return this.normalizedConstraintViolation_;
    }

    /**
     * Sets the number of constraints violated by the solution.
     *
     * @param value The number of constraints violated by the solution.
     */
    public void setNumberOfViolatedConstraint(int value) {
        this.numberOfViolatedConstraints_ = value;
    } // setNumberOfViolatedConstraint

    /**
     * Gets the number of constraint violated by the solution. <b> REQUIRE </b>:
     * This method has to be invoked after calling
     * <code>setNumberOfViolatedConstraint</code>.
     *
     * @return the number of constraints violated by the solution.
     */
    public int getNumberOfViolatedConstraint() {
        return this.numberOfViolatedConstraints_;
    } // getNumberOfViolatedConstraint

    /**
     * Sets the location of the solution into a solutionSet.
     *
     * @param location The location of the solution.
     */
    public void setLocation(int location) {
        this.location_ = location;
    } // setLocation

    /**
     * Gets the location of this solution in a <code>SolutionSet</code>. <b>
     * REQUIRE </b>: This method has to be invoked after calling
     * <code>setLocation</code>.
     *
     * @return the location of the solution into a solutionSet
     */
    public int getLocation() {
        return this.location_;
    } // getLocation

    /**
     * Sets the type of the encodings.variable.
     *
     * @param type
     *            The type of the encodings.variable.
     */
    // public void setType(String type) {
    // type_ = Class.forName("") ;
    // } // setType

    /**
     * Sets the type of the encodings.variable.
     *
     * @param type The type of the encodings.variable.
     */
    public void setType(SolutionType type) {
        type_ = type;
    } // setType

    /**
     * Gets the type of the encodings.variable
     *
     * @return the type of the encodings.variable
     */
    public SolutionType getType() {
        return type_;
    } // getType

    /**
     * Returns the aggregative value of the solution
     *
     * @return The aggregative value.
     */
    public double getAggregativeValue() {
        double value = 0.0;
        for (int i = 0; i < getNumberOfObjectives(); i++) {
            value += getObjective(i);
        }
        return value;
    } // getAggregativeValue

    /**
     * Returns the number of bits of the chromosome in case of using a binary
     * representation
     *
     * @return The number of bits if the case of binary variables, 0 otherwise
     * This method had a bug which was fixed by Rafael Olaechea
     */
    public int getNumberOfBits() {
        int bits = 0;

        for (int i = 0; i < variable_.length; i++) {
            if ((variable_[i].getVariableType() == Binary.class)
                    || (variable_[i].getVariableType() == BinaryReal.class)) {
                bits += ((Binary) (variable_[i])).getNumberOfBits();
            }
        }

        return bits;
    } // getNumberOfBits

    public void Set_location(int i) {
        this.location_ = i;
    }

    public int read_location() {
        return this.location_;
    }

    public void Set_diversity(double i) {
        this.diversity_ = i;
    }

    public double read_diversity() {
        return this.diversity_;
    }

    public void setRegion(int i) {
        this.region_ = i;
    }

    public int readRegion() {
        return this.region_;
    }

    public void Set_associateDist(double distance) {
        this.associateDist_ = distance;
    }

    public double read_associateDist() {
        return this.associateDist_;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o.getClass() == Solution.class) {
            Solution n = (Solution) o;
            Boolean flag = true;
            for (int i = 0; i < n.getNumberOfObjectives(); i++) {
                if (n.getObjective(i) != this.getObjective(i)) {
                    flag = false;
                }
            }
            if (n.getOverallConstraintViolation() != this.getOverallConstraintViolation()) {
                flag = false;
            }
            return flag;
        }
        return false;

    }

    public int isDominated(Solution solution) {

        int less_than = 0;
        int more_than = 0;

        for (int i = 0; i < numberOfObjectives_; i++) {
            if (solution.getObjective(i) < this.getObjective(i)) {
                less_than += 1;
            } else if (solution.getObjective(i) > this.getObjective(i)) {
                more_than += 1;
            }
        }

        if (more_than == 0 && less_than != 0) {
            return 1;
        }

        if (less_than == 0 && more_than != 0) {
            return -1;
        }

        return 0;
    }


    @Override
    public int hashCode() {
        return objective_.hashCode();
    }
}
