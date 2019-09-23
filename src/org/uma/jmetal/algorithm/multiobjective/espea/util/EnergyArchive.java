package org.uma.jmetal.algorithm.multiobjective.espea.util;

import java.util.Collections;
import java.util.Comparator;

import org.uma.jmetal.algorithm.multiobjective.espea.ESPEA;
import org.uma.jmetal.algorithm.multiobjective.espea.util.ScalarizationWrapper.ScalarizationType;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.SolutionListUtils;
import org.uma.jmetal.util.archive.impl.AbstractBoundedArchive;
import org.uma.jmetal.util.comparator.FitnessComparator;
import org.uma.jmetal.util.front.util.FrontNormalizer;
import org.uma.jmetal.util.solutionattribute.impl.Fitness;

/**
 * The archive that is used within the {@link ESPEA} algorithm. The archive is
 * of variable size and bounded by the population size. A new solution can only
 * replace an existing archive member if it leads to a reduction of the total
 * energy of the archive.
 *
 * @author marlon.braun <marlon.braun@partner.kit.edu>
 */
@SuppressWarnings("serial")
public class EnergyArchive<S extends Solution<?>> extends AbstractBoundedArchive<S> {

	/**
	 * The replacement strategy defines the rule by which an existing archive member
	 * is replaced by a new solution. Computational studies have revealed that
	 * {@link #BEST_FEASIBLE_POSITION} is inferior to {@link #LARGEST_DIFFERENCE}
	 * and {@link #WORST_IN_ARCHIVE}. No significant performance difference could be
	 * founnd between {@link #LARGEST_DIFFERENCE} and {@link #WORST_IN_ARCHIVE}. See
	 * "Obtaining Optimal Pareto Front Appxoimations" by Braun et al. and
	 * "Scalarized Preferences in Multi-objective Optimizaiton" by Braun for
	 * details.
	 *
	 * @author marlon.braun
	 */
	public static enum ReplacementStrategy {
	/**
	 * Inserts the new solution such that the energy it introduces into the archive
	 * is minimized.
	 */
	BEST_FEASIBLE_POSITION,

	/**
	 * Maximizes the energy differences before and after replacement.
	 */
	LARGEST_DIFFERENCE,

	/**
	 * Among all eligible archive members that can be replaced the one exhibiting
	 * the largest energy contribution is replaced.
	 */
	WORST_IN_ARCHIVE;
	}

	/**
	 * Object for reading and writing scalarization values.
	 */
	private final ScalarizationValue<S> scalarization = new ScalarizationValue<>();

	/**
	 * Reading and writing fitness values. Fitness values are used to represent the
	 * energy that each solution contributes to the archive.
	 */
	private final Fitness<S> fitness = new Fitness<>();

	/**
	 * Used for comparing energy contributions of solutions. Energy contribution of
	 * each archive member is stored in its fitness value.
	 */
	private final Comparator<S> fitnessComparator = new FitnessComparator<>();

	/**
	 * Handles the computation of scalarization values. Scalarization values express
	 * the preferences for individual solutions.
	 */
	private final ScalarizationWrapper scalWrapper;

	/**
	 * The replacement strategy used for operating the archive.
	 */
	private ReplacementStrategy replacementStrategy = ReplacementStrategy.LARGEST_DIFFERENCE;

	/**
	 * If true, objective values are normalized to [0,1] before distances between
	 * archive members are computed. Min and max values for normalization are
	 * computed from the current archive members.
	 */
	private boolean normalizeObjectives = true;

	/**
	 * Standard constructor that uses uniform preferences - all Pareto optimal
	 * solutions are equally desirable.
	 *
	 * @param maxSize Size of the final distribution of points generated by the
	 *                archive.
	 */
	public EnergyArchive(int maxSize) {
		this(maxSize, new ScalarizationWrapper(ScalarizationType.UNIFORM));
	}

