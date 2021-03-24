package opt.easyjmetal.util.ranking;

import opt.easyjmetal.core.SolutionSet;

import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * 分层函数
 */
public abstract class AbstractRanking {
    protected SolutionSet solutionSet_;
    protected SolutionSet[] ranking_;
    protected Comparator dominance_;

    public AbstractRanking(SolutionSet solutionSet) {
        this.solutionSet_ = solutionSet;
    }

    /**
     * 排序
     */
    public void ranking() {
        // dominateMe[i] contains the number of solutions dominating i
        int[] dominateMe = new int[solutionSet_.size()];

        // iDominate[k] contains the list of solutions dominated by k
        List<Integer>[] iDominate = new List[solutionSet_.size()];

        // front[i] contains the list of individuals belonging to the front i
        List<Integer>[] front = new List[solutionSet_.size() + 1];

        // flagDominate is an auxiliar encodings.variable
        int flagDominate;

        // Initialize the fronts
        for (int i = 0; i < front.length; i++)
            front[i] = new LinkedList<>();

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
                flagDominate = 0;
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
            // If nobody dominates p, p belongs to the first front
        }
        for (int p = 0; p < solutionSet_.size(); p++) {
            if (dominateMe[p] == 0) {
                front[0].add(p);
                solutionSet_.get(p).setRank(0);
            }
        }

        // Obtain the rest of fronts
        int i = 0;
        Iterator<Integer> it1, it2; // Iterators
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
        // <-

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

    /**
     * Returns a <code>SolutionSet</code> containing the solutions of a given
     * rank.
     *
     * @param rank The rank
     * @return Object representing the <code>SolutionSet</code>.
     */
    public SolutionSet getSubfront(int rank) {
        if(ranking_ == null){
            ranking();
        }
        return ranking_[rank];
    }

    /**
     * Returns the total number of subFronts founds.
     */
    public int getNumberOfSubfronts() {
        if(ranking_ == null){
            ranking();
        }
        return ranking_.length;
    }
}
