// SPEA2: Improving the Strength Pareto Evolutionary Algorithm For Multiobjective Optimization.
package opt.easyjmetal.algorithm.cmoeas.impl.spea2_cdp;

import opt.easyjmetal.core.*;
import opt.easyjmetal.util.JMException;
import opt.easyjmetal.util.MoeadUtils;
import opt.easyjmetal.util.sqlite.SqlUtils;

public class SPEA2_CDP extends Algorithm {

    public SPEA2_CDP(Problem problem) {
        super(problem);
    }

    private SolutionSet external_archive_;
    private int populationSize_;
    private int maxEvaluations_;
    private String dataDirectory_;

    @Override
    public SolutionSet execute() throws JMException, ClassNotFoundException {
        int runningTime = (Integer) getInputParameter("runningTime") + 1;

        //Read the parameters
        populationSize_ = (Integer) getInputParameter("populationSize");
        maxEvaluations_ = (Integer) getInputParameter("maxEvaluations");
        dataDirectory_ = getInputParameter("dataDirectory").toString();

        //Initialize the variables
        SolutionSet population_ = new SolutionSet(populationSize_);
        int evaluations_ = 0;

        //Read the operators
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
        } //for

        SolutionSet allPop = population_;

        // Initialize the external archive
        external_archive_ = new SolutionSet(populationSize_);
        MoeadUtils.initializeExternalArchive(population_, populationSize_, external_archive_);

        //creat database
        String dbName = dataDirectory_;
        String tableName = "SPEA2_CDP_" + runningTime;
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
        }

        SqlUtils.insertSolutionSet(dbName, tableName, external_archive_);

        return external_archive_;
    }

    // 环境选择
    protected SolutionSet replacement(SolutionSet population, SolutionSet offspringPopulation) throws JMException {

        // Create the solutionSet union of solutionSet and offSpring
        SolutionSet jointPopulation = population.union(offspringPopulation);

        // Environmental selection
        EnvironmentalSelection selection = new EnvironmentalSelection(populationSize_);
        population = selection.execute(jointPopulation);

        return population;
    }
}
