package opt.easyjmetal.problem.schedule.cop;

import opt.easyjmetal.core.Solution;
import opt.easyjmetal.util.JMException;
import opt.easyjmetal.util.ParetoFrontUtil;

public class ScheduleReverse {
    public static void main(String[] args) {
        String[] algorithmNameList = new String[]{
                "NSGAII_CDP",
                "ISDEPLUS_CDP",
                "NSGAIII_CDP",
                "MOEAD_CDP",
                "MOEAD_IEpsilon",
                "MOEAD_Epsilon",
                "MOEAD_SR",
                "C_MOEAD",
                "PPS_MOEAD"
        };
        String[] problemList = new String[]{
                "EDF_PS",
                "EDF_TSS"
        };
        int independentRuns = 10;
        String basePath_ = "result/easyjmetal/twopipeline";

        // 查找出指定的解
        double[][] tofind = new double[][]{
                {882.28, 195.0, 221.0, 23.0, 9.0},
                {1055.42, 169.0, 205.0, 25.0, 10.0},
                {837.04, 196.0, 194.0, 26.0, 11.0},
                {908.23, 196.0, 145.0, 24.0, 11.0}
        };

        try {
            ParetoFrontUtil.getSolutionFromDB(algorithmNameList, problemList, independentRuns, tofind,
                    new ParetoFrontUtil.ToDo() {
                        @Override
                        public void dosomething(Solution solution, String rule) {
                            // 解码位置
                            COPDecoder.decode(solution, rule, false);
                        }
                    }, basePath_);
        } catch (JMException ex) {
            ex.printStackTrace();
        }
    }
}
