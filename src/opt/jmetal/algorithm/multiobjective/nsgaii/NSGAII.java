package opt.jmetal.algorithm.multiobjective.nsgaii;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import opt.jmetal.algorithm.impl.AbstractGeneticAlgorithm;
import opt.jmetal.operator.CrossoverOperator;
import opt.jmetal.operator.MutationOperator;
import opt.jmetal.operator.SelectionOperator;
import opt.jmetal.operator.impl.selection.RankingAndCrowdingSelection;
import opt.jmetal.problem.Problem;
import opt.jmetal.solution.Solution;
import opt.jmetal.util.SolutionListUtils;
import opt.jmetal.util.comparator.DominanceComparator;
import opt.jmetal.util.evaluator.SolutionListEvaluator;

import oil.sim.common.CloneUtils;
import oil.sim.ui.MainMethod;

/**
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
@SuppressWarnings("serial")
public class NSGAII<S extends Solution<?>> extends AbstractGeneticAlgorithm<S, List<S>> {
    protected final int maxEvaluations;

    protected final SolutionListEvaluator<S> evaluator;

    protected int evaluations;
    protected Comparator<S> dominanceComparator;

    protected int matingPoolSize;
    protected int offspringPopulationSize;

    protected List<Double[]> solutions = new LinkedList<>();

    /**
     * Constructor
     */
    public NSGAII(Problem<S> problem, int maxEvaluations, int populationSize, int matingPoolSize,
                  int offspringPopulationSize, CrossoverOperator<S> crossoverOperator, MutationOperator<S> mutationOperator,
                  SelectionOperator<List<S>, S> selectionOperator, SolutionListEvaluator<S> evaluator) {
        this(problem, maxEvaluations, populationSize, matingPoolSize, offspringPopulationSize, crossoverOperator,
                mutationOperator, selectionOperator, new DominanceComparator<S>(), evaluator);
    }

    /**
     * Constructor
     */
    public NSGAII(Problem<S> problem, int maxEvaluations, int populationSize, int matingPoolSize,
                  int offspringPopulationSize, CrossoverOperator<S> crossoverOperator, MutationOperator<S> mutationOperator,
                  SelectionOperator<List<S>, S> selectionOperator, Comparator<S> dominanceComparator,
                  SolutionListEvaluator<S> evaluator) {
        super(problem);
        this.maxEvaluations = maxEvaluations;
        setMaxPopulationSize(populationSize);

        this.crossoverOperator = crossoverOperator;
        this.mutationOperator = mutationOperator;
        this.selectionOperator = selectionOperator;

        this.evaluator = evaluator;
        this.dominanceComparator = dominanceComparator;

        this.matingPoolSize = matingPoolSize;
        this.offspringPopulationSize = offspringPopulationSize;
    }

    @Override
    protected void initProgress() {
        evaluations = getMaxPopulationSize();
    }

    @Override
    protected void updateProgress() {
        evaluations += offspringPopulationSize;
    }

    @Override
    protected boolean isStoppingConditionReached() {
        // 更新进度条
        MainMethod.frame.updateProcessBar(evaluations);
        return evaluations >= maxEvaluations;
    }

    @Override
    protected List<S> evaluatePopulation(List<S> population) {
        // 1.评价种群中个体的适应度
        population = evaluator.evaluate(population, getProblem());
        // 2.保存到种群列表中
        for (int i = 0; i < population.size(); i++) {
            solutions.add(CloneUtils.clone(ArrayUtils.toObject(population.get(i).getObjectives())));
        }

        return population;
    }

    /**
     * This method iteratively applies a {@link SelectionOperator} to the population
     * to fill the mating pool population.
     *
     * @param population
     * @return The mating pool population
     */
    @Override
    protected List<S> selection(List<S> population) {
        List<S> matingPopulation = new ArrayList<>(population.size());
        for (int i = 0; i < matingPoolSize; i++) {
            S solution = selectionOperator.execute(population);
            matingPopulation.add(solution);
        }

        return matingPopulation;
    }

    /**
     * This methods iteratively applies a {@link CrossoverOperator} a
     * {@link MutationOperator} to the population to create the offspring
     * population. The population size must be divisible by the number of parents
     * required by the {@link CrossoverOperator}; this way, the needed parents are
     * taken sequentially from the population.
     * <p>
     * The number of solutions returned by the {@link CrossoverOperator} must be
     * equal to the offspringPopulationSize state variable
     *
     * @param matingPool
     * @return The new created offspring population
     */
    @Override
    protected List<S> reproduction(List<S> matingPool) {
        int numberOfParents = crossoverOperator.getNumberOfRequiredParents();

        checkNumberOfParents(matingPool, numberOfParents);

        List<S> offspringPopulation = new ArrayList<>(offspringPopulationSize);
        for (int i = 0; i < matingPool.size(); i += numberOfParents) {
            List<S> parents = new ArrayList<>(numberOfParents);
            for (int j = 0; j < numberOfParents; j++) {
                parents.add(population.get(i + j));
            }

            List<S> offspring = crossoverOperator.execute(parents);

            for (S s : offspring) {
                mutationOperator.execute(s);
                offspringPopulation.add(s);
                if (offspringPopulation.size() >= offspringPopulationSize)
                    break;
            }
        }
        return offspringPopulation;
    }

    @Override
    protected List<S> replacement(List<S> population, List<S> offspringPopulation) {
        List<S> jointPopulation = new ArrayList<>();
        jointPopulation.addAll(population);
        jointPopulation.addAll(offspringPopulation);

        RankingAndCrowdingSelection<S> rankingAndCrowdingSelection;
        rankingAndCrowdingSelection = new RankingAndCrowdingSelection<S>(getMaxPopulationSize(), dominanceComparator);

        return rankingAndCrowdingSelection.execute(jointPopulation);
    }

    @Override
    public List<S> getResult() {
        return SolutionListUtils.getNondominatedSolutions(getPopulation());
    }

    @Override
    public String getName() {
        return "NSGAII";
    }

    @Override
    public String getDescription() {
        return "Nondominated Sorting Genetic Algorithm version II";
    }

    @Override
    public List<Double[]> getSolutions() {
        return solutions;
    }

    @Override
    public void clearSolutions() {
        solutions = null;
    }
}
