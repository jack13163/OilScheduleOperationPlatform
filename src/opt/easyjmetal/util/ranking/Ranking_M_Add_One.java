//  Ranking.java
//
//  Author:
//       Antonio J. Nebro <antonio@lcc.uma.es>
//       Juan J. Durillo <durillo@lcc.uma.es>
//
//  Copyright (c) 2011 Antonio J. Nebro, Juan J. Durillo
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

package opt.easyjmetal.util.ranking;

import opt.easyjmetal.core.SolutionSet;
import opt.easyjmetal.util.comparators.DominanceComparator_M_Add_One;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * This class implements some facilities for ranking solutions. Given a
 * <code>SolutionSet</code> object, their solutions are ranked according to
 * scheme proposed in NSGA-II; as a result, a set of subsets are obtained. The
 * subsets are numbered starting from 0 (in NSGA-II, the numbering starts from
 * 1); thus, subset 0 contains the non-dominated solutions, subset 1 contains
 * the non-dominated solutions after removing those belonging to subset 0, and
 * so on.
 */
public class Ranking_M_Add_One {

    private SolutionSet solutionSet_;
    private SolutionSet[] ranking_;
    private static final Comparator dominance_ = new DominanceComparator_M_Add_One();

    /**
     * Constructor.
     *
     * @param solutionSet The <code>SolutionSet</code> to be ranked.
     */
    public  Ranking_M_Add_One(SolutionSet solutionSet) {
        solutionSet_ = solutionSet;
    }

    public List<Integer> GetNondominatedIndexes() {

        // dominateMe[i] contains the number of solutions dominating i
        int[] dominateMe = new int[solutionSet_.size()];

        // iDominate[k] contains the list of solutions dominated by k
        List<Integer>[] iDominate = new List[solutionSet_.size()];

        // front[i] contains the list of individuals belonging to the front i
        List<Integer> firstFront = new LinkedList<Integer>();

        // flagDominate is an auxiliar encodings.variable
        int flagDominate;


		/*
         * //-> Fast non dominated sorting algorithm for (int p = 0; p <
		 * solutionSet_.size(); p++) { // Initialice the list of individuals
		 * that i dominate and the number // of individuals that dominate me
		 * iDominate[p] = new LinkedList<Integer>(); dominateMe[p] = 0; // For
		 * all q individuals , calculate if p dominates q or vice versa for (int
		 * q = 0; q < solutionSet_.size(); q++) { flagDominate
		 * =constraint_.compare(solutionSet.get(p),solutionSet.get(q)); if
		 * (flagDominate == 0) { flagDominate
		 * =dominance_.compare(solutionSet.get(p),solutionSet.get(q)); }
		 *
		 * if (flagDominate == -1) { iDominate[p].add(new Integer(q)); } else if
		 * (flagDominate == 1) { dominateMe[p]++; } }
		 *
		 * // If nobody dominates p, p belongs to the first front if
		 * (dominateMe[p] == 0) { front[0].add(new Integer(p));
		 * solutionSet.get(p).setRank(0); } }
		 */

        // -> Fast non dominated sorting algorithm
        // Contribution of Guillaume Jacquenot
        for (int p = 0; p < solutionSet_.size(); p++) {
            // Initialize the list of individuals that i dominate and the number
            // of individuals that dominate me
            iDominate[p] = new LinkedList<Integer>();
            dominateMe[p] = 0;
        }
        for (int p = 0; p < (solutionSet_.size() - 1); p++) {
            // For all q individuals , calculate if p dominates q or vice versa
            for (int q = p + 1; q < solutionSet_.size(); q++) {

                flagDominate = dominance_.compare(solutionSet_.get(p),
                        solutionSet_.get(q));

                if (flagDominate == -1) {
                    iDominate[p].add(q);
                    dominateMe[q]++;
                } else if (flagDominate == 1) {
                    iDominate[q].add(p);
                    dominateMe[p]++;
                }
            }
            // If nobody dominates p, p belongs to the first front
        }
        for (int p = 0; p < solutionSet_.size(); p++) {
            if (dominateMe[p] == 0) {
                firstFront.add(p);
            }
        }

        return firstFront;

    } // Ranking

} // Ranking
