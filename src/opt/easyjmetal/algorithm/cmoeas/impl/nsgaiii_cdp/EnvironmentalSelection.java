package opt.easyjmetal.algorithm.cmoeas.impl.nsgaiii_cdp;


import opt.easyjmetal.algorithm.common.ReferencePoint;
import opt.easyjmetal.core.Solution;
import opt.easyjmetal.core.SolutionSet;
import opt.easyjmetal.util.JMException;
import opt.jmetal.util.pseudorandom.JMetalRandom;

import java.util.ArrayList;
import java.util.List;

public class EnvironmentalSelection {

    private List<SolutionSet> fronts;
    private int solutionsToSelect;
    private List<ReferencePoint> referencePoints;
    private int numberOfObjectives;

    public EnvironmentalSelection(List<SolutionSet> fronts, int solutionsToSelect, List<ReferencePoint> referencePoints, int numberOfObjectives) {
        this.fronts = fronts;
        this.solutionsToSelect = solutionsToSelect;
        this.referencePoints = referencePoints;
        this.numberOfObjectives = numberOfObjectives;
    }

    public List<Double> translateObjectives(SolutionSet population) {
        List<Double> ideal_point;
        ideal_point = new ArrayList<>(numberOfObjectives);

        for (int f = 0; f < numberOfObjectives; f += 1) {
            double minf = Double.MAX_VALUE;
            for (int i = 0; i < fronts.get(0).size(); i += 1) // min values must appear in the first front
            {
                minf = Math.min(minf, fronts.get(0).get(i).getObjective(f));
            }
            ideal_point.add(minf);

            for (SolutionSet list : fronts) {

                for (int i = 0; i < list.size(); i++) {
                    list.get(i).setConvertedObjective(f, list.get(i).getObjective(f) - minf);
                }
            }
        }

        return ideal_point;
    }


    // 寻找极端值
    private SolutionSet findExtremePoints(SolutionSet population) {
        SolutionSet extremePoints = new SolutionSet(numberOfObjectives);
        opt.easyjmetal.core.Solution min_indv = null;
        for (int f = 0; f < numberOfObjectives; f += 1) {
            double min_ASF = Double.MAX_VALUE;
            for (int i = 0; i < fronts.get(0).size(); i++) {// only consider the individuals in the first front
                opt.easyjmetal.core.Solution s = fronts.get(0).get(i);
                double asf = ASF(s, f);
                if (asf < min_ASF) {
                    min_ASF = asf;
                    min_indv = s;
                }
            }

            extremePoints.add(min_indv);
        }
        return extremePoints;
    }

    // ----------------------------------------------------------------------
    // ASF: Achivement Scalarization Function
    // I implement here a effcient version of it, which only receives the index
    // of the objective which uses 1.0; the rest will use 0.00001. This is
    // different to the one impelemented in C++
    // ----------------------------------------------------------------------
    private double ASF(Solution s, int index) {
        double max_ratio = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < s.getNumberOfObjectives(); i++) {
            double weight = (index == i) ? 1.0 : 0.000001;
            max_ratio = Math.max(max_ratio, s.getObjective(i) / weight);
        }
        return max_ratio;
    }

    // 高斯消去法
    public List<Double> guassianElimination(List<List<Double>> A, List<Double> b) {
        List<Double> x = new ArrayList<>();

        int N = A.size();
        for (int i = 0; i < N; i += 1) {
            A.get(i).add(b.get(i));
        }

        for (int base = 0; base < N - 1; base += 1) {
            for (int target = base + 1; target < N; target += 1) {
                double ratio = A.get(target).get(base) / A.get(base).get(base);
                for (int term = 0; term < A.get(base).size(); term += 1) {
                    A.get(target).set(term, A.get(target).get(term) - A.get(base).get(term) * ratio);
                }
            }
        }

        for (int i = 0; i < N; i++) {
            x.add(0.0);
        }

        for (int i = N - 1; i >= 0; i -= 1) {
            for (int known = i + 1; known < N; known += 1) {
                A.get(i).set(N, A.get(i).get(N) - A.get(i).get(known) * x.get(known));
            }
            x.set(i, A.get(i).get(N) / A.get(i).get(i));
        }
        return x;
    }

    // 构造超平面
    public List<Double> constructHyperplane(SolutionSet population, SolutionSet extreme_points) {
        // Check whether there are duplicate extreme points.
        // This might happen but the original paper does not mention how to deal with it.
        boolean duplicate = false;
        for (int i = 0; !duplicate && i < extreme_points.size(); i += 1) {
            for (int j = i + 1; !duplicate && j < extreme_points.size(); j += 1) {
                duplicate = extreme_points.get(i).equals(extreme_points.get(j));
            }
        }

        List<Double> intercepts = new ArrayList<>();

        if (duplicate) // cannot construct the unique hyperplane (this is a casual method to deal with the condition)
        {
            for (int f = 0; f < numberOfObjectives; f += 1) {
                // extreme_points[f] stands for the individual with the largest value of objective f
                intercepts.add(extreme_points.get(f).getObjective(f));
            }
        } else {
            // Find the equation of the hyperplane
            List<Double> b = new ArrayList<>(); //(pop[0].objs().size(), 1.0);
            for (int i = 0; i < numberOfObjectives; i++) {
                b.add(1.0);
            }

            List<List<Double>> A = new ArrayList<>();
            for (int i = 0; i < extreme_points.size(); i++) {
                opt.easyjmetal.core.Solution s = extreme_points.get(i);
                List<Double> aux = new ArrayList<>();
                for (int j = 0; j < numberOfObjectives; j++) {
                    aux.add(s.getObjective(j));
                }
                A.add(aux);
            }

            List<Double> x = guassianElimination(A, b);

            // Find intercepts
            for (int f = 0; f < numberOfObjectives; f += 1) {
                intercepts.add(1.0 / x.get(f));
            }
        }
        return intercepts;
    }

    // 标准化
    public void normalizeObjectives(SolutionSet population, List<Double> intercepts, List<Double> ideal_point) {
        for (int t = 0; t < fronts.size(); t += 1) {
            for (int i = 0; i < fronts.get(t).size(); i++) {
                opt.easyjmetal.core.Solution s = fronts.get(t).get(i);

                for (int f = 0; f < numberOfObjectives; f++) {
                    if (Math.abs(intercepts.get(f) - ideal_point.get(f)) > 10e-10) {
                        s.setConvertedObjective(f, s.getConvertedObjective(f) / (intercepts.get(f) - ideal_point.get(f)));
                    } else {
                        s.setConvertedObjective(f, s.getConvertedObjective(f) / (10e-10));
                    }

                }
            }
        }
    }

    public double perpendicularDistance(List<Double> direction, List<Double> point) {
        double numerator = 0, denominator = 0;
        for (int i = 0; i < direction.size(); i += 1) {
            numerator += direction.get(i) * point.get(i);
            denominator += Math.pow(direction.get(i), 2.0);
        }
        double k = numerator / denominator;

        double d = 0;
        for (int i = 0; i < direction.size(); i += 1) {
            d += Math.pow(k * direction.get(i) - point.get(i), 2.0);
        }
        return Math.sqrt(d);
    }

    // 为种群中的每一个个体关联参考点
    public void associate(SolutionSet population) {

        for (int t = 0; t < fronts.size(); t++) {
            for (int i = 0; i < fronts.get(t).size(); i++) {
                Solution s = fronts.get(t).get(i);

                int min_rp = -1;
                double min_dist = Double.MAX_VALUE;
                // 找出当前个体s的参考点
                for (int r = 0; r < this.referencePoints.size(); r++) {
                    List<Double> values = new ArrayList<>();
                    for (int j = 0; j < numberOfObjectives; j++) {
                        values.add(s.getConvertedObjective(j));
                    }
                    // 计算点到直线的距离
                    double d = perpendicularDistance(this.referencePoints.get(r).position, values);
                    if (d < min_dist) {
                        min_dist = d;
                        min_rp = r;
                    }
                }
                if (t + 1 != fronts.size()) {
                    this.referencePoints.get(min_rp).AddMember();
                } else {
                    this.referencePoints.get(min_rp).AddPotentialMember(s, min_dist);
                }
            }
        }

    }


    int FindNicheReferencePoint() {
        // find the minimal cluster size
        int min_size = Integer.MAX_VALUE;
        for (ReferencePoint referencePoint : this.referencePoints) {
            min_size = Math.min(min_size, referencePoint.MemberSize());
        }

        // find the reference points with the minimal cluster size Jmin
        List<Integer> min_rps = new ArrayList<>();


        for (int r = 0; r < this.referencePoints.size(); r += 1) {
            if (this.referencePoints.get(r).MemberSize() == min_size) {
                min_rps.add(r);
            }
        }
        // return a random reference point (j-bar)
        return min_rps.get(min_rps.size() > 1 ? JMetalRandom.getInstance().nextInt(0, min_rps.size() - 1) : 0);
    }

    // ----------------------------------------------------------------------
    // SelectClusterMember():
    //
    // Select a potential member (an individual in the front Fl) and associate
    // it with the reference point.
    //
    // Check the last two paragraphs in Section IV-E in the original paper.
    // ----------------------------------------------------------------------
    Solution SelectClusterMember(ReferencePoint rp) {
        Solution chosen = null;
        if (rp.HasPotentialMember()) {
            if (rp.MemberSize() == 0) // currently has no member
            {
                chosen = rp.FindClosestMember();
            } else {
                chosen = rp.RandomMember();
            }
        }
        return chosen;
    }

    /* This method performs the environmental Selection indicated in the paper describing NSGAIII*/
    public SolutionSet execute(SolutionSet source) throws JMException {
        // The comments show the C++ code

        // ---------- Steps 9-10 in Algorithm 1 ----------
        if (source.size() == this.solutionsToSelect) {
            return source;
        }


        // ---------- Step 14 / Algorithm 2 ----------
        //vector<double> ideal_point = TranslateObjectives(&cur, fronts);
        List<Double> ideal_point = translateObjectives(source);
        SolutionSet extreme_points = findExtremePoints(source);
        List<Double> intercepts = constructHyperplane(source, extreme_points);

        normalizeObjectives(source, intercepts, ideal_point);
        // ---------- Step 15 / Algorithm 3, Step 16 ----------
        associate(source);

        // ---------- Step 17 / Algorithm 4 ----------
        while (source.size() < this.solutionsToSelect) {
            int min_rp = FindNicheReferencePoint();

            Solution chosen = SelectClusterMember(this.referencePoints.get(min_rp));
            if (chosen == null) // no potential member in Fl, disregard this reference point
            {
                this.referencePoints.remove(min_rp);
            } else {
                this.referencePoints.get(min_rp).AddMember();
                this.referencePoints.get(min_rp).RemovePotentialMember(chosen);
                source.add(chosen);
            }
        }

        return source;
    }
}
