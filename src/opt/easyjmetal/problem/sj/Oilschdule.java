package opt.easyjmetal.problem.sj;


import java.util.ArrayList;
import java.util.List;

public class Oilschdule {
    public static void main(String[] args) {
        int M = 4; //优化目标个数 100*50
        int k = 50;
        int Na = 50, popsize = 50, Tmax = 200;
        int[] acnum = new int[Tmax];
        double[][] pop = new double[popsize][k];
        for (int i = 0; i < popsize; i++) {
            for (int j = 0; j < k; j++) {
                pop[i][j] = Math.random();  //产生随机种群
            }
        }

        List<List<Double>> eff = new ArrayList<List<Double>>();
        eff = fat(pop);
        System.out.println("对应的成本为：");

    }

    public static List<List<Double>> fat(double[][] pop) {
        List<List<Double>> eff = new ArrayList<List<Double>>();
        double inf = -1;
        double f1, f2, f3, f4;
        int popsize = pop.length;
        int w = pop[0].length;
        for (int p = 0; p < popsize; p++) { //解遍历
            BackTrace back = null;
            int RT = 6;
            double[] DSFR = new double[]{333.3, 291.7, 625}; //蒸馏塔炼油速率
            int PIPEFR = 1250;
            int[][] FPORDER = new int[][]{{5, 1}, {6, 2}, {4, 3}};
            double[][] TKS = new double[][]{                     //容量  原油类型  已有容量 蒸馏塔  供油开始时间 供油结束时间
                    {16000, 5, 8000, 0, 0, 0},
                    {34000, 5, 30000, 0, 0, 0},
                    {34000, 4, 30000, 0, 0, 0},
                    {34000, inf, 0, 0, 0, 0},
                    {34000, 3, 30000, 0, 0, 0},
                    {16000, 1, 16000, 0, 0, 0},
                    {20000, 6, 16000, 0, 0, 0},
                    {16000, 6, 5000, 0, 0, 0},
                    {16000, inf, 0, 0, 0, 0},
                    {30000, inf, 0, 0, 0, 0}
            };
            int[][] c1 = new int[][]{
                    {0, 11, 12, 13, 7, 15},
                    {10, 0, 9, 12, 13, 7},
                    {13, 8, 0, 7, 12, 13},
                    {13, 12, 7, 0, 11, 12},
                    {7, 13, 12, 11, 0, 11},
                    {15, 7, 13, 12, 11, 0}
            };//管道混合成本
            int[][] c2 = new int[][]{
                    {0, 11, 12, 13, 10, 15},
                    {11, 0, 11, 12, 13, 10},
                    {12, 11, 0, 10, 12, 13},
                    {13, 12, 10, 0, 11, 12},
                    {10, 13, 12, 11, 0, 11},
                    {15, 10, 13, 12, 11, 0}
            };//罐底混合成本
            double[] FP = {25992, 49008, 90000, 0, 0, 0};//需要从管道运输的油量
            List<Integer> ET = new ArrayList<Integer>();//空油罐集合
            double[] DSFET = {0, 0, 0}; //三个蒸馏塔最后一次炼油结束时间
            double PET = 0;
            List<List<Double>> schedulePlan = new ArrayList<List<Double>>();
            double[] x = pop[p];
            for (int ii = 0; ii < TKS.length; ii++) { //计算空罐集合
                if (Math.abs(TKS[ii][2]) < 0.1)
                    ET.add(ii + 1);
            }
            List<Integer> UD = new ArrayList<Integer>();//未完炼油任务蒸馏塔集合
            UD.add(1);
            UD.add(2);
            UD.add(3);
            if (OilSchedule.Schedulable(FPORDER, DSFR, PIPEFR, RT, TKS, PET, FP, UD)) {
                for (int i1 = 0; i1 < UD.size(); i1++) {//蒸馏塔
                    for (int j = 0; j < 2; j++) {//原油类型
                        for (int k = 0; k < TKS.length; k++) { //油罐选择
                            if (FPORDER[i1][j] == TKS[k][1]) {
                                double Temp = DSFET[i1];
                                DSFET[i1] = DSFET[i1] + TKS[k][2] / DSFR[i1];
                                List<Double> list = new ArrayList<Double>();
                                list.add((double) i1 + 1);//蒸馏塔号
                                list.add((double) k + 1); //油罐号
                                list.add((double) Math.round(Temp * 100) / 100);//开始供油t
                                list.add((double) Math.round(DSFET[i1] * 100) / 100); //供油罐结束t
                                list.add(TKS[k][1]);//原油类型
                                schedulePlan.add(list);
                                TKS[k][3] = i1 + 1; //记录蒸馏塔
                                TKS[k][4] = Temp;//供油开始时间
                                TKS[k][5] = DSFET[i1];//供油结束时间
                            }
                        }
                    }
                }
                //*******************回溯部分开始*********************//

                back = new BackTrace(x, 0, ET, UD, PET, DSFET, FP, TKS, schedulePlan, DSFR, PIPEFR, FPORDER, RT);
                back.setFlag(false);
                back = backSchedule(back);//每次返回一个可行调度解
                //PlotUtils.plotSchedule2(back.getSchedulePlan());
            } else {
                System.out.println("初始库存不满足指定条件，系统不可调度！");
            }
            double[][] plan = switchL_A(back.getSchedulePlan());
            TestFun.sort(plan, new int[]{0});
            List<List<Double>> plannew = switchA_L(plan);
            //输出炼油调度表
            //System.out.println("第" + (p + 1) + "个解对应的调度表");
            //output_plan(plannew);
            if (back.getFlag()) {
                f1 = TestFun.gNum(plannew); //供油罐个数
                f2 = TestFun.gChange(plannew);//蒸馏塔的油罐切换次数
                f3 = TestFun.gDmix(plannew, c1);//管道混合成本
                f4 = TestFun.gDimix(plannew, c2);//罐底混合成本
            } else {
                f1 = inf;
                f2 = inf;
                f3 = inf;
                f4 = inf;
            }
            List<Double> t_list = new ArrayList<Double>();
            for (int i = 0; i < x.length; i++) {
                t_list.add(x[i]);
            }
            t_list.add(f1);
            t_list.add(f2);
            t_list.add(f3);
            t_list.add(f4);
            eff.add(t_list);
        }
        return eff;
    }


