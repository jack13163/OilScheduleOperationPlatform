//  Utils.java
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

package org.uma.jmetal.algorithm.multiobjective.moead.cmoead;

import org.uma.jmetal.core.*;
import org.uma.jmetal.util.Distance;
import org.uma.jmetal.util.ENS_FirstRank;
import org.uma.jmetal.util.JMException;
import org.uma.jmetal.util.PseudoRandom;
import org.uma.jmetal.util.comparator.CrowdingComparator;

import java.lang.reflect.Constructor;

/**
 * Utilities methods to used by MOEA/D
 */
public class Utils {

    public static double distVector(double[] vector1, double[] vector2) {
        int dim = vector1.length;
        double sum = 0;
        for (int n = 0; n < dim; n++) {
            sum += (vector1[n] - vector2[n]) * (vector1[n] - vector2[n]);
        }
        return Math.sqrt(sum);
    } // distVector

    public static void minFastSort(double x[], int idx[], int n, int m) {
        for (int i = 0; i < m; i++) {
            for (int j = i + 1; j < n; j++) {
                if (x[i] > x[j]) {
                    double temp = x[i];
                    x[i]   = x[j];
                    x[j]   = temp;
                    int id = idx[i];
                    idx[i] = idx[j];
                    idx[j] = id;
                } // if
            }
        } // for

    } // minFastSort


    public static int[] returnSortedIndex(double x[], int flag){
        if(x == null || x.length == 0){
            return null;
        }else {
            int arrayLength = x.length;
            int[] result = new int[arrayLength];

            // Initialize the result
            for(int i = 0; i < arrayLength; i++){
                result[i] = i;
            }

            // bubble sort
            if(flag == 1){ // ascending order
                for(int i = 0; i < arrayLength; i++){
                    for(int j = i + 1; j < arrayLength; j++){
                        if(x[i] > x[j]){
                            double temp = x[i];
                            x[i] = x[j];
                            x[j] = temp;

                            int tempIndex = result[i];
                            result[i] = result[j];
                            result[j] = tempIndex;
                        }
                    }
                }
            }else if(flag == -1){ //descending order
                for(int i = 0; i < arrayLength; i++){
                    for(int j = i + 1; j < arrayLength; j++){
                        if(x[i] < x[j]){
                            double temp = x[i];
                            x[i] = x[j];
                            x[j] = temp;

                            int tempIndex = result[i];
                            result[i] = result[j];
                            result[j] = tempIndex;
                        }
                    }
                }

            }else {
                System.out.println("Unknown parameter");
            }
            return result;
        }

    }

    /**
     * Quick sort procedure (ascending order)
     *
     * @param array
     * @param idx
     * @param from
     * @param to
     */
    static void QuickSort(double[] array, int[] idx, int from, int to) {
        if (from < to) {
            double temp = array[to];
            int tempIdx = idx[to];
            int i = from - 1;
            for (int j = from; j < to; j++) {
                if (array[j] <= temp) {
                    i++;
                    double tempValue = array[j];
                    array[j] = array[i];
                    array[i] = tempValue;
                    int tempIndex = idx[j];
                    idx[j] = idx[i];
                    idx[i] = tempIndex;
                }
            }
            array[to] = array[i + 1];
            array[i + 1] = temp;
            idx[to] = idx[i + 1];
            idx[i + 1] = tempIdx;
            QuickSort(array, idx, from, i);
            QuickSort(array, idx, i + 1, to);
        }
    }

    public static void randomPermutation(int[] perm, int size) {
        int[] index = new int[size];
        boolean[] flag = new boolean[size];

        for (int n = 0; n < size; n++) {
            index[n] = n;
            flag[n]  = true;
        }

        int num = 0;
        while (num < size) {
            int start = PseudoRandom.randInt(0, size - 1);
            // int start = int(size*nd_uni(&rnd_uni_init));
            while (true) {
                if (flag[start]) {
                    perm[num]   = index[start];
                    flag[start] = false;
                    num++;
                    break;
                }
                if (start == (size - 1)) {
                    start = 0;
                } else {
                    start++;
                }
            }
        } // while
    } // randomPermutation

    /**
     * Calculate the dot product of two vectors
     *
     * @param vec1
     * @param vec2
     * @return
     */
    public static double innerproduct(double[] vec1, double[] vec2) {
        double sum = 0;

        for (int i = 0; i < vec1.length; i++)
            sum += vec1[i] * vec2[i];

        return sum;
    }

