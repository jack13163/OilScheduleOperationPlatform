package opt.easyjmetal.algorithm.moeas.entrence;

import opt.easyjmetal.algorithm.moeas.util.Utils;
import opt.easyjmetal.core.Algorithm;
import opt.easyjmetal.core.Problem;
import opt.easyjmetal.problem.ProblemFactory;
import opt.easyjmetal.util.FileUtils;

import java.util.Arrays;
import java.util.List;

public class OnlineMix_MOEAs_main {

    public static void main(String[] args) throws Exception {
        batchRun(Arrays.asList( "NSGAII"), 3);
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
        Problem problem = (new ProblemFactory()).getProblem(problemName, new Object[]{"Real"});

        // 算法运行结果保存的路径
        String resultFile = mainPath + "/" + problem.getName() + ".db";
        FileUtils.deleteFile(resultFile);

        // 独立运行
        for (int j = 0; j < runtime; j++) {
            for (int i = 0; i < algorithmNames.size(); i++) {
                // 定义算法
                String algorithmName = algorithmNames.get(i);
                Algorithm algorithm = Utils.getAlgorithm(algorithmName, new Object[]{problem});
                // 参数设置
                algorithm.setInputParameter("AlgorithmName", algorithmName);
                algorithm.setInputParameter("populationSize", 100);
                algorithm.setInputParameter("maxEvaluations", 10000);
                algorithm.setInputParameter("externalArchiveSize", 100);
                algorithm.setInputParameter("runningTime", j + 1);
                algorithm.setInputParameter("dataDirectory", mainPath + "/pf_data/" + problem.getName() + "/");
                algorithm.setInputParameter("DBName", problemName);
                algorithm.setInputParameter("gamma", 1);
                algorithm.setInputParameter("beta0", 1);

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