    /**
     * 回溯调度
     *
     * @param backTrace 调度之前的系统状态
     * @return 调度之后的系统状态【back】
     */
    public static BackTrace backSchedule(BackTrace backTrace) {

        BackTrace back = CloneUtil.clone(backTrace);
        back.setFlag(false);
        int[] footprint = new int[back.getUD().size() + 1];
        // 判断是否完成炼油计划
        if (Math.abs(TestFun.sum(back.getFP())) < 0.01 || back.getUD().isEmpty()) {
            back.setFlag(true);
        } else if (back.getStep() >= 25) {
            back.setFlag(false);
        } else {
            while (TestFun.all(footprint) == 0 && !back.getFlag() && back.getStep() < 25) {
                int TK_NO = TestFun.getInt(back.getX()[2 * back.getStep()], back.getET().size());//返回0-ET.size的数
                int DS = back.getUD().get(TestFun.getInt(back.getX()[2 * back.getStep() + 1], back.getUD().size() - 1));
                boolean ff = false;
                if (TK_NO == back.getET().size()) { //停运情况
                    double PipeStoptime = TestFun.getPipeStopTime(back.getFPORDER(), back.getDSFR(), back.getRT(), back.getTKS(), back.getPET(), back.getFP(), back.getUD());
                    if (Math.round(PipeStoptime) > 0) {
                        ff = true;
                        //进行停运
                        back = stop(back, PipeStoptime);
                    }
                } else { //试调度
                    back = tryschedule(back, back.getET().get(TK_NO), TK_NO, DS);
                    if (back.getFlag() && (OilSchedule.Schedulable(back.getFPORDER(), back.getDSFR(), back.getPIPEFR(), back.getRT(), back.getTKS(), back.getPET(), back.getFP(), back.getUD()))) {
                        ff = true;
                    }
                }
                if (ff) {

                    // 绘制甘特图
                    // PlotUtils.plotSchedule2(back.getSchedulePlan());

                    back.setStep(back.getStep() + 1);
                    back = backSchedule(back);
                } else {
                    back = CloneUtil.clone(backTrace); //数据回滚
                    back.setFlag(false);
                    double t1, t2, t3;
                    //***********策略是否已经全部执行尝试**********
                    //*************更改策略*************************
                    for (int i = 0; i < footprint.length; i++) {
                        if (footprint[i] == 0) { //第一位代表管道停运
                            if (i == 0) {
                                while (TestFun.getInt(back.getX()[2 * back.getStep()], back.getET().size()) != back.getET().size()) {
                                    t1 = Math.random();
                                    back.getX()[2 * back.getStep()] = t1;
                                }
                                footprint[0] = 1;
                            } else if (back.getET().isEmpty()) { //尝试过停运，但没有空罐
                                for (int j = 0; j < footprint.length; j++) {
                                    footprint[i] = 1;
                                }
                            } else {
                                //停运不行，但有空罐
                                while (TestFun.getInt(back.getX()[2 * back.getStep()], back.getET().size()) == back.getET().size()) {//选空罐
                                    t2 = Math.random();
                                    back.getX()[2 * back.getStep()] = t2;
                                    //System.out.println(",,,,,,,,");
                                }
                                while (TestFun.getInt(back.getX()[2 * back.getStep() + 1], back.getUD().size() - 1) != i - 1) {
                                    t3 = Math.random();
                                    back.getX()[2 * back.getStep() + 1] = t3;
                                    //System.out.println(".......");
                                }
                                footprint[i] = 1;
                            }
                            break;
                        }
                    }

                }
            }
        }
        return back;
    }

