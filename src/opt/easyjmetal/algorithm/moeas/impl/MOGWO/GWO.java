package opt.easyjmetal.algorithm.moeas.impl.MOGWO;

import opt.easyjmetal.algorithm.moeas.impl.MOGWO.EntityClass.Grid;
import opt.easyjmetal.algorithm.moeas.impl.MOGWO.EntityClass.Wolf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GWO {
    static int GreyWolves_num = 100;
    static int MaxIt = 100;
    static int Archive_size = 100;
    static double alpha = 0.1;
    static int nGrid = 10;
    static int beta = 4;
    static int gamma = 2;
    static int nVar = 10;
    static int[] low = new int[]{0, -1, -1, -1, -1, -1, -1, -1, -1, -1};
    static int[] up = new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1};

    public static void main(String[] args) {
        List<Wolf> GreyWolves = initWolves(GreyWolves_num);// 初始化狼群
        List<Wolf> GGreyWolves = DetermineDomination(GreyWolves);
        List<Wolf> Archive = GetNonDominatedParticles(GGreyWolves);
        List<Grid> G = CreateHypercubes(Archive, nGrid, alpha);
        for (int i = 0; i < Archive.size(); i++) {// 储备集合中的每个粒子找到对应的网格
            getGridIndex(Archive.get(i), G);
        }

        for (int it = 0; it < MaxIt; it++) {
            double a = 2 - (it + 1) * (2 / MaxIt);
            for (int i = 0; i < GreyWolves_num; i++) {
                Wolf Delta = SelectLeader(Archive, beta);
                Wolf Beta = SelectLeader(Archive, beta);
                Wolf Alpha = SelectLeader(Archive, beta);

                // 如果在最不拥挤的地方少于三个解，则从第二个不拥挤的地方取选择
                List<Wolf> rep2 = new ArrayList<>();
                if (Archive.size() > 1) {
                    for (int newi = 0; newi < Archive.size(); newi++) {
                        if (sum(Delta.getPosition(), Archive.get(newi).getPosition()) != 0) {
                            rep2.add(Archive.get(newi));
                        }
                    }
                    Beta = SelectLeader(rep2, beta);
                }

                if (Archive.size() > 2) {
                    List<Wolf> rep3 = new ArrayList<>();
                    for (int newi = 0; newi < rep2.size(); newi++) {
                        if (sum(Beta.getPosition(), rep2.get(newi).getPosition()) != 0) {
                            rep3.add(rep2.get(newi));
                        }
                    }
                    Alpha = SelectLeader(rep3, beta);
                }

                // 根据选择的三只狼更新其他狼的位置
                List<Double> c1 = getC(nVar).stream().map(e -> e * 2).collect(Collectors.toList());
                List<Double> D1 = getD(c1, Delta.getPosition(), GGreyWolves.get(i).getPosition());
                List<Double> A1 = getC(nVar).stream().map(e -> (e * 2 * a - a)).collect(Collectors.toList());
                List<Double> X1 = getX(Delta.getPosition(), A1, D1);

                List<Double> c2 = getC(nVar).stream().map(e -> e * 2).collect(Collectors.toList());
                List<Double> D2 = getD(c2, Beta.getPosition(), GGreyWolves.get(i).getPosition());
                List<Double> A2 = getC(nVar).stream().map(e -> (e * 2 * a - a)).collect(Collectors.toList());
                List<Double> X2 = getX(Beta.getBestPos(), A2, D2);

                List<Double> c3 = getC(nVar).stream().map(e -> e * 2).collect(Collectors.toList());
                List<Double> D3 = getD(c3, Alpha.getPosition(), GGreyWolves.get(i).getPosition());
                List<Double> A3 = getC(nVar).stream().map(e -> (e * 2 * a - a)).collect(Collectors.toList());
                List<Double> X3 = getX(Alpha.getBestPos(), A3, D3);

                GGreyWolves.get(i).setPosition(getX123(X1, X2, X3));
                // 判断位置是否超出边界
                GGreyWolves.get(i).setPosition(bound_check(GGreyWolves.get(i).getPosition(), low, up));

                GGreyWolves.get(i).setFit(UF1.fitness(GGreyWolves.get(i).getPosition()));

            }
            GGreyWolves = DetermineDomination(GGreyWolves);

            //本次更新产生的非支配解
            List<Wolf> non_dominated_wolves = GetNonDominatedParticles(GGreyWolves);

            Archive.addAll(non_dominated_wolves);

            Archive = DetermineDomination(Archive);
            Archive = GetNonDominatedParticles(Archive);

            for (int i = 0; i < Archive.size(); i++) {// 储备集合中的每个粒子找到对应的网格
                getGridIndex(Archive.get(i), G);
            }
            System.out.println((it + 1) + " " + Archive.size());
            if (Archive.size() > Archive_size) {
                int extra = Archive.size() - Archive_size;

                Archive = DeleteFromRep(Archive, extra, gamma);
                G = CreateHypercubes(Archive, nGrid, alpha);
            }
        }

    }

    public static List<Wolf> DeleteFromRep(List<Wolf> rep, int extra, double gamma) {
        for (int k = 0; k < extra; k++) {
            List<List<Integer>> ssum = GetOccupiedCells(rep);
            List<Integer> occ_cell_index = ssum.get(0);
            List<Integer> occ_cell_member_count = ssum.get(1);
            List<Double> pp = occ_cell_member_count.stream().map(e -> Math.pow(e, gamma)).collect(Collectors.toList());
            Double sump = pp.stream().mapToDouble(x -> x).summaryStatistics().getSum();
            List<Double> p = pp.stream().map(e -> e / sump).collect(Collectors.toList());
            int select_index = occ_cell_index.get(RouletteWheelSelection(p));// 轮盘赌获得所选网格下标
            List<Integer> GridIndices = rep.stream().map(e -> e.getGridIndex()).collect(Collectors.toList());
            List<Integer> select_cell_members = findSelect(GridIndices, select_index);// 被选的下标
            int n = select_cell_members.size();
            int select_member_index = (int) Math.random() * n;//[0,n)的整数
            int h = select_cell_members.get(select_member_index);
            rep.remove(h);
        }
        return rep;
    }

    public static List<Double> bound_check(List<Double> pos, int[] low, int[] up) {
        for (int i = 0; i < pos.size(); i++) {
            double t = Math.min(Math.max(pos.get(i), low[i]), up[i]);
            pos.set(i, t);
        }
        return pos;
    }

    public static List<Double> getX123(List<Double> X1, List<Double> X2, List<Double> X3) {
        List<Double> pos = new ArrayList<>();
        for (int i = 0; i < X1.size(); i++) {
            pos.add(X1.get(i) + X2.get(i) + X3.get(i));
        }
        List<Double> ppos = pos.stream().map(e -> e / 3).collect(Collectors.toList());
        return ppos;
    }

    public static List<Double> getX(List<Double> pos, List<Double> A, List<Double> D) {
        List<Double> X = new ArrayList<>();
        for (int i = 0; i < pos.size(); i++) {
            double t = pos.get(i) - (A.get(i) * Math.abs(D.get(i)));
            X.add(t);
        }
        return X;
    }

    public static List<Double> getD(List<Double> c, List<Double> Beta_pos, List<Double> Grey) {
        List<Double> newpos = new ArrayList<>();
        for (int i = 0; i < Beta_pos.size(); i++) {
            double t = Math.abs(Beta_pos.get(i) * c.get(i) - Grey.get(i));
            newpos.add(t);
        }
        return newpos;
    }

    public static void getGridIndex(Wolf particle, List<Grid> G) {
        int nobj = particle.getBestFit().size();//目标值个数
        List<Double> list = new ArrayList<>();
        List<Double> U = new ArrayList<>();
        List<Integer> temp = new ArrayList<>();
        int ngrid = G.get(0).getUpper().size();
        for (int j = 0; j < nobj; j++) {
            U = G.get(j).getUpper();
            temp.add(find(particle.getFit().get(j), U));
        }
        particle.setGridsubIndex(temp);
        particle.setGridIndex(ngrid * (temp.get(1) - 1) + temp.get(0));
    }

    public static List<Wolf> initWolves(int GreyWolves_num) {
        List<Wolf> wolves = new ArrayList<>();
        for (int i = 0; i < GreyWolves_num; i++) {
            Wolf wolf = new Wolf();
            List<Double> pos = new ArrayList<>();
            wolf.setV(0);
            for (int p = 0; p < nVar; p++) {
                pos.add(Math.random() * (up[p] - low[p]) + low[p]);
            }
            wolf.setPosition(pos);
            wolf.setFit(UF1.fitness(pos));
            wolf.setDominated(false);
            wolf.setBestPos(wolf.getPosition());
            wolf.setBestFit(wolf.getFit());
            wolves.add(wolf);
        }
        return wolves;
    }

    public static int find(double costj, List<Double> U) {
        int index = 0;
        for (int i = 0; i < U.size(); i++) {
            if (costj < U.get(i)) {
                index = i + 1;
                break;
            }
        }
        return index;
    }

    public static List<Wolf> DetermineDomination(List<Wolf> pop) {
        for (int i = 0; i < pop.size(); i++) {
            pop.get(i).setDominated(false);
            for (int j = 0; j < i - 1; j++) {
                if (!pop.get(j).getDominated()) {
                    if (Dominates(pop.get(i), pop.get(j))) {
                        pop.get(j).setDominated(true);
                    } else if (Dominates(pop.get(j), pop.get(i))) {
                        pop.get(i).setDominated(true);
                        break;
                    }
                }
            }
        }
        return pop;
    }

    public static boolean Dominates(Wolf x, Wolf y) {
        boolean Flag = false;
        int a = 0, b = 0;
        for (int i = 0; i < x.getFit().size(); i++) {
            if (x.getFit().get(i) < y.getFit().get(i)) {
                a++;
            }
            if (x.getFit().get(i) == y.getFit().get(i)) {
                b++;
            }
        }
        if (a == x.getFit().size() || (a + b == x.getFit().size() && a > 0)) {
            Flag = true;
        }
        return Flag;
    }

    public static List<Wolf> GetNonDominatedParticles(List<Wolf> pop) {//获得非支配解
        List<Wolf> NonDom = new ArrayList<>();
        for (int i = 0; i < pop.size(); i++) {
            if (!pop.get(i).getDominated()) {
                NonDom.add(pop.get(i));
            }
        }
        return NonDom;
    }

    //    public static List<Wolf> wolfOpt(List<Wolf> Archive){
//        for(int it=0;it<MaxIt;it++){
//            double a=2-(it+1)*(2/MaxIt);
//            for(int i=0;i<GreyWolves_num;i++){
//                Wolf Delta=SelectLeader(Archive,beta);
//                Wolf Beta=SelectLeader(Archive,beta);
//                Wolf Alpha=SelectLeader(Archive,beta);
//
//                //如果在最不拥挤的地方少于三个解，则从第二个不拥挤的地方取选择
//                List<Wolf> rep2=new ArrayList<>();
//                if(Archive.size()>1){
//                    for(int newi=0;newi<Archive.size();newi++){
//                        if(sum(Delta.getPosition(),Archive.get(newi).getPosition())!=0){
//                            rep2.add(Archive.get(newi));
//                        }
//                    }
//                    Beta=SelectLeader(rep2,beta);
//                }
//
//                if(Archive.size()>2){
//                    List<Wolf> rep3=new ArrayList<>();
//                    for(int newi=0;newi<rep2.size();i++){
//                        if(sum(Beta.getPosition(),rep2.get(newi).getPosition())!=0){
//                            rep3.add(rep2.get(newi));
//                        }
//                    }
//                    Alpha=SelectLeader(rep3,beta);
//                }
//                //根据选择的三只狼更新其他狼的位置
//                List<Double> c=getC(nVar);
//                D=getD(c,Beta.getPosition(),G)
//            }
//
//
//
//        }
//    }
    public static List<Double> getC(int nVar) {
        List<Double> rand = new ArrayList<>();
        for (int i = 0; i < nVar; i++) {
            rand.add(Math.random());
        }
        return rand;
    }

    public static int sum(List<Double> x, List<Double> y) {
        int sum = 0;
        for (int i = 0; i < x.size(); i++) {
            if (x.get(i) != y.get(i)) {
                sum++;
            }
        }
        return sum;
    }

    public static Wolf SelectLeader(List<Wolf> rep, double beta) {
        List<List<Integer>> ssum = GetOccupiedCells(rep);
        List<Integer> occ_cell_index = ssum.get(0);//去重之后的网格下标
        List<Integer> occ_member_count = ssum.get(1);
        List<Double> pp = occ_member_count.stream().map(e -> Math.pow(e, -beta)).collect(Collectors.toList());
        Double sump = pp.stream().mapToDouble(x -> x).summaryStatistics().getSum();
        List<Double> p = pp.stream().map(e -> e / sump).collect(Collectors.toList());
        int select_index = occ_cell_index.get(RouletteWheelSelection(p));//轮盘赌获得所选网格下标
        List<Integer> GridIndices = rep.stream().map(e -> e.getGridIndex()).collect(Collectors.toList());
        List<Integer> select_cell_members = findSelect(GridIndices, select_index);//被选的网格下标
        int n = select_cell_members.size();
        int select_member_index = (int) Math.random() * n;//[0,n)的整数
        int h = select_cell_members.get(select_member_index);
        return rep.get(h);
    }

    public static List<Integer> findSelect(List<Integer> GridIndices, int select_index) {
        List<Integer> select_cell_members = new ArrayList<>();
        for (int i = 0; i < GridIndices.size(); i++) {
            if (GridIndices.get(i) == select_index) {
                select_cell_members.add(i);
            }
        }
        return select_cell_members;
    }

    public static List<Grid> CreateHypercubes(List<Wolf> Archive, int nGrid, double alpha) {
        double inf1 = Double.POSITIVE_INFINITY;
        double inf2 = Double.NEGATIVE_INFINITY;
        List<List<Double>> costs = Archive.stream().map(e -> e.getFit()).collect(Collectors.toList());
        int nobj = costs.get(0).size();// 目标值个数
        Grid empty_grid = new Grid();
        List<Grid> G = new ArrayList<>();// 存放nobj个目标的最大最小值其size为目标值个数
        for (int j = 0; j < nobj; j++) {
            int index = j;
            double min_cj = Collections.min(costs.stream().map(e -> e.get(index)).collect(Collectors.toList()));
            double max_cj = Collections.max(costs.stream().map(e -> e.get(index)).collect(Collectors.toList()));
            double dcj = alpha * (max_cj - min_cj);
            min_cj = min_cj - dcj;
            max_cj = max_cj + dcj;
            List<Double> min = new ArrayList<>();
            List<Double> max = new ArrayList<>();
            min.add(inf2);
            min.addAll(linespace(min_cj, max_cj, nGrid - 1));
            max.addAll(linespace(min_cj, max_cj, nGrid - 1));
            max.add(inf1);
            empty_grid.setLower(min);
            empty_grid.setUpper(max);
            G.add(empty_grid);
        }
        return G;
    }

    public static List<Double> linespace(double min_cj, double max_cj, int nGrid) {
        List<Double> gx = new ArrayList<>();
        double temp = min_cj;
        double gap = (max_cj - min_cj) / (nGrid - 1);
        gx.add(temp);
        for (int i = 0; i < nGrid - 1; i++) {
            temp = temp + gap;
            gx.add(temp);
        }
        return gx;
    }

    public static List<List<Integer>> GetOccupiedCells(List<Wolf> rep) {
        List<List<Integer>> occ_indm = new ArrayList<>();
        List<Integer> GridIndices = rep.stream().map(e -> e.getGridIndex()).collect(Collectors.toList());
        List<Integer> occ_cell_index = GridIndices.stream().distinct().collect(Collectors.toList());
        List<Integer> occ_cell_member_count = new ArrayList<>();
        for (int i = 0; i < occ_cell_index.size(); i++) {
            int index = i;
            occ_cell_member_count.add(GridIndices.stream().filter(e -> e == occ_cell_index.get(index)).collect(Collectors.toList()).size());
        }
        occ_indm.add(occ_cell_index);
        occ_indm.add(occ_cell_member_count);
        return occ_indm;
    }

    public static int RouletteWheelSelection(List<Double> p) {//p中存储的是个体的选择概率  返回的是java中对应的下标
        double r = Math.random();
        double sum = 0;
        int index = 0;
        List<Double> cum = new ArrayList<>();
        for (int i = 0; i < p.size(); i++) {
            sum = sum + p.get(i);
            cum.add(sum);
        }
        for (int j = 0; j < cum.size(); j++) {
            if (r <= cum.get(j)) {
                index = j;
                break;
            }
        }
        return index;

    }
}
