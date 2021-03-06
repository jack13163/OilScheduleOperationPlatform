package opt.jmetal.algorithm.multiobjective.spea2;

import opt.jmetal.operator.CrossoverOperator;
import opt.jmetal.operator.MutationOperator;
import opt.jmetal.operator.SelectionOperator;
import opt.jmetal.operator.impl.selection.BinaryTournamentSelection;
import opt.jmetal.problem.Problem;
import opt.jmetal.solution.Solution;
import opt.jmetal.util.AlgorithmBuilder;
import opt.jmetal.util.JMetalException;
import opt.jmetal.util.evaluator.SolutionListEvaluator;
import opt.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;

import java.util.List;

/**
 * @author Juan J. Durillo
 */
public class SPEA2Builder<S extends Solution<?>> implements AlgorithmBuilder<SPEA2<S>> {
    /**
     * SPEA2Builder class
     */
    protected final Problem<S> problem;
    protected int maxIterations;
    protected int populationSize;
    protected CrossoverOperator<S> crossoverOperator;
    protected MutationOperator<S> mutationOperator;
    protected SelectionOperator<List<S>, S> selectionOperator;
    protected SolutionListEvaluator<S> evaluator;
    protected int k;

    /**
     * SPEA2Builder constructor
     */
    public SPEA2Builder(Problem<S> problem, CrossoverOperator<S> crossoverOperator,
                        MutationOperator<S> mutationOperator) {
        this.problem = problem;
        maxIterations = 250;
        populationSize = 100;
        this.crossoverOperator = crossoverOperator;
        this.mutationOperator = mutationOperator;
        selectionOperator = new BinaryTournamentSelection<S>();
        evaluator = new SequentialSolutionListEvaluator<S>();
        k = 1;
    }

    public SPEA2Builder<S> setMaxIterations(int maxIterations) {
        if (maxIterations < 0) {
            throw new JMetalException("maxIterations is negative: " + maxIterations);
        }
        this.maxIterations = maxIterations;

        return this;
    }

    public SPEA2Builder<S> setPopulationSize(int populationSize) {
        if (populationSize < 0) {
            throw new JMetalException("Population size is negative: " + populationSize);
        }

        this.populationSize = populationSize;

        return this;
    }

    public SPEA2Builder<S> setSelectionOperator(SelectionOperator<List<S>, S> selectionOperator) {
        if (selectionOperator == null) {
            throw new JMetalException("selectionOperator is null");
        }
        this.selectionOperator = selectionOperator;

        return this;
    }

    public SPEA2Builder<S> setSolutionListEvaluator(SolutionListEvaluator<S> evaluator) {
        if (evaluator == null) {
            throw new JMetalException("evaluator is null");
        }
        this.evaluator = evaluator;

        return this;
    }

    public SPEA2Builder<S> setK(int k) {
        this.k = k;

        return this;
    }

    public SPEA2<S> build() {
        SPEA2<S> algorithm = null;
        algorithm = new SPEA2<S>(problem, maxIterations, populationSize, crossoverOperator,
                mutationOperator, selectionOperator, evaluator, k);

        return algorithm;
    }

    /* Getters */
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

    public SolutionListEvaluator<S> getSolutionListEvaluator() {
        return evaluator;
    }
}
