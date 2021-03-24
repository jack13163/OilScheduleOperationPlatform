package opt.easyjmetal.algorithm.cmoeas.impl.modefy;

import opt.easyjmetal.core.*;
import opt.easyjmetal.util.distance.Distance;
import opt.easyjmetal.util.JMException;
import opt.easyjmetal.util.MoeadUtils;
import opt.easyjmetal.util.comparators.one.DistanceComparator;
import opt.easyjmetal.util.ranking.AbstractRanking;
import opt.easyjmetal.util.ranking.impl.CDPRanking;
import opt.easyjmetal.util.ranking.impl.Ranking_M_Add_One;
import opt.easyjmetal.util.sqlite.SqlUtils;

public class NSGAII_CDP_ManyAddOne extends Algorithm {

    private SolutionSet population_;
    private SolutionSet external_archive_;
    private String dataDirectory_;

    /**
     * Constructor
     *
     * @param problem The problem to be solved
     */
    public NSGAII_CDP_ManyAddOne(Problem problem) {
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
        String tableName = "NSGAII_CDP_ISDEPlus_" + runningTime;
        SqlUtils.createTable(tableName, dbName);
        SqlUtils.clearTable(tableName, dbName);

        int gen = 0;
        while (evaluations_ < maxEvaluations_) {
            // Create the offSpring solutionSet
            SolutionSet offspringPopulation_ = new SolutionSet(populationSize_);
            for (int i = 0; i < (populationSize_ / 2); i++) {
                //obtain parents
                Solution[] offSpring = new Solution[2];
                // Apply Crossover for Real codification
                if (crossoverOperator_.getClass().getSimpleName().equalsIgnoreCase("SBXCrossover")) {
                    Solution[] parents = new Solution[2];
                    parents[0] = (Solution) selectionOperator_.execute(population_);
                    parents[1] = (Solution) selectionOperator_.execute(population_);
                    offSpring = ((Solution[]) crossoverOperator_.execute(parents));
                }
                // Apply DE crossover
                else if (crossoverOperator_.getClass().getSimpleName().equalsIgnoreCase("DifferentialEvolutionCrossover")) {
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

            // 合并种群
            SolutionSet union_ = population_.union(offspringPopulation_);
            population_.clear();

            // 计算退火比例（下降）1-exp(-8*x)
            double lamb = 8.0;
            double iterationRate = 1.0 - Math.exp(-1.0 * lamb * evaluations_ / maxEvaluations_);
            AbstractRanking ranking = null;

            // 根据比例进行非支配排序
            if (Math.random() < iterationRate) {
                System.out.println("Iteration: " + evaluations_ / populationSize_ + ", ManyAddOne");
                ranking = new Ranking_M_Add_One(union_);
            } else {
                System.out.println("Iteration: " + evaluations_ / populationSize_ + ", CDP");
                ranking = new CDPRanking(union_);
            }
            ranking.ranking();

            int remain = populationSize_;
            int index = 0;
            // Obtain the next front
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
                new Distance().crowdingDistanceAssignment(front, problem_.getNumberOfObjectives());
                front.sort(new DistanceComparator());

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