    /**
     * 试调度
     *
     * @param backtrace 调度之前的系统状态
     * @return 调度之后的系统状态【back】
     */
    public static BackTrace tryschedule(BackTrace backtrace, int TK, int TK_NO, int DS) {

        // 拷贝一份调度之前的系统状态，以后的更改都会在这个新拷贝的对象上进行。
        BackTrace back = CloneUtil.clone(backtrace);

        int inf = -1;
        int COT;

        if (Math.abs(back.getFP()[back.getFPORDER()[DS - 1][0] - 1]) < 0.01) {
            COT = back.getFPORDER()[DS - 1][1];
        } else {
            COT = back.getFPORDER()[DS - 1][0];
        }
        double[] total = new double[back.getUD().size()]; //计算当前的所有油量
        total = getTotal(back.getUD(), back.getDSFR(), back.getFP(), back.getTKS(), back.getPET(), back.getFPORDER());
        double V = getVolume(back, total, DS);
        if (V >= 150) {
            if (back.getTKS()[TK - 1][0] < V) {
                V = back.getTKS()[TK - 1][0];
            }
            if (back.getFP()[COT - 1] < V) {
                V = back.getFP()[COT - 1];
            }
            back.getFP()[COT - 1] = back.getFP()[COT - 1] - V;
            back.getET().remove(TK_NO);
            //转运期间释放的供油罐添加到空罐集合
            for (int i = 0; i < back.getTKS().length; i++) {
                if (back.getTKS()[i][5] > back.getPET() && back.getTKS()[i][5] <= back.getPET() + V / back.getPIPEFR()) {
                    back.getET().add(i + 1);
                    back.getTKS()[i][1] = inf;
                    back.getTKS()[i][2] = 0;
                    back.getTKS()[i][3] = 0;
                    back.getTKS()[i][4] = 0;
                    back.getTKS()[i][5] = 0;
                }
            }
            double PETOLD = back.getPET();
            back.setPET(PETOLD + V / back.getPIPEFR());
            double DSFETOLD = back.getDSFET()[DS - 1];
            back.getDSFET()[DS - 1] = DSFETOLD + V / back.getDSFR()[DS - 1];
            // 更新供油罐的状态
            back.getTKS()[TK - 1][1] = COT;
            back.getTKS()[TK - 1][2] = V;
            back.getTKS()[TK - 1][3] = DS;
            back.getTKS()[TK - 1][4] = DSFETOLD;
            back.getTKS()[TK - 1][5] = back.getDSFET()[DS - 1];
            back.setFlag(true);
            List<Double> list = new ArrayList<Double>();
            list.add(4d);
            list.add((double) TK); //油罐号
            list.add((double) Math.round(PETOLD * 100) / 100);//开始供油t
            list.add((double) Math.round(back.getPET() * 100) / 100); //供油罐结束t
            list.add((double) COT);//原油类型
            back.getSchedulePlan().add(list);
            // 输出转运操作
            // System.out.println(list);

            List<Double> list1 = new ArrayList<Double>();
            list1.add((double) DS);
            list1.add((double) TK);
            list1.add((double) Math.round(DSFETOLD * 100) / 100);
            list1.add((double) Math.round(back.getDSFET()[DS - 1] * 100) / 100);
            list1.add((double) COT);
            back.getSchedulePlan().add(list1);
            // 输出炼油操作
            //  System.out.println(list1);

            //判断DS是否炼油成功
            if (Math.round(back.getDSFET()[DS - 1]) == 240) {
                int temp = back.getUD().indexOf(DS);
                back.getUD().remove(temp);
            }
        } else {
            back.setFlag(false);
        }
        return back;
    }

