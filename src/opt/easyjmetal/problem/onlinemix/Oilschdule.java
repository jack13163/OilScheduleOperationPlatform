package opt.easyjmetal.problem.onlinemix;

import opt.easyjmetal.problem.onlinemix.gante.PlotUtils;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class Oilschdule {

    public static class KeyValue implements Serializable {
        private String type;
        private double volume;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public double getVolume() {
            return volume;
        }

        public void setVolume(double volume) {
            this.volume = volume;
        }

        public KeyValue(String type, double volume) {
            this.type = type;
            this.volume = volume;
        }
    }

    public static void main(String[] args) {
        int M = 4; // 优化目标个数 100*50
        int k = 75;
        int Na = 50, popsize = 50;
        double[][] pop = new double[popsize][k];
        for (int i = 0; i < popsize; i++) {
            for (int j = 0; j < k; j++) {
                pop[i][j] = Math.random();  // 产生随机种群
            }
        }
        fat(pop);
    }

    // 混合配方
    private static Map<String, String> peifang = new HashMap<>();

    static {
        peifang.put("M1", "R5:R1=1.5:1");
        peifang.put("M2", "R2:R6=1.5:1");
        peifang.put("M3", "R3:R2=5:1");
        peifang.put("M4", "R4");
        peifang.put("M5", "R5");
        peifang.put("M6", "R3:R2=3:1");
        peifang.put("M7", "R2:R6=1:1");
        peifang.put("M8", "R5:R4=2:1");
    }

    static int RT = 6;                                  // 驻留时间
    static double[] DSFR = new double[]{250, 279, 304}; // 蒸馏塔炼油速率
    static int PIPEFR = 840;                            // 匀速

    public static List<List<Double>> fat(double[][] pop) {
        List<List<Double>> eff = new ArrayList<List<Double>>();
        double inf = -1;
        double f1, f2, f3, f4;
        int popsize = pop.length;

        int[][] c1 = new int[][]{
                {0, 11, 12, 13, 7, 15},
                {10, 0, 9, 12, 13, 7},
                {13, 8, 0, 7, 12, 13},
                {13, 12, 7, 0, 11, 12},
                {7, 13, 12, 11, 0, 11},
                {15, 7, 13, 12, 11, 0}
        };// 管道混合成本
        int[][] c2 = new int[][]{
                {0, 11, 12, 13, 10, 15},
                {11, 0, 11, 12, 13, 10},
                {12, 11, 0, 10, 12, 13},
                {13, 12, 10, 0, 11, 12},
                {10, 13, 12, 11, 0, 11},
                {15, 10, 13, 12, 11, 0}
        };// 罐底混合成本

        for (int p = 0; p < popsize; p++) { //解遍历
            BackTrace back = null;

            // 炼油计划
            Map<String, Queue<KeyValue>> feedingPackages = new HashMap<>();
            Queue<KeyValue> ds1 = new LinkedList<>();//按照插入顺序排序
            ds1.add(new KeyValue("M1", 3500.0));
            ds1.add(new KeyValue("M6", 38500.0));
            feedingPackages.put("DS1", ds1);
            Queue<KeyValue> ds2 = new LinkedList<>();//按照插入顺序排序
            ds2.add(new KeyValue("M3", 12000.0));
            ds2.add(new KeyValue("M5", 20369.5));
            ds2.add(new KeyValue("M7", 14502.4));
            feedingPackages.put("DS2", ds2);
            Queue<KeyValue> ds3 = new LinkedList<>();//按照插入顺序排序
            ds3.add(new KeyValue("M2", 7200.0));
            ds3.add(new KeyValue("M4", 32000.0));
            ds3.add(new KeyValue("M2", 11872.0));
            feedingPackages.put("DS3", ds3);

            Object[][] TKS = new Object[][]{                     // 容量  原油类型  已有容量 蒸馏塔  供油开始时间 供油结束时间
                    {20000, 1, 3500, 1, 0, 0},
                    {20000, 2, 7200, 3, 0, 0},
                    {20000, 3, 12000, 2, 0, 0},
                    {20000, 4, 20000, 3, 0, 0},
                    {16000, 5, 16000, 2, 0, 0},
                    {16000, inf, 0, 0, 0, 0},
                    {16000, inf, 0, 0, 0, 0},
                    {16000, inf, 0, 0, 0, 0},
                    {16000, inf, 0, 0, 0, 0},
                    {16000, inf, 0, 0, 0, 0}
            };

            double[] x = pop[p];
            List<List<Double>> schedulePlan = new ArrayList<>();

            // 初始指派
            double[] DSFET = new double[]{0, 0, 0};
            for (int k = 0; k < TKS.length; k++) { // 油罐选择
                if (Double.parseDouble(TKS[k][1].toString()) != inf) {
                    int ds = Integer.parseInt(TKS[k][3].toString());
                    String type = TKS[k][1].toString();
                    double vol = Double.parseDouble(TKS[k][2].toString());
                    double Temp = DSFET[ds - 1];
                    DSFET[ds - 1] = Temp + vol / DSFR[ds - 1];
                    List<Double> list = new ArrayList<>();
                    list.add((double) ds);// 蒸馏塔号
                    list.add((double) k + 1); // 油罐号
                    list.add((double) Math.round(Temp * 100.0) / 100.0);            // 开始供油t
                    list.add((double) Math.round(DSFET[ds - 1] * 100.0) / 100.0);   // 供油罐结束t
                    list.add(Double.parseDouble(type));                             // 原油类型
                    schedulePlan.add(list);
                    TKS[k][3] = ds; // 记录蒸馏塔
                    TKS[k][4] = Temp;// 供油开始时间
                    TKS[k][5] = DSFET[ds - 1];// 供油结束时间

                    // 进料包减去罐中的原油
                    Queue<KeyValue> keyValues = feedingPackages.get("DS" + ds);
                    if (keyValues.peek().getType().equals("M" + type)) {
                        if (keyValues.peek().getVolume() == vol) {
                            keyValues.remove();
                        } else {
                            keyValues.peek().setVolume(keyValues.peek().getVolume() - vol);
                        }
                    }
                }
            }

            //*******************回溯部分开始*********************//
            back = new BackTrace(x, 0, 0.0, DSFET, feedingPackages, TKS, schedulePlan);
            back.setFlag(false);
            back = backSchedule(back);//每次返回一个可行调度解

            if (back.getFlag()) {
                f1 = TestFun.gNum(schedulePlan);        // 供油罐个数
                f2 = TestFun.gChange(schedulePlan);     // 蒸馏塔的油罐切换次数
                f3 = TestFun.gDmix(schedulePlan, c1);   // 管道混合成本
                f4 = TestFun.gDimix(schedulePlan, c2);  // 罐底混合成本
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
     * 检查调度计划是否合理
     *
     * @param schedulePlan 蒸馏塔号 | 油罐号1 | 开始供油时间 | 结束供油时间 | 原油类型 | 油罐号2
     * @return
     */
    public static boolean check(List<List<Double>> schedulePlan) {
        // 按照油罐进行分组
        Map<Double, List<List<Double>>> collect = schedulePlan.stream().collect(Collectors.groupingBy(e -> e.get(1)));
        // 检查每个罐是否冲突
        for (Double d : collect.keySet()) {
            // 按照开始时间排序
            List<List<Double>> lists = collect.get(d);
            Collections.sort(lists, (e1, e2) -> (int) Math.ceil(e1.get(2) - e2.get(2)));

            double lastEnd = 0.0;
            double lastType = -1.0;

            for (List<Double> operation : lists) {

                // 各个operation不重叠
                if (operation.get(2) >= lastEnd && lastType != operation.get(4)) {
                    lastEnd = operation.get(3);
                    lastType = operation.get(4);
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 求可用罐的集合，仅考虑空罐
     *
     * @param backTrace
     * @return
     */
    public static List<Integer> getET(BackTrace backTrace) {
        List<Integer> ET = new ArrayList<>();

        int length = backTrace.getTKS().length;
        for (int i = 0; i < length; i++) {
            int tk = i + 1;
            List<List<Double>> ops = new ArrayList<>();
            for (int j = 0; j < backTrace.getSchedulePlan().size(); j++) {
                List<Double> op = backTrace.getSchedulePlan().get(j);
                if (op.get(1) == tk || (op.size() > 5 && op.get(5) == tk)) {
                    ops.add(op);
                }
            }
            List<List<Double>> opCollections = ops.stream()
                    .sorted((e1, e2) -> (int) Math.ceil(Math.abs(e1.get(3) - e2.get(3))))
                    .filter(e -> e.get(3) > backTrace.getTime())    // 过滤结束时间大于当前时间的操作
                    .collect(Collectors.toList());

            if (opCollections.isEmpty()) {
                ET.add(tk);
            }
        }
        return ET;
    }

    /**
     * 求未完成炼油任务的蒸馏塔的集合
     *
     * @param backTrace
     * @return
     */
    public static List<Integer> getUD(BackTrace backTrace) {
        List<Integer> UD = new ArrayList<>();
        Map<String, Queue<KeyValue>> FPs = backTrace.getFP();
        for (int i = 0; i < FPs.size(); i++) {
            int ds = i + 1;

            if (FPs.containsKey("DS" + ds) && !FPs.get("DS" + ds).isEmpty()) {
                UD.add(ds);
            }
        }
        return UD;
    }

    /**
     * 回溯调度
     *
     * @param backTrace 调度之前的系统状态
     * @return 调度之后的系统状态
     */
    public static BackTrace backSchedule(BackTrace backTrace) {

        BackTrace back = CloneUtil.clone(backTrace);
        back.setFlag(false);
        // 绘制甘特图
        PlotUtils.plotSchedule2(back.getSchedulePlan());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List<Integer> ET = getET(back);
        List<Integer> UD = getUD(back);
        int[] footprint = new int[UD.size() + 1];
        // 没有可用的供油罐，当前状态为不可行状态
        if (ET.isEmpty()) {
            for (int i = 0; i < footprint.length; i++) {
                footprint[i] = 1;
            }
        }

        // 判断是否完成炼油计划
        if (isFinished(back)) {
            back.setFlag(true);
        } else if (back.getStep() >= 25) {
            back.setFlag(false);
        } else {
            while (TestFun.all(footprint) == 0 && !back.getFlag() && back.getStep() < 25) {
                ET = getET(back);
                UD = getUD(back);

                boolean ff = false;

                // 停运情况：供油罐的个数不足两个【停运的目的就是为了等待空闲供油罐的释放】
                if (footprint[0] == 0 && ET.size() <= 1) {
                    double pipeStoptime = getPipeStopTime(back);
                    // 计算停运截至时间，即能够停运的最晚时间
                    double[] feedTimes = back.getFeedTime();
                    double tmp = Double.MAX_VALUE;
                    for (int i = 0; i < feedTimes.length; i++) {
                        if(tmp > feedTimes[i]) {
                            tmp = feedTimes[i];
                        }
                    }
                    tmp -= RT;
                    // 若能够停运，则停运
                    if (pipeStoptime > 0 && pipeStoptime < tmp) {
                        ff = true;
                        // 进行停运
                        back = stop(back, pipeStoptime);
                        footprint[0] = 1;
                    }
                } else if (ET.size() > 1) {
                    // TK1和TK2应该不等
                    int TK1 = ET.get(TestFun.getInt(back.getX()[3 * back.getStep()], ET.size() - 1));// 返回0 ~ ET.size - 1的数
                    int TK2 = ET.get(TestFun.getInt(back.getX()[3 * back.getStep() + 1], ET.size() - 1));// 返回0 ~ ET.size - 1的数
                    int DS = UD.get(TestFun.getInt(back.getX()[3 * back.getStep() + 2], UD.size() - 1));

                    // 确保两个供油罐不相等
                    while (TK1 == TK2) {
                        back.getX()[3 * back.getStep() + 1] = Math.random();
                        TK2 = ET.get(TestFun.getInt(back.getX()[3 * back.getStep() + 1], ET.size() - 1));// 返回0 ~ ET.size - 1的数
                    }
                    // 试调度，需要选择两个塔
                    back = tryschedule(back, TK1, TK2, DS);
                    if (back.getFlag() && (OilSchedule.Schedulable(back))) {
                        ff = true;
                    }
                } else {
                    ff = false;
                }
                if (ff) {
                    back.setStep(back.getStep() + 1);
                    back = backSchedule(back);
                } else {
                    back = CloneUtil.clone(backTrace); // 数据回滚
                    UD = getUD(back);
                    back.setFlag(false);

                    //*********** 策略是否已经全部执行尝试 **********
                    //*************** 更改蒸馏塔 *****************
                    for (int i = 0; i < footprint.length; i++) {
                        if (footprint[i] == 0) {
                            if (i > 0) {
                                // 有两个及以上的空罐
                                while (TestFun.getInt(back.getX()[3 * back.getStep() + 2], UD.size() - 1) != i - 1) {
                                    back.getX()[3 * back.getStep() + 2] = Math.random();
                                }
                                footprint[i] = 1;
                            } else {
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
     * 计算停运时间
     *
     * @param backTrace
     * @return
     */
    public static double getPipeStopTime(BackTrace backTrace) {
        double t = Double.MAX_VALUE;

        // 按照油罐进行分组
        Map<Double, List<List<Double>>> collect = backTrace.getSchedulePlan().stream()
                .filter(e -> e.get(0) <= 3 && e.get(3) > backTrace.getTime()) // 过滤
                .collect(Collectors.groupingBy(e -> e.get(1)));
        for (Double d : collect.keySet()) {
            List<List<Double>> lists = collect.get(d);
            double min = Collections.max(lists, (e1, e2) -> (int) Math.ceil(e1.get(3) - e2.get(3))).get(3);
            if (t > min) {
                t = min;
            }
        }
        return t;
    }

    /**
     * 判断是否调度结束
     *
     * @param backtrace
     * @return
     */
    private static boolean isFinished(BackTrace backtrace) {
        if (backtrace.getFP().isEmpty() ||
                (backtrace.getFP().get("DS1").isEmpty() && backtrace.getFP().get("DS2").isEmpty() && backtrace.getFP().get("DS3").isEmpty())) {
            return true;
        }
        return false;
    }

    /**
     * 试调度
     *
     * @param backtrace 调度之前的系统状态
     * @return 调度之后的系统状态【back】
     */
    public static BackTrace tryschedule(BackTrace backtrace, int TK1, int TK2, int DS) {

        // 拷贝一份调度之前的系统状态，以后的更改都会在这个新拷贝的对象上进行。
        BackTrace back = CloneUtil.clone(backtrace);

        // 当前进料包
        Map<String, Queue<KeyValue>> FPs = back.getFP();
        Queue<KeyValue> FP = FPs.get("DS" + DS);

        // 需要混合类型的原油的体积
        String type = FP.peek().getType();
        double volume = FP.peek().getVolume();
        List<KeyValue> oilTypeVolumeRates = getKeyValues(type);

        // 1.计算能够转运的最大体积
        double[] V = getSafeVolume(back, DS, oilTypeVolumeRates);
        double total = 0;
        int types = V.length;
        for (int i = 0; i < types; i++) {
            total += V[i];
        }

        if (total >= 1500) {
            // 2.考虑供油罐的容量约束
            for (int i = 0; i < types; i++) {
                if (i == 0) {
                    V[i] = Math.min(V[i], Double.parseDouble(back.getTKS()[TK1 - 1][0].toString()));
                }
                if (i == 1) {
                    V[i] = Math.min(V[i], Double.parseDouble(back.getTKS()[TK2 - 1][0].toString()));
                }
            }

            // 3.考虑进料包约束
            if (total > volume) {
                for (int i = 0; i < types; i++) {
                    V[i] = Math.min(V[i], volume * oilTypeVolumeRates.get(i).getVolume());
                }
            }

            // 调整转运的原油的体积
            if (types > 1) {
                if (Math.round(V[0] / V[1]) > Math.round(oilTypeVolumeRates.get(0).getVolume() / oilTypeVolumeRates.get(1).getVolume())) {
                    V[0] = V[1] * oilTypeVolumeRates.get(0).getVolume() / oilTypeVolumeRates.get(1).getVolume();
                } else {
                    V[1] = V[0] * oilTypeVolumeRates.get(1).getVolume() / oilTypeVolumeRates.get(0).getVolume();
                }
            }

            if ((types > 1 && V[0] + V[1] == volume) || V[0] == volume) {
                // 进料包转运结束
                FP.remove();
            } else {
                // 进料包递减
                FP.peek().setVolume(types > 1 ? FP.peek().getVolume() - V[0] - V[1] : FP.peek().getVolume() - V[0]);
            }

            // 第一次转运操作
            List<Double> list1 = new ArrayList<>();
            list1.add(4.0);
            list1.add((double) TK1);                                                                    // 油罐号
            list1.add((double) Math.round(back.getTime() * 100.0) / 100.0);                             // 开始供油t
            list1.add((double) Math.round((back.getTime() + V[0] / PIPEFR) * 100.0) / 100.0);           // 供油罐结束t
            list1.add(Double.parseDouble(oilTypeVolumeRates.get(0).getType().substring(1)));            // 原油类型
            back.getSchedulePlan().add(list1);
            back.setTime(list1.get(3));

            // 第二次转运操作
            if (types > 1) {
                List<Double> list2 = new ArrayList<>();
                list2.add(4.0);
                list2.add((double) TK2);                                                                // 油罐号
                list2.add((double) Math.round(back.getTime() * 100.0) / 100.0);                         // 开始供油t
                list2.add((double) Math.round((back.getTime() + V[1] / PIPEFR) * 100.0) / 100.0);       // 供油罐结束t
                list2.add(Double.parseDouble(oilTypeVolumeRates.get(1).getType().substring(1)));        // 原油类型
                back.getSchedulePlan().add(list2);
                back.setTime(list2.get(3));
            }

            // 炼油操作
            double endTime = getFeedingEndTime(back.getSchedulePlan(), back.getTime(), DS);
            total = types > 1 ? V[0] + V[1] : V[0];
            List<Double> list3 = new ArrayList<>();
            list3.add((double) DS);
            list3.add((double) TK1);
            list3.add((double) Math.round(endTime * 100.0) / 100.0);
            list3.add((double) Math.round((endTime + total / DSFR[DS - 1]) * 100.0) / 100.0);   // 炼油结束时间
            list3.add(Double.parseDouble(type.substring(1)));                                   // 原油类型
            if (types > 1) {
                list3.add((double) TK2);
            }
            back.getSchedulePlan().add(list3);
            back.getFeedTime()[DS - 1] = list3.get(3);                                          // 更新炼油结束时间

            back.setFlag(true);
        } else {
            back.setFlag(false);
        }
        return back;
    }

    /**
     * 计算需要原始类型的原油类型和体积
     *
     * @param type
     * @return
     */
    public static List<KeyValue> getKeyValues(String type) {
        // 需要原始类型的原油类型和体积
        List<KeyValue> oilTypeVolumeRates = new ArrayList<>();
        if (peifang.get(type).contains("=")) {
            String[] types = peifang.get(type).split("=")[0].split(":"); // R5:R1=1.5:1
            String[] volumes = peifang.get(type).split("=")[1].split(":"); // R5:R1=1.5:1
            // 混合比例
            KeyValue keyValue1 = new KeyValue(types[0], Double.parseDouble(volumes[0]) / (Double.parseDouble(volumes[0]) + Double.parseDouble(volumes[1])));
            KeyValue keyValue2 = new KeyValue(types[1], Double.parseDouble(volumes[1]) / (Double.parseDouble(volumes[0]) + Double.parseDouble(volumes[1])));
            oilTypeVolumeRates.add(keyValue1);
            oilTypeVolumeRates.add(keyValue2);
        } else {
            // 只有一种原油类型
            KeyValue keyValue = new KeyValue(type, 1);
            oilTypeVolumeRates.add(keyValue);
        }
        return oilTypeVolumeRates;
    }

    public static double getFeedingEndTime(List<List<Double>> schedulePlan, double currentTime, double DS) {
        Map<Double, List<List<Double>>> collect = schedulePlan.stream()
                .filter(e -> e.get(0) <= 3 && e.get(3) > currentTime) // 过滤
                .collect(Collectors.groupingBy(e -> e.get(0)));
        List<List<Double>> lists = collect.get(DS);
        double endTime = Double.MAX_VALUE;
        if (lists != null && !lists.isEmpty()) {
            endTime = Collections.max(lists, (e1, e2) -> (int) Math.ceil(Math.abs(e1.get(3) - e2.get(3)))).get(3);
        }
        return endTime;
    }

    /**
     * 计算要转运的原始原油的类型和体积【满足驻留时间约束的最大体积】
     *
     * @param back
     * @param DS
     * @param oilTypeVolumes
     * @return
     */
    public static double[] getSafeVolume(BackTrace back, int DS, List<KeyValue> oilTypeVolumes) {
        int len = oilTypeVolumes.size();
        double[] V = new double[len];

        // 当前时间
        double currentTime = back.getTime();

        // 计算炼油时间
        double endTime = getFeedingEndTime(back.getSchedulePlan(), back.getTime(), DS);

        // 计算能够转运的最大的原油体积
        double volume = PIPEFR * (endTime - currentTime - RT);

        // 计算原始种类的原油需要转运的体积
        for (int i = 0; i < len; i++) {
            V[i] = Math.round(volume * oilTypeVolumes.get(i).getVolume() * 100.0) / 100.0;
        }

        return V;
    }

    /**
     * 停运
     *
     * @param backtrace 调度之前的系统状态
     * @return 调度之后的系统状态【back】
     */
    public static BackTrace stop(BackTrace backtrace, double pipeStoptime) {
        // 拷贝一份调度之前的系统状态，以后的更改都会在这个新拷贝的对象上进行。
        BackTrace back = CloneUtil.clone(backtrace);
        back.setFlag(true);

        // 停运操作
        List<Double> list1 = new ArrayList<>();
        list1.add(4.0);
        list1.add(0.0);                                         // 油罐号
        list1.add(back.getTime());                              // 开始供油t
        list1.add(pipeStoptime);                                // 供油罐结束t
        list1.add(0.0);            // 原油类型
        back.getSchedulePlan().add(list1);
        back.setTime(list1.get(3));

        return back;
    }
}
