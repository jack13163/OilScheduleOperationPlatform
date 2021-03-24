package opt.easyjmetal.algorithm.cmoeas.impl.modefy;

import opt.easyjmetal.algorithm.common.UtilityFunctions;
import opt.easyjmetal.core.*;
import opt.easyjmetal.util.JMException;
import opt.easyjmetal.util.comparators.line.CrowdingDistanceComparator;
import opt.easyjmetal.util.distance.Distance;
import opt.easyjmetal.util.ranking.AbstractRanking;
import opt.easyjmetal.util.ranking.impl.RankingByCDP;
import opt.easyjmetal.util.ranking.impl.RankingByContribution;
import opt.easyjmetal.util.solution.MoeadUtils;
import opt.easyjmetal.util.sqlite.SqlUtils;

public class NSGAII_CDP_Contribution extends Algorithm {

    private SolutionSet population_;
    private SolutionSet external_archive_;
    private String dataDirectory_;

    public NSGAII_CDP_Contribution(Problem problem) {
        super(problem);
    }

    @Override
    public SolutionSet execute() throws JMException, ClassNotFoundException {

        int runningTime = (Integer) getInputParameter("runningTime") + 1;
        int populationSize_ = (Integer) getInputParameter("populationSize");
        int maxEvaluations_ = (Integer) getInputParameter("maxEvaluations");
        dataDirectory_ = getInputParameter("dataDirectory").toString();
        population_ = new SolutionSet(populationSize_);
        int evaluations_ = 0;
        Operator mutationOperator_ = operators_.get("mutation");
        Operator crossoverOperator_ = operators_.get("crossover");
        Operator selectionOperator_ = operators_.get("selection");

        // Create the initial solutionSet
        Solution newSolution;
        for (int i = 0; i < populationSize_; i++) {
            newSolution = new Solution(problem_);
            problem_.evaluate(newSolution);
            problem_.evaluateConstraints(newSolution);
            evaluations_++;
            population_.add(newSolution);
        }
        SolutionSet allPop = population_;

        // Initialize the external archive
        external_archive_ = new SolutionSet(populationSize_);
        MoeadUtils.initializeExternalArchive(population_, populationSize_, external_archive_);

        // creat database
        String dbName = dataDirectory_;
        String tableName = "NSGAII_CDP_Contribution_" + runningTime;
        SqlUtils.createTable(tableName, dbName);
        SqlUtils.clearTable(tableName, dbName);

        int gen = 0;
        while (evaluations_ < maxEvaluations_) {
            SolutionSet offspringPopulation_ = new SolutionSet(populationSize_);
            for (int i = 0; i < (populationSize_ / 2); i++) {
                Solution[] offSpring = UtilityFunctions.generateOffsprings(population_, population_, mutationOperator_, crossoverOperator_, selectionOperator_);
                offspringPopulation_.add(offSpring[0]);
                offspringPopulation_.add(offSpring[1]);
                evaluations_ += 2;
            }

            // 重新评价适应度
            for (int i = 0; i < offspringPopulation_.size(); i++) {
                Solution solution = offspringPopulation_.get(i);
                problem_.evaluate(solution);
                problem_.evaluateConstraints(solution);
            }

            // 合并种群
            SolutionSet union_ = population_.union(offspringPopulation_);
            population_.clear();

            // 计算退火比例（下降）1-exp(-8*x)
            double lamb = 7.0;
            double iterationRate = 1.0 - Math.exp(-1.0 * lamb * evaluations_ / maxEvaluations_);
            AbstractRanking ranking;

            // 根据比例进行非支配排序
            if (Math.random() < iterationRate) {
                System.out.println("Iteration: " + evaluations_ / populationSize_ + ", Contribution");
                ranking = new RankingByContribution(union_);
            } else {
                System.out.println("Iteration: " + evaluations_ / populationSize_ + ", CDP");
                ranking = new RankingByCDP(union_);
            }

            int remain = populationSize_;
            int index = 0;
            SolutionSet front = ranking.getSubfront(index);
            while ((remain > 0) && (remain >= front.size())) {
                // Add the individuals of this front
                for (int k = 0; k < front.size(); k++) {
                    population_.add(front.get(k));
                }

                remain = remain - front.size();
                index++;
                if (remain > 0) {
                    front = ranking.getSubfront(index);
                }
            }

            // Remain is less than front(index).size, insert only the best one
            if (remain > 0) {
                Distance.crowdingDistanceAssignment(front, problem_.getNumberOfObjectives());
                front.sort(new CrowdingDistanceComparator());

                // 将剩下的解添加到种群中
                for (int k = 0; k < remain; k++) {
                    population_.add(front.get(k));
                }
            }

            MoeadUtils.updateExternalArchive(population_, populationSize_, external_archive_);

            if (gen % 50 == 0) {
                allPop = allPop.union(population_);
            }
            gen++;
        }

        SqlUtils.insertSolutionSet(dbName, tableName, external_archive_);
        return external_archive_;
    }
}
