package opt.jmetal.algorithm.multiobjective.cellde;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import opt.jmetal.algorithm.Algorithm;
import opt.jmetal.operator.SelectionOperator;
import opt.jmetal.operator.impl.crossover.DifferentialEvolutionCrossover;
import opt.jmetal.problem.Problem;
import opt.jmetal.solution.DoubleSolution;
import opt.jmetal.util.archive.BoundedArchive;
import opt.jmetal.util.comparator.CrowdingDistanceComparator;
import opt.jmetal.util.comparator.DominanceComparator;
import opt.jmetal.util.evaluator.SolutionListEvaluator;
import opt.jmetal.util.neighborhood.Neighborhood;
import opt.jmetal.util.pseudorandom.JMetalRandom;
import opt.jmetal.util.solutionattribute.Ranking;
import opt.jmetal.util.solutionattribute.impl.CrowdingDistance;
import opt.jmetal.util.solutionattribute.impl.DominanceRanking;
import opt.jmetal.util.solutionattribute.impl.LocationAttribute;

/**
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
@SuppressWarnings("serial")
public class CellDE45 implements Algorithm<List<DoubleSolution>> {
    private Problem<DoubleSolution> problem;
    private List<DoubleSolution> population;
    private int populationSize;

    protected int evaluations;
    protected int maxEvaluations;

    private Neighborhood<DoubleSolution> neighborhood;
    private int currentIndividual;
    private List<DoubleSolution> currentNeighbors;

    private SelectionOperator<List<DoubleSolution>, DoubleSolution> selection;
    private DifferentialEvolutionCrossover crossover;

    private BoundedArchive<DoubleSolution> archive;

    private Comparator<DoubleSolution> dominanceComparator;
    private LocationAttribute<DoubleSolution> location;

    private SolutionListEvaluator<DoubleSolution> evaluator;

    private double feedback;

    private CrowdingDistanceComparator<DoubleSolution> comparator = new CrowdingDistanceComparator<>();
    private CrowdingDistance<DoubleSolution> distance = new CrowdingDistance<>();

    public CellDE45(Problem<DoubleSolution> problem, int maxEvaluations, int populationSize,
                    BoundedArchive<DoubleSolution> archive, Neighborhood<DoubleSolution> neighborhood,
                    SelectionOperator<List<DoubleSolution>, DoubleSolution> selection, DifferentialEvolutionCrossover crossover,
                    double feedback, SolutionListEvaluator<DoubleSolution> evaluator) {
        this.problem = problem;
        this.populationSize = populationSize;
        this.maxEvaluations = maxEvaluations;
        this.archive = archive;
        this.neighborhood = neighborhood;
        this.selection = selection;
        this.crossover = crossover;
        this.dominanceComparator = new DominanceComparator<DoubleSolution>();
        this.feedback = feedback;

        this.evaluator = evaluator;
    }

    @Override
    public void run() {
        population = createInitialPopulation();
        population = evaluatePopulation(population);
        initProgress();

        while (!isStoppingConditionReached()) {
            for (int i = 0; i < populationSize; i++) {
                DoubleSolution solution = (DoubleSolution) population.get(i).copy();

                currentNeighbors = neighborhood.getNeighbors(population, i);
                currentNeighbors.add(population.get(i));

                List<DoubleSolution> parents = new ArrayList<>();
                parents.add(selection.execute(currentNeighbors));
                parents.add(selection.execute(currentNeighbors));
                parents.add(solution);

                crossover.setCurrentSolution(population.get(i));
                List<DoubleSolution> children = crossover.execute(parents);

                DoubleSolution offspring = children.get(0);
                problem.evaluate(offspring);
                evaluations++;

                int result = dominanceComparator.compare(population.get(i), offspring);
                if (result == 1) {
                    location.setAttribute(offspring, location.getAttribute(population.get(i)));
                    population.set(i, (DoubleSolution) offspring.copy());
                    archive.add((DoubleSolution) offspring.copy());
                } else if (result == 0) {
                    Ranking<DoubleSolution> ranking = computeRanking(currentNeighbors);

                    distance.computeDensityEstimator(ranking.getSubfront(0));
                    boolean deleteMutant = true;
                    int compareResult = comparator.compare(solution, offspring);

                    if (compareResult == 1) {
                        deleteMutant = false;
                    }

                    if (!deleteMutant) {
                        location.setAttribute(offspring, location.getAttribute(solution));
                        population.set(location.getAttribute(offspring), offspring);
                        archive.add((DoubleSolution) offspring.copy());
                    } else {
                        archive.add((DoubleSolution) offspring.copy());
                    }
                }
            }

            for (int i = 0; i < feedback; i++) {
                if (archive.size() > i) {
                    int random = JMetalRandom.getInstance().nextInt(0, population.size() - 1);
                    if (random < population.size()) {
                        DoubleSolution solution = archive.get(i);
                        location.setAttribute(solution, random);
                        population.set(random, (DoubleSolution) solution.copy());
                    }
                }
            }
        }

    }

    protected List<DoubleSolution> createInitialPopulation() {
        List<DoubleSolution> population = new ArrayList<>(populationSize);
        for (int i = 0; i < populationSize; i++) {
            DoubleSolution newIndividual = problem.createSolution();
            population.add(newIndividual);
        }
        location = new LocationAttribute<>(population);
        return population;
    }

    protected List<DoubleSolution> evaluatePopulation(List<DoubleSolution> population) {
        return evaluator.evaluate(population, problem);
    }

    protected void initProgress() {
        evaluations = populationSize;
        currentIndividual = 0;
    }

    protected void updateProgress() {
        evaluations++;
        currentIndividual = (currentIndividual + 1) % populationSize;
    }

    protected boolean isStoppingConditionReached() {
        return (evaluations == maxEvaluations);
    }

    @Override
    public String getName() {
        return "CellDE";
    }

    @Override
    public String getDescription() {
        return "Multi-Objective Differential Evolution Cellular evolutionary algorithm";
    }

    @Override
    public List<DoubleSolution> getResult() {
        return archive.getSolutionList();
    }

    protected Ranking<DoubleSolution> computeRanking(List<DoubleSolution> solutionList) {
        Ranking<DoubleSolution> ranking = new DominanceRanking<DoubleSolution>();
        ranking.computeRanking(solutionList);

        return ranking;
    }

    @Override
    public List<Double[]> getSolutions() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void clearSolutions() {
        // TODO Auto-generated method stub

    }
}
