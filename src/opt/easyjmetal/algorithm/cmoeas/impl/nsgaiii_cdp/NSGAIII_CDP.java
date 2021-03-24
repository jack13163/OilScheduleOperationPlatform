// An Evolutionary Many-Objective Optimization Algorithm Using Reference-point
// Based Non-dominated Sorting Approach,
// Part I: Solving Problems with Box Constraints.
package opt.easyjmetal.algorithm.cmoeas.impl.nsgaiii_cdp;

import opt.easyjmetal.algorithm.common.ReferencePoint;
import opt.easyjmetal.algorithm.common.UtilityFunctions;
import opt.easyjmetal.core.*;
import opt.easyjmetal.util.distance.Distance;
import opt.easyjmetal.util.JMException;
import opt.easyjmetal.util.MoeadUtils;
import opt.easyjmetal.util.ranking.impl.CDPRanking;
import opt.easyjmetal.util.sqlite.SqlUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class NSGAIII_CDP extends Algorithm {

    public NSGAIII_CDP(Problem problem) {
        super(problem);
    }

    private SolutionSet population_;
    private SolutionSet external_archive_;
    private int populationSize_;
    private int maxEvaluations_;
    private String dataDirectory_;
    private String weightDirectory_;

    Distance distance;

    // 参考点
    private double[][] lambda_;
    protected List<ReferencePoint> referencePoints = new Vector<>();

    /**
     * Runs the NSGA-II algorithm.
     *
     * @return a <code>SolutionSet</code> that is a set of non dominated solutions
     * as a result of the algorithm execution
     * @throws JMException
     */
    @Override
    public SolutionSet execute() throws JMException, ClassNotFoundException {

        distance = new Distance();
        int runningTime = (Integer) getInputParameter("runningTime") + 1;

        //Read the parameters
        populationSize_ = (Integer) getInputParameter("populationSize");
        maxEvaluations_ = (Integer) getInputParameter("maxEvaluations");
        dataDirectory_ = getInputParameter("dataDirectory").toString();
        weightDirectory_ = getInputParameter("weightDirectory").toString();
        lambda_ = new double[populationSize_][problem_.getNumberOfObjectives()];

        //Initialize the variables
        population_ = new SolutionSet(populationSize_);
        int evaluations_ = 0;

        //Read the operators
        Operator mutationOperator_ = operators_.get("mutation");
        Operator crossoverOperator_ = operators_.get("crossover");
        Operator selectionOperator_ = operators_.get("selection");


        // STEP 1. Initialization
        UtilityFunctions.initUniformWeight(weightDirectory_, lambda_);

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
        String tableName = "NSGAIII_CDP_" + runningTime;
        SqlUtils.createTable(tableName, dbName);

        int gen = 0;
        // Generations
        while (evaluations_ < maxEvaluations_) {
            SolutionSet offspringPopulation_ = new SolutionSet(populationSize_);
            for (int i = 0; i < (populationSize_ / 2); i++) {
                // obtain parents
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

            // 环境选择
            population_ = replacement(population_, offspringPopulation_);

            MoeadUtils.updateExternalArchive(population_, populationSize_, external_archive_);

            if (gen % 50 == 0) {
                allPop = allPop.union(population_);
            }
            gen++;
        }

        SqlUtils.insertSolutionSet(dbName, tableName, external_archive_);

        return external_archive_;
    }

    // 环境选择
    protected SolutionSet replacement(SolutionSet population, SolutionSet offspringPopulation) throws JMException {

        // Create the solutionSet union of solutionSet and offSpring
        SolutionSet jointPopulation = population_.union(offspringPopulation);

        // CDPRanking the union
        CDPRanking ranking = new CDPRanking(jointPopulation);

        // List<Solution> pop = crowdingDistanceSelection(ranking);
        SolutionSet pop = new SolutionSet(populationSize_);
        List<SolutionSet> fronts = new ArrayList<>();
        int rankingIndex = 0;
        int candidateSolutions = 0;
        while (candidateSolutions < populationSize_) {

            SolutionSet solutions = ranking.getSubfront(rankingIndex);
            fronts.add(solutions);

            candidateSolutions += ranking.getSubfront(rankingIndex).size();
            if ((pop.size() + ranking.getSubfront(rankingIndex).size()) <= populationSize_) {

                for (int i = 0; i < solutions.size(); i++) {
                    pop.add(solutions.get(i));
                }
            }
            rankingIndex++;
        }

        // 环境选择
        EnvironmentalSelection selection =
                new EnvironmentalSelection(
                        fronts,
                        populationSize_,
                        ReferencePoint.generateReferencePoints(lambda_),
                        problem_.getNumberOfObjectives());
        pop = selection.execute(pop);

        return pop;
    }
}

