package opt.easyjmetal.algorithm.cmoeas.isdeplus_cdp;

import opt.easyjmetal.algorithm.util.FitnessComparator;
import opt.easyjmetal.core.Solution;
import opt.easyjmetal.core.SolutionSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Juanjo Durillo
 */
@SuppressWarnings("serial")
public class EnvironmentalSelection {

    private int solutionsToSelect;
    private ISDEPlus_Fitness isdePlus_fitness;

    public EnvironmentalSelection(int solutionsToSelect) {
        this.solutionsToSelect = solutionsToSelect;
        isdePlus_fitness = new ISDEPlus_Fitness();
    }

    public SolutionSet execute(SolutionSet pop, List<SolutionSet> fronts) {
        // 取出最后一层的解
        SolutionSet source2 = fronts.get(fronts.size() - 1);
        // 计算适应度值
        isdePlus_fitness.computeFitnessValue(source2);

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
