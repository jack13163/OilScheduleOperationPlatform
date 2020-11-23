package opt.jmetal.algorithm.multiobjective.ibea;

import opt.jmetal.operator.CrossoverOperator;
import opt.jmetal.operator.MutationOperator;
import opt.jmetal.operator.SelectionOperator;
import opt.jmetal.operator.impl.crossover.SBXCrossover;
import opt.jmetal.operator.impl.mutation.PolynomialMutation;
import opt.jmetal.operator.impl.selection.BinaryTournamentSelection;
import opt.jmetal.problem.Problem;
import opt.jmetal.solution.DoubleSolution;
import opt.jmetal.util.AlgorithmBuilder;

import java.util.List;

/**
 * This class implements the IBEA algorithm
 */
public class IBEABuilder implements AlgorithmBuilder<IBEA<DoubleSolution>> {
    private Problem<DoubleSolution> problem;
    private int populationSize;
    private int archiveSize;
    private int maxEvaluations;

    private CrossoverOperator<DoubleSolution> crossover;
    private MutationOperator<DoubleSolution> mutation;
    private SelectionOperator<List<DoubleSolution>, DoubleSolution> selection;

    public IBEABuilder(Problem<DoubleSolution> problem) {
        this.problem = problem;
        populationSize = 100;
        archiveSize = 100;
        maxEvaluations = 25000;

        double crossoverProbability = 0.9;
        double crossoverDistributionIndex = 20.0;
        crossover = new SBXCrossover(crossoverProbability, crossoverDistributionIndex);

        double mutationProbability = 1.0 / problem.getNumberOfVariables();
        double mutationDistributionIndex = 20.0;
        mutation = new PolynomialMutation(mutationProbability, mutationDistributionIndex);

        selection = new BinaryTournamentSelection<DoubleSolution>();
    }

    public int getPopulationSize() {
        return populationSize;
    }

    public int getArchiveSize() {
        return archiveSize;
    }

    public int getMaxEvaluations() {
        return maxEvaluations;
    }

    public CrossoverOperator<DoubleSolution> getCrossover() {
        return crossover;
    }

    public MutationOperator<DoubleSolution> getMutation() {
        return mutation;
    }

    public SelectionOperator<List<DoubleSolution>, DoubleSolution> getSelection() {
        return selection;
    }

    /* Setters */
    public IBEABuilder setPopulationSize(int populationSize) {
        this.populationSize = populationSize;

        return this;
    }

    public IBEABuilder setArchiveSize(int archiveSize) {
        this.archiveSize = archiveSize;

        return this;
    }

    public IBEABuilder setMaxEvaluations(int maxEvaluations) {
        this.maxEvaluations = maxEvaluations;

        return this;
    }

    public IBEABuilder setCrossover(CrossoverOperator<DoubleSolution> crossover) {
        this.crossover = crossover;

        return this;
    }

    public IBEABuilder setMutation(MutationOperator<DoubleSolution> mutation) {
        this.mutation = mutation;

        return this;
    }

    public IBEABuilder setSelection(SelectionOperator<List<DoubleSolution>, DoubleSolution> selection) {
        this.selection = selection;

        return this;
    }

    @Override
    public IBEA<DoubleSolution> build() {
        return new IBEA<DoubleSolution>(problem, populationSize, archiveSize, maxEvaluations, selection, crossover,
                mutation);
    }
}
