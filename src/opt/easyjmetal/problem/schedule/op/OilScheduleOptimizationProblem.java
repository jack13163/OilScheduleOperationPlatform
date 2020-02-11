package opt.easyjmetal.problem.schedule.op;

import opt.easyjmetal.core.Problem;
import opt.easyjmetal.core.Solution;
import opt.easyjmetal.encodings.solutiontype.RealSolutionType;
import opt.easyjmetal.problem.schedule.Config;
import opt.easyjmetal.problem.schedule.models.DSObject;
import opt.easyjmetal.problem.schedule.models.FPObject;
import opt.easyjmetal.problem.schedule.operation.Operation;
import opt.easyjmetal.problem.schedule.util.CloneUtils;
import opt.easyjmetal.problem.schedule.util.RealtimeChart;
import opt.easyjmetal.util.JMException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 原油短期生产调度的无约束求解方法
 *
 * @author Administrator
 */
public class OilScheduleOptimizationProblem extends Problem {
    private Config config;
    private boolean ShowDetail = true;

    /**
     * Creates a new instance of opt.jmetal.problem.oil schedule problem.
     */
    public OilScheduleOptimizationProblem(String ruleName) {
        this(false, ruleName);
    }

    /**
     * 构造函数入口
     */
    @SuppressWarnings("unchecked")
    public OilScheduleOptimizationProblem(boolean showEachStep, String ruleName) {
        config = CloneUtils.clone(Config.getInstance().loadConfig());

        // 【决策次数】
        int numberOfVariables = 0;
        int N1 = 0;
        int N2 = 0;
        List<DSObject> dss = Config.getInstance().getDSs();
        for (int i = 0; i < dss.size(); i++) {
            List<FPObject> fps = dss.get(i).getFps();
            for (int j = 0; j < fps.size(); j++) {
                if (fps.get(j).getVolume() > 0) {
                    if (fps.get(j).getSite() == 1) {
                        // 【低熔点管道调度】
                        N1 += Math.ceil(fps.get(j).getVolume() / config.VolMin);
                    } else if (fps.get(j).getSite() == 2) {
                        // 【高熔点管道调度】
                        N2 += Math.ceil(fps.get(j).getVolume() / config.VolMin);
                    }
                }
            }
        }
        numberOfVariables_ = (N2 + N1) * 2 + Config.stopTimes;// 决策变量的个数
        numberOfObjectives_ = 5;// 目标个数
        numberOfConstraints_ = 0;// 约束个数【只有一个硬性约束】
        problemName_ = "Oil";// 问题名

        upperLimit_ = new double[numberOfVariables_];
        lowerLimit_ = new double[numberOfVariables_];
        for (int i = 0; i < numberOfVariables; i++) {
            lowerLimit_[i] = 0.0;
            upperLimit_[i] = 1.0;
        }

        solutionType_ = new RealSolutionType(this);// 编码类型
    }

    /**
     * 获取问题配置
     *
     * @return
     */
    public Map<String, Object> getProblemConfig() {
        Map<String, Object> result = new HashMap<String, Object>();

        // 【决策次数】
        int numberOfVariables = 0;
        int N1 = 0;
        int N2 = 0;
        List<DSObject> dss = Config.getInstance().getDSs();
        for (int i = 0; i < dss.size(); i++) {
            List<FPObject> fps = dss.get(i).getFps();
            for (int j = 0; j < fps.size(); j++) {
                if (fps.get(j).getVolume() > 0) {
                    if (fps.get(j).getSite() == 1) {
                        // 【低熔点管道调度】
                        N1 += Math.ceil(fps.get(j).getVolume() / config.VolMin);
                    } else if (fps.get(j).getSite() == 2) {
                        // 【高熔点管道调度】
                        N2 += Math.ceil(fps.get(j).getVolume() / config.VolMin);
                    }
                }
            }
        }
        numberOfVariables = (N2 + N1) * 2 + Config.stopTimes;//决策变量个数，当设置停运次数过小时，可能导致数组越界异常
        result.put("numberOfVariables", numberOfVariables);
        // 上下界
        List<Double> lowerLimit = new ArrayList<>(numberOfVariables);
        List<Double> upperLimit = new ArrayList<>(numberOfVariables);
        for (int i = 0; i < numberOfVariables; i++) {
            if (i % 2 == 0) {
                lowerLimit.add(0.0);
                upperLimit.add(1.0);// TK,DS【主动停运或被动停运】
            } else if (i % 2 == 1) {
                lowerLimit.add(0.0);
                upperLimit.add(1.0);// Speed
            }
        }
        result.put("lowerLimit", lowerLimit);
        result.put("upperLimit", upperLimit);

        return result;
    }

    /**
     * 解码【需要保证解码操作的原子性】
     *
     * @param solution
     * @param config
     * @return
     */
    public double[] decode(Solution solution) {

        // java类锁：确保多个对象访问一个代码块时的进程同步
        synchronized (OilScheduleOptimizationProblem.class) {
            // 开始仿真: "EDF_PS", "EDF_TSS", "BT"
            OPOilScheduleSimulationScheduler controller = new OPOilScheduleSimulationScheduler(CloneUtils.clone(config), true, "BT");
            try {
                controller.start(solution);
            } catch (JMException e) {
                e.printStackTrace();
            }
            List<Operation> operations = controller.getOperations();
            // 检查是否违背供油罐生命周期约束
            if (!Operation.check(operations)) {
                System.out.println("operation error.");
                System.exit(1);
            }

            // 计算硬约束
            double hardCost = Operation.getDelayCost(operations);
            if (hardCost != 0.0) {
                System.out.println("optimization problem's hardCost don't equals to 0.");
            }

            // 计算软约束
            double energyCost = Operation.getEnergyCost(operations);
            double pipeMixingCost = Operation.getPipeMixingCost(operations);
            double tankMixingCost = Operation.getTankMixingCost(operations);
            double numberOfChange = Operation.getNumberOfChange(operations);
            double numberOfTankUsed = Operation.getNumberOfTankUsed(operations);

            if (Config.ShowDetail) {
                // 输出详细调度
                System.out.println("============================================================================");
                System.out.println("detail schedule :");
                Operation.printOperation(operations);
                System.out.println("============================================================================");

                // 输出代价
                System.out.println("============================================================================");
                System.out.println("cost :");
                System.out.println("hardCost :" + hardCost);
                System.out.println("----------------------------------------------------------------------------");
                System.out.println("energyCost :" + energyCost);
                System.out.println("pipeMixingCost :" + pipeMixingCost);
                System.out.println("tankMixingCost :" + tankMixingCost);
                System.out.println("numberOfChange :" + numberOfChange);
                System.out.println("numberOfTankUsed :" + numberOfTankUsed);
                System.out.println("============================================================================");

                // 绘制甘特图
                Operation.plotSchedule2(operations);
            }

            // 绘制实时图像
            if (Config.ShowHardCostChart) {
                RealtimeChart.getInstance().plot(hardCost);
            }

            return new double[]{hardCost, energyCost, pipeMixingCost, tankMixingCost, numberOfChange,
                    numberOfTankUsed};
        }
    }

    @Override
    public void evaluate(Solution solution) throws JMException {
        int violatedConstraints = 0;
        // 解码
        double[] result = decode(solution);
        for (int i = 1; i < result.length; i++) {
            // 设置目标值【目标自动设置】
            solution.setObjective(i - 1, result[i]);
        }
    }
}
