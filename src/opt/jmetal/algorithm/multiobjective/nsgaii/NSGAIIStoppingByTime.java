package opt.jmetal.algorithm.multiobjective.nsgaii;

import opt.jmetal.operator.CrossoverOperator;
import opt.jmetal.operator.MutationOperator;
import opt.jmetal.operator.SelectionOperator;
import opt.jmetal.problem.Problem;
import opt.jmetal.solution.Solution;
import opt.jmetal.util.evaluator.SolutionListEvaluator;

import java.util.Comparator;
import java.util.List;

/**
 * This class shows a version of NSGA-II having a stopping condition depending on run-time
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
@SuppressWarnings("serial")
public class NSGAIIStoppingByTime<S extends Solution<?>> extends NSGAII<S> {
    private long initComputingTime;
    private long thresholdComputingTime;

    /**
     * Constructor
     */
    public NSGAIIStoppingByTime(Problem<S> problem, int populationSize,
                                long maxComputingTime, int matingPoolSize, int offspringPopulationSize,
                                CrossoverOperator<S> crossoverOperator, MutationOperator<S> mutationOperator,
                                SelectionOperator<List<S>, S> selectionOperator, Comparator<S> dominanceComparator,
                                SolutionListEvaluator<S> evaluator) {
        super(problem, 0, populationSize, matingPoolSize, offspringPopulationSize,
                crossoverOperator, mutationOperator,
                selectionOperator, dominanceComparator, evaluator);

        initComputingTime = System.currentTimeMillis();
        thresholdComputingTime = maxComputingTime;
    }

    @Override
    protected boolean isStoppingConditionReached() {
        long currentComputingTime = System.currentTimeMillis() - initComputingTime;
        return currentComputingTime > thresholdComputingTime;
    }
}
