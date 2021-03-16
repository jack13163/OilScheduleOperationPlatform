package opt.easyjmetal.algorithm.cmoeas.impl.c_taea;

import opt.easyjmetal.core.SolutionSet;
import opt.easyjmetal.util.comparators.DominanceComparator_CV_ASSO;

import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * 按照CV值和基于参考点的拥挤度度量来进行非支配排序
 */
public class Ranking_CV_ASSO {

	private SolutionSet solutionSet_;
	private SolutionSet[] ranking_;
	private static final Comparator dominance_ = new DominanceComparator_CV_ASSO();

	/**
	 * 计算拥挤距离，并根据它和约束违背值进行非支配排序
	 * @param solutionSet
	 * @param w
	 */
	public Ranking_CV_ASSO(SolutionSet solutionSet, double[][] w) {
		solutionSet_ = solutionSet;

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
				flagDominate = dominance_.compare(solutionSet.get(p), solutionSet.get(q));
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
				front[0].add(p);
				solutionSet.get(p).setRank(0);
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
				ranking_[j].add(solutionSet.get(it1.next()));
			}
		}
	}

	/**
	 * 获取某一水平的个体集合
	 * @param rank
	 * @return
	 */
	public SolutionSet getSubfront(int rank) {
		return ranking_[rank];
	}

	/**
	 * 获取水平数
	 * @return
	 */
	public int getNumberOfSubfronts() {
		return ranking_.length;
	}
}
