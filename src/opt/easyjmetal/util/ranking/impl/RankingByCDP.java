//  RankingByCDP.java
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

package opt.easyjmetal.util.ranking.impl;

import opt.easyjmetal.core.SolutionSet;
import opt.easyjmetal.util.comparators.line.ConstraintDominanceComparator;
import opt.easyjmetal.util.comparators.line.OverallConstraintViolationComparator;
import opt.easyjmetal.util.ranking.AbstractRanking;

import java.util.Comparator;
import java.util.Iterator;
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
public class RankingByCDP extends AbstractRanking {

	private Comparator constraint_;

	public RankingByCDP(SolutionSet solutionSet) {
		super(solutionSet);
		dominance_ = new ConstraintDominanceComparator();
		constraint_ = new OverallConstraintViolationComparator();
		ranking();
	}

	@Override
	protected void ranking() {
		// dominateMe[i] contains the number of solutions dominating i
		int[] dominateMe = new int[solutionSet_.size()];

		// iDominate[k] contains the list of solutions dominated by k
		List<Integer>[] iDominate = new List[solutionSet_.size()];

		// front[i] contains the list of individuals belonging to the front i
		List<Integer>[] front = new List[solutionSet_.size() + 1];

		// flagDominate is an auxiliar encodings.variable
		int flagDominate;

		// Initialize the fronts
		for (int i = 0; i < front.length; i++) {
			front[i] = new LinkedList<>();
		}

		// --------------- Fast non dominated sorting algorithm -----------------
		for (int p = 0; p < solutionSet_.size(); p++) {
			iDominate[p] = new LinkedList<>();
			dominateMe[p] = 0;
		}
		for (int p = 0; p < (solutionSet_.size() - 1); p++) {
			// For all q individuals , calculate if p dominates q or vice versa
			for (int q = p + 1; q < solutionSet_.size(); q++) {
				flagDominate = constraint_.compare(solutionSet_.get(p), solutionSet_.get(q));
				if (flagDominate == 0) {
					flagDominate = dominance_.compare(solutionSet_.get(p), solutionSet_.get(q));
				}
				if (flagDominate == -1) {
					iDominate[p].add(q);
					dominateMe[q]++;
				} else if (flagDominate == 1) {
					iDominate[q].add(p);
					dominateMe[p]++;
				}
			}
		}
		for (int p = 0; p < solutionSet_.size(); p++) {
			if (dominateMe[p] == 0) {
				front[0].add(p);
				solutionSet_.get(p).setRank(0);
			}
		}

		// Obtain the rest of fronts
		int i = 0;
		Iterator<Integer> it1, it2;
		while (front[i].size() != 0) {
			i++;
			it1 = front[i - 1].iterator();
			while (it1.hasNext()) {
				it2 = iDominate[it1.next()].iterator();
				while (it2.hasNext()) {
					int index = it2.next();
					dominateMe[index]--;
					if (dominateMe[index] == 0) {
						front[i].add(index);
						solutionSet_.get(index).setRank(i);
					}
				}
			}
		}

		ranking_ = new SolutionSet[i];
		// 0,1,2,....,i-1 are front, then i fronts
		for (int j = 0; j < i; j++) {
			ranking_[j] = new SolutionSet(front[j].size());
			it1 = front[j].iterator();
			while (it1.hasNext()) {
				ranking_[j].add(solutionSet_.get(it1.next()));
			}
		}
	}
}