	/**
	 * Constructor that requires archive size and scalarization method
	 *
	 * @param maxSize     Size of the final distribution of points generated by the
	 *                    archive.
	 * @param scalWrapper The scalarization method that is used for computing energy
	 *                    contributions.
	 */
	public EnergyArchive(int maxSize, ScalarizationWrapper scalWrapper) {
		super(maxSize);
		this.scalWrapper = scalWrapper;
	}

	/**
	 * Constructor that requires archive size, scalarization method and whether
	 * objectives are normliazed.
	 *
	 * @param maxSize             Size of the final distribution of points generated
	 *                            by the archive.
	 * @param scalWrapper         The scalarization method that is used for
	 *                            computing energy contributions.
	 * @param normalizeObjectives Whether or not objective values are normlalized
	 *                            between distance computation.
	 */
	public EnergyArchive(int maxSize, ScalarizationWrapper scalWrapper, boolean normalizeObjectives) {
		super(maxSize);
		this.scalWrapper = scalWrapper;
		this.normalizeObjectives = normalizeObjectives;
	}

	/**
	 * Constructor that requires archive size, scalarization method, whether
	 * objectives are normalized and the replacement strategy.
	 *
	 * @param maxSize             Size of the final distribution of points generated
	 *                            by the archive.
	 * @param scalWrapper         The scalarization method that is used for
	 *                            computing energy contributions.
	 * @param normalizeObjectives Whether or not objective values are normlalized
	 *                            between distance computation.
	 * @param replacementStrategy Replacement strategy for archive update.
	 */
	public EnergyArchive(int maxSize, ScalarizationWrapper scalWrapper, boolean normalizeObjectives,
			ReplacementStrategy replacementStrategy) {
		super(maxSize);
		this.scalWrapper = scalWrapper;
		this.normalizeObjectives = normalizeObjectives;
		this.replacementStrategy = replacementStrategy;
	}

	@Override
	public Comparator<S> getComparator() {
		return fitnessComparator;
	}

	/*
	 * Note that a new solution can only replace an existing archive member. If the
	 * archive is pruned, density information is only computed for those archive
	 * member that are eligible for replacement.
	 *
	 * @see org.uma.jmetal.util.archive.BoundedArchive#computeDensityEstimator()
	 */
	@Override
	public void computeDensityEstimator() {
		// Compute scalarization values
		this.scalWrapper.execute(getSolutionList());
		scaleToPositive();

		// Distance matrix
		double[][] distanceMatrix;
		if (normalizeObjectives) {
			FrontNormalizer normalizer = new FrontNormalizer(getSolutionList());
			distanceMatrix = SolutionListUtils.distanceMatrix(normalizer.normalize(getSolutionList()));
		} else
			distanceMatrix = SolutionListUtils.distanceMatrix(getSolutionList());

		// Set fitness based on replacement strategy
		double[] energyVector = energyVector(distanceMatrix);
		double[] replacementVector = replacementVector(distanceMatrix);
		// Flag for memorizing whether solution can improve archive
		boolean eligible = false;
		for (int i = 0; i < replacementVector.length; i++) {
			// New solution is better than incumbent
			if (replacementVector[i] < energyVector[i]) {
				eligible = true;
				switch (replacementStrategy) {
				case BEST_FEASIBLE_POSITION:
					// Energy decrease is maximal if replacement energy is
					// minimized. This is why the the negated entry in
					// replacementVector is the corresponding fitness.
					fitness.setAttribute(archive.get(i), -replacementVector[i]);
					break;
				case LARGEST_DIFFERENCE:
					fitness.setAttribute(archive.get(i), energyVector[i]);
					break;
				case WORST_IN_ARCHIVE:
					fitness.setAttribute(archive.get(i), energyVector[i] - replacementVector[i]);
					break;
				}
			} else {
				// If archive member is not eligible for replacement, make sure
				// it is retained in any case.
				archive.get(i).setAttribute(fitness.getAttributeIdentifier(), -Double.MAX_VALUE);
			}
			if (eligible) {
				// New solution is always retained
				fitness.setAttribute(archive.get(maxSize), -Double.MAX_VALUE);
			} else {
				// New solution is rejected in any case
				fitness.setAttribute(archive.get(maxSize), Double.MAX_VALUE);
			}
		}
	}

