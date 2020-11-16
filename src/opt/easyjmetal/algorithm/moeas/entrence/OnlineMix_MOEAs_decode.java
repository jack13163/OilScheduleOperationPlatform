package opt.easyjmetal.algorithm.moeas.entrence;

import opt.easyjmetal.core.Solution;
import opt.easyjmetal.core.SolutionSet;
import opt.easyjmetal.core.Variable;
import opt.easyjmetal.problem.onlinemix.Oilschdule;
import opt.easyjmetal.problem.onlinemix.OnlineMixOIL;
import opt.easyjmetal.util.JMException;
import opt.easyjmetal.util.sqlite.SqlUtils;

/**
 * 解码
 */
public class OnlineMix_MOEAs_decode {

    public static void main(String[] args) throws Exception {
        OnlineMixOIL problem = new OnlineMixOIL("Real");
        String dbName = problem.getName();
        String tableName = "NSGAII_1";

        SolutionSet solutionSet = SqlUtils.SelectData(dbName, tableName);
        for (int i = 0; i < solutionSet.size(); i++) {
            evaluate(solutionSet.get(i));
        }
    }

    /**
     * 评价解的适应度，并进行可视化
     * @param solution
     * @throws JMException
     */
    public static void evaluate(Solution solution) throws JMException {

        Variable[] decisionVariables = solution.getDecisionVariables();
        double[][] x = new double[1][decisionVariables.length];
        for (int i = 0; i < x.length; i++) {
            x[0][i] = decisionVariables[i].getValue();
        }
        Oilschdule.fat(x, true);
    }
}