    /**
     * 停运
     *
     * @param backtrace 调度之前的系统状态
     * @return 调度之后的系统状态【back】
     */
    public static BackTrace stop(BackTrace backtrace, double PipeStoptime) {
        // 拷贝一份调度之前的系统状态，以后的更改都会在这个新拷贝的对象上进行。
        BackTrace back = CloneUtil.clone(backtrace);

        double PETOLD = back.getPET();
        int tk = -1;
        double[] feedendtimes = new double[back.getTKS().length];
        for (int i = 0; i < feedendtimes.length; i++) {
            feedendtimes[i] = back.getTKS()[i][5];
        }
        //按照原油结束时间进行排序 从小到大
        int[] ind = new int[feedendtimes.length];
        ind = Arraysort(feedendtimes, false);
        //停运期间，各个蒸馏塔是否炼油结束
        for (int i = 0; i < back.getTKS().length; i++) {
            if (feedendtimes[i] > back.getPET() && feedendtimes[i] < back.getPET() + PipeStoptime) {
                tk = i;
                break;
            }
        }
        if (tk != -1) { //停运期间有油罐释放
            int inf = -1;
            back.setPET(feedendtimes[tk]);
            back.getET().add(ind[tk] + 1);
            back.getTKS()[ind[tk]][1] = inf;
            back.getTKS()[ind[tk]][2] = 0;
            back.getTKS()[ind[tk]][3] = 0;
            back.getTKS()[ind[tk]][4] = 0;
            back.getTKS()[ind[tk]][5] = 0;
        } else {//正常停运
            back.setPET(back.getPET() + PipeStoptime);
        }
        //更新调度表
        List<Double> list = new ArrayList<Double>();
        list.add((double) back.getDSFR().length + 1);//蒸馏塔号
        list.add(0d); //油罐号
        list.add(PETOLD);//开始供油t
        list.add(back.getPET()); //供油罐结束t
        list.add(0d);//原油类型
        back.getSchedulePlan().add(list);
        // 输出停运的信息
        // System.out.println(list);
        return back;
    }

