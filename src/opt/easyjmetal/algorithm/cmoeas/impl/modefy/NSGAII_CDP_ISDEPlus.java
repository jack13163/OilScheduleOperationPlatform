package opt.easyjmetal.algorithm.cmoeas.impl.modefy;

import opt.easyjmetal.core.*;
import opt.easyjmetal.qualityindicator.util.MetricsUtil;
import opt.easyjmetal.util.Distance;
import opt.easyjmetal.util.JMException;
import opt.easyjmetal.util.MoeadUtils;
import opt.easyjmetal.util.comparators.DistanceComparator;
import opt.easyjmetal.util.ranking.Ranking;
import opt.easyjmetal.util.sqlite.SqlUtils;

/**
 * 改进传统的NSGA-II
 */
public class NSGAII_CDP_ISDEPlus extends Algorithm {

    public NSGAII_CDP_ISDEPlus(Problem problem) {
        super(problem);
    }

    private SolutionSet population_;
    private SolutionSet external_archive_;
    private String dataDirectory_;

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

        //creat database
        String dbName = dataDirectory_;
        String tableName = "NSGAII_CDP_ISDEPlus_" + runningTime;
        SqlUtils.CreateTable(tableName, dbName);

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

            // Ranking the union
            SolutionSet union_ = population_.union(offspringPopulation_);
            Ranking ranking = new Ranking(union_);

            int remain = populationSize_;
            int index = 0;
            SolutionSet front;
            population_.clear();

            // Obtain the next front
            front = ranking.getSubfront(index);
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
                SolutionSet remainSolutions = new SolutionSet();
                // 计算退火比例（下降）1-exp(-8*x)
                double lamb = 8.0;
                double iterationRate = 1.0 - Math.exp(-1.0 * lamb * evaluations_ / maxEvaluations_);

                // 获取当前层和下一层中的个体
                // Math.min(index + 1, ranking.getNumberOfSubfronts() - 1);
                int maxFrontsToBeSelected = index;
                for (int i = index; i <= maxFrontsToBeSelected; i++) {
                    remainSolutions = remainSolutions.union(ranking.getSubfront(i));
                }

                // 根据比例进行非支配排序
                if (Math.random() < iterationRate) {
                    System.out.println("Iteration: " + evaluations_ / populationSize_ + ", ignored: true   " + remain + "<----" + remainSolutions.size());
//                    CCMO_Fitness.computeFitnessValue(remainSolutions, false);
//                    remainSolutions.sort(new FitnessComparator());

                    actualiseHVContribution(remainSolutions, problem_.getNumberOfObjectives());

                    // 将剩下的解添加到种群中
                    for (int k = 0; k < remain; k++) {
                        population_.add(remainSolutions.get(k));
                    }
                } else {
                    System.out.println("Iteration: " + evaluations_ / populationSize_ + ", ignored: false  " + remain + "<----" + remainSolutions.size());
                    new Distance().crowdingDistanceAssignment(remainSolutions, problem_.getNumberOfObjectives());
                    remainSolutions.sort(new DistanceComparator());

                    // 将剩下的解添加到种群中
                    for (int k = 0; k < remain; k++) {
                        population_.add(remainSolutions.get(k));
                    }
                }
            }

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
     * 生成一个指定范围内的整数，输入为0~1之间的数
     *
     * @param min
     * @param max
     * @param feasibleRate
     * @return
     */
    private int generateRandomInteger(int min, int max, double feasibleRate) {
        if (feasibleRate == 1) {
            return max;
        }
        return (int) (feasibleRate * (max - min + 1) + min);
    }


    /**
     * 计算解集集合中每个个体对HV指标的贡献
     */
    public void actualiseHVContribution(SolutionSet solutionSet, int numberOfObjectives) {
        if (solutionSet.size() > 2) {
            double offset_ = 100;
            MetricsUtil utils_ = new MetricsUtil();
            // The contribution can be updated
            double[][] frontValues = solutionSet.writeObjectivesToMatrix();
            // STEP 1. Obtain the maximum and minimum values of the Pareto front
            double[] maximumValues = utils_.getMaximumValues(frontValues, numberOfObjectives);
            double[] minimumValues = utils_.getMinimumValues(frontValues, numberOfObjectives);
            // STEP 2. Get the normalized front
            double[][] normalizedFront = utils_.getNormalizedFront(frontValues, maximumValues, minimumValues);
            // compute offsets for reference point in normalized space
            double[] offsets = new double[maximumValues.length];
            for (int i = 0; i < maximumValues.length; i++) {
                offsets[i] = offset_ / (maximumValues[i] - minimumValues[i]);
            }
            // STEP 3. Inverse the pareto front. This is needed because the original metric by Zitzler is for maximization problems
            double[][] invertedFront = utils_.invertedFront(normalizedFront);
            // shift away from origin, so that boundary points also get a contribution > 0
            for (double[] point : invertedFront) {
                for (int i = 0; i < point.length; i++) {
                    point[i] += offsets[i];
                }
            }

            // calculate contributions and sort
            double[] contributions = utils_.hvContributions(numberOfObjectives, invertedFront);
            for (int i = 0; i < contributions.length; i++) {
                // contribution values are used analogously to crowding distance
                solutionSet.get(i).setCrowdingDistance(contributions[i]);
            }
        }
    }
}
