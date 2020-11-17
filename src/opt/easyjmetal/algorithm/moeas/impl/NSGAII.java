package opt.easyjmetal.algorithm.moeas.impl;

import opt.easyjmetal.algorithm.cmoeas.util.Utils;
import opt.easyjmetal.core.*;
import opt.easyjmetal.operator.crossover.CrossoverFactory;
import opt.easyjmetal.operator.mutation.MutationFactory;
import opt.easyjmetal.operator.selection.SelectionFactory;
import opt.easyjmetal.util.Distance;
import opt.easyjmetal.util.JMException;
import opt.easyjmetal.util.Ranking;
import opt.easyjmetal.util.comparators.CrowdingComparator;
import opt.easyjmetal.util.sqlite.SqlUtils;

import java.util.HashMap;

public class NSGAII extends Algorithm {

    public NSGAII(Problem problem) {
        super(problem);
    }

    private SolutionSet population_;
    private SolutionSet external_archive_;

    @Override
    public SolutionSet execute() throws JMException, ClassNotFoundException {

        int runningTime = (Integer) getInputParameter("runningTime");
        int populationSize_ = (Integer) getInputParameter("populationSize");
        int maxEvaluations_ = (Integer) getInputParameter("maxEvaluations");
        String dbName = getInputParameter("DBName").toString();
        Operator mutationOperator_ = MutationFactory.getMutationOperator("PolynomialMutation", new HashMap(){{
            put("probability", 1.0 / problem_.getNumberOfVariables());// 变异概率
            put("distributionIndex", 20.0);
        }});
        Operator crossoverOperator_ = CrossoverFactory.getCrossoverOperator("SBXCrossover", new HashMap<String, Double>(){{
            put("probability", 1.0);
            put("distributionIndex", 20.0);
        }});
        Operator selectionOperator_ = SelectionFactory.getSelectionOperator("BinaryTournament2", null);// 选择算子

        // 生成初始种群
        population_ = new SolutionSet(populationSize_);
        int evaluations_ = 0;
        for (int i = 0; i < populationSize_; i++) {
            Solution newSolution = new Solution(problem_);
            problem_.evaluate(newSolution);
            problem_.evaluateConstraints(newSolution);
            evaluations_++;
            population_.add(newSolution);
        }

        SolutionSet allPop = population_;

        // 初始化外部储备集
        external_archive_ = new SolutionSet(populationSize_);
        Utils.initializeExternalArchive(population_, populationSize_, external_archive_);

        // 创建数据表，用来保存结果
        String tableName = "NSGAII_" + runningTime;
        SqlUtils.CreateTable(tableName, dbName);

        int gen = 0;
        while (evaluations_ < maxEvaluations_) {
            // 生成子代个体
            SolutionSet offspringPopulation_ = new SolutionSet(populationSize_);
            for (int i = 0; i < (populationSize_ / 2); i++) {
                Solution[] offSpring = new Solution[2];
                // 交叉操作
                if (crossoverOperator_.getClass().getSimpleName().equalsIgnoreCase("SBXCrossover")) {
                    Solution[] parents = new Solution[2];
                    parents[0] = (Solution) selectionOperator_.execute(population_);
                    parents[1] = (Solution) selectionOperator_.execute(population_);
                    offSpring = ((Solution[]) crossoverOperator_.execute(parents));
                } else if (crossoverOperator_.getClass().getSimpleName().equalsIgnoreCase("DifferentialEvolutionCrossover")) {
                    Solution[] parents = new Solution[3];
                    parents[0] = (Solution) selectionOperator_.execute(population_);
                    parents[1] = (Solution) selectionOperator_.execute(population_);
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
                offspringPopulation_.add(offSpring[0]);
                offspringPopulation_.add(offSpring[1]);
                evaluations_ += 2;
            }

            // 合并父代种群和子代种群
            SolutionSet union_ = population_.union(offspringPopulation_);

            // 将合并后的种群进行快速非支配排序
            Ranking ranking = new Ranking(union_);

            int remain = populationSize_;
            int index = 0;
            SolutionSet front;
            population_.clear();

            // Obtain the next front
            front = ranking.getSubfront(index);
            Distance distance = new Distance();

            while ((remain > 0) && (remain >= front.size())) {
                //Assign crowding distance to individuals
                distance.crowdingDistanceAssignment(front, problem_.getNumberOfObjectives());
                //Add the individuals of this front
                for (int k = 0; k < front.size(); k++) {
                    population_.add(front.get(k));
                } // for

                //Decrement remain
                remain = remain - front.size();

                //Obtain the next front
                index++;
                if (remain > 0) {
                    front = ranking.getSubfront(index);
                }
            }

            // Remain is less than front(index).size, insert only the best one
            if (remain > 0) {  // front contains individuals to insert
                distance.crowdingDistanceAssignment(front, problem_.getNumberOfObjectives());
                front.sort(new CrowdingComparator());
                for (int k = 0; k < remain; k++) {
                    population_.add(front.get(k));
                }
            }

            Utils.updateExternalArchive(population_, populationSize_, external_archive_);

            if (gen % 50 == 0) {
                allPop = allPop.union(population_);
            }
            gen++;
        }

        SqlUtils.InsertSolutionSet(dbName, tableName, external_archive_);

        return external_archive_;
    }
}