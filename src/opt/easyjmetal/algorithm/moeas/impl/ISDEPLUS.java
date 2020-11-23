// ISDE+ - An Indicator for Multi and Many-objective Optimization.
package opt.easyjmetal.algorithm.moeas.impl;

import opt.easyjmetal.core.*;
import opt.easyjmetal.operator.crossover.CrossoverFactory;
import opt.easyjmetal.operator.mutation.MutationFactory;
import opt.easyjmetal.operator.selection.SelectionFactory;
import opt.easyjmetal.util.*;
import opt.easyjmetal.util.comparators.FitnessComparator;
import opt.easyjmetal.util.sqlite.SqlUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


public class ISDEPLUS extends Algorithm {

    public ISDEPLUS(Problem problem) {
        super(problem);
    }

    private SolutionSet population_;
    private SolutionSet external_archive_;
    private int populationSize_;
    private int archiveSize_;
    private int maxEvaluations_;
    private String dataDirectory_;
    private Distance distance_;
    private int evaluations_;
    private Operator crossoverOperator_;
    private Operator mutationOperator_;
    private Operator selectionOperator_;

    @Override
    public SolutionSet execute() throws JMException, ClassNotFoundException {

        // 参数初始化
        this.distance_ = new Distance();
        evaluations_ = 0;
        maxEvaluations_ = (Integer) getInputParameter("maxEvaluations");
        populationSize_ = (Integer) getInputParameter("populationSize");
        archiveSize_ = (Integer) getInputParameter("externalArchiveSize");
        String dbName = getInputParameter("DBName").toString();
        int runningTime = (Integer) getInputParameter("runningTime");
        population_ = new SolutionSet(populationSize_);
        boolean isDisplay_ = (Boolean) getInputParameter("isDisplay");
        this.population_ = new SolutionSet(populationSize_);
        this.mutationOperator_ = MutationFactory.getMutationOperator("PolynomialMutation", new HashMap() {{
            put("probability", 1.0 / problem_.getNumberOfVariables());// 变异概率
            put("distributionIndex", 20.0);
        }});
        this.crossoverOperator_ = CrossoverFactory.getCrossoverOperator("SBXCrossover", new HashMap<String, Double>() {{
            put("probability", 1.0);
            put("distributionIndex", 20.0);
        }});
        this.selectionOperator_ = SelectionFactory.getSelectionOperator("BinaryTournament2", null);// 选择算子

        // 创建数据表，方便后面保存结果
        String tableName = "ISDEPlus_" + runningTime;
        SqlUtils.CreateTable(tableName, dbName);

        // 创建初始种群
        for (int i = 0; i < populationSize_; i++) {
            Solution newSolution = new Solution(problem_);
            problem_.evaluate(newSolution);
            problem_.evaluateConstraints(newSolution);
            evaluations_++;
            population_.add(newSolution);
        }

        // 初始化外部储备集
        external_archive_ = new SolutionSet(populationSize_);
        MoeadUtils.initializeExternalArchive(population_, populationSize_, external_archive_);

        // 迭代
        while (evaluations_ < maxEvaluations_) {
            // 生成子代种群
            SolutionSet offspringPopulation_ = new SolutionSet(populationSize_);
            for (int i = 0; i < (populationSize_ / 2); i++) {
                Solution[] offSpring = new Solution[2];
                if (crossoverOperator_.getClass().getSimpleName().equalsIgnoreCase("SBXCrossover")) {
                    // SBX交叉算子
                    Solution[] parents = new Solution[2];
                    parents[0] = (Solution) selectionOperator_.execute(population_);
                    parents[1] = (Solution) selectionOperator_.execute(population_);
                    offSpring = ((Solution[]) crossoverOperator_.execute(parents));
                } else {
                    // DE交叉算子
                    Solution[] parents = new Solution[3];
                    parents[0] = (Solution) selectionOperator_.execute(population_);
                    parents[1] = (Solution) selectionOperator_.execute(population_);
                    parents[2] = parents[0];
                    offSpring[0] = (Solution) crossoverOperator_.execute(new Object[]{parents[0], parents});
                    offSpring[1] = (Solution) crossoverOperator_.execute(new Object[]{parents[1], parents});
                }
                mutationOperator_.execute(offSpring[0]);
                mutationOperator_.execute(offSpring[1]);
                problem_.evaluate(offSpring[0]);
                problem_.evaluateConstraints(offSpring[0]);
                problem_.evaluate(offSpring[1]);
                problem_.evaluateConstraints(offSpring[1]);
                offspringPopulation_.add(offSpring[0]);
                offspringPopulation_.add(offSpring[1]);
                evaluations_ += 2;
            }

            // 环境选择
            population_ = replacement(population_, offspringPopulation_);

            // 更新外部储备集
            MoeadUtils.updateExternalArchive(population_, populationSize_, external_archive_);

            // 显示当前储备集中的解
            if (isDisplay_) {
                PlotObjectives.plotSolutions("ISDEPlus", external_archive_);
            }
        }

        SqlUtils.InsertSolutionSet(dbName, tableName, external_archive_);
        return external_archive_;
    }

