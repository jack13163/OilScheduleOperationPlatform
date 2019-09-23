package org.uma.jmetal.operator.impl.crossover;

import java.util.ArrayList;
import java.util.List;

import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.solution.IntegerDoubleSolution;
import org.uma.jmetal.solution.impl.DefaultIntegerDoubleSolution;
import org.uma.jmetal.solution.util.RepairDoubleSolution;
import org.uma.jmetal.solution.util.RepairDoubleSolutionAtBounds;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;
import org.uma.jmetal.util.pseudorandom.RandomGenerator;

public class IntegerDoubleSBXCrossover implements CrossoverOperator<IntegerDoubleSolution> {

	private static final long serialVersionUID = 1L;

	/** EPS defines the minimum difference allowed between real values */
	private static final double EPS = 1.0e-14;

	private double distributionIndex;
	private double crossoverProbability;
	private RepairDoubleSolution solutionRepair;

	private RandomGenerator<Double> randomGenerator;

	/** Constructor */
	public IntegerDoubleSBXCrossover(double crossoverProbability, double distributionIndex) {
		this(crossoverProbability, distributionIndex, new RepairDoubleSolutionAtBounds());
	}

	/** Constructor */
	public IntegerDoubleSBXCrossover(double crossoverProbability, double distributionIndex,
			RandomGenerator<Double> randomGenerator) {
		this(crossoverProbability, distributionIndex, new RepairDoubleSolutionAtBounds(), randomGenerator);
	}

	/** Constructor */
	public IntegerDoubleSBXCrossover(double crossoverProbability, double distributionIndex,
			RepairDoubleSolution solutionRepair) {
		this(crossoverProbability, distributionIndex, solutionRepair, () -> JMetalRandom.getInstance().nextDouble());
	}

	/** Constructor */
	public IntegerDoubleSBXCrossover(double crossoverProbability, double distributionIndex,
			RepairDoubleSolution solutionRepair, RandomGenerator<Double> randomGenerator) {
		if (crossoverProbability < 0) {
			throw new JMetalException("Crossover probability is negative: " + crossoverProbability);
		} else if (distributionIndex < 0) {
			throw new JMetalException("Distribution index is negative: " + distributionIndex);
		}

		this.crossoverProbability = crossoverProbability;
		this.distributionIndex = distributionIndex;
		this.solutionRepair = solutionRepair;

		this.randomGenerator = randomGenerator;
	}

	/* Getters */
	public double getCrossoverProbability() {
		return crossoverProbability;
	}

	public double getDistributionIndex() {
		return distributionIndex;
	}

	/* Setters */
	public void setDistributionIndex(double distributionIndex) {
		this.distributionIndex = distributionIndex;
	}

	public void setCrossoverProbability(double crossoverProbability) {
		this.crossoverProbability = crossoverProbability;
	}

	@Override
	public List<IntegerDoubleSolution> execute(List<IntegerDoubleSolution> solutions) {

		if (null == solutions) {
			throw new JMetalException("Null parameter");
		} else if (solutions.size() != 2) {
			throw new JMetalException("There must be two parents instead of " + solutions.size());
		}

		return doCrossover(crossoverProbability, solutions.get(0), solutions.get(1));
	}

