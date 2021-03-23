package opt.easyjmetal.algorithm.cmoeas.impl.modefy;

import opt.easyjmetal.core.*;
import opt.easyjmetal.util.JMException;
import opt.easyjmetal.util.MoeadUtils;
import opt.easyjmetal.util.comparators.FitnessComparator;
import opt.easyjmetal.util.fitness.CCMO_Fitness;
import opt.easyjmetal.util.fitness.ISDEPlus_Fitness;
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
                remainSolutions = remainSolutions.union(front);
                // 计算可行解的比例
                double feasibleRate = 1.0 * population_.getFeasible().size() / populationSize_;
                if(population_.size() == 0){
                    feasibleRate = 1.0 * front.getFeasible().size() / front.size();
                }

                // 获取当前层和下一层中的个体
                try {
                    int maxFrontsToBeSelected = generateRandomInteger(index, ranking.getNumberOfSubfronts(), feasibleRate);
                    for (int i = index; i < maxFrontsToBeSelected; i++) {
                        remainSolutions = remainSolutions.union(ranking.getSubfront(i));
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                // 根据可行解的比例进行非支配排序
                if (Math.random() < feasibleRate) {
                    // 可行解很多，则需要
                    System.out.println("Iteration: " + evaluations_ / populationSize_ + ", ignored: true   " + remain + "<----" + remainSolutions.size());
                    CCMO_Fitness.computeFitnessValue(remainSolutions, true);
                    remainSolutions.sort(new FitnessComparator());
                } else {
                    System.out.println("Iteration: " + evaluations_ / populationSize_ + ", ignored: false  " + remain + "<----" + remainSolutions.size());
                    //CCMO_Fitness.computeFitnessValue(remainSolutions, false);
                    ISDEPlus_Fitness.computeFitnessValue(remainSolutions);
                    remainSolutions.sort(new FitnessComparator());
                }

                // 将剩下的解添加到种群中
                for (int k = 0; k < remain; k++) {
                    population_.add(remainSolutions.get(k));
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
        if(feasibleRate == 1){
            return max;
        }
        return (int) (feasibleRate * (max - min + 1) + min);
    }
}
