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

package opt.easyjmetal.algorithm.cmoeas.util;

import opt.easyjmetal.core.*;
import opt.easyjmetal.qualityindicator.Epsilon;
import opt.easyjmetal.qualityindicator.Hypervolume;
import opt.easyjmetal.qualityindicator.InvertedGenerationalDistance;
import opt.easyjmetal.qualityindicator.Spread;
import opt.easyjmetal.util.*;
import opt.easyjmetal.util.comparators.CrowdingComparator;
import opt.easyjmetal.util.sqlite.SqlUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilities methods to used by MOEA/D
 */
public class Utils {
    public static final String resultBaseDirectory_ = "result/easyjmetal";

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
        String base = "opt.easyjmetal.algorithm.cmoeas.";

        if (name.equalsIgnoreCase("NSGAIII_CDP")) {
            base += "nsgaiii_cdp.";
        } else if (name.equalsIgnoreCase("SPEA2_CDP")) {
            base += "spea2_cdp.";
        }

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

    public static SolutionSet initializeExternalArchive(SolutionSet pop, int popSize, SolutionSet externalArchive){
        SolutionSet feasible_solutions = new SolutionSet(popSize);
        for (int i = 0; i < popSize; i++) {
            if (pop.get(i).getOverallConstraintViolation() == 0.0) {
                feasible_solutions.add(new Solution(pop.get(i)));
            }
        }

        if (feasible_solutions.size() > 0) {
            // 执行非支配排序获取非支配解集
            Ranking ranking = new Ranking(feasible_solutions);
            externalArchive = externalArchive.union(ranking.getSubfront(0));
        }
        return externalArchive;
    }




    /**
     * Generate the Pareto Front
     *
     * @param algorithmNameList_ 算法列表
     * @param problemList_       问题列表
     * @param independentRuns_   独立运行次数
     */
    public static void generateParetoFront(String[] algorithmNameList_, String[] problemList_,
                                           int independentRuns_) throws JMException {
        for (String problemName : problemList_) {
            String paretoFrontPath = resultBaseDirectory_ + "/";
            List<Solution> solutionList = new ArrayList<>();

            // 读取某一问题的所有结果
            for (int numRun = 0; numRun < independentRuns_; numRun++) {
                for (String algorithmName : algorithmNameList_) {
                    String tableName = problemName + "_" + (numRun + 1);
                    SolutionSet tmp = SqlUtils.SelectData(algorithmName, tableName);
                    for (int i = 0; i < tmp.size(); i++) {
                        solutionList.add(tmp.get(i));
                    }
                }
            }

            // 进行非支配排序，获取非支配解集
            SolutionSet solutionSet = new SolutionSet(solutionList.size());
            for (int i = 0; i < solutionList.size(); i++) {
                solutionSet.add(solutionList.get(i));
            }
            Ranking ranking = new Ranking(solutionSet);
            SolutionSet nondominatedSolutionSet = ranking.getSubfront(0);

            // 输出非支配解集
            File dir = new File(paretoFrontPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            nondominatedSolutionSet.printObjectivesToFile(paretoFrontPath + problemName + ".pf");
        }
    }


    /**
     * Generate the Quality Indicators
     *
     * @param algorithmName
     * @param problemName
     * @param indicatorName
     * @param runId         从1开始
     */
    public static double generateQualityIndicators(String algorithmName, String problemName,
                                                   String indicatorName, int runId) throws JMException {
        double value = 0;

        String paretoFrontPath = resultBaseDirectory_ + "/" + problemName + ".pf";
        double[][] trueFront = new Hypervolume().utils_.readFront(paretoFrontPath);
        String dbName = algorithmName;
        String tableName = problemName + "_" + runId;
        double[][] solutionFront = SqlUtils.SelectData(dbName, tableName).writeObjectivesToMatrix();

        if (indicatorName.equals("HV")) {
            Hypervolume indicators = new Hypervolume();
            value = indicators.hypervolume(solutionFront, trueFront, trueFront[0].length);
        }
        if (indicatorName.equals("SPREAD")) {
            Spread indicators = new Spread();
            value = indicators.spread(solutionFront, trueFront, trueFront[0].length);
        }
        if (indicatorName.equals("IGD")) {
            InvertedGenerationalDistance indicators = new InvertedGenerationalDistance();
            value = indicators.invertedGenerationalDistance(solutionFront, trueFront, trueFront[0].length);
        }
        if (indicatorName.equals("EPSILON")) {
            Epsilon indicators = new Epsilon();
            value = indicators.epsilon(solutionFront, trueFront, trueFront[0].length);
        }

        return value;
    }

    /**
     * Generate the Quality Indicators
     *
     * @param algorithmNameList_ 算法列表
     * @param problemList_       问题列表
     * @param indicatorList_     指标列表
     * @param independentRuns_   独立运行次数
     */
    public static void generateQualityIndicators(String[] algorithmNameList_, String[] problemList_,
                                                 String[] indicatorList_, int independentRuns_) throws JMException {
        if (indicatorList_.length > 0) {
            for (String algorithmName : algorithmNameList_) {
                for (String problemName : problemList_) {
                    for (String indicator : indicatorList_) {

                        try {
                            // 输出到文件
                            String dirName = resultBaseDirectory_ + "/data/" + algorithmName + "/" + problemName + "/";
                            File file = new File(dirName);
                            if (!file.exists()) {
                                file.mkdirs();// 不存在，则创建目录
                            }
                            String filepath = dirName + indicator;
                            FileWriter writer = new FileWriter(filepath);

                            // 计算每次实验的指标值
                            for (int numRun = 1; numRun <= independentRuns_; numRun++) {
                                double value = generateQualityIndicators(algorithmName, problemName, indicator, numRun);
                                writer.write(String.format("%.5f\n", value));
                            }

                            writer.flush();
                            writer.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}