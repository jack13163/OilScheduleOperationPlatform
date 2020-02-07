package opt.easyjmetal.problem.schedule.cop;

import oil.models.DSObject;
import oil.models.FPObject;
import opt.easyjmetal.core.Problem;
import opt.easyjmetal.core.Solution;
import opt.easyjmetal.encodings.solutiontype.RealSolutionType;
import opt.easyjmetal.problem.schedule.Config;
import opt.easyjmetal.problem.schedule.operation.Operation;
import opt.easyjmetal.problem.schedule.util.CloneUtils;
import opt.easyjmetal.util.JMException;

import java.util.List;

/**
 * 原油短期生产调度的约束求解方法
 *
 * @author Administrator
 */
public class OilScheduleConstrainedOptimizationProblem extends Problem {

    private Config config;
    private boolean ShowDetail = true;

    public OilScheduleConstrainedOptimizationProblem() {
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
        numberOfConstraints_ = 1;// 约束个数【只有一个硬性约束】
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
     * 解码【需要保证解码操作的原子性】
     *
     * @param solution
     * @return
     */
    public double[] decode(Solution solution) {

        // java类锁：确保多个对象访问一个代码块时的进程同步
        synchronized (OilScheduleConstrainedOptimizationProblem.class) {
            // 开始仿真: "EDF_PS", "EDF_TSS", "BT"
            COPOilScheduleSimulationScheduler controller = new COPOilScheduleSimulationScheduler(CloneUtils.clone(config), true, "EDF_PS");
            controller.start(solution);
            List<Operation> operations = controller.getOperations();
            // 检查是否违背供油罐生命周期约束
            if (!Operation.check(operations)) {
                System.out.println("operation error.");
                System.exit(1);
            }

            // 计算硬约束
            double delayCost = Operation.getDelayCost(operations);
            double maintenanceCost = Operation.getTankMaintenanceTime(operations);
            double hardCost = delayCost + maintenanceCost;

            // 计算软约束
            double energyCost = Operation.getEnergyCost(operations);
            double pipeMixingCost = Operation.getPipeMixingCost(operations);
            double tankMixingCost = Operation.getTankMixingCost(operations);
            double numberOfChange = Operation.getNumberOfChange(operations);
            double numberOfTankUsed = Operation.getNumberOfTankUsed(operations);

            if (Config.ShowDetail && hardCost == 0.0) {
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
                if (ShowDetail) {
                    Operation.plotSchedule2(operations);
                }
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
        for (int i = 0; i < result.length; i++) {
            if (i == 0) {
                // 设置约束值【最小化约束违背，所以应该将其规划化为最小化问题】
                double overallConstraintViolation = -result[0];
                if (Math.abs(overallConstraintViolation) > 0) {
                    violatedConstraints = 1;
                }

                overallConstraintViolationDegree.setAttribute(solution, overallConstraintViolation);
                numberOfViolatedConstraints.setAttribute(solution, violatedConstraints);
            } else {
                // 设置目标值【目标自动设置】
                solution.setObjective(i - 1, result[i]);
            }
        }
    }

    /**
     * Evaluates the constraint overhead of a solution
     *
     * @param solution
     *            The solution
     * @throws JMException
     */
    public void evaluateConstraints(Solution solution) throws JMException {

        int violatedConstraints = 0;
        // 解码
        double[] result = decode(solution);
        for (int i = 0; i < result.length; i++) {
            if (i == 0) {
                // 设置约束值【最小化约束违背，所以应该将其规划化为最小化问题】
                double overallConstraintViolation = -result[0];
                if (Math.abs(overallConstraintViolation) > 0) {
                    violatedConstraints = 1;
                }

                overallConstraintViolationDegree.setAttribute(solution, overallConstraintViolation);
                numberOfViolatedConstraints.setAttribute(solution, violatedConstraints);
            } else {
                // 设置目标值【目标自动设置】
                solution.setObjective(i - 1, result[i]);
            }
        }

        solution.setOverallConstraintViolation(total);
        solution.setNumberOfViolatedConstraint(number);
    } // evaluateConstraints
}