package opt.easyjmetal.problem.schedule.cop;

import opt.easyjmetal.core.Solution;
import opt.easyjmetal.util.JMException;
import opt.easyjmetal.util.ParetoFrontUtil;

public class findBug {
    public static void main(String[] args) {
        String[] algorithmNameList = new String[]{
                "MOEAD_IEpsilon"
        };
        String[] problemList = new String[]{
                "EDF_PS",
                "EDF_TSS"
        };
        int independentRuns = 10;

        // 查找出指定的解
        double[][] tofind = new double[][]{
                {562.11, 65.0, 60.0, 17.0, 11.0}
        };

        try {
            ParetoFrontUtil.getSolutionFromDB(algorithmNameList, problemList, independentRuns, tofind,
                    new ParetoFrontUtil.ToDo() {
                        @Override
                        public void dosomething(Solution solution, String rule) {
                            // 解码位置
                            COPDecoder.decode(solution, rule, true);
                        }
                    });
        } catch (JMException ex) {
            ex.printStackTrace();
        }
    }
}
