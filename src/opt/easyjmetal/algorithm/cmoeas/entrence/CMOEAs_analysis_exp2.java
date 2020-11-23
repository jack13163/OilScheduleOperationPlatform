package opt.easyjmetal.algorithm.cmoeas.entrence;

import opt.easyjmetal.util.MoeadUtils;
import opt.easyjmetal.util.JMException;

public class CMOEAs_analysis_exp2 {
    public static void main(String[] args) {
        try {
            // "NSGAII_CDP", "ISDEPLUS_CDP", "NSGAIII_CDP", "SPEA2_CDP", "MOEAD_CDP", "MOEAD_IEpsilon", "MOEAD_Epsilon", "MOEAD_SR", "C_MOEAD", "PPS_MOEAD"
            String[] algorithmNames = {"NSGAII_CDP", "ISDEPLUS_CDP", "NSGAIII_CDP", "MOEAD_CDP", "MOEAD_IEpsilon", "MOEAD_Epsilon", "MOEAD_SR", "C_MOEAD", "PPS_MOEAD"};
            String[] problemNames = {"EDF_PS", "EDF_TSS"};
            String[] indicatorNames = {"HV", "IGD"};
            String[] configs = {"config1", "config2", "config3"};
            int runtimes = 1;

            // 生成pareto前沿面
            MoeadUtils.generateParetoFrontForAllConfigs(configs, algorithmNames, problemNames, runtimes);
            // 计算性能指标
            for (int i = 0; i < configs.length; i++) {
                double value = MoeadUtils.generateQualityIndicatorsForAllConfigs(configs[i], indicatorNames[0]);
                System.out.println(configs[i] + " : " + value);
            }

//
//            // Friedman测试
//            Friedman.executeTest("HV", algorithmNames, problemNames);
//            Friedman.executeTest("IGD", algorithmNames, problemNames);
//
//            // 生成均值和方差
//            MeanStandardDeviation generateLatexTables = new MeanStandardDeviation(algorithmNames, problemNames, indicatorNames);
//            generateLatexTables.run();
//
//            // 进行TTest
//            TTest tTest = new TTest(algorithmNames, problemNames, indicatorNames);
//            tTest.run();
//
//            // 进行TTest
//            WilcoxonSignedRankTest wilcoxonSignedRankTest = new WilcoxonSignedRankTest(algorithmNames, problemNames, indicatorNames);
//            wilcoxonSignedRankTest.run();
//
//            // 计算不同策略C指标
//            CMetrics cMetrics = new CMetrics(problemNames, algorithmNames,runtimes);
//            cMetrics.run();
        } catch (JMException e) {
            e.printStackTrace();
        }
    }

}
