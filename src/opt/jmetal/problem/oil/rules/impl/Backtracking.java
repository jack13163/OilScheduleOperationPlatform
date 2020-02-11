package opt.jmetal.problem.oil.rules.impl;

import opt.jmetal.problem.oil.models.FactObject;
import opt.jmetal.problem.oil.models.Fragment;
import opt.jmetal.solution.DoubleSolution;
import opt.jmetal.util.JMetalLogger;

import opt.jmetal.problem.oil.rules.AbstractRule;
import opt.jmetal.problem.oil.sim.common.CodeHelper;
import opt.jmetal.problem.oil.sim.oil.Config;
import opt.jmetal.problem.oil.sim.common.ISimulationScheduler;
import opt.jmetal.problem.oil.sim.oil.op.OPOilScheduleSimulationScheduler;

/**
 * 主动停运策略
 *
 * @author Administrator
 */
public class Backtracking extends AbstractRule {
    // 判断冲突是否解决
    public static int emergencyDs = -1;

    public Backtracking(ISimulationScheduler scheduler) {
        super(scheduler);
    }

    @Override
    public Fragment decode(FactObject factObject) {
        OPOilScheduleSimulationScheduler scheduler = (OPOilScheduleSimulationScheduler) _scheduler;
        int numOfDSs = factObject.getConfig().getDSs().size();
        int numOfTanks = factObject.getConfig().getTanks().size();
        Integer[][] policies = scheduler.policyStack.peek();// 可用策略
        int loc = factObject.getLoc();
        Config config = factObject.getConfig();
        DoubleSolution solution = ((DoubleSolution) factObject.getSolution());
        int tank = -1;
        int ds = -1;
        double vol = -1.0;
        boolean backFlag = false;

        // 1.计算所有策略的最大转运体积
        double[][] vols = scheduler.calculateMaxVolume();

        // 2.剔除生成策略中的不可用策略，即原油的最大转运体积低于某一个下限或者供油罐不可用的策略
        for (int i = 0; i < vols.length; i++) {
            for (int j = 1; j < vols[i].length; j++) {// 停运策略不剔除
                if (vols[i][j] == 0.0 && policies[i][j] != 0) {
                    policies[i][j] = 0;
                }
            }
        }

        // 3.进入不可行状态，标记
        if (scheduler.enterUnsafeState()) {
            // 回溯，将高熔点塔的所有策略标记为0，低熔点塔的所有策略将会自动在回溯后标记
            backFlag = true;
        } else {
            try {
                double code1 = solution.getVariableValue(loc * 2).doubleValue();

                // 3.1 确定蒸馏塔
                if (emergencyDs > 0) {
                    // 先转运最需要转运原油的蒸馏塔
                    ds = emergencyDs;
                } else {
                    // 从中间向两边搜索距离最近的策略，上边优先搜索
                    ds = CodeHelper.getRow(code1, numOfDSs, numOfTanks + 1);
                    int sum = 0;
                    for (int j = 0; j < numOfTanks + 1; j++) {
                        sum += policies[ds - 1][j];
                    }
                    // 若当前行不存在可行决策
                    if (sum == 0) {
                        int maxSearch = Math.max(ds - 1, numOfDSs - ds);
                        for (int k = 1; k <= maxSearch; k++) {
                            // 上边搜索【防止越界】
                            int top = ds - k;
                            if (top >= 1) {
                                int sum1 = 0;
                                for (int j = 0; j < numOfTanks + 1; j++) {
                                    sum1 += policies[top - 1][j];
                                }
                                if (sum1 > 0) {
                                    ds = top;
                                    break;
                                }
                            }

                            // 右边搜索
                            int buttom = ds + k;
                            if (buttom <= numOfDSs) {
                                int sum2 = 0;
                                for (int j = 0; j < numOfTanks + 1; j++) {
                                    sum2 += policies[buttom - 1][j];
                                }
                                if (sum2 > 0) {
                                    ds = buttom;
                                    break;
                                }
                            }
                        }
                    }
                }

                // 4.确定供油罐
                if (emergencyDs > 0) {
                    // 回溯时不考虑停运
                    for (int i = 0; i < policies.length; i++) {
                        policies[i][0] = 0;
                    }
                }
                // 从中间向两边搜索距离最近的策略，左边优先搜索
                tank = CodeHelper.getCol(code1, numOfDSs, numOfTanks + 1) - 1;
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

                // 5.未找到可用策略，则做设置记号
                if (policies[ds - 1][tank] == 0) {
                    backFlag = true;
                }

                // 出不安全状态做标记
                if (ds == emergencyDs && tank != 0) {
                    // 封路
                    for (int i = 0; i < policies.length; i++) {
                        if (i + 1 != emergencyDs) {
                            for (int j = 0; j < policies[i].length; j++) {
                                policies[i][j] = 0;
                            }
                        }
                    }
                    emergencyDs = -1;
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }

        // 回溯，优先调度最需要的
        if (backFlag) {
            tank = 0;
            ds = config.HighOilDS;
            scheduler.preemptiveScheduling();
        }

        // 5.解码转运体积
        vol = vols[ds - 1][tank];
        if (tank != 0 && vol == 0.0) {
            JMetalLogger.logger.info("停运异常");
            System.exit(1);
        }

        // 6.解码转运速度
        double speed = 0;
        try {
            // 解码转运速度
            double code2 = solution.getVariableValue(loc * 2 + 1).doubleValue();

            // 判断转运管道，并选择转运速度
            double[] chargingSpeeds = scheduler.getChargingSpeed(ds);
            speed = chargingSpeeds[CodeHelper.getRow(code2, 3, 1) - 1];
        } catch (Exception e) {
            System.out.println("getSpeed error");
            e.printStackTrace();
        }

        return new Fragment(ds, tank, vol, speed);
    }

    @Override
    public String toString() {
        return "Backtracking";
    }
}
