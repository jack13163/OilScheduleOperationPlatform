package opt.easyjmetal.algorithm.moeas.entrence;

import opt.easyjmetal.statistics.*;
import opt.easyjmetal.util.JMException;
import opt.easyjmetal.util.fileinput.ParetoFrontUtil;

/**
 * 分析多目标进化算法运行效果
 */
public class OnlineMix_MOEAs_analysis {
    public static void main(String[] args) {
        try {
            String[] algorithmNames = {
                    "ISDEPlus", "IBEA",
                    "MOEAD", "NSGAII",
                    "MOFA", "MOPSO",
                    "SPEA2", "NSGAIII"};
            String[] problemNames = {"OnlineMixOIL"};
            String[] indicatorNames = {"HV", "IGD"};
            int runtimes = 10;
            // 结果生成的路径，建议：每个问题建立一个单独的文件夹
            String basePath = "result/easyjmetal/onlinemix/";

            // 生成pareto前沿面
            ParetoFrontUtil.generateParetoFront(algorithmNames, problemNames, runtimes, basePath);
            // 计算性能指标
            ParetoFrontUtil.generateQualityIndicators(algorithmNames, problemNames, indicatorNames, runtimes, basePath);

            // Friedman测试
            Friedman.executeTest("HV", algorithmNames, problemNames, basePath);
            Friedman.executeTest("IGD", algorithmNames, problemNames, basePath);

            // 生成均值和方差
            MeanStandardDeviation generateLatexTables = new MeanStandardDeviation(algorithmNames, problemNames, indicatorNames, basePath);
            generateLatexTables.run();

            // 进行TTest
            TTest tTest = new TTest(algorithmNames, problemNames, indicatorNames, basePath);
            tTest.run();

            // 进行TTest
            WilcoxonSignedRankTest wilcoxonSignedRankTest = new WilcoxonSignedRankTest(algorithmNames, problemNames, indicatorNames, basePath);
            wilcoxonSignedRankTest.run();

            // 计算不同策略C指标
            CMetrics cMetrics = new CMetrics(problemNames, algorithmNames, runtimes, basePath);
            cMetrics.run();
        } catch (JMException e) {
            e.printStackTrace();
        }
    }
}