	@Override
	public void sortByDensityEstimator() {
		Collections.sort(getSolutionList(), fitnessComparator);
	}

	@Override
	public void prune() {
		if (getSolutionList().size() > getMaxSize()) {
			computeDensityEstimator();
			S worst = new SolutionListUtils().findWorstSolution(getSolutionList(), fitnessComparator);
			getSolutionList().remove(worst);
		}
	}

	/**
	 * The niching mechanism of ESPEA only works if the scalarization values are
	 * positive. Otherwise scalarization values cannot be interpreted as charges of
	 * a physical system that signal desirability.
	 *
	 * <p>
	 * Scalarization values are scaled to positive values by subtracting the minimum
	 * scalariaztion value from all archive members and adding a small positive
	 * constant eps.
	 */
	private void scaleToPositive() {
		// Obtain min value
		double minScalarization = Double.MAX_VALUE;
		for (S solution : getSolutionList()) {
			if (scalarization.getAttribute(solution) < minScalarization) {
				minScalarization = scalarization.getAttribute(solution);
			}
		}
		if (minScalarization < 0) {
			// Avoid scalarization values of 0
			double eps = 10e-6;
			for (S solution : getSolutionList()) {
				scalarization.setAttribute(solution, eps + scalarization.getAttribute(solution) + minScalarization);
			}
		}
	}

	/**
	 * Computes the energy contribution of each archive member. Note that the
	 * archive member at position maxSize + 1 is the new solution that is tested for
	 * eligibility of replacement.
	 *
	 * @param distanceMatrix Distance between archive members
	 * @return The amount of energy that each member contributes to the archive.
	 */
	private double[] energyVector(double[][] distanceMatrix) {
		// Ignore the set (maxSize + 1)'th archive member since it's the new
		// solution that is tested for eligibility of replacement.
		double[] energyVector = new double[distanceMatrix.length - 1];
		for (int i = 0; i < energyVector.length - 1; i++) {
			for (int j = i + 1; j < energyVector.length; j++) {
				energyVector[i] += scalarization.getAttribute(archive.get(j)) / distanceMatrix[i][j];
				energyVector[j] += scalarization.getAttribute(archive.get(i)) / distanceMatrix[i][j];
			}
			energyVector[i] *= scalarization.getAttribute(archive.get(i));
		}
		return energyVector;
	}

	/**
	 * Computes the replacement energy vector. Each component k of the replacement
	 * vector states how much energy the new solution would introduce into the
	 * archive instead of the archive member at position k.
	 *
	 * @param distanceMatrix Distance between archive members
	 * @return The replacement energy vector.
	 */
	private double[] replacementVector(double[][] distanceMatrix) {
		double[] replacementVector = new double[distanceMatrix.length - 1];
		// Energy between archive member k and new solution
		double[] individualEnergy = new double[distanceMatrix.length - 1];
		// Sum of all individual energies
		double totalEnergy = 0.0;
		for (int i = 0; i < replacementVector.length; i++) {
			individualEnergy[i] = scalarization.getAttribute(archive.get(i)) / distanceMatrix[i][maxSize];
			totalEnergy += individualEnergy[i];
		}
		for (int i = 0; i < individualEnergy.length; i++) {
			replacementVector[i] = totalEnergy - individualEnergy[i];
			replacementVector[i] *= scalarization.getAttribute(archive.get(maxSize));
		}
		return replacementVector;
	}

	/**
	 * A check for testing whether the archive is full.
	 *
	 * @return true if the archive possesses the maximum number of elements. False
	 *         otherwise.
	 */
	public boolean isFull() {
		return getSolutionList().size() == maxSize;
	}

}
