package opt.jmetal.problem.storagetankshedule;

import opt.easyjmetal.core.Solution;
import opt.easyjmetal.core.SolutionSet;
import opt.jmetal.util.point.Point;

import java.util.ArrayList;
import java.util.List;

public class FirstRank {

    private int[] rank_;

    private List<Integer> F_;

    /**
     * 获取非支配解
     * @param pop
     * @return
     */
    public List<Point> getFirstfront(List<Point> pop) {

        int N = pop.size();
        int M = pop.get(0).getDimension();
        F_ = new ArrayList(N) ;
        rank_ = sort_rows(pop);
        F_.add(rank_[0]);

        for (int i = 1; i < N; i++){
            int x = 2;
            while(true){
                for(int j = F_.size() - 1; j >= 0; j--) {
                    x = 2;
                    for (int j2 = 1; j2 < M; j2++) {
                        if (pop.get(rank_[i]).getValue(j2) < pop.get(F_.get(j)).getValue(j2)) {
                            x = 0;
                            break;
                        }
                    }
                    if (x == 2 || M == 2) {
                        break;
                    }
                }

                if (x != 2){
                    F_.add(rank_[i]);
                    break;
                }else {
                    break;
                }
            }
        }

        int rank_no = F_.size();
        List<Point> result = new ArrayList<>();
        for(int i = 0; i < rank_no; i++){
            result.add(pop.get(F_.get(i)));
        }
        return result;
    }

    private int[] sort_rows(List<Point> pop){
        SolutionSet tempSols = new SolutionSet(pop.size());
        int popsize = pop.size();
        int objs = pop.get(0).getDimension();
        int[] result = new int[popsize];
        for(int i = 0; i <pop.size(); i++){
            result[i] = i;
            Solution temp = new Solution(objs);
            for(int j = 0; j < objs; j++){
                temp.setObjective(j,pop.get(i).getValue(j));
            }
            tempSols.add(temp);
        }


        for(int i = 0; i < popsize; i++){
            for(int j = i + 1; j < popsize; j++){
                if(compare_solutions(tempSols.get(i),tempSols.get(j),0) == -1){
                    Solution sol = tempSols.get(i);
                    tempSols.replace(i,tempSols.get(j));
                    tempSols.replace(j,sol);

                    int temp = result[i];
                    result[i] = result[j];
                    result[j] = temp;
                }
            }
        }
        return result;
    }

    private int compare_solutions(Solution s1, Solution s2, int ind){
        if(ind >= s1.getNumberOfObjectives())
            return 0;
        else{
            if(s1.getObjective(ind) > s2.getObjective(ind))
                return -1;
            else if (s1.getObjective(ind) < s2.getObjective(ind))
                return 1;
            else return compare_solutions(s1,s2,ind + 1);
        }
    }
}
