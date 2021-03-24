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
import opt.easyjmetal.util.comparators.DominanceComparator;
import opt.easyjmetal.util.comparators.OverallConstraintViolationComparator;

import java.util.Comparator;

/**
 * Ëæ»ú·Ö²ãÅÅÐòËã·¨
 * Jan, Muhammad Asif; Khanum, Rashida Adeeb (2013).
 * A study of two penalty-parameterless constraint handling techniques in the framework of MOEA/D.
 * Applied Soft Computing, 13(1), 128¨C148. doi:10.1016/j.asoc.2012.07.027
 */
public class StochasticRanking {

	private SolutionSet solutionSet_;
	private static final Comparator dominance_ = new DominanceComparator();
	private static final Comparator constraint_ = new OverallConstraintViolationComparator();

	public StochasticRanking(SolutionSet solutionSet) {
		solutionSet_ = solutionSet;
	}

	/**
	 * Stochastic ranking
	 * @param numberOfSolution
	 * @return
	 */
	public SolutionSet ranking(int numberOfSolution) {
		SolutionSet result = new SolutionSet();
		double pc = 0.0;
		for (int i = 0; i < Math.ceil(1.0 * numberOfSolution / 2); i++) {
			boolean swapdone = false;
			for (int j = 0; j < numberOfSolution - 1; j++) {
				if(Math.random() < pc) {

				}
			}
		}
		return result;
	}
}
