package opt.easyjmetal.algorithm.moeas.entrence;

import opt.easyjmetal.statistics.*;
import opt.easyjmetal.util.MoeadUtils;
import opt.easyjmetal.util.JMException;

/**
 * 分析多目标进化算法运行效果
 */
public class OnlineMix_MOEAs_analysis {
    public static void main(String[] args) {
        try {
            String[] algorithmNames = {"MOFA", "MOPSO", "MOEAD", "NSGAII"};
            String[] problemNames = {"OnlineMixOIL"};
            String[] indicatorNames = {"HV", "IGD"};
            int runtimes = 3;

            // 生成pareto前沿面
            MoeadUtils.generateParetoFront(algorithmNames, problemNames, runtimes);
            // 计算性能指标
            MoeadUtils.generateQualityIndicators(algorithmNames, problemNames, indicatorNames, runtimes);

            // Friedman测试
            Friedman.executeTest("HV", algorithmNames, problemNames);
            Friedman.executeTest("IGD", algorithmNames, problemNames);

            // 生成均值和方差
            MeanStandardDeviation generateLatexTables = new MeanStandardDeviation(algorithmNames, problemNames, indicatorNames);
            generateLatexTables.run();

            // 进行TTest
            TTest tTest = new TTest(algorithmNames, problemNames, indicatorNames);
            tTest.run();

            // 进行TTest
            WilcoxonSignedRankTest wilcoxonSignedRankTest = new WilcoxonSignedRankTest(algorithmNames, problemNames, indicatorNames);
            wilcoxonSignedRankTest.run();

            // 计算不同策略C指标
            CMetrics cMetrics = new CMetrics(problemNames, algorithmNames,runtimes);
            cMetrics.run();
        } catch (JMException e) {
            e.printStackTrace();
        }
    }

}