	/** doCrossover method */
	public List<IntegerDoubleSolution> doCrossover(double probability, IntegerDoubleSolution parent1,
			IntegerDoubleSolution parent2) {
		List<IntegerDoubleSolution> offspring = new ArrayList<IntegerDoubleSolution>(2);

		offspring.add((DefaultIntegerDoubleSolution) parent1.copy());
		offspring.add((DefaultIntegerDoubleSolution) parent2.copy());

		double rand;
		double y1, y2, yL, yU;
		double c1, c2;
		double alpha, beta, betaq;

		if (randomGenerator.getRandomValue() <= probability) {
			for (int i = 0; i < parent1.getNumberOfVariables(); i++) {
				if (i < parent1.getNumberOfIntegerVariables()) {
					int valueX1 = parent1.getVariableValue(i).intValue();
					int valueX2 = parent2.getVariableValue(i).intValue();

					if (randomGenerator.getRandomValue() <= 0.5) {
						if (Math.abs(valueX1 - valueX2) > EPS) {

							if (valueX1 < valueX2) {
								y1 = valueX1;
								y2 = valueX2;
							} else {
								y1 = valueX2;
								y2 = valueX1;
							}

							yL = parent1.getLowerBound(i).intValue();
							yU = parent1.getUpperBound(i).intValue();
							rand = randomGenerator.getRandomValue();
							beta = 1.0 + (2.0 * (y1 - yL) / (y2 - y1));
							alpha = 2.0 - Math.pow(beta, -(distributionIndex + 1.0));

							if (rand <= (1.0 / alpha)) {
								betaq = Math.pow((rand * alpha), (1.0 / (distributionIndex + 1.0)));
							} else {
								betaq = Math.pow(1.0 / (2.0 - rand * alpha), 1.0 / (distributionIndex + 1.0));
							}

							c1 = 0.5 * ((y1 + y2) - betaq * (y2 - y1));
							beta = 1.0 + (2.0 * (yU - y2) / (y2 - y1));
							alpha = 2.0 - Math.pow(beta, -(distributionIndex + 1.0));

							if (rand <= (1.0 / alpha)) {
								betaq = Math.pow((rand * alpha), (1.0 / (distributionIndex + 1.0)));
							} else {
								betaq = Math.pow(1.0 / (2.0 - rand * alpha), 1.0 / (distributionIndex + 1.0));
							}

							c2 = 0.5 * (y1 + y2 + betaq * (y2 - y1));

							if (c1 < yL) {
								c1 = yL;
							}

							if (c2 < yL) {
								c2 = yL;
							}

							if (c1 > yU) {
								c1 = yU;
							}

							if (c2 > yU) {
								c2 = yU;
							}

							if (randomGenerator.getRandomValue() <= 0.5) {
								offspring.get(0).setVariableValue(i, (int) c2);
								offspring.get(1).setVariableValue(i, (int) c1);
							} else {
								offspring.get(0).setVariableValue(i, (int) c1);
								offspring.get(1).setVariableValue(i, (int) c2);
							}
						} else {
							offspring.get(0).setVariableValue(i, valueX1);
							offspring.get(1).setVariableValue(i, valueX2);
						}
					} else {
						offspring.get(0).setVariableValue(i, valueX2);
						offspring.get(1).setVariableValue(i, valueX1);
					}
				} else {
					double valueX1 = parent1.getVariableValue(i).doubleValue();
					double valueX2 = parent2.getVariableValue(i).doubleValue();

					if (randomGenerator.getRandomValue() <= 0.5) {
						if (Math.abs(valueX1 - valueX2) > EPS) {
							if (valueX1 < valueX2) {
								y1 = valueX1;
								y2 = valueX2;
							} else {
								y1 = valueX2;
								y2 = valueX1;
							}

							yL = parent1.getLowerBound(i).doubleValue();
							yU = parent1.getUpperBound(i).doubleValue();

							rand = randomGenerator.getRandomValue();
							beta = 1.0 + (2.0 * (y1 - yL) / (y2 - y1));
							alpha = 2.0 - Math.pow(beta, -(distributionIndex + 1.0));

							if (rand <= (1.0 / alpha)) {
								betaq = Math.pow(rand * alpha, (1.0 / (distributionIndex + 1.0)));
							} else {
								betaq = Math.pow(1.0 / (2.0 - rand * alpha), 1.0 / (distributionIndex + 1.0));
							}
							c1 = 0.5 * (y1 + y2 - betaq * (y2 - y1));

							beta = 1.0 + (2.0 * (yU - y2) / (y2 - y1));
							alpha = 2.0 - Math.pow(beta, -(distributionIndex + 1.0));

							if (rand <= (1.0 / alpha)) {
								betaq = Math.pow((rand * alpha), (1.0 / (distributionIndex + 1.0)));
							} else {
								betaq = Math.pow(1.0 / (2.0 - rand * alpha), 1.0 / (distributionIndex + 1.0));
							}
							c2 = 0.5 * (y1 + y2 + betaq * (y2 - y1));

							c1 = solutionRepair.repairSolutionVariableValue(c1, yL, yU);
							c2 = solutionRepair.repairSolutionVariableValue(c2, yL, yU);

							if (randomGenerator.getRandomValue() <= 0.5) {
								offspring.get(0).setVariableValue(i, c2);
								offspring.get(1).setVariableValue(i, c1);
							} else {
								offspring.get(0).setVariableValue(i, c1);
								offspring.get(1).setVariableValue(i, c2);
							}
						} else {
							offspring.get(0).setVariableValue(i, valueX1);
							offspring.get(1).setVariableValue(i, valueX2);
						}
					} else {
						offspring.get(0).setVariableValue(i, valueX2);
						offspring.get(1).setVariableValue(i, valueX1);
					}
				}
			}
		}

		return offspring;
	}

	@Override
	public int getNumberOfRequiredParents() {
		return 2;
	}

	@Override
	public int getNumberOfGeneratedChildren() {
		return 2;
	}

}
