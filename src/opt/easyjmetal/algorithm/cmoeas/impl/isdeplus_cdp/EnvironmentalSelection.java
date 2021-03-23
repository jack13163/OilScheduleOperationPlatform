package opt.easyjmetal.algorithm.cmoeas.impl.isdeplus_cdp;

import opt.easyjmetal.core.Solution;
import opt.easyjmetal.core.SolutionSet;
import opt.easyjmetal.util.comparators.FitnessComparator;
import opt.easyjmetal.util.fitness.ISDEPlus_Fitness;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EnvironmentalSelection {

    private int solutionsToSelect;

    public EnvironmentalSelection(int solutionsToSelect) {
        this.solutionsToSelect = solutionsToSelect;
    }

    public SolutionSet execute(SolutionSet pop, List<SolutionSet> fronts) {
        // 取出最后一层的解
        SolutionSet source2 = fronts.get(fronts.size() - 1);
        // 计算适应度值
        ISDEPlus_Fitness.computeFitnessValue(source2);

        List<Solution> source = new ArrayList<>(source2.size());
        for (int i = 0; i < source2.size(); i++) {
            source.add(source2.get(i));
        }

        // 再按照适应度值排序选择剩余个体
        if (pop.size() < this.solutionsToSelect) {
            FitnessComparator comparator = new FitnessComparator();
            Collections.sort(source, comparator);
            int remain = this.solutionsToSelect - pop.size();
            for (int i = 0; i < remain; i++) {
                pop.add(source.get(i));
            }
            return pop;
        } else if (pop.size() == this.solutionsToSelect) {
            return pop;
        } else{
            return null;
        }
    }
}
