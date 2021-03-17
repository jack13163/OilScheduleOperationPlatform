// SPEA2: Improving the Strength Pareto Evolutionary Algorithm For Multiobjective Optimization.
package opt.easyjmetal.algorithm.cmoeas.impl.ccmo;

import opt.easyjmetal.core.*;
import opt.easyjmetal.util.Distance;
import opt.easyjmetal.util.JMException;
import opt.easyjmetal.util.MoeadUtils;
import opt.easyjmetal.util.sqlite.SqlUtils;

public class CCMO extends Algorithm {

    public CCMO(Problem problem) {
        super(problem);
    }

    private SolutionSet external_archive_;
    private int populationSize_;
    private int maxEvaluations_;
    private String dataDirectory_;

    Distance distance;

    @Override
    public SolutionSet execute() throws JMException, ClassNotFoundException {

        distance = new Distance();
        int runningTime = (Integer) getInputParameter("runningTime") + 1;
        populationSize_ = (Integer) getInputParameter("populationSize");
        maxEvaluations_ = (Integer) getInputParameter("maxEvaluations");
        dataDirectory_ = getInputParameter("dataDirectory").toString();
        Operator mutationOperator_ = operators_.get("mutation");
        Operator crossoverOperator_ = operators_.get("crossover");
        Operator selectionOperator_ = operators_.get("selection");

        // Initialize the variables
        SolutionSet population1 = new SolutionSet(populationSize_ / 2);
        SolutionSet population2 = new SolutionSet(populationSize_ / 2);
        int evaluations_ = 0;
        for (int i = 0; i < populationSize_; i++) {
            Solution newSolution = new Solution(problem_);
            problem_.evaluate(newSolution);
            problem_.evaluateConstraints(newSolution);
            evaluations_++;
            if (i % 2 == 0) {
                population1.add(newSolution);
            } else {
                population2.add(newSolution);
            }
        }

        SolutionSet allPop = population1.union(population2);

        // 初始化储备集
        external_archive_ = new SolutionSet(populationSize_);
        MoeadUtils.initializeExternalArchive(allPop, populationSize_, external_archive_);

        // 创建数据库和数据表
        String dbName = dataDirectory_;
        String tableName = "CCMO_" + runningTime;
        SqlUtils.CreateTable(tableName, dbName);

        int gen = 0;
        while (evaluations_ < maxEvaluations_) {
            SolutionSet offspringPopulation1 = new SolutionSet(populationSize_ / 2);
            SolutionSet offspringPopulation2 = new SolutionSet(populationSize_ / 2);
            for (int i = 0; i < (populationSize_ / 2); i++) {
                Solution[] offSpring = new Solution[2];
                // 交叉变异
                if (crossoverOperator_.getClass().getSimpleName().equalsIgnoreCase("SBXCrossover")) {
                    Solution[] parents = new Solution[2];
                    parents[0] = (Solution) selectionOperator_.execute(population1);
                    parents[1] = (Solution) selectionOperator_.execute(population2);
                    offSpring = ((Solution[]) crossoverOperator_.execute(parents));
                } else if (crossoverOperator_.getClass().getSimpleName().equalsIgnoreCase("DifferentialEvolutionCrossover")) {
                    Solution[] parents = new Solution[3];
                    parents[0] = (Solution) selectionOperator_.execute(population1);
                    parents[1] = (Solution) selectionOperator_.execute(population2);
                    parents[2] = parents[0];
                    offSpring[0] = (Solution) crossoverOperator_.execute(new Object[]{parents[0], parents});
                    offSpring[1] = (Solution) crossoverOperator_.execute(new Object[]{parents[1], parents});
                } else {
                    System.out.println("unknown crossover");

                }
                mutationOperator_.execute(offSpring[0]);
                mutationOperator_.execute(offSpring[1]);
                problem_.evaluate(offSpring[0]);
                problem_.evaluateConstraints(offSpring[0]);
                problem_.evaluate(offSpring[1]);
                problem_.evaluateConstraints(offSpring[1]);

                // 两个种群独立进行更新
                offspringPopulation1.add(offSpring[0]);
                offspringPopulation2.add(offSpring[1]);
                evaluations_ += 2;
            }

            // 环境选择
            SolutionSet offspringPopulation_ = offspringPopulation1.union(offspringPopulation2);
            population1 = replacement(population1, offspringPopulation_, true);
            population2 = replacement(population2, offspringPopulation_, false);
            SolutionSet population_ = population1.union(population2);
            MoeadUtils.updateExternalArchive(population_, populationSize_, external_archive_);

            if (gen % 50 == 0) {
                allPop = allPop.union(population_);
            }
            gen++;
        }

        SqlUtils.InsertSolutionSet(dbName, tableName, external_archive_);

        return external_archive_;
    }

    /**
     * 环境选择
     *
     * @param population          父代种群
     * @param offspringPopulation 子代种群
     * @param ignoreConstraints   计算适应度值时是否忽略约束
     * @return
     * @throws JMException
     */
    private SolutionSet replacement(SolutionSet population, SolutionSet offspringPopulation, boolean ignoreConstraints) throws JMException {
        SolutionSet jointPopulation = population.union(offspringPopulation);
        EnvironmentalSelection selection = new EnvironmentalSelection(populationSize_);
        population = selection.execute(jointPopulation, ignoreConstraints);
        return population;
    }
}
