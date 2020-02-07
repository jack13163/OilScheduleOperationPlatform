package opt.jmetal.algorithm.multiobjective.espea;

import opt.jmetal.algorithm.multiobjective.espea.util.EnergyArchive;
import opt.jmetal.algorithm.multiobjective.espea.util.ScalarizationWrapper;
import opt.jmetal.operator.CrossoverOperator;
import opt.jmetal.operator.MutationOperator;
import opt.jmetal.operator.SelectionOperator;
import opt.jmetal.operator.impl.selection.RandomSelection;
import opt.jmetal.problem.Problem;
import opt.jmetal.solution.Solution;
import opt.jmetal.util.AlgorithmBuilder;
import opt.jmetal.util.evaluator.SolutionListEvaluator;
import opt.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;

import java.util.List;

public class ESPEABuilder<S extends Solution<?>> implements AlgorithmBuilder<ESPEA<S>> {

    private final Problem<S> problem;
    private int maxEvaluations;
    private int populationSize;
    private CrossoverOperator<S> crossoverOperator;
    private CrossoverOperator<S> fullArchiveCrossoverOperator;
    private MutationOperator<S> mutationOperator;
    private SelectionOperator<List<S>, S> selectionOperator;
    private SolutionListEvaluator<S> evaluator;
    private ScalarizationWrapper scalarization;
    private boolean normalizeObjectives;
    private EnergyArchive.ReplacementStrategy replacementStrategy;

    public ESPEABuilder(Problem<S> problem, CrossoverOperator<S> crossoverOperator, MutationOperator<S> mutationOperator) {
        this.problem = problem;
        this.maxEvaluations = 25000;
        this.populationSize = 100;
        this.crossoverOperator = crossoverOperator;
        this.fullArchiveCrossoverOperator = crossoverOperator;
        this.mutationOperator = mutationOperator;
        this.selectionOperator = new RandomSelection<>();
        this.evaluator = new SequentialSolutionListEvaluator<>();
        this.scalarization = new ScalarizationWrapper(ScalarizationWrapper.ScalarizationType.UNIFORM);
        this.normalizeObjectives = true;
        this.replacementStrategy = EnergyArchive.ReplacementStrategy.LARGEST_DIFFERENCE;
    }

    @Override
    public ESPEA<S> build() {
        return new ESPEA<>(problem, maxEvaluations, populationSize, crossoverOperator, fullArchiveCrossoverOperator, mutationOperator,
                selectionOperator, scalarization, evaluator, normalizeObjectives, replacementStrategy);
    }

    /**
     * @return the maxEvaluations
     */
    public int getMaxEvaluations() {
        return maxEvaluations;
    }

    /**
     * @return the populationSize
     */
    public int getPopulationSize() {
        return populationSize;
    }

    /**
     * @return the crossoverOperator
     */
    public CrossoverOperator<S> getCrossoverOperator() {
        return crossoverOperator;
    }

    /**
     * @return the fullArchiveCrossoverOperator
     */
    public CrossoverOperator<S> getFullArchiveCrossoverOperator() {
        return fullArchiveCrossoverOperator;
    }

    /**
     * @return the mutationOperator
     */
    public MutationOperator<S> getMutationOperator() {
        return mutationOperator;
    }

    /**
     * @return the selectionOperator
     */
    public SelectionOperator<List<S>, S> getSelectionOperator() {
        return selectionOperator;
    }

    /**
     * @return the evaluator
     */
    public SolutionListEvaluator<S> getEvaluator() {
        return evaluator;
    }

    /**
     * @return the scalarization
     */
    public ScalarizationWrapper getScalarization() {
        return scalarization;
    }

    /**
     * @param maxEvaluations the maxEvaluations to set
     */
    public void setMaxEvaluations(int maxEvaluations) {
        this.maxEvaluations = maxEvaluations;
    }

    /**
     * @param populationSize the populationSize to set
     */
    public void setPopulationSize(int populationSize) {
        this.populationSize = populationSize;
    }

    /**
     * @param crossoverOperator the crossoverOperator to set
     */
    public void setCrossoverOperator(CrossoverOperator<S> crossoverOperator) {
        this.crossoverOperator = crossoverOperator;
    }

    /**
     * @param fullArchiveCrossoverOperator the fullArchiveCrossoverOperator to set
     */
    public void setFullArchiveCrossoverOperator(CrossoverOperator<S> fullArchiveCrossoverOperator) {
        this.fullArchiveCrossoverOperator = fullArchiveCrossoverOperator;
    }

    /**
     * @param mutationOperator the mutationOperator to set
     */
    public void setMutationOperator(MutationOperator<S> mutationOperator) {
        this.mutationOperator = mutationOperator;
    }

    /**
     * @param selectionOperator the selectionOperator to set
     */
    public void setSelectionOperator(SelectionOperator<List<S>, S> selectionOperator) {
        this.selectionOperator = selectionOperator;
    }

    /**
     * @param evaluator the evaluator to set
     */
    public void setEvaluator(SolutionListEvaluator<S> evaluator) {
        this.evaluator = evaluator;
    }

    /**
     * @param scalarization the scalarization to set
     */
    public void setScalarization(ScalarizationWrapper scalarization) {
        this.scalarization = scalarization;
    }

    /**
     * @return the normalizeObjectives
     */
    public boolean isNormalizeObjectives() {
        return normalizeObjectives;
    }

    /**
     * @param normalizeObjectives the normalizeObjectives to set
     */
    public void setNormalizeObjectives(boolean normalizeObjectives) {
        this.normalizeObjectives = normalizeObjectives;
    }

    /**
     * @return the replacement strategy
     */
    public EnergyArchive.ReplacementStrategy getOperationType() {
        return replacementStrategy;
    }

    /**
     * @param replacementStrategy the replacement strategy to set
     */
    public void setReplacementStrategy(EnergyArchive.ReplacementStrategy replacementStrategy) {
        this.replacementStrategy = replacementStrategy;
    }
}
