package opt.jmetal.algorithm.singleobjective.geneticalgorithm;

import opt.jmetal.algorithm.Algorithm;
import opt.jmetal.operator.CrossoverOperator;
import opt.jmetal.operator.MutationOperator;
import opt.jmetal.operator.SelectionOperator;
import opt.jmetal.operator.impl.selection.BinaryTournamentSelection;
import opt.jmetal.problem.Problem;
import opt.jmetal.solution.Solution;
import opt.jmetal.util.JMetalException;
import opt.jmetal.util.evaluator.SolutionListEvaluator;
import opt.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;

import java.util.List;

/**
 * Created by ajnebro on 10/12/14.
 */
public class GeneticAlgorithmBuilder<S extends Solution<?>> {
    public enum GeneticAlgorithmVariant {GENERATIONAL, STEADY_STATE}

    /**
     * Builder class
     */
    private Problem<S> problem;
    private int maxEvaluations;
    private int populationSize;
    private CrossoverOperator<S> crossoverOperator;
    private MutationOperator<S> mutationOperator;
    private SelectionOperator<List<S>, S> selectionOperator;
    private SolutionListEvaluator<S> evaluator;

    private GeneticAlgorithmVariant variant;
    private SelectionOperator<List<S>, S> defaultSelectionOperator = new BinaryTournamentSelection<S>();

    /**
     * Builder constructor
     */
    public GeneticAlgorithmBuilder(Problem<S> problem,
                                   CrossoverOperator<S> crossoverOperator,
                                   MutationOperator<S> mutationOperator) {
        this.problem = problem;
        maxEvaluations = 25000;
        populationSize = 100;
        this.mutationOperator = mutationOperator;
        this.crossoverOperator = crossoverOperator;
        this.selectionOperator = defaultSelectionOperator;

        evaluator = new SequentialSolutionListEvaluator<S>();

        this.variant = GeneticAlgorithmVariant.GENERATIONAL;
    }

    public GeneticAlgorithmBuilder<S> setMaxEvaluations(int maxEvaluations) {
        this.maxEvaluations = maxEvaluations;

        return this;
    }

    public GeneticAlgorithmBuilder<S> setPopulationSize(int populationSize) {
        this.populationSize = populationSize;

        return this;
    }

    public GeneticAlgorithmBuilder<S> setSelectionOperator(SelectionOperator<List<S>, S> selectionOperator) {
        this.selectionOperator = selectionOperator;

        return this;
    }

    public GeneticAlgorithmBuilder<S> setSolutionListEvaluator(SolutionListEvaluator<S> evaluator) {
        this.evaluator = evaluator;

        return this;
    }

    public GeneticAlgorithmBuilder<S> setVariant(GeneticAlgorithmVariant variant) {
        this.variant = variant;

        return this;
    }

    public Algorithm<S> build() {
        if (variant == GeneticAlgorithmVariant.GENERATIONAL) {
            return new GenerationalGeneticAlgorithm<S>(problem, maxEvaluations, populationSize,
                    crossoverOperator, mutationOperator, selectionOperator, evaluator);
        } else if (variant == GeneticAlgorithmVariant.STEADY_STATE) {
            return new SteadyStateGeneticAlgorithm<S>(problem, maxEvaluations, populationSize,
                    crossoverOperator, mutationOperator, selectionOperator);
        } else {
            throw new JMetalException("Unknown variant: " + variant);
        }
    }

    /*
     * Getters
     */
    public Problem<S> getProblem() {
        return problem;
    }

    public int getMaxEvaluations() {
        return maxEvaluations;
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

    public SolutionListEvaluator<S> getEvaluator() {
        return evaluator;
    }

    public GeneticAlgorithmVariant getVariant() {
        return variant;
    }
}
