package opt.easyjmetal.util.distance.impl;

import opt.easyjmetal.core.Solution;
import opt.easyjmetal.util.comparators.ObjectiveComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 实现拥挤距离的计算
 */
public class CrowdingDistance {

    public void computeDensityEstimator(List<Solution> solutionList) {
        int size = solutionList.size();

        if (size == 0) {
            return;
        }

        if (size == 1) {
            solutionList.get(0).setCrowdingDistance(Double.POSITIVE_INFINITY);
            return;
        }

        if (size == 2) {
            solutionList.get(0).setCrowdingDistance(Double.POSITIVE_INFINITY);
            solutionList.get(1).setCrowdingDistance(Double.POSITIVE_INFINITY);
            return;
        }

        // Use a new SolutionSet to avoid altering the original solutionSet
        List<Solution> front = new ArrayList<>(size);
        for (Solution solution : solutionList) {
            front.add(solution);
        }

        for (int i = 0; i < size; i++) {
            front.get(i).setCrowdingDistance(0.0);
        }

        double objetiveMaxn;
        double objetiveMinn;
        double distance;

        int numberOfObjectives = solutionList.get(0).getNumberOfObjectives();

        for (int i = 0; i < numberOfObjectives; i++) {
            // Sort the population by Obj n
            Collections.sort(front, new ObjectiveComparator(i));
            objetiveMinn = front.get(0).getObjective(i);
            objetiveMaxn = front.get(front.size() - 1).getObjective(i);

            // Set de crowding distance
            front.get(0).setCrowdingDistance(Double.POSITIVE_INFINITY);
            front.get(size - 1).setCrowdingDistance(Double.POSITIVE_INFINITY);

            for (int j = 1; j < size - 1; j++) {
                distance = front.get(j + 1).getObjective(i) - front.get(j - 1).getObjective(i);
                distance = distance / (objetiveMaxn - objetiveMinn);
                distance += (double) front.get(j).getCrowdingDistance();
                front.get(j).setCrowdingDistance(distance);
            }
        }
    }
}

