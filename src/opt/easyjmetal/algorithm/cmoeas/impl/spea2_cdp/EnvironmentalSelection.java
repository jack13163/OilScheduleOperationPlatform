package opt.easyjmetal.algorithm.cmoeas.impl.spea2_cdp;

import opt.easyjmetal.util.fitness.StrengthRawFitness;
import opt.easyjmetal.core.Solution;
import opt.easyjmetal.core.SolutionSet;
import opt.easyjmetal.util.distance.Distance;
import opt.easyjmetal.util.comparators.line.FitnessComparator;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class EnvironmentalSelection {

    private int solutionsToSelect;
    private StrengthRawFitness strengthRawFitness;

    Distance _distance = new Distance();

    public EnvironmentalSelection(int solutionsToSelect) {
        this(solutionsToSelect, 1);
    }

    public EnvironmentalSelection(int solutionsToSelect, int k) {
        this.solutionsToSelect = solutionsToSelect;
        this.strengthRawFitness = new StrengthRawFitness(k);// k推荐为1
    }

    public SolutionSet execute(SolutionSet source2) {
        // 计算适应度值
        this.strengthRawFitness.computeDensityEstimator(source2);

        int size;
        List<Solution> source = new ArrayList<>(source2.size());
        for (int i = 0; i < source2.size(); i++) {
            source.add(source2.get(i));
        }
        if (source2.size() < this.solutionsToSelect) {
            size = source.size();
        } else {
            size = this.solutionsToSelect;
        }

        SolutionSet aux = new SolutionSet(source.size());

        // 先选出非支配解，即fitness小于1的解
        int i = 0;
        while (i < source.size()) {
            double fitness = source.get(i).getFitness();
            if (fitness < 1.0) {
                aux.add(source.get(i));
                source.remove(i);
            } else {
                i++;
            }
        }

        // 再按照适应度值排序选择剩余个体
        if (aux.size() < size) {
            FitnessComparator comparator = new FitnessComparator();
            Collections.sort(source, comparator);
            int remain = size - aux.size();
            for (i = 0; i < remain; i++) {
                aux.add(source.get(i));
            }
            return aux;
        } else if (aux.size() == size) {
            return aux;
        }

        // 若非支配解个数很多，则进行规模为2的锦标赛算法
        List<List<Pair<Integer, Double>>> distanceList = new ArrayList<>();
        for (int pos = 0; pos < aux.size(); pos++) {
            List<Pair<Integer, Double>> distanceNodeList = new ArrayList<>();
            for (int ref = 0; ref < aux.size(); ref++) {
                if (pos != ref) {
                    distanceNodeList.add(Pair.of(ref,
                            _distance.distanceBetweenObjectives(aux.get(pos), aux.get(ref))));
                }
            }
            distanceList.add(distanceNodeList);
        }

        for (int q = 0; q < distanceList.size(); q++) {
            Collections.sort(distanceList.get(q), (pair1, pair2) -> {
                if (pair1.getRight() < pair2.getRight()) {
                    return -1;
                } else if (pair1.getRight() > pair2.getRight()) {
                    return 1;
                } else {
                    return 0;
                }
            });
        }

        while (aux.size() > size) {
            double minDistance = Double.MAX_VALUE;
            int toRemove = 0;
            i = 0;
            Iterator<List<Pair<Integer, Double>>> iterator = distanceList.iterator();
            while (iterator.hasNext()) {
                List<Pair<Integer, Double>> dn = iterator.next();
                if (dn.get(0).getRight() < minDistance) {
                    toRemove = i;
                    minDistance = dn.get(0).getRight();
                    //i y toRemove have the same distance to the first solution
                } else if (dn.get(0).getRight().equals(minDistance)) {
                    int k = 0;
                    while ((dn.get(k).getRight().equals(
                            distanceList.get(toRemove).get(k).getRight())) &&
                            k < (distanceList.get(i).size() - 1)) {
                        k++;
                    }

                    if (dn.get(k).getRight() <
                            distanceList.get(toRemove).get(k).getRight()) {
                        toRemove = i;
                    }
                }
                i++;
            }

            int tmp = aux.get(toRemove).getLocation();
            aux.remove(toRemove);
            distanceList.remove(toRemove);

            Iterator<List<Pair<Integer, Double>>> externIterator = distanceList.iterator();
            while (externIterator.hasNext()) {
                Iterator<Pair<Integer, Double>> interIterator = externIterator.next().iterator();
                while (interIterator.hasNext()) {
                    if (interIterator.next().getLeft() == tmp) {
                        interIterator.remove();
                        continue;
                    }
                }
            }
        }
        return aux;
    }

}
