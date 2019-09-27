package org.uma.jmetal.algorithm.multiobjective.nsgaiii;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang3.ArrayUtils;
import org.uma.jmetal.algorithm.impl.AbstractGeneticAlgorithm;
import org.uma.jmetal.algorithm.multiobjective.nsgaiii.util.EnvironmentalSelection;
import org.uma.jmetal.algorithm.multiobjective.nsgaiii.util.ReferencePoint;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.SolutionListUtils;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.solutionattribute.Ranking;
import org.uma.jmetal.util.solutionattribute.impl.DominanceRanking;

import com.sim.common.CloneUtils;
import com.sim.common.UniformPointHelper;
import com.sim.ui.MainMethod;

/**
 * Created by ajnebro on 30/10/14. Modified by Juanjo on 13/11/14
 *
 * This implementation is based on the code of Tsung-Che Chiang
 * http://web.ntnu.edu.tw/~tcchiang/publications/nsga3cpp/nsga3cpp.htm
 */
@SuppressWarnings("serial")
public class NSGAIII<S extends Solution<?>> extends AbstractGeneticAlgorithm<S, List<S>> {
	protected int iterations;
	protected int maxIterations;

	protected SolutionListEvaluator<S> evaluator;

	protected Vector<Integer> numberOfDivisions;
	protected List<ReferencePoint<S>> referencePoints = new Vector<>();

	protected List<Double[]> solutions = new LinkedList<>();

	/** Constructor */
	public NSGAIII(NSGAIIIBuilder<S> builder) { // can be created from the NSGAIIIBuilder within the same package
		super(builder.getProblem());
		maxIterations = builder.getMaxIterations();

		crossoverOperator = builder.getCrossoverOperator();
		mutationOperator = builder.getMutationOperator();
		selectionOperator = builder.getSelectionOperator();

		evaluator = builder.getEvaluator();

		/// NSGAIII生成参考点
		numberOfDivisions = new Vector<>(1);
		int div = UniformPointHelper.getNumOfDivide(builder.getPopulationSize(),
				builder.getProblem().getNumberOfObjectives());
		numberOfDivisions.add(div);// 自适应确定参考点集大小和种群大小
		(new ReferencePoint<S>()).generateReferencePoints(referencePoints, getProblem().getNumberOfObjectives(),
				numberOfDivisions);
		setMaxPopulationSize(builder.getPopulationSize());

		JMetalLogger.logger.info("参考平面点数: " + referencePoints.size());
	}

	@Override
	protected void initProgress() {
		iterations = 1;
	}

	@Override
	protected void updateProgress() {
		iterations++;
		// 更新进度条
		int populationSize = population.size();
		MainMethod.frame.updateProcessBar(iterations * populationSize);
	}

	@Override
	protected boolean isStoppingConditionReached() {
		return iterations >= maxIterations;
	}

	@Override
	protected List<S> evaluatePopulation(List<S> population) {
		// 1.评价种群中个体的适应度
		population = evaluator.evaluate(population, getProblem());
		// 2.保存到种群列表中
		for (S s : population) {
			solutions.add(CloneUtils.clone(ArrayUtils.toObject(s.getObjectives())));
		}

		return population;
	}

	@Override
	protected List<S> selection(List<S> population) {
		List<S> matingPopulation = new ArrayList<>(population.size());
		for (int i = 0; i < getMaxPopulationSize(); i++) {
			S solution = selectionOperator.execute(population);
			matingPopulation.add(solution);
		}

		return matingPopulation;
	}

	@Override
	protected List<S> reproduction(List<S> population) {
		List<S> offspringPopulation = new ArrayList<>(getMaxPopulationSize());
		for (int i = 0; i < getMaxPopulationSize(); i += 2) {
			List<S> parents = new ArrayList<>(2);
			parents.add(population.get(i));
			parents.add(population.get(Math.min(i + 1, getMaxPopulationSize() - 1)));

			List<S> offspring = crossoverOperator.execute(parents);

			mutationOperator.execute(offspring.get(0));
			mutationOperator.execute(offspring.get(1));

			offspringPopulation.add(offspring.get(0));
			offspringPopulation.add(offspring.get(1));
		}
		return offspringPopulation;
	}

	private List<ReferencePoint<S>> getReferencePointsCopy() {
		List<ReferencePoint<S>> copy = new ArrayList<>();
		for (ReferencePoint<S> r : this.referencePoints) {
			copy.add(new ReferencePoint<>(r));
		}
		return copy;
	}

	@Override
	protected List<S> replacement(List<S> population, List<S> offspringPopulation) {

		List<S> jointPopulation = new ArrayList<>();
		jointPopulation.addAll(population);
		jointPopulation.addAll(offspringPopulation);

		Ranking<S> ranking = computeRanking(jointPopulation);

		// List<Solution> pop = crowdingDistanceSelection(ranking);
		List<S> pop = new ArrayList<>();
		List<List<S>> fronts = new ArrayList<>();
		int rankingIndex = 0;
		int candidateSolutions = 0;
		while (candidateSolutions < getMaxPopulationSize()) {
			fronts.add(ranking.getSubfront(rankingIndex));
			candidateSolutions += ranking.getSubfront(rankingIndex).size();
			if ((pop.size() + ranking.getSubfront(rankingIndex).size()) <= getMaxPopulationSize())
				addRankedSolutionsToPopulation(ranking, rankingIndex, pop);
			rankingIndex++;
		}

		// A copy of the reference list should be used as parameter of the environmental
		// selection
		EnvironmentalSelection<S> selection = new EnvironmentalSelection<>(fronts, getMaxPopulationSize(),
				getReferencePointsCopy(), getProblem().getNumberOfObjectives());

		pop = selection.execute(pop);

		return pop;
	}

	@Override
	public List<S> getResult() {
		return getNonDominatedSolutions(getPopulation());
	}

	protected Ranking<S> computeRanking(List<S> solutionList) {
		Ranking<S> ranking = new DominanceRanking<>();
		ranking.computeRanking(solutionList);

		return ranking;
	}

	protected void addRankedSolutionsToPopulation(Ranking<S> ranking, int rank, List<S> population) {
		List<S> front;

		front = ranking.getSubfront(rank);

		for (int i = 0; i < front.size(); i++) {
			population.add(front.get(i));
		}
	}

	protected List<S> getNonDominatedSolutions(List<S> solutionList) {
		return SolutionListUtils.getNondominatedSolutions(solutionList);
	}

	@Override
	public String getName() {
		return "NSGAIII";
	}

	@Override
	public String getDescription() {
		return "Nondominated Sorting Genetic Algorithm version III";
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
