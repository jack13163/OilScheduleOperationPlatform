// ISDE+ - An Indicator for Multi and Many-objective Optimization.
package opt.easyjmetal.algorithm.moeas.impl;

import opt.easyjmetal.core.*;
import opt.easyjmetal.util.*;
import opt.easyjmetal.util.comparators.one.FitnessComparator;
import opt.easyjmetal.util.distance.Distance;
import opt.easyjmetal.util.fitness.ISDEPlus_Fitness;
import opt.easyjmetal.util.plot.PlotObjectives;
import opt.easyjmetal.util.ranking.impl.CDPRanking;
import opt.easyjmetal.util.solution.MoeadUtils;
import opt.easyjmetal.util.sqlite.SqlUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ISDEPlus extends Algorithm {

    public ISDEPlus(Problem problem) {
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
        int runningTime = (Integer) getInputParameter("runningTime");
        population_ = new SolutionSet(populationSize_);
        boolean isDisplay_ = (Boolean) getInputParameter("isDisplay");
        dataDirectory_ = getInputParameter("dataDirectory").toString();
        this.population_ = new SolutionSet(populationSize_);

        // 交叉选择算子
        Operator mutationOperator_ = (Operator) getInputParameter("mutation");
        Operator crossoverOperator_ = (Operator) getInputParameter("crossover");
        Operator selectionOperator_ = (Operator) getInputParameter("selection");

        // 创建数据表，方便后面保存结果
        String dbName = dataDirectory_ + problem_.getName();
        String tableName = "ISDEPlus_" + runningTime;
        SqlUtils.createTable(tableName, dbName);

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

        SqlUtils.insertSolutionSet(dbName, tableName, external_archive_);
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
        CDPRanking ranking = new CDPRanking(jointPopulation);

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
        ISDEPlus_Fitness.computeFitnessValue(source2);

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
