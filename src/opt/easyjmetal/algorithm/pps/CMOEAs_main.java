/**
 * Created by liwenji  Email: wenji_li@126.com
 */
package opt.easyjmetal.algorithm.pps;

import opt.easyjmetal.core.Algorithm;
import opt.easyjmetal.core.Operator;
import opt.easyjmetal.core.Problem;
import opt.easyjmetal.core.SolutionSet;
import opt.easyjmetal.operator.crossover.CrossoverFactory;
import opt.easyjmetal.operator.mutation.MutationFactory;
import opt.easyjmetal.operator.selection.SelectionFactory;
import opt.easyjmetal.problem.ProblemFactory;
import opt.easyjmetal.util.FileUtils;

import java.util.HashMap;


public class CMOEAs_main {

    public static void main(String[] args) throws Exception {

        singleRun("NSGAII_CDP",0);

    }

    private static void singleRun(String algorithmName, int crossMethod) throws Exception {
        Problem problem;              // The problem to solve
        Algorithm algorithm;            // The algorithm to use
        Operator crossover;            // Crossover operator
        Operator mutation;             // Mutation operator
        Operator selection;            // Selection operator
        HashMap parameters;           // Operator parameters

/////////////////////////////////////////// parameter setting //////////////////////////////////
        //parameter setting
        int popSize = 300;
        int neighborSize = (int) (0.1 * popSize);
        int maxFES = 150000;
        int updateNumber = 2;
        double deDelta = 0.9;
        double DeCrossRate = 1.0;
        double DeFactor = 0.5;

        double tao = 0.1;
        double alpha = 0.9;
        double threshold = 1e-3;

        String AlgorithmName = algorithmName;


        String mainPath = System.getProperty("user.dir");
        String weightPath = mainPath + "/weight";
        int runtime = 1;

        // cMOEAD_DRA parameters
        double epsilon = 0.02;
        int lastGen = 20;

        // MOEAD_SR parameters
        double srFactor = 0.05;
        // IDEA parameter
        float infeasibleRatio = 0.1f;


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
            algorithm.setInputParameter("paretoPath", paretoPath);
            algorithm.setInputParameter("epsilon", epsilon);
            algorithm.setInputParameter("lastGen", lastGen);
            algorithm.setInputParameter("srFactor", srFactor);
            algorithm.setInputParameter("infeasibleRatio", infeasibleRatio);

            algorithm.setInputParameter("tao", tao);
            algorithm.setInputParameter("alpha", alpha);

            algorithm.setInputParameter("threshold_change", threshold);

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