    public static int[] Arraysort(double[] arr, boolean desc) {
        double temp;
        int index;
        int k = arr.length;
        int[] Index = new int[k];
        for (int i = 0; i < k; i++) {
            Index[i] = i;
        }

        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr.length - i - 1; j++) {
                if (desc) {
                    if (arr[j] < arr[j + 1]) { //从大到小排序 true
                        temp = arr[j];
                        arr[j] = arr[j + 1];
                        arr[j + 1] = temp;

                        index = Index[j];
                        Index[j] = Index[j + 1];
                        Index[j + 1] = index;
                    }
                } else {
                    if (arr[j] > arr[j + 1]) { //从小到大 false
                        temp = arr[j];
                        arr[j] = arr[j + 1];
                        arr[j + 1] = temp;

                        index = Index[j];
                        Index[j] = Index[j + 1];
                        Index[j + 1] = index;
                    }
                }
            }
        }
        return Index;
    }

    public static double[][] switchL_A(List<List<Double>> plan) {
        double[][] temp = new double[plan.size()][plan.get(0).size()];
        for (int i = 0; i < temp.length; i++) {
            for (int j = 0; j < temp[i].length; j++) {
                temp[i][j] = plan.get(i).get(j);
            }
        }
        return temp;
    }

    public static List<List<Double>> switchA_L(double[][] plannew) {
        List<List<Double>> temp = new ArrayList<List<Double>>();
        for (int i = 0; i < plannew.length; i++) {
            List<Double> t = new ArrayList<Double>();
            for (int j = 0; j < plannew[i].length; j++) {
                t.add(plannew[i][j]);
            }
            temp.add(t);
        }
        return temp;
    }

    public static double[] getTotal(List<Integer> UD, double[] DSFR, double[] FP, double[][] TKS, double PET, int[][] FPORDER) {
        double[][] UFDR = new double[UD.size()][2];//蒸馏塔炼油速率升序排列
        for (int i = 0; i < UD.size(); i++) {
            UFDR[i][0] = (UD.get(i));
            UFDR[i][1] = DSFR[i];
        }
        TestFun.sort(UFDR, new int[]{1});
        int K = UFDR.length;
        double[][] available = new double[DSFR.length][2];
        for (int i = 0; i < K; i++) {
            int DSN = (int) UFDR[i][0]; //蒸馏塔编号
            int COTN1 = FPORDER[DSN - 1][0];
            int COTN2 = FPORDER[DSN - 1][1];
            for (int j = 0; j < TKS.length; j++) {
                if (TKS[j][1] == COTN1) {
                    if (TKS[j][4] <= PET && TKS[j][5] > PET) { //供油罐是否在给蒸馏塔供油
                        available[DSN - 1][0] = available[DSN - 1][0] + TKS[j][2] - ((PET - TKS[j][4]) * DSFR[DSN - 1]);
                    } else {
                        available[DSN - 1][0] = available[DSN - 1][0] + TKS[j][2];
                    }
                } else if (TKS[j][1] == COTN2) {
                    if (TKS[j][4] <= PET && TKS[j][5] > PET) {
                        available[DSN - 1][1] = available[DSN - 1][1] + TKS[j][2] - ((PET - TKS[j][4]) * DSFR[DSN - 1]);
                    } else {
                        available[DSN - 1][1] = available[DSN - 1][1] + TKS[j][2];
                    }
                }
            }
        }
        double[] total = new double[FPORDER.length]; //各个蒸馏塔的总油量
        for (int i = 0; i < available.length; i++) {
            if (FP[FPORDER[i][0] - 1] != 0) { //判断进料包中是否含有该原油
                total[i] = available[i][0];
            } else {
                total[i] = available[i][0] + available[i][1];
            }
        }
        return total;
    }

    public static double getVolume(BackTrace back, double[] total, int DS) {
        double V = (back.getDSFET()[DS - 1] - back.getPET() - back.getRT()) * back.getPIPEFR();  //DSFET[DS-1]表示蒸馏塔DS最后一次炼油结束时间***
        //其他原油转运的安全体积
        double VSec = 9999999;
        double mincapacity, temp1, temp2, temp3;
        for (int i = 0; i < back.getUD().size(); i++) {
            if (i != (DS - 1)) {
                if (back.getDSFR()[back.getUD().get(i) - 1] == TestFun.getMax(back.getDSFR())) { //最小原油量
                    temp1 = 2 * back.getUD().size() * back.getRT() * back.getDSFR()[back.getUD().get(i) - 1];  //保证最快速率的蒸馏塔有油可炼
                    mincapacity = temp1;
                } else {
                    temp2 = back.getUD().size() * back.getRT() * back.getDSFR()[back.getUD().get(i) - 1];  //满足静置时间的油量
                    mincapacity = temp2;
                }
                if (VSec > (back.getPIPEFR() / back.getDSFR()[back.getUD().get(i) - 1] * (total[back.getUD().get(i) - 1] - mincapacity))) {
                    temp3 = (back.getPIPEFR() / back.getDSFR()[back.getUD().get(i) - 1] * (total[back.getUD().get(i) - 1] - mincapacity));
                    VSec = temp3;
                }
            }
        }
        if (VSec < V) {
            V = VSec;
        }
        return V;
    }

    public static void output_plan(List<List<Double>> plan) {
        for (int i = 0; i < plan.size(); i++) {
            for (int j = 0; j < plan.get(0).size(); j++) {
                System.out.print(Math.round(plan.get(i).get(j)) + ",");
            }
            System.out.print("\b\n");
        }
    }
}
