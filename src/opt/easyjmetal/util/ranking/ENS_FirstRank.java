package opt.easyjmetal.util.ranking;

/**
 * Created by lwj
 * This method is based on the following paper
 * X. Zhang, Y. Tian, R. Cheng, and Y. Jin.
 * An efficient approach to non-dominated sorting for evolutionary multi-objective optimization.
 * IEEE Transactions on Evolutionary Computation, 19(2):201-213, 2015
 */

import opt.easyjmetal.core.Solution;
import opt.easyjmetal.core.SolutionSet;

import java.util.ArrayList;
import java.util.List;

public class ENS_FirstRank {

    private int[] rank_ ;
    private List<Integer> F_  ;
    private SolutionSet population_;

    public ENS_FirstRank(){
    }

    public ENS_FirstRank(SolutionSet pop) {
        population_ =  pop;
        int NoC = 0;
        int N = population_.size();
        int M = population_.get(0).getNumberOfObjectives();
        F_ = new ArrayList(N) ;
        rank_ = sort_rows(population_);
        F_.add(rank_[0]);

        for (int i = 1; i < N; i++){
            int x = 2;
            while(true){
                for(int j = F_.size() - 1; j >= 0; j--) {
                    x = 2;
                    for (int j2 = 1; j2 < M; j2++) {
                        if (population_.get(rank_[i]).getObjective(j2) < population_.get(F_.get(j)).getObjective(j2)) {
                            x = 0;
                            break;
                        }
                    }
                    NoC += 1;
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
    }

    public SolutionSet getFirstfront() {
        int rank_no = F_.size();
        SolutionSet result = new SolutionSet(rank_no);
        for(int i = 0; i < rank_no; i++){
            result.add(new Solution(population_.get(F_.get(i))));
        }
        return result;
    }

    private int[] sort_rows(SolutionSet pop){
        SolutionSet tempSols = new SolutionSet(pop.size());
        int popNo = pop.size();
        int objNo = pop.get(0).getNumberOfObjectives();
        int[] result = new int[popNo];
        for(int i = 0; i <pop.size(); i++){
            result[i] = i;
            Solution temp = new Solution(objNo);
            for(int j = 0; j < objNo; j++){
                temp.setObjective(j,pop.get(i).getObjective(j));
            }
            tempSols.add(temp);
        }


        for(int i = 0; i < popNo; i++){
            for(int j = i + 1; j < popNo; j++){
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
