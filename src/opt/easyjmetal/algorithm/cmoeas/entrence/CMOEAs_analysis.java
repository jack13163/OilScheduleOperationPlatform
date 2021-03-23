package opt.easyjmetal.algorithm.cmoeas.entrence;

import opt.easyjmetal.statistics.*;
import opt.easyjmetal.util.JMException;
import opt.easyjmetal.util.ParetoFrontUtil;

public class CMOEAs_analysis {
    public static void main(String[] args) {
        try {
            // "NSGAII_CDP", "ISDEPLUS_CDP", "NSGAIII_CDP", "SPEA2_CDP", "MOEAD_CDP", "MOEAD_IEpsilon", "MOEAD_Epsilon", "MOEAD_SR", "C_MOEAD", "PPS_MOEAD"
            String[] algorithmNames = {
                    "NSGAII_CDP_ISDEPlus",
                    "NSGAII_CDP",
//                    "ISDEPLUS_CDP",
//                    "NSGAIII_CDP",
//                    "MOEAD_CDP",
//                    "MOEAD_IEpsilon",
//                    "MOEAD_Epsilon",
//                    "MOEAD_SR",
//                    "C_MOEAD",
//                    "PPS_MOEAD",
//                    "C_TAEA",
//                    "CCMO"
            };
            String[] problemNames = {"EDF_PS"};//, "EDF_TSS"
            String[] indicatorNames = {"HV", "IGD"};
            int runtimes = 2;
            String basePath = "result/easyjmetal/twopipeline/";

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
