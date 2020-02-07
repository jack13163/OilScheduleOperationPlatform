package opt.jmetal.algorithm.impl;

import opt.jmetal.operator.impl.crossover.DifferentialEvolutionCrossover;
import opt.jmetal.operator.impl.selection.DifferentialEvolutionSelection;
import opt.jmetal.solution.DoubleSolution;

/**
 * Abstract class representing differential evolution (DE) algorithms
 *
 * @author Antonio J. Nebro
 * @version 1.0
 */
@SuppressWarnings("serial")
public abstract class AbstractDifferentialEvolution<Result> extends AbstractEvolutionaryAlgorithm<DoubleSolution, Result> {
    protected DifferentialEvolutionCrossover crossoverOperator;
    protected DifferentialEvolutionSelection selectionOperator;
}
