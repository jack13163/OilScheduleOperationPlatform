package opt.easyjmetal.algorithm.common;

import opt.easyjmetal.core.SolutionSet;
import opt.easyjmetal.util.comparators.DominanceComparator;

import java.util.Arrays;
import java.util.Comparator;

public class StrengthRawFitness {

    private static final Comparator DOMINANCE_COMPARATOR = new DominanceComparator();
    // k-th individual
    private int k;

    public StrengthRawFitness(int k) {
        this.k = k;
    }

    public StrengthRawFitness() {
        this.k = 1;
    }

    public void computeDensityEstimator(SolutionSet solutionSet) {
        double[][] distance = solutionSet.writeObjectivesToMatrix();
        double[] strength = new double[solutionSet.size()];
        double[] rawFitness = new double[solutionSet.size()];
        double kDistance;

        // strength(i) = |{j | j <- SolutionSet and i dominate j}|
        for (int i = 0; i < solutionSet.size(); i++) {
            for (int j = 0; j < solutionSet.size(); j++) {
                if (DOMINANCE_COMPARATOR.compare(solutionSet.get(i), solutionSet.get(j)) == -1) {
                    strength[i] += 1.0;
                }
            }
        }

        //Calculate the raw fitness
        // rawFitness(i) = |{sum strenght(j) | j <- SolutionSet and j dominate i}|
        for (int i = 0; i < solutionSet.size(); i++) {
            for (int j = 0; j < solutionSet.size(); j++) {
                if (DOMINANCE_COMPARATOR.compare(solutionSet.get(i), solutionSet.get(j)) == 1) {
                    rawFitness[i] += strength[j];
                }
            }
        }

        // Add the distance to the k-th individual. In the reference paper of SPEA2,
        // k = sqrt(population.size()), but a value of k = 1 is recommended. See
        // http://www.tik.ee.ethz.ch/pisa/selectors/spea2/spea2_documentation.txt
        for (int i = 0; i < distance.length; i++) {
            Arrays.sort(distance[i]);
            kDistance = 1.0 / (distance[i][k] + 2.0);
            solutionSet.get(i).setFitness(rawFitness[i] + kDistance);
        }
    }

    public int getK() {
        return k;
    }
}
