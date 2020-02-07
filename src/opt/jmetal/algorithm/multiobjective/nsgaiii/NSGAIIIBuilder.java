package opt.jmetal.algorithm.multiobjective.nsgaiii;

import opt.jmetal.operator.CrossoverOperator;
import opt.jmetal.operator.MutationOperator;
import opt.jmetal.operator.SelectionOperator;
import opt.jmetal.problem.Problem;
import opt.jmetal.solution.Solution;
import opt.jmetal.util.AlgorithmBuilder;
import opt.jmetal.util.evaluator.SolutionListEvaluator;
import opt.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;

import java.util.List;


/**
 * Builder class
 */
public class NSGAIIIBuilder<S extends Solution<?>> implements AlgorithmBuilder<NSGAIII<S>> {
    // no access modifier means access from classes within the same package
    private Problem<S> problem;
    private int maxIterations;
    private int populationSize;
    private CrossoverOperator<S> crossoverOperator;
    private MutationOperator<S> mutationOperator;
    private SelectionOperator<List<S>, S> selectionOperator;

    private SolutionListEvaluator<S> evaluator;

    /**
     * Builder constructor
     */
    public NSGAIIIBuilder(Problem<S> problem) {
        this.problem = problem;
        maxIterations = 250;
        populationSize = 100;
        evaluator = new SequentialSolutionListEvaluator<S>();
    }

    public NSGAIIIBuilder<S> setMaxIterations(int maxIterations) {
        this.maxIterations = maxIterations;

        return this;
    }

    public NSGAIIIBuilder<S> setPopulationSize(int populationSize) {
        this.populationSize = populationSize;

        return this;
    }

    public NSGAIIIBuilder<S> setCrossoverOperator(CrossoverOperator<S> crossoverOperator) {
        this.crossoverOperator = crossoverOperator;

        return this;
    }

    public NSGAIIIBuilder<S> setMutationOperator(MutationOperator<S> mutationOperator) {
        this.mutationOperator = mutationOperator;

        return this;
    }

    public NSGAIIIBuilder<S> setSelectionOperator(SelectionOperator<List<S>, S> selectionOperator) {
        this.selectionOperator = selectionOperator;

        return this;
    }

    public NSGAIIIBuilder<S> setSolutionListEvaluator(SolutionListEvaluator<S> evaluator) {
        this.evaluator = evaluator;

        return this;
    }

    public SolutionListEvaluator<S> getEvaluator() {
        return evaluator;
    }

    public Problem<S> getProblem() {
        return problem;
    }

    public int getMaxIterations() {
        return maxIterations;
    }

    public int getPopulationSize() {
        return populationSize;
    }

    public CrossoverOperator<S> getCrossoverOperator() {
        return crossoverOperator;
    }

    public MutationOperator<S> getMutationOperator() {
        return mutationOperator;
    }

    public SelectionOperator<List<S>, S> getSelectionOperator() {
        return selectionOperator;
    }

    public NSGAIII<S> build() {
        return new NSGAIII<>(this);
    }
}
