/**
 * Created by liwenji  Email: wenji_li@126.com
 */
package org.uma.jmetal.algorithm.multiobjective.moead.cmoead;

import org.uma.jmetal.core.Algorithm;
import org.uma.jmetal.core.Operator;
import org.uma.jmetal.core.Problem;
import org.uma.jmetal.core.SolutionSet;
import org.uma.jmetal.operator.crossover.CrossoverFactory;
import org.uma.jmetal.operator.mutation.MutationFactory;
import org.uma.jmetal.operator.selection.SelectionFactory;
import org.uma.jmetal.problem.ProblemFactory;
import org.uma.jmetal.util.FileUtils;

import java.util.HashMap;


public class CMOEAs_main {

    public static void main(String[] args) throws Exception {
        // 0 represents for DE, 1 represents for SBX
        int crossoverMethod = 0;
        //batchRun(new String[]{"MOEAD_IEpsilon","MOEAD_Epsilon","MOEAD_SR","MOEAD_CDP","C_MOEAD"},crossoverMethod);

        batchRun(new String[]{"MOEAD_IEpsilon","MOEAD_Epsilon","MOEAD_SR","MOEAD_CDP","C_MOEAD"},crossoverMethod);
    }

    private static void batchRun(String[] methods, int crossMethod) throws Exception {
        String[] algorithmSet = methods;
        int algorithmNo = algorithmSet.length;
        for (int i = 0; i < algorithmNo; i++) {
            System.out.println("The tested algorithm: " + algorithmSet[i]);
            System.out.println("The process: " + (100.0 * i / algorithmNo) + "%");
            singleRun(algorithmSet[i],crossMethod); // 0 represents for DE, 1 represents for SBX
        }
    }

    private static void singleRun(String algorithmName, int crossMethod) throws Exception {
        Problem problem;                // The problem to solve
        Algorithm algorithm;            // The algorithm to use
        Operator crossover;            // Crossover operator
        Operator mutation;             // Mutation operator
        Operator selection;            // Selection operator
        HashMap parameters;           // Operator parameters

/////////////////////////////////////////// parameter setting //////////////////////////////////

        int popSize = 300;
        int neighborSize = (int) (0.1 * popSize);
        int maxFES = 300000;
        int updateNumber = 2;
        double deDelta = 0.9;
        double DeCrossRate = 1.0;
        double DeFactor = 0.5;

        double tao = 0.1;
        double alpha = 0.9;

        String AlgorithmName = algorithmName;

        String mainPath = System.getProperty("user.dir");
        String weightPath = "MOEAD_Weights";// 权重文件路径
        int runtime = 30;
        Boolean isDisplay = false;
        int plotFlag = 0; // 0 for the working population; 1 for the external archive

        // MOEAD_SR parameters
        double srFactor = 0.05;

        String resultFile = mainPath + "/" + AlgorithmName + ".db";
        FileUtils.deleteFile(resultFile);

        Object[] params = {"Real"};
        String[] problemStrings = {"LIRCMOP1","LIRCMOP2","LIRCMOP3","LIRCMOP4","LIRCMOP5","LIRCMOP6","LIRCMOP7","LIRCMOP8","LIRCMOP9","LIRCMOP10","LIRCMOP11","LIRCMOP12","LIRCMOP13","LIRCMOP14"};

//////////////////////////////////////// End parameter setting //////////////////////////////////

        for (int i = 0; i < problemStrings.length; i++) {
            problem = (new ProblemFactory()).getProblem(problemStrings[i], params);
            //define algorithm
            Object[] algorithmParams = {problem};
            algorithm = (new Utils()).getAlgorithm(AlgorithmName, algorithmParams);

            //define pareto file path
            String paretoPath = mainPath + "/pf_data/" + problemStrings[i] + ".pf";
            // Algorithm parameters
            algorithm.setInputParameter("AlgorithmName", AlgorithmName);
            algorithm.setInputParameter("populationSize", popSize);
            algorithm.setInputParameter("maxEvaluations", maxFES);
            algorithm.setInputParameter("dataDirectory", weightPath);
            algorithm.setInputParameter("T", neighborSize);
            algorithm.setInputParameter("delta", deDelta);
            algorithm.setInputParameter("nr", updateNumber);
            algorithm.setInputParameter("isDisplay", isDisplay);
            algorithm.setInputParameter("plotFlag", plotFlag);
            algorithm.setInputParameter("paretoPath", paretoPath);
            algorithm.setInputParameter("srFactor", srFactor);
            algorithm.setInputParameter("tao", tao);
            algorithm.setInputParameter("alpha", alpha);

            // Crossover operator
            if (crossMethod == 0) {                      // DE operator
                parameters = new HashMap();
                parameters.put("CR", DeCrossRate);
                parameters.put("F", DeFactor);
                crossover = CrossoverFactory.getCrossoverOperator(
                        "DifferentialEvolutionCrossover", parameters);
                algorithm.addOperator("crossover", crossover);
            } else if (crossMethod == 1) {                // SBX operator
                parameters = new HashMap();
                parameters.put("probability", 1.0);
                parameters.put("distributionIndex", 20.0);
                crossover = CrossoverFactory.getCrossoverOperator("SBXCrossover",
                        parameters);
                algorithm.addOperator("crossover", crossover);
            }

            // Mutation operator
            parameters = new HashMap();
            parameters.put("probability", 1.0 / problem.getNumberOfVariables());
            parameters.put("distributionIndex", 20.0);
            mutation = MutationFactory.getMutationOperator("PolynomialMutation", parameters);
            algorithm.addOperator("mutation", mutation);

            // Selection Operator
            parameters = null;
            selection = SelectionFactory.getSelectionOperator("BinaryTournament2", parameters);
            algorithm.addOperator("selection", selection);
            // each problem runs runtime times
            for (int j = 0; j < runtime; j++) {
                algorithm.setInputParameter("runningTime", j);
                // Execute the Algorithm
                System.out.println("The " + j + " run of " + problemStrings[i]);
                long initTime = System.currentTimeMillis();
                SolutionSet pop = algorithm.execute();
                long estimatedTime = System.currentTimeMillis() - initTime;
                // Result messages
                System.out.println("Total execution time: " + estimatedTime + "ms");
                System.out.println("Problem:  " + problemStrings[i] + "  running time:  " + j);
            }
        }
    }
}
