package opt.jmetal.operator.impl.mutation;

import opt.jmetal.solution.IntegerDoubleSolution;
import opt.jmetal.solution.util.RepairDoubleSolution;
import opt.jmetal.solution.util.RepairDoubleSolutionAtBounds;
import opt.jmetal.operator.MutationOperator;
import opt.jmetal.problem.DoubleProblem;
import opt.jmetal.util.JMetalException;
import opt.jmetal.util.pseudorandom.JMetalRandom;
import opt.jmetal.util.pseudorandom.RandomGenerator;

public class IntegerDoublePolynomialMutation implements MutationOperator<IntegerDoubleSolution> {

    private static final long serialVersionUID = 1L;

    private static final double DEFAULT_PROBABILITY = 0.01;
    private static final double DEFAULT_DISTRIBUTION_INDEX = 20.0;
    private double distributionIndex;
    private double mutationProbability;
    private RepairDoubleSolution solutionRepair;

    private RandomGenerator<Double> randomGenerator;

    public IntegerDoublePolynomialMutation() {
        this(DEFAULT_PROBABILITY, DEFAULT_DISTRIBUTION_INDEX);
    }

    /**
     * Constructor
     */
    public IntegerDoublePolynomialMutation(DoubleProblem problem, double distributionIndex) {
        this(1.0 / problem.getNumberOfVariables(), distributionIndex);
    }

    /**
     * Constructor
     */
    public IntegerDoublePolynomialMutation(DoubleProblem problem, double distributionIndex,
                                           RandomGenerator<Double> randomGenerator) {
        this(1.0 / problem.getNumberOfVariables(), distributionIndex);
        this.randomGenerator = randomGenerator;
    }

    /**
     * Constructor
     */
    public IntegerDoublePolynomialMutation(double mutationProbability, double distributionIndex) {
        this(mutationProbability, distributionIndex, new RepairDoubleSolutionAtBounds());
    }

    /**
     * Constructor
     */
    public IntegerDoublePolynomialMutation(double mutationProbability, double distributionIndex,
                                           RandomGenerator<Double> randomGenerator) {
        this(mutationProbability, distributionIndex, new RepairDoubleSolutionAtBounds(), randomGenerator);
    }

    /**
     * Constructor
     */
    public IntegerDoublePolynomialMutation(double mutationProbability, double distributionIndex,
                                           RepairDoubleSolution solutionRepair) {
        this(mutationProbability, distributionIndex, solutionRepair, () -> JMetalRandom.getInstance().nextDouble());
    }

    /**
     * Constructor
     */
    public IntegerDoublePolynomialMutation(double mutationProbability, double distributionIndex,
                                           RepairDoubleSolution solutionRepair, RandomGenerator<Double> randomGenerator) {
        if (mutationProbability < 0) {
            throw new JMetalException("Mutation probability is negative: " + mutationProbability);
        } else if (distributionIndex < 0) {
            throw new JMetalException("Distribution index is negative: " + distributionIndex);
        }
        this.mutationProbability = mutationProbability;
        this.distributionIndex = distributionIndex;
        this.solutionRepair = solutionRepair;

        this.randomGenerator = randomGenerator;
    }

    /* Getters */
    public double getMutationProbability() {
        return mutationProbability;
    }

    public double getDistributionIndex() {
        return distributionIndex;
    }

    /* Setters */
    public void setMutationProbability(double probability) {
        this.mutationProbability = probability;
    }

    public void setDistributionIndex(double distributionIndex) {
        this.distributionIndex = distributionIndex;
    }

    @Override
    public IntegerDoubleSolution execute(IntegerDoubleSolution solution) {
        if (null == solution) {
            throw new JMetalException("Null parameter");
        }

        doMutation(mutationProbability, solution);
        return solution;
    }

    /**
     * Perform the mutation operation
     */
    private void doMutation(double probability, IntegerDoubleSolution solution) {

        for (int i = 0; i < solution.getNumberOfVariables(); i++) {
            if (randomGenerator.getRandomValue() <= probability) {
                if (i < solution.getNumberOfIntegerVariables()) {
                    int y = (int) compute(solution, i);
                    solution.setVariableValue(i, y);
                } else {
                    double y = compute(solution, i);
                    solution.setVariableValue(i, y);
                }
            }
        }
    }

    /**
     * 由于整形和浮点型计算方法一样，所以统一封装
     *
     * @return
     */
    private double compute(IntegerDoubleSolution solution, int i) {
        Double rnd, delta1, delta2, mutPow, deltaq;
        double y, yl, yu, val, xy;

        y = solution.getVariableValue(i).doubleValue();
        yl = solution.getLowerBound(i).doubleValue();
        yu = solution.getUpperBound(i).doubleValue();
        if (yl == yu) {
            y = yl;
        } else {
            delta1 = (y - yl) / (yu - yl);
            delta2 = (yu - y) / (yu - yl);
            rnd = randomGenerator.getRandomValue();
            mutPow = 1.0 / (distributionIndex + 1.0);
            if (rnd <= 0.5) {
                xy = 1.0 - delta1;
                val = 2.0 * rnd + (1.0 - 2.0 * rnd) * (Math.pow(xy, distributionIndex + 1.0));
                deltaq = Math.pow(val, mutPow) - 1.0;
            } else {
                xy = 1.0 - delta2;
                val = 2.0 * (1.0 - rnd) + 2.0 * (rnd - 0.5) * (Math.pow(xy, distributionIndex + 1.0));
                deltaq = 1.0 - Math.pow(val, mutPow);
            }
            y = y + deltaq * (yu - yl);
            y = solutionRepair.repairSolutionVariableValue(y, yl, yu);
        }
        return y;
    }
}