    /**
     * Calculate the norm of the vector
     *
     * @param z
     * @return
     */
    public static double norm_vector(double[] z, int numberObjectives) {
        double sum = 0;

        for (int i = 0; i < numberObjectives; i++)
            sum += z[i] * z[i];

        return Math.sqrt(sum);
    }

    public Algorithm getAlgorithm(String name, Object[] params) throws JMException {
        // Params are the arguments
        // The number of argument must correspond with the algorithm constructor params
        String base = "org.uma.jmetal.algorithm.multiobjective.moead.cmoead.";
        try {
            Class AlgorithmClass = Class.forName(base+name);
            Constructor[] constructors = AlgorithmClass.getConstructors();
            int i = 0;
            //find the constructor
            while ((i < constructors.length) &&
                    (constructors[i].getParameterTypes().length!=params.length)) {
                i++;
            }
            // constructors[i] is the selected one constructor
            Algorithm algorithm = (Algorithm)constructors[i].newInstance(params);
            return algorithm;
        }// try
        catch(Exception e) {
            e.printStackTrace();
            throw new JMException("Exception in " + name + ".getAlgorithm()") ;
        } // catch
    }

    public static void repairSolution(Solution solution, Problem problem_) throws JMException {
        Variable[] x = solution.getDecisionVariables();
        double a = x[0].getValue();
        double b = x[1].getValue();
        double e = x[3].getValue();
        double l = x[5].getValue();
        double rule_1 = Math.pow((a + b),2) - Math.pow(l,2) - Math.pow(e,2);
        double rule_2 = Math.pow((a - b),2) - Math.pow((l - 100),2) - Math.pow(e,2);

        while(rule_1 <= 0 || rule_2 >= 0){
            try{
                Solution tempSolution = new Solution(problem_);
                x = tempSolution.getDecisionVariables();
                a = x[0].getValue();
                b = x[1].getValue();
                e = x[3].getValue();
                l = x[5].getValue();
                rule_1 = Math.pow((a + b),2) - Math.pow(l,2) - Math.pow(e,2);
                rule_2 = Math.pow((a - b),2) - Math.pow((l - 100),2) - Math.pow(e,2);
            }catch (ClassNotFoundException exception){
                exception.printStackTrace();
            }
        }
        solution.setDecisionVariables(x);
    }

    public static void updateExternalArchive(SolutionSet pop, int popSize, SolutionSet externalArchive){
        SolutionSet feasible_solutions = new SolutionSet(popSize);
        int objectiveNo = pop.get(0).getNumberOfObjectives();
        Distance distance = new Distance();
        for (int i = 0; i < popSize; i++) {
            if (pop.get(i).getOverallConstraintViolation() == 0.0) {
                feasible_solutions.add(new Solution(pop.get(i)));
            }
        }

        if (feasible_solutions.size() > 0) {
            SolutionSet union = feasible_solutions.union(externalArchive);
            ENS_FirstRank ranking = new ENS_FirstRank(union);
            SolutionSet firstRankSolutions = ranking.getFirstfront();

            if (firstRankSolutions.size() <= popSize) {
                externalArchive.clear();
                for (int i = 0; i < firstRankSolutions.size(); i++) {
                    externalArchive.add(new Solution(firstRankSolutions.get(i)));
                }
            } else {

                //delete the element of the set until N <= popSize
                while (firstRankSolutions.size() > popSize){
                    distance.crowdingDistanceAssignment(firstRankSolutions, objectiveNo);
                    firstRankSolutions.sort(new CrowdingComparator());
                    firstRankSolutions.remove(firstRankSolutions.size() - 1);
                }

                externalArchive.clear();
                for (int i = 0; i < popSize; i++) {
                    externalArchive.add(new Solution(firstRankSolutions.get(i)));
                }
            }

        }

    }

    public static void initializeExternalArchive(SolutionSet pop, int popSize, SolutionSet externalArchive){
        SolutionSet feasible_solutions = new SolutionSet(popSize);
        for (int i = 0; i < popSize; i++) {
            if (pop.get(i).getOverallConstraintViolation() == 0.0) {
                feasible_solutions.add(new Solution(pop.get(i)));
            }
        }

        if (feasible_solutions.size() > 0) {
            ENS_FirstRank ranking = new ENS_FirstRank(feasible_solutions);
            externalArchive.union(ranking.getFirstfront());
        }

    }




}
