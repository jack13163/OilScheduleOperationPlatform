package opt.easyjmetal.algorithm.cmoeas.impl.c_taea;

import opt.easyjmetal.algorithm.cmoeas.impl.nsgaiii_cdp.EnvironmentalSelection;
import opt.easyjmetal.algorithm.common.MatlabUtilityFunctionsWrapper;
import opt.easyjmetal.algorithm.common.ReferencePoint;
import opt.easyjmetal.core.Solution;
import opt.easyjmetal.core.SolutionSet;
import opt.easyjmetal.util.JMException;
import opt.easyjmetal.util.ranking.impl.CDPRanking;
import opt.easyjmetal.util.comparators.one.FitnessComparator;
import opt.easyjmetal.util.comparators.one.OverallConstraintViolationComparator;

import java.util.ArrayList;
import java.util.List;

public class TwoArchiveUpdate {

    /**
     * 更新储备集
     *
     * @param ca      储备集
     * @param q       子代种群个体
     * @param lambda_
     * @return
     */
    public static SolutionSet updateCA(SolutionSet ca, SolutionSet q, double[][] lambda_) throws JMException {
        SolutionSet s = new SolutionSet();
        SolutionSet hc = ca.union(q);
        SolutionSet sc = hc.getFeasible();
        // 储备集的容量
        int popSize = q.getCapacity();
        int numberOfObjectives = lambda_[0].length;

        if (sc.size() == popSize) {
            s = sc;
        } else if (sc.size() > popSize) {
            // 非支配排序
            CDPRanking ranking = new CDPRanking(sc);

            // 按层合并
            int rankingIndex = 0;
            int candidateSolutions = 0;
            SolutionSet pop = new SolutionSet(popSize);
            List<SolutionSet> fronts = new ArrayList<>();
            while (candidateSolutions < popSize) {
                SolutionSet solutions = ranking.getSubfront(rankingIndex);
                if ((pop.size() + ranking.getSubfront(rankingIndex).size()) <= popSize) {
                    for (int i = 0; i < solutions.size(); i++) {
                        pop.add(solutions.get(i));
                    }
                }
                candidateSolutions += ranking.getSubfront(rankingIndex).size();
                rankingIndex++;
                fronts.add(solutions);
            }

            // 通过环境选择将多的元素删除
            EnvironmentalSelection selection =
                    new EnvironmentalSelection(
                            fronts,
                            popSize,
                            ReferencePoint.generateReferencePoints(lambda_),
                            numberOfObjectives);

            s = selection.execute(pop);
        } else {
            SolutionSet si = hc.getInfeasible();
            s = s.union(sc);
            try {
                double[] g_tch = MatlabUtilityFunctionsWrapper.g_tch(si.writeObjectivesToMatrix(), lambda_);
                // 计算不可行解解集中每个个体的拥挤距离
                for (int i = 0; i < si.size(); i++) {
                    Solution solution = si.get(i);
                    double distance = g_tch[i];
                    solution.setCrowdingDistance(distance);
                }

                // 按照CV值和基于参考点的拥挤度度量来进行非支配排序
                Ranking_CV_ASSO ranking = new Ranking_CV_ASSO(si, lambda_);

                int maxNo = ranking.getNumberOfSubfronts();
                int last = 0;

                for (int i = 0; i < maxNo; i++) {
                    SolutionSet front = ranking.getSubfront(i);
                    s = s.union(front);
                    if (s.size() >= popSize) {
                        last = i;
                        break;
                    }
                }

                if (s.size() > popSize) {
                    s = sc;
                    for (int i = 0; i < last; i++) {
                        s = s.union(ranking.getSubfront(i));
                    }
                    SolutionSet F_last = ranking.getSubfront(last);
                    F_last.sort(new OverallConstraintViolationComparator());
                    for (int i = s.size(); i < popSize; i++) {
                        s.add(F_last.get(i - s.size()));
                    }
                }
            } catch (Exception e) {
                System.out.println("Matlab调用失败");
                e.printStackTrace();
            }
        }

        return s;
    }

    /**
     * 更新储备集
     *
     * @param ca
     * @param da
     * @param q
     * @param w
     * @return
     */
    public static SolutionSet updateDA(SolutionSet ca, SolutionSet da, SolutionSet q, double[][] w) {
        int popSize = q.getCapacity();
        SolutionSet s = new SolutionSet();
        SolutionSet hd = da.union(q);

        double[] Region_Hd = MatlabUtilityFunctionsWrapper.associate(hd.writeObjectivesToMatrix(), w);
        double[] Region_CA = MatlabUtilityFunctionsWrapper.associate(ca.writeObjectivesToMatrix(), w);
        int itr = 1;

        while (s.size() < popSize) {
            for (int i = 0; i < popSize; i++) {
                // current_c=find(Region_CA==i);
                List<Integer> current_c = findIndexs(Region_CA, i);

                if (current_c.size() < itr) {
                    // j denotes the number of solutions from Hd that need to join into the region(i)
                    for (int j = 0; j < itr - current_c.size(); j++) {
                        // current_d=find(Region_Hd==i);
                        List<Integer> current_d = findIndexs(Region_Hd, i);
                        if (current_d != null && !current_d.isEmpty()) {
                            SolutionSet o = new SolutionSet();
                            for (int k = 0; k < current_d.size(); k++) {
                                int ind = current_d.get(k);
                                o.add(hd.get(ind));
                            }

                            // 非支配排序
                            CDPRanking ranking = new CDPRanking(o);
                            o = ranking.getSubfront(0);

                            double[] g_tch = MatlabUtilityFunctionsWrapper.g_tch(o.writeObjectivesToMatrix(), w);
                            for (int k = 0; k < o.size(); k++) {
                                o.get(k).setFitness(g_tch[k]);
                            }
                            int x_best_index = o.indexBest(new FitnessComparator());
                            Solution x_best = o.get(x_best_index);

                            // Hd(current_d(Hd(current_d)==x_best))=[];% update Region_Hd
                            hd.remove(x_best_index);

                            if (hd.size() == 0) {
                                Region_Hd = null;
                            }else{
                                Region_Hd = MatlabUtilityFunctionsWrapper.associate(hd.writeObjectivesToMatrix(), w);
                            }

                            if(s.size() < popSize) {
                                s.add(x_best);
                            }
                        } else {
                            break;
                        }
                    }
                }

                if (s.size() == popSize) {
                    break;
                }
            }
            itr=itr+1;
        }
        return s;
    }

    private static List<Integer> findIndexs(double[] data, int l) {
        if (data == null || data.length == 0) {
            return null;
        }
        List<Integer> current_c = new ArrayList<>();
        for (int j = 0; j < data.length; j++) {
            if (data[j] == l) {
                current_c.add(j);
            }
        }
        return current_c;
    }
}
