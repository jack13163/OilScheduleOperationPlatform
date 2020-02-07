package opt.jmetal.algorithm.multiobjective.rnsgaii;

import opt.jmetal.algorithm.InteractiveAlgorithm;
import opt.jmetal.algorithm.multiobjective.nsgaii.NSGAII;
import opt.jmetal.operator.CrossoverOperator;
import opt.jmetal.operator.MutationOperator;
import opt.jmetal.operator.SelectionOperator;
import opt.jmetal.operator.impl.selection.RankingAndPreferenceSelection;
import opt.jmetal.problem.Problem;
import opt.jmetal.solution.Solution;
import opt.jmetal.util.comparator.DominanceComparator;
import opt.jmetal.util.evaluator.SolutionListEvaluator;
import opt.jmetal.util.measure.Measurable;
import opt.jmetal.util.measure.MeasureManager;
import opt.jmetal.util.measure.impl.BasicMeasure;
import opt.jmetal.util.measure.impl.CountingMeasure;
import opt.jmetal.util.measure.impl.DurationMeasure;
import opt.jmetal.util.measure.impl.SimpleMeasureManager;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
@SuppressWarnings("serial")
public class RNSGAII<S extends Solution<?>> extends NSGAII<S> implements
        InteractiveAlgorithm<S, List<S>>, Measurable {

    private List<Double> interestPoint;
    private double epsilon;

    protected SimpleMeasureManager measureManager;
    protected BasicMeasure<List<S>> solutionListMeasure;
    protected CountingMeasure evaluations;
    protected DurationMeasure durationMeasure;

    /**
     * Constructor
     */
    public RNSGAII(Problem<S> problem, int maxEvaluations, int populationSize,
                   int matingPoolSize, int offspringPopulationSize,
                   CrossoverOperator<S> crossoverOperator, MutationOperator<S> mutationOperator,
                   SelectionOperator<List<S>, S> selectionOperator, SolutionListEvaluator<S> evaluator,
                   List<Double> interestPoint, double epsilon) {
        super(problem, maxEvaluations, populationSize, matingPoolSize, offspringPopulationSize, crossoverOperator,
                mutationOperator, selectionOperator, new DominanceComparator<S>(), evaluator);
        this.interestPoint = interestPoint;
        this.epsilon = epsilon;

        measureManager = new SimpleMeasureManager();
        measureManager.setPushMeasure("currentPopulation", solutionListMeasure);
        measureManager.setPushMeasure("currentEvaluation", evaluations);

        initMeasures();
    }

    @Override
    public void updatePointOfInterest(List<Double> newReferencePoints) {
        this.interestPoint = newReferencePoints;
    }

    @Override
    protected void initProgress() {
        evaluations.reset(getMaxPopulationSize());
    }

    @Override
    protected void updateProgress() {
        evaluations.increment(getMaxPopulationSize());
        solutionListMeasure.push(getPopulation());
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
        solutionListMeasure = new BasicMeasure<>();

        measureManager = new SimpleMeasureManager();
        measureManager.setPullMeasure("currentExecutionTime", durationMeasure);
        measureManager.setPullMeasure("currentEvaluation", evaluations);

        measureManager.setPushMeasure("currentPopulation", solutionListMeasure);
        measureManager.setPushMeasure("currentEvaluation", evaluations);
    }

    @Override
    public MeasureManager getMeasureManager() {
        return measureManager;
    }

    @Override
    protected List<S> replacement(List<S> population, List<S> offspringPopulation) {
        List<S> jointPopulation = new ArrayList<>();
        jointPopulation.addAll(population);
        jointPopulation.addAll(offspringPopulation);

        RankingAndPreferenceSelection<S> rankingAndCrowdingSelection;
        rankingAndCrowdingSelection = new RankingAndPreferenceSelection<S>(getMaxPopulationSize(), interestPoint, epsilon);

        return rankingAndCrowdingSelection.execute(jointPopulation);
    }

    @Override
    public String getName() {
        return "RNSGAII";
    }

    @Override
    public String getDescription() {
        return "Reference Point Based Nondominated Sorting Genetic Algorithm version II";
    }
}
