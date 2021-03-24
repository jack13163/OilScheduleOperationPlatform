//  HypervolumeArchive.java
//
//  Author:
//       Antonio J. Nebro <antonio@lcc.uma.es>
//
//  Copyright (c) 2013 Antonio J. Nebro
//
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU Lesser General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU Lesser General Public License for more details.
//
//  You should have received a copy of the GNU Lesser General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.

package opt.easyjmetal.util.archive;

import opt.easyjmetal.core.Solution;
import opt.easyjmetal.qualityindicator.util.MetricsUtil;
import opt.easyjmetal.util.distance.Distance;
import opt.easyjmetal.util.comparators.one.CrowdingDistanceComparator;
import opt.easyjmetal.util.comparators.one.DominanceComparator;
import opt.easyjmetal.util.comparators.one.EqualSolutionsComparator;

import java.util.Comparator;

/**
 * 按照超体积贡献值进行排序的储备集
 */
public class HypervolumeArchive extends Archive {

    private int maxSize_;
    private int objectives_;
    private Comparator dominance_;
    private Comparator equals_;
    private Distance distance_;
    private MetricsUtil utils_;
    private double offset_;
    private Comparator crowdingDistance_;

    /**
     * Constructor.
     *
     * @param maxSize            The maximum size of the archive.
     * @param numberOfObjectives The number of objectives.
     */
    public HypervolumeArchive(int maxSize, int numberOfObjectives) {
        super(maxSize);
        maxSize_ = maxSize;
        objectives_ = numberOfObjectives;
        dominance_ = new DominanceComparator();
        equals_ = new EqualSolutionsComparator();
        distance_ = new Distance();
        utils_ = new MetricsUtil();
        offset_ = 100;
        crowdingDistance_ = new CrowdingDistanceComparator();
    }


    /**
     * Adds a <code>Solution</code> to the archive. If the <code>Solution</code>
     * is dominated by any member of the archive, then it is discarded. If the
     * <code>Solution</code> dominates some members of the archive, these are
     * removed. If the archive is full and the <code>Solution</code> has to be
     * inserted, the solutions are sorted by crowding distance and the one having
     * the minimum crowding distance value.
     *
     * @param solution The <code>Solution</code>
     * @return true if the <code>Solution</code> has been inserted, false
     * otherwise.
     */
    @Override
    public boolean add(Solution solution) {
        int flag = 0;
        int i = 0;
        Solution aux;

        while (i < solutionsList_.size()) {
            aux = solutionsList_.get(i);

            flag = dominance_.compare(solution, aux);
            // 被支配，不加入集合
            if (flag == 1) {
                return false;
            } else if (flag == -1) {
                // 支配别人，则移除别人
                solutionsList_.remove(i);
            } else {
                // 目标值相同，则不加入集合
                if (equals_.compare(aux, solution) == 0) {
                    return false;
                }
                i++;
            }
        }
        // 将解加入到集合中
        solutionsList_.add(solution);
        // 调整集合大小
        if (size() > maxSize_) {
            // The archive is full
            double[][] frontValues = this.writeObjectivesToMatrix();
            int numberOfObjectives = objectives_;
            // STEP 1. Obtain the maximum and minimum values of the Pareto front
            double[] maximumValues = utils_.getMaximumValues(this.writeObjectivesToMatrix(), numberOfObjectives);
            double[] minimumValues = utils_.getMinimumValues(this.writeObjectivesToMatrix(), numberOfObjectives);
            // STEP 2. Get the normalized front
            double[][] normalizedFront = utils_.getNormalizedFront(frontValues, maximumValues, minimumValues);
            // compute offsets for reference point in normalized space
            double[] offsets = new double[maximumValues.length];
            for (i = 0; i < maximumValues.length; i++) {
                offsets[i] = offset_ / (maximumValues[i] - minimumValues[i]);
            }
            // STEP 3. Inverse the pareto front. This is needed because the original metric by Zitzler is for maximization problems
            double[][] invertedFront = utils_.invertedFront(normalizedFront);
            // shift away from origin, so that boundary points also get a contribution > 0
            for (double[] point : invertedFront) {
                for (i = 0; i < point.length; i++) {
                    point[i] += offsets[i];
                }
            }

            // calculate contributions and sort
            double[] contributions = utils_.hvContributions(objectives_, invertedFront);
            for (i = 0; i < contributions.length; i++) {
                // contribution values are used analogously to crowding distance
                this.get(i).setCrowdingDistance(contributions[i]);
            }

            this.sort(new CrowdingDistanceComparator());

            // remove(indexWorst(crowdingDistance_));
            remove(size() - 1);
        }
        return true;
    }

    public int getLocation(Solution solution) {
        int location = -1;
        int index = 0;
        while ((index < size()) && (location != -1)) {
            if (equals_.compare(solution, get(index)) == 0) {
                location = index;
            }
            index++;
        }
        return location;
    }
}
