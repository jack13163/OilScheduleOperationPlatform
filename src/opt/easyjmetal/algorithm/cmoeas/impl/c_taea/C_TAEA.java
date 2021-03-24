package opt.easyjmetal.algorithm.cmoeas.impl.c_taea;

import opt.easyjmetal.algorithm.common.MatlabUtilityFunctionsWrapper;
import opt.easyjmetal.algorithm.common.UtilityFunctions;
import opt.easyjmetal.core.*;
import opt.easyjmetal.util.JMException;
import opt.easyjmetal.util.sqlite.SqlUtils;

/**
 * % Two-archive evolutionary algorithm for constrained MOPs
 * %------------------------------- Reference --------------------------------
 * % K. Li, R. Chen, G. Fu, and X. Yao, Two-archive evolutionary algorithm for
 * % constrained multi-objective optimization, IEEE Transactions on
 * % Evolutionary Computation, 2018, 23(2): 303-315.
 */
public class C_TAEA extends Algorithm {

    private SolutionSet population_;
    private SolutionSet external_archive_ca;
    private SolutionSet external_archive_da;
    private int populationSize_;
    private int maxEvaluations_;
    private String dataDirectory_;
    private String weightDirectory_;
    // 参考点
    private double[][] lambda_;

    public C_TAEA(Problem problem) {
        super(problem);
    }

    @Override
    public SolutionSet execute() throws JMException, ClassNotFoundException {

        int runningTime = (Integer) getInputParameter("runningTime") + 1;

        //Read the parameters
        populationSize_ = (Integer) getInputParameter("populationSize");
        maxEvaluations_ = (Integer) getInputParameter("maxEvaluations");
        dataDirectory_ = getInputParameter("dataDirectory").toString();
        weightDirectory_ = getInputParameter("weightDirectory").toString();
        Operator mutationOperator_ = operators_.get("mutation");
        Operator crossoverOperator_ = operators_.get("crossover");
        Operator selectionOperator_ = operators_.get("selection");

        int evaluations_ = 0;

        // Generate the weight vectors
        lambda_ = MatlabUtilityFunctionsWrapper.initUniformWeight(populationSize_, problem_.getNumberOfObjectives());

        // Generate random population
        population_ = new SolutionSet(populationSize_);
        for (int i = 0; i < populationSize_; i++) {
            Solution newSolution = new Solution(problem_);
            problem_.evaluate(newSolution);
            problem_.evaluateConstraints(newSolution);
            evaluations_++;
            population_.add(newSolution);
        }
        SolutionSet allPop = population_;

        // 初始化储备集
        external_archive_ca = new SolutionSet(populationSize_);
        external_archive_ca = TwoArchiveUpdate.updateCA(external_archive_ca, population_, lambda_);
        external_archive_da = new SolutionSet(populationSize_);
        external_archive_da = TwoArchiveUpdate.updateDA(external_archive_ca, external_archive_da, population_, lambda_);

        //creat database
        String dbName = dataDirectory_;
        String tableName = "C_TAEA_" + runningTime;
        SqlUtils.createTable(tableName, dbName);

        int gen = 0;
        while (evaluations_ < maxEvaluations_) {
            // calculate the ratio of non-dominated solutions of CA and DA in Hm
            double[] probabilities = MatlabUtilityFunctionsWrapper.probability(external_archive_ca.writeObjectivesToMatrix(), external_archive_da.writeObjectivesToMatrix());
            double Pc = probabilities[0];
            double Pd = probabilities[1];
            double PC = probabilities[2];
            SolutionSet q = new SolutionSet(populationSize_);
            for (int i = 0; i < (int)(populationSize_ / 2); i++) {
                SolutionSet P1, P2;
                if (Pc > Pd) {
                    P1 = external_archive_ca;
                } else {
                    P1 = external_archive_da;
                }
                double pf = Math.random();
                if (pf < PC) {
                    P2 = external_archive_ca;
                } else {
                    P2 = external_archive_da;
                }
                // 选择两个个体进行交叉操作
                Solution[] offSpring = UtilityFunctions.generateOffsprings(P1, P2, mutationOperator_, crossoverOperator_, selectionOperator_);
                q.add(offSpring[0]);
                q.add(offSpring[1]);
                evaluations_ += 2;
            }

            // 重新计算适应度值
            for (int i = 0; i < populationSize_; i++) {
                Solution solution = q.get(i);
                problem_.evaluate(solution);
                problem_.evaluateConstraints(solution);
            }

            if (gen % 50 == 0) {
                allPop = allPop.union(population_);
            }
            gen++;

            // 更新储备集
            external_archive_ca = TwoArchiveUpdate.updateCA(external_archive_ca, q, lambda_);
            external_archive_da = TwoArchiveUpdate.updateDA(external_archive_ca, external_archive_da, q, lambda_);
        }

        SqlUtils.insertSolutionSet(dbName, tableName, external_archive_ca);

        return external_archive_ca;
    }
}
