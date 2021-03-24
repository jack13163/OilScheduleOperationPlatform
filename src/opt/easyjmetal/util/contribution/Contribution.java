package opt.easyjmetal.util.contribution;

import opt.easyjmetal.core.SolutionSet;
import opt.easyjmetal.qualityindicator.util.MetricsUtil;

/**
 * 种群中个体贡献值计算
 */
public class Contribution {
    private static MetricsUtil utils_ = new MetricsUtil();

    /**
     * 计算解集集合中每个个体对HV指标的贡献
     * @param solutionSet
     */
    public static void calculateHVContribution(SolutionSet solutionSet) {
        if (solutionSet.size() > 2) {
            double offset_ = 100;
            // The contribution can be updated
            double[][] frontValues = solutionSet.writeObjectivesToMatrix();
            // STEP 1. Obtain the maximum and minimum values of the Pareto front
            int numberOfObjectives = frontValues[0].length;
            double[] maximumValues = utils_.getMaximumValues(frontValues, numberOfObjectives);
            double[] minimumValues = utils_.getMinimumValues(frontValues, numberOfObjectives);
            // STEP 2. Get the normalized front
            double[][] normalizedFront = utils_.getNormalizedFront(frontValues, maximumValues, minimumValues);
            // compute offsets for reference point in normalized space
            double[] offsets = new double[maximumValues.length];
            for (int i = 0; i < maximumValues.length; i++) {
                offsets[i] = offset_ / (maximumValues[i] - minimumValues[i]);
            }
            // STEP 3. Inverse the pareto front. This is needed because the original metric by Zitzler is for maximization problems
            double[][] invertedFront = utils_.invertedFront(normalizedFront);
            // shift away from origin, so that boundary points also get a contribution > 0
            for (double[] point : invertedFront) {
                for (int i = 0; i < point.length; i++) {
                    point[i] += offsets[i];
                }
            }

            // calculate contributions and sort
            double[] contributions = utils_.hvContributions(numberOfObjectives, invertedFront);
            for (int i = 0; i < contributions.length; i++) {
                solutionSet.get(i).setContribution(contributions[i]);
            }
        }
    }
}
