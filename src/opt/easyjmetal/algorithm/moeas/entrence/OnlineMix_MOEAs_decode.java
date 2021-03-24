package opt.easyjmetal.algorithm.moeas.entrence;

import opt.easyjmetal.core.Solution;
import opt.easyjmetal.core.SolutionSet;
import opt.easyjmetal.core.Variable;
import opt.easyjmetal.problem.onlinemix.Oilschdule;
import opt.easyjmetal.problem.onlinemix.OnlineMixOIL;
import opt.easyjmetal.util.JMException;
import opt.easyjmetal.util.sqlite.SqlUtils;

import java.util.List;

/**
 * ���Խ�������Ľ���Ƿ�һ��
 */
public class OnlineMix_MOEAs_decode {

    public static void main(String[] args) throws Exception {
        OnlineMixOIL problem = new OnlineMixOIL("Real");
        String dbName = problem.getName();
        String tableName = "MOEAD_1";

        SolutionSet solutionSet = SqlUtils.selectData(dbName, tableName);
        int count = 0;
        for (int i = 0; i < solutionSet.size(); i++) {
            boolean flag = evaluate(solutionSet.get(i));
            if (flag) {
                count++;
            }
        }

        // ������һ�±���
        System.out.println("����" + solutionSet.size() + "����֧��⣬����" + count + "�����Ԥ�ڽ��һ��");
    }

    /**
     * ���۽����Ӧ�ȣ������п��ӻ�
     *
     * @param solution
     * @throws JMException
     */
    public static boolean evaluate(Solution solution) throws JMException {

        Variable[] decisionVariables = solution.getDecisionVariables();
        int numberOfVariables = decisionVariables.length;
        double[][] x = new double[1][numberOfVariables];
        for (int i = 0; i < numberOfVariables; i++) {
            x[0][i] = decisionVariables[i].getValue();
        }

        List<List<Double>> eff = Oilschdule.fat(x, false);

        // �Ա�Ŀ��ֵ�Ƿ�ǰ��һ��
        if (solution.getObjective(0) == eff.get(0).get(numberOfVariables + 0)
                && solution.getObjective(1) == eff.get(0).get(numberOfVariables + 1)
                && solution.getObjective(2) == eff.get(0).get(numberOfVariables + 2)
                && solution.getObjective(3) == eff.get(0).get(numberOfVariables + 3)) {
            return true;
        }
        return false;
    }
}
