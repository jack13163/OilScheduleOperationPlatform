package opt.easyjmetal.algorithm.cmoeas.entrence;

import opt.easyjmetal.statistics.*;
import opt.easyjmetal.util.ParetoFrontUtil;

public class CMOEAs_analysis_exp2 {
    public static void main(String[] args) {
        try {
            // "NSGAII_CDP", "ISDEPLUS_CDP", "NSGAIII_CDP", "SPEA2_CDP", "MOEAD_CDP", "MOEAD_IEpsilon", "MOEAD_Epsilon", "MOEAD_SR", "C_MOEAD", "PPS_MOEAD"
            String[] algorithmNames = {"NSGAII_CDP", "ISDEPLUS_CDP", "NSGAIII_CDP", "MOEAD_CDP", "MOEAD_IEpsilon", "MOEAD_Epsilon", "MOEAD_SR", "C_MOEAD", "PPS_MOEAD"};
            String[] problemNames = {"EDF_PS", "EDF_TSS"};
            String[] indicatorNames = {"HV", "IGD"};
            String[] configs = {"config1", "config2", "config3"};
            int runtimes = 1;
            String basePath = "result/easyjmetal/twopipeline/";

            // 生成pareto前沿面
            ParetoFrontUtil.generateParetoFrontForAllConfigs(configs, algorithmNames, problemNames, runtimes, basePath);
            // 计算性能指标
            for (int i = 0; i < configs.length; i++) {
                String trueParetoFontPath = basePath + "oil.pf";
                String resultParetoFontPath = basePath + configs[i] + ".pf";
                double value = ParetoFrontUtil.calculateQualityIndicator(indicatorNames[0], resultParetoFontPath, trueParetoFontPath);
                System.out.println(configs[i] + " : " + value);
            }


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
            CMetrics cMetrics = new CMetrics(problemNames, algorithmNames,runtimes, basePath);
            cMetrics.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
