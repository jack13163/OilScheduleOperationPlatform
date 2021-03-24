//  FPGAFitness.java
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

package opt.easyjmetal.util.fitness;

import opt.easyjmetal.core.Problem;
import opt.easyjmetal.core.SolutionSet;
import opt.easyjmetal.util.comparators.line.ConstraintDominanceComparator;

import java.util.Comparator;

/**
 * This class implements facilities for calculating the fitness for the
 * FPGA algorithm
 */
public class FPGAFitness {

    private SolutionSet solutionSet_ = null;
    private Problem problem_ = null;
    private static final Comparator dominance_ = new ConstraintDominanceComparator();

    /**
     * Constructor.
     * Create a new instance of Spea2Fitness
     *
     * @param solutionSet The solutionSet to assign the fitness
     * @param problem     The problem to solve
     */
    public FPGAFitness(SolutionSet solutionSet, Problem problem) {
        solutionSet_ = solutionSet;
        problem_ = problem;
        for (int i = 0; i < solutionSet_.size(); i++) {
            solutionSet_.get(i).setLocation(i);
        }
    }


    /**
     * Assign FPGA fitness to the solutions. Similar to the SPEA2 fitness.
     */
    public void fitnessAssign() {
        double[] strength = new double[solutionSet_.size()];
        double[] rawFitness = new double[solutionSet_.size()];

        for (int i = 0; i < solutionSet_.size(); i++) {
            if (solutionSet_.get(i).getRank() == 0) {
                solutionSet_.get(i).setFitness(solutionSet_.get(i).getCrowdingDistance());
            }
        }

        //Calculate the strength value
        // strength(i) = |{j | j <- SolutionSet and i dominate j}|
        for (int i = 0; i < solutionSet_.size(); i++) {
            for (int j = 0; j < solutionSet_.size(); j++) {
                if (dominance_.compare(solutionSet_.get(i), solutionSet_.get(j)) == -1) {
                    strength[i] += 1.0;
                }
            }
        }


        //Calculate the fitness
        //F(i) = sum(strength(j) | i dominate j) - sum(strenth(j) | j dominate i)
        for (int i = 0; i < solutionSet_.size(); i++) {
            double fitness = 0.0;
            for (int j = 0; j < solutionSet_.size(); j++) {
                int flag = dominance_.compare(solutionSet_.get(i), solutionSet_.get(j));
                if (flag == -1) {
                    // i domiante j
                    fitness += strength[j];
                } else if (flag == 1) {
                    fitness -= strength[j];
                }
            }
        }
    }
}
