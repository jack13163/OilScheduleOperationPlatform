package opt.easyjmetal.algorithm.moeas.entrence;

import opt.easyjmetal.algorithm.moeas.impl.MOFA;
import opt.easyjmetal.core.Algorithm;
import opt.easyjmetal.core.Problem;
import opt.easyjmetal.core.SolutionSet;
import opt.easyjmetal.problem.ProblemFactory;
import opt.easyjmetal.util.FileUtils;

public class OnlineMix_MOEAs_main {

    public static void main(String[] args) throws Exception {
        batchRun("MOFA", 3);
    }

    /**
     * 独立运行某个算法若干次
     * @param algorithmName
     * @param runtime
     * @throws Exception
     */
    private static void batchRun(String algorithmName,int runtime) throws Exception {
        String mainPath = System.getProperty("user.dir");
        String resultFile = mainPath + "/" + algorithmName + ".db";
        FileUtils.deleteFile(resultFile);

        Problem problem = (new ProblemFactory()).getProblem("OnlineMixOIL", new Object[]{"Real"});

        // 独立运行
        for (int j = 0; j < runtime; j++) {
            // 定义算法
            Algorithm algorithm = new MOFA(problem);
            // 参数设置
            algorithm.setInputParameter("AlgorithmName", algorithmName);
            algorithm.setInputParameter("populationSize", 50);
            algorithm.setInputParameter("maxEvaluations", 5000);
            algorithm.setInputParameter("externalArchiveSize", 100);
            algorithm.setInputParameter("runningTime", j + 1);
            algorithm.setInputParameter("dataDirectory", mainPath + "/pf_data/" + problem.getName() + "/");
            algorithm.setInputParameter("DBName", "onlinemix");
            algorithm.setInputParameter("gamma", 1);
            algorithm.setInputParameter("beta0", 1);

            System.out.println("==================================================================");
            // 运行算法
            System.out.println("The " + j + " run of " + algorithmName);
            long initTime = System.currentTimeMillis();
            SolutionSet pop = algorithm.execute();
            long estimatedTime = System.currentTimeMillis() - initTime;
            System.out.println("Total execution time: " + estimatedTime + "ms");
            System.out.println("Problem:  " + problem.getName() + "  running time:  " + j);
            System.out.println("==================================================================");
        }
    }
}
