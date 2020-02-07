package opt.jmetal.algorithm.multiobjective.nsgaii;

import opt.jmetal.qualityindicator.impl.hypervolume.PISAHypervolume;
import opt.jmetal.operator.CrossoverOperator;
import opt.jmetal.operator.MutationOperator;
import opt.jmetal.operator.SelectionOperator;
import opt.jmetal.problem.Problem;
import opt.jmetal.solution.Solution;
import opt.jmetal.util.SolutionListUtils;
import opt.jmetal.util.evaluator.SolutionListEvaluator;
import opt.jmetal.util.front.Front;
import opt.jmetal.util.front.imp.ArrayFront;
import opt.jmetal.util.measure.Measurable;
import opt.jmetal.util.measure.MeasureManager;
import opt.jmetal.util.measure.impl.BasicMeasure;
import opt.jmetal.util.measure.impl.CountingMeasure;
import opt.jmetal.util.measure.impl.DurationMeasure;
import opt.jmetal.util.measure.impl.SimpleMeasureManager;
import opt.jmetal.util.solutionattribute.Ranking;
import opt.jmetal.util.solutionattribute.impl.DominanceRanking;

import java.util.Comparator;
import java.util.List;

/**
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
@SuppressWarnings("serial")
public class NSGAIIMeasures<S extends Solution<?>> extends NSGAII<S> implements Measurable {
    protected CountingMeasure evaluations;
    protected DurationMeasure durationMeasure;
    protected SimpleMeasureManager measureManager;

    protected BasicMeasure<List<S>> solutionListMeasure;
    protected BasicMeasure<Integer> numberOfNonDominatedSolutionsInPopulation;
    protected BasicMeasure<Double> hypervolumeValue;

    protected Front referenceFront;

    /**
     * Constructor
     */
    public NSGAIIMeasures(Problem<S> problem, int maxIterations, int populationSize,
                          int matingPoolSize, int offspringPopulationSize,
                          CrossoverOperator<S> crossoverOperator, MutationOperator<S> mutationOperator,
                          SelectionOperator<List<S>, S> selectionOperator, Comparator<S> dominanceComparator, SolutionListEvaluator<S> evaluator) {
        super(problem, maxIterations, populationSize, matingPoolSize, offspringPopulationSize,
                crossoverOperator, mutationOperator, selectionOperator, dominanceComparator, evaluator);

        referenceFront = new ArrayFront();

        initMeasures();
    }

    @Override
    protected void initProgress() {
        evaluations.reset(getMaxPopulationSize());
    }

    @Override
    protected void updateProgress() {
        evaluations.increment(getMaxPopulationSize());

        solutionListMeasure.push(getPopulation());

        if (referenceFront.getNumberOfPoints() > 0) {
            hypervolumeValue.push(
                    new PISAHypervolume<S>(referenceFront).evaluate(
                            SolutionListUtils.getNondominatedSolutions(getPopulation())));
        }
    }

    @Override
    protected boolean isStoppingConditionReached() {
        return evaluations.get() >= maxEvaluations;
    }

    @Override
    public void run() {
        durationMeasure.reset();
        durationMeasure.start();
        super.run();
        durationMeasure.stop();
    }

    /* Measures code */
    private void initMeasures() {
        durationMeasure = new DurationMeasure();
        evaluations = new CountingMeasure(0);
        numberOfNonDominatedSolutionsInPopulation = new BasicMeasure<>();
        solutionListMeasure = new BasicMeasure<>();
        hypervolumeValue = new BasicMeasure<>();

        measureManager = new SimpleMeasureManager();
        measureManager.setPullMeasure("currentExecutionTime", durationMeasure);
        measureManager.setPullMeasure("currentEvaluation", evaluations);
        measureManager.setPullMeasure("numberOfNonDominatedSolutionsInPopulation",
                numberOfNonDominatedSolutionsInPopulation);

        measureManager.setPushMeasure("currentPopulation", solutionListMeasure);
        measureManager.setPushMeasure("currentEvaluation", evaluations);
        measureManager.setPushMeasure("hypervolume", hypervolumeValue);
    }

    @Override
    public MeasureManager getMeasureManager() {
        return measureManager;
    }

    @Override
    protected List<S> replacement(List<S> population,
                                  List<S> offspringPopulation) {
        List<S> pop = super.replacement(population, offspringPopulation);

        Ranking<S> ranking = new DominanceRanking<S>(dominanceComparator);
        ranking.computeRanking(population);

        numberOfNonDominatedSolutionsInPopulation.set(ranking.getSubfront(0).size());

        return pop;
    }

    public CountingMeasure getEvaluations() {
        return evaluations;
    }

    @Override
    public String getName() {
        return "NSGAIIM";
    }

    @Override
    public String getDescription() {
        return "Nondominated Sorting Genetic Algorithm version II. Version using measures";
    }

    public void setReferenceFront(Front referenceFront) {
        this.referenceFront = referenceFront;
    }
}
