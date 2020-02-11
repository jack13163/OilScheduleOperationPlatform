package opt.easyjmetal.problem.schedule.rules.impl;

import opt.easyjmetal.core.Solution;
import opt.easyjmetal.problem.schedule.Config;
import opt.easyjmetal.problem.schedule.cop.COPOilScheduleSimulationScheduler;
import opt.easyjmetal.problem.schedule.models.FactObject;
import opt.easyjmetal.problem.schedule.models.Fragment;
import opt.easyjmetal.problem.schedule.rules.AbstractRule;
import opt.easyjmetal.problem.schedule.util.ArrayHelper;
import opt.easyjmetal.problem.schedule.util.CodeHelper;
import opt.easyjmetal.problem.schedule.util.ISimulationScheduler;
import opt.easyjmetal.util.JMException;

import java.util.List;

/**
 * 主动停运策略
 *
 * @author Administrator
 */
public class EarliestDealineFirstAndPreemptiveScheduling extends AbstractRule {

    public EarliestDealineFirstAndPreemptiveScheduling(ISimulationScheduler scheduler) {
        super(scheduler);
    }

    @Override
    public Fragment decode(FactObject factObject) throws JMException {
        COPOilScheduleSimulationScheduler scheduler = (COPOilScheduleSimulationScheduler) _scheduler;
        int numOfTanks = factObject.getConfig().getTanks().size();
        Integer[][] policies = scheduler.policyStack.peek();// 可用策略
        int loc = factObject.getLoc();
        Config config = factObject.getConfig();
        Solution solution = factObject.getSolution();
        int tank = -1;
        int ds = -1;
        double vol = -1.0;

        // 1.计算所有策略的最大转运体积
        double[][] vols = calculateMaxVolume(factObject);

        // 2.剔除生成策略中的不可用策略，即原油的最大转运体积低于某一个下限或者供油罐不可用的策略
        for (int i = 0; i < vols.length; i++) {
            for (int j = 1; j < vols[i].length; j++) {// 停运策略不剔除
                if (vols[i][j] == 0.0 && policies[i][j] != 0) {
                    policies[i][j] = 0;
                }
            }
        }

        // 3.蒸馏塔按照紧急程度排序
        double[] feedEndTime = scheduler.getFeedingEndTime();
        int[] sortedDsIndexs = ArrayHelper.Arraysort(feedEndTime);

        try {
            // 3.确定蒸馏塔
            for (int i = 0; i < sortedDsIndexs.length; i++) {
                ds = sortedDsIndexs[i] + 1;
                int sum = 0;
                for (int j = 0; j < numOfTanks + 1; j++) {
                    sum += policies[ds - 1][j];
                }
                if (sum > 0) {
                    break;
                }
            }
            // 4.解码供油罐【关键】
            double code1 = solution.getDecisionVariables()[loc * 2].getValue();
            tank = CodeHelper.getCol(code1, 1, numOfTanks + 1) - 1;
            // 从中间向两边搜索距离最近的策略，左边优先搜索
            if (policies[ds - 1][tank] == 0) {
                int maxSearch = Math.max(tank, numOfTanks - tank);
                for (int k = 1; k <= maxSearch; k++) {
                    // 左边搜索【防止越界】
                    int left = tank - k;
                    if (left >= 0) {
                        int indexOfTank = left;

                        if (policies[ds - 1][indexOfTank] != 0) {
                            tank = indexOfTank;
                            break;
                        }
                    }
                    // 右边搜索
                    int right = tank + k;
                    if (right < numOfTanks + 1) {
                        int indexOfTank = right;

                        if (policies[ds - 1][indexOfTank] != 0) {
                            tank = indexOfTank;
                            break;
                        }
                    }
                }
            }

            // 5.解码转运体积
            vol = vols[ds - 1][tank];
            // 6.未找到可用策略，则利用高熔点管道停运回溯
            if (policies[ds - 1][tank] == 0) {
                ds = config.HighOilDS;
                tank = 0;
            } else {
                if (tank != 0 && vol == 0.0) {
                    throw new Exception("停运异常");
                }
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        // 6.解码转运速度
        double speed = 0;
        try {
            // 解码转运速度
            double code2 = solution.getDecisionVariables()[loc * 2 + 1].getValue();

            // 判断转运管道，并选择转运速度
            int pipe = scheduler.getCurrentPipe(ds);
            double[] chargingSpeeds = scheduler.getCharingSpeed(pipe);
            speed = chargingSpeeds[CodeHelper.getRow(code2, 3, 1) - 1];
        } catch (Exception e) {
            System.out.println("getSpeed error");
            e.printStackTrace();
        }

        return new Fragment(ds, tank, vol, speed);
    }

    /**
     * 计算所有的最大体积
     *
     * @return
     */
    public double[][] calculateMaxVolume(FactObject factObjects) throws JMException {
        COPOilScheduleSimulationScheduler scheduler = (COPOilScheduleSimulationScheduler) _scheduler;
        Config config = factObjects.getConfig();
        Integer[][] policies = scheduler.policyStack.peek();
        int rows = policies.length;
        int cols = policies[0].length;
        double[][] vols = new double[rows][cols];
        for (int i = 0; i < vols.length; i++) {
            for (int j = 0; j < vols[i].length; j++) {
                vols[i][j] = 0;
            }
        }

        // 1.确定转运速度的下标，具体速度需要根据管道确定，而管道又可以通过蒸馏塔确定
        int loc = factObjects.getLoc();
        Solution solution = factObjects.getSolution();
        double code = solution.getDecisionVariables()[loc * 2 + 1].getValue();
        int indexOfSpeed = -1;
        try {
            indexOfSpeed = CodeHelper.getRow(code, 3, 1) - 1;
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 2.筛选可用的策略【是否满足体积下限，供油罐是否可用】
        for (int i = 0; i < policies.length; i++) {
            // 每个蒸馏塔对应一条管道，不同管道对应的不同可用罐集合不同
            int ds = i + 1;
            int pipe = scheduler.getCurrentPipe(ds);
            List<Integer> tankSet = scheduler.getTankSet(scheduler.getCurrentTime(pipe));

            for (int j = 0; j < policies[i].length; j++) {
                int tank = j;
                if (tank > 0 && policies[i][tank] != 0 && tankSet.contains(tank)) {
                    double chargingSpeed = scheduler.getChargingSpeed(ds)[indexOfSpeed];// 计算转运速度
                    // 1.进料包
                    double fp_vol = config.getDSs().get(ds - 1).getNextOilVolume();
                    // 2.供油罐容量
                    double capacity = config.getTanks().get(tank - 1).getCapacity();
                    // 3.满足驻留时间约束的安全体积
                    double rt_vol = scheduler.getRTVolume(ds, chargingSpeed);
                    // 4.根据是否满足定理1来计算安全体积
                    if (rt_vol > 0) {
                        // 4.1 保证供油罐占用不冲突的安全体积，正常情况下
                        double safe_vol = scheduler.getMaxSafeVolume(tank, ds, chargingSpeed);
                        vols[i][j] = scheduler.getVolume(fp_vol, rt_vol, safe_vol, capacity);
                    } else {
                        // 4.2 处理不满足驻留时间约束的意外情况
                        double safe_vol = scheduler.getMaxSafeVolumeUnnormal(tank, ds, chargingSpeed);
                        vols[i][j] = scheduler.getVolume(fp_vol, safe_vol, capacity);
                    }
                    // 5.判断体积是否低于下限
                    if (scheduler.filterCondition(vols[i][j], fp_vol)) {
                        vols[i][j] = 0;// 【会出现可选该罐，但体积为0的情况】
                    }
                }
            }
        }
        return vols;
    }

    @Override
    public String toString() {
        return "EDF_PS";
    }
}
