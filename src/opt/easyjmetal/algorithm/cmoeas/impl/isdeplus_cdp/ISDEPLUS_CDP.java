// ISDE+ - An Indicator for Multi and Many-objective Optimization.
package opt.easyjmetal.algorithm.cmoeas.impl.isdeplus_cdp;

import opt.easyjmetal.util.MoeadUtils;
import opt.easyjmetal.core.*;
import opt.easyjmetal.util.Distance;
import opt.easyjmetal.util.JMException;
import opt.easyjmetal.util.ranking.Ranking;
import opt.easyjmetal.util.sqlite.SqlUtils;

import java.util.ArrayList;
import java.util.List;


public class ISDEPLUS_CDP extends Algorithm {

    public ISDEPLUS_CDP(Problem problem) {
        super(problem);
    }

    private SolutionSet population_;
    private SolutionSet external_archive_;
    private int populationSize_;
    private int maxEvaluations_;
    private String dataDirectory_;

    Distance distance;

    private int iterations;
    private SolutionSet archive;

    /**
     * Runs the SPEA2 algorithm.
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

        //Initialize the variables
        population_ = new SolutionSet(populationSize_);
        int evaluations_ = 0;

        //Read the operators
        Operator mutationOperator_ = operators_.get("mutation");
        Operator crossoverOperator_ = operators_.get("crossover");
        Operator selectionOperator_ = operators_.get("selection");

        // Create the initial solutionSet
        for (int i = 0; i < populationSize_; i++) {
            Solution newSolution = new Solution(problem_);
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
        String tableName =  "ISDEPLUS_CDP_" + runningTime;
        String dbName = dataDirectory_;
        SqlUtils.createTable(tableName, dbName);

        int gen = 0;
        // Generations
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

            // 环境选择
            population_ = replacement(population_, offspringPopulation_);

            MoeadUtils.updateExternalArchive(population_, populationSize_, external_archive_);

            if (gen % 50 == 0) {
                allPop = allPop.union(population_);
            }
            gen++;
        } // while

        SqlUtils.insertSolutionSet(dbName, tableName, external_archive_);

        return external_archive_;
    } // execute

    // 环境选择
    protected SolutionSet replacement(SolutionSet population, SolutionSet offspringPopulation) throws JMException {

        // Create the solutionSet union of solutionSet and offSpring
        SolutionSet jointPopulation = population_.union(offspringPopulation);

        // Ranking the union
        Ranking ranking = new Ranking(jointPopulation);

        // List<Solution> pop = crowdingDistanceSelection(ranking);
        SolutionSet pop = new SolutionSet(populationSize_);
        List<SolutionSet> fronts = new ArrayList<>();
        int rankingIndex = 0;
        int candidateSolutions = 0;
        while (candidateSolutions < populationSize_) {

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

        // Environmental selection
        // A copy of the reference list should be used as parameter of the environmental selection
        EnvironmentalSelection selection = new EnvironmentalSelection(populationSize_);
        pop = selection.execute(pop, fronts);

        return pop;
    }
}