    /**
     * 环境选择
     *
     * @param population
     * @param offspringPopulation
     * @return
     * @throws JMException
     */
    protected SolutionSet replacement(SolutionSet population, SolutionSet offspringPopulation) throws JMException {

        // 合并子代种群和父代种群
        SolutionSet jointPopulation = population.union(offspringPopulation);

        // 非支配排序
        Ranking ranking = new Ranking(jointPopulation);

        // 逐层选择
        SolutionSet pop = new SolutionSet(populationSize_);
        List<SolutionSet> fronts = new ArrayList<>();
        int rankingIndex = 0;
        int candidateSolutions = 0;
        while (candidateSolutions < populationSize_) {
            // 获取第rankingIndex层个体
            SolutionSet solutions = ranking.getSubfront(rankingIndex);
            fronts.add(solutions);

            candidateSolutions += solutions.size();
            if (pop.size() + solutions.size() <= populationSize_) {
                for (int i = 0; i < solutions.size(); i++) {
                    pop.add(solutions.get(i));
                }
            }
            rankingIndex++;
        }

        pop = select(pop, fronts, populationSize_);
        return pop;
    }

    /**
     * 计算解集中每个个体的适应度值
     *
     * @param solutionSet
     */
    public void computeFitnessValue(SolutionSet solutionSet) {
        // FunctionValue为目标函数值 ，N为种群规模，M为目标函数的数量
        double[][] distance = solutionSet.writeObjectivesToMatrix();
        int numOfObjects = solutionSet.get(0).getNumberOfObjectives();

        // 最大最小归一化
        double[][] normalizedDistance = LinearNormalization.normalize4Scale(distance);

        // 按照个体目标值的和的升序排序
        double[] sumedDistance = LinearNormalization.sumByRow(normalizedDistance);
        int[] indexs = LinearNormalization.sortArray(sumedDistance);// 排序结果

        for (int i = 1; i < solutionSet.size(); i++) {

            // 计算第i个个体与前i-1个个体的SDE距离
            double[] betweenDistances = new double[i];
            for (int j = 0; j < i - 1; j++) {
                for (int k = 0; k < numOfObjects; k++) {
                    // 只计算qi<pj时的值
                    if (normalizedDistance[indexs[i]][k] < normalizedDistance[indexs[j]][k]) {
                        betweenDistances[j] += (normalizedDistance[indexs[i]][k] - normalizedDistance[indexs[j]][k]) * (normalizedDistance[indexs[i]][k] - normalizedDistance[indexs[j]][k]);
                    }
                }
            }

            // 求个体j与种群中其他个体的欧式距离的最小值
            double minDistance = LinearNormalization.minV(betweenDistances);
            solutionSet.get(indexs[i]).setFitness(Math.exp(-minDistance));
        }
    }

    /**
     * 从临界层中选择一定数量的个体
     *
     * @param pop
     * @param fronts
     * @param toSelect
     * @return
     */
    public SolutionSet select(SolutionSet pop, List<SolutionSet> fronts, int toSelect) {
        // 取出最后一层的解
        SolutionSet source2 = fronts.get(fronts.size() - 1);
        // 计算适应度值
        computeFitnessValue(source2);

        List<Solution> source = new ArrayList<>(source2.size());
        for (int i = 0; i < source2.size(); i++) {
            source.add(source2.get(i));
        }

        // 再按照适应度值排序选择剩余个体
        if (pop.size() < toSelect) {
            FitnessComparator comparator = new FitnessComparator();
            Collections.sort(source, comparator);
            int remain = toSelect - pop.size();
            for (int i = 0; i < remain; i++) {
                pop.add(source.get(i));
            }
            return pop;
        } else if (pop.size() == toSelect) {
            return pop;
        } else {
            return null;
        }
    }
}
