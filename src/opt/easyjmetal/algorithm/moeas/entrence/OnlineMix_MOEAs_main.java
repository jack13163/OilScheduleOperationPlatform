package opt.easyjmetal.algorithm.moeas.entrence;

import opt.easyjmetal.algorithm.AlgorithmFactory;
import opt.easyjmetal.core.Algorithm;
import opt.easyjmetal.core.Operator;
import opt.easyjmetal.core.Problem;
import opt.easyjmetal.operator.crossover.CrossoverFactory;
import opt.easyjmetal.operator.mutation.MutationFactory;
import opt.easyjmetal.operator.selection.SelectionFactory;
import opt.easyjmetal.problem.ProblemFactory;
import opt.easyjmetal.util.FileUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class OnlineMix_MOEAs_main {

    public static void main(String[] args) throws Exception {
//        batchRun(Arrays.asList("MOFA", "MOPSO", "MOEAD", "NSGAII", "ISDEPlus", "IBEA", "SPEA2"), 5);
//        batchRun(Arrays.asList("ISDEPlus"), 1);
//        batchRun(Arrays.asList("IBEA"), 1);
        batchRun(Arrays.asList("IBEA"), 1);
    }

    /**
     * 独立运行某个算法若干次
     *
     * @param algorithmNames
     * @param runtime
     * @throws Exception
     */
    private static void batchRun(List<String> algorithmNames, int runtime) throws Exception {
        String problemName = "OnlineMixOIL";
        String mainPath = System.getProperty("user.dir");
        Problem problem = ProblemFactory.getProblem(problemName, new Object[]{"Real"});

        // 先清楚上次运行的结果
        String resultFile = mainPath + "/" + problem.getName() + ".db";
        boolean deleted = false;
        do {
            deleted = FileUtils.deleteFile(resultFile);
            Thread.sleep(500);
        } while (!deleted);
        System.out.println("Initialization finished successfully...");

        // 参数配置
        int popSize = 100;
        int externalArchiveSize = 100;
        int neighborSize = (int) (0.1 * popSize);
        int maxFES = 10000;
        int updateNumber = 2;
        double gamma = 1;
        double beta0 = 1;
        double deDelta = 0.9;
        int k = 1;
        Boolean isDisplay = true;
        // 0 for the working population; 1 for the external archive
        int plotFlag = 0;

        // 独立运行若干次
        for (int j = 0; j < runtime; j++) {
            for (int i = 0; i < algorithmNames.size(); i++) {
                // 定义算法
                String algorithmName = algorithmNames.get(i);
                Algorithm algorithm = AlgorithmFactory.getAlgorithm(algorithmName, new Object[]{problem});
                // 参数设置
                algorithm.setInputParameter("AlgorithmName", algorithmName);
                algorithm.setInputParameter("populationSize", popSize);
                algorithm.setInputParameter("maxEvaluations", maxFES);
                algorithm.setInputParameter("externalArchiveSize", externalArchiveSize);
                algorithm.setInputParameter("runningTime", j + 1);
                algorithm.setInputParameter("weightsDirectory", mainPath + "/resources/MOEAD_Weights/");
                algorithm.setInputParameter("DBName", problemName);
                algorithm.setInputParameter("T", neighborSize);
                algorithm.setInputParameter("delta", deDelta);
                algorithm.setInputParameter("gamma", gamma);
                algorithm.setInputParameter("beta0", beta0);
                algorithm.setInputParameter("nr", updateNumber);
                algorithm.setInputParameter("k", k);
                algorithm.setInputParameter("isDisplay", isDisplay);
                algorithm.setInputParameter("plotFlag", plotFlag);

                Operator mutationOperator_ = MutationFactory.getMutationOperator("PolynomialMutation", new HashMap(){{
                    put("probability", 1.0 / problem.getNumberOfVariables());
                    put("distributionIndex", 20.0);
                }});
                algorithm.setInputParameter("mutation", mutationOperator_);
                Operator crossoverOperator_ = CrossoverFactory.getCrossoverOperator("SBXCrossover", new HashMap<String, Double>(){{
                    put("probability", 1.0);
                    put("distributionIndex", 20.0);
                }});
                algorithm.setInputParameter("crossover", crossoverOperator_);
                Operator selectionOperator_ = SelectionFactory.getSelectionOperator("BinaryTournament2", null);
                algorithm.setInputParameter("selection", selectionOperator_);

                System.out.println("==================================================================");
                // 运行算法
                System.out.println("The " + j + " run of " + algorithmName);
                long initTime = System.currentTimeMillis();
                algorithm.execute();
                long estimatedTime = System.currentTimeMillis() - initTime;
                System.out.println("Total execution time: " + estimatedTime + "ms");
                System.out.println("Problem:  " + problem.getName() + "  running time:  " + j);
                System.out.println("==================================================================");
            }
        }
    }
}
