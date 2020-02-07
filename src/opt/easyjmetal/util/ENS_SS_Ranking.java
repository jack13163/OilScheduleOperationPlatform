package opt.easyjmetal.util;

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

public class ENS_SS_Ranking {

    /**
     * The <code>SolutionSet</code> to rank
     */
    private int[] rank_ ;

    /**
     * An array containing all the fronts found during the search
     */
    private List<Integer>[] F_  ;


    private SolutionSet population_;

    /**
     * default constructor.
     */
    public ENS_SS_Ranking(){

    }

    public ENS_SS_Ranking(SolutionSet pop) {
        population_ =  pop;

        int NoC = 0;
        int N = population_.size();
        int M = population_.get(0).getNumberOfObjectives();
        F_ = new ArrayList[N] ;
        for(int i = 0; i < N ; i++){
            F_[i] = new ArrayList<Integer>(N);
        }
        int NoF = 0;
        rank_ = sort_rows(population_);
        F_[0].add(rank_[0]);
        NoF = NoF + 1;

        for (int i = 1; i < N; i++){
            int k = 0; int x = 2;
            while(true){
                for(int j = F_[k].size() - 1; j >= 0; j--) {
                    x = 2;
                    for (int j2 = 1; j2 < M; j2++) {
                        if (population_.get(rank_[i]).getObjective(j2) < population_.get(F_[k].get(j)).getObjective(j2)) {
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
                    F_[k].add(rank_[i]);
                    break;
                }else {
                    if(k < NoF - 1){
                        k = k + 1;
                    }else {
                        NoF = NoF + 1;
                        F_[NoF - 1].add(rank_[i]);
                        break;
                    }

                }
            }

        }
    }

    /**
     * Returns a <code>SolutionSet</code> containing the solutions of a given rank.
     * @param rank The rank
     * @return Object representing the <code>SolutionSet</code>.
     */
    public SolutionSet getSubfront(int rank) {
        int rank_no = F_[rank].size();
        SolutionSet result = new SolutionSet(rank_no);
        for(int i = 0; i < rank_no; i++){
            result.add(new Solution(population_.get(F_[rank].get(i))));
        }
        return result;
    } // getSubfront

    private int[] sort_rows(SolutionSet Population){

        SolutionSet tempSols = new SolutionSet(Population.size());
        for(int i = 0; i <Population.size(); i++){
            tempSols.add(new Solution((Population.get(i))));
        }


        int pop_no = tempSols.size();
        int[] result = new int[pop_no];
        for(int i = 0; i < pop_no; i++){
            result[i] = i;
        }

        for(int i = 0; i < pop_no; i++){
            for(int j = i + 1; j < pop_no; j++){
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
