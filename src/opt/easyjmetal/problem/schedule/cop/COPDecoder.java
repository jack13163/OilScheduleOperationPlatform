package opt.easyjmetal.problem.schedule.cop;

import opt.easyjmetal.core.Solution;
import opt.easyjmetal.problem.schedule.Config;
import opt.easyjmetal.problem.schedule.operation.Operation;
import opt.easyjmetal.util.JMException;
import opt.easyjmetal.util.ParetoFrontUtil;

import java.util.List;

public class COPDecoder {

    public static void main(String[] args) {

        String[] algorithmNames = {"NSGAII_CDP", "ISDEPLUS_CDP", "NSGAIII_CDP",
                "MOEAD_CDP", "MOEAD_IEpsilon", "MOEAD_Epsilon",
                "MOEAD_SR", "C_MOEAD", "PPS_MOEAD"};
        String[] problemNames = {"EDF_PS", "EDF_TSS"};
        int runtimes = 10;
        String basePath_ = "result/easyjmetal/twopipeline";

        // 查找出指定的解
        double[][] tofind = new double[][]{
                {815.42, 276.0, 230.0, 29.0, 11.0}
        };
        try {
            ParetoFrontUtil.getSolutionFromDB(algorithmNames, problemNames, runtimes, tofind,
                    (solution, rule) -> COPDecoder.decode(solution, rule), basePath_);
        } catch (JMException e) {
            e.printStackTrace();
        }
    }

    /**
     * 解码
     *
     * @param solution
     * @param ruleName
     * @return
     */
    public static double[] decode(Solution solution, String ruleName) {
        return decode(solution, ruleName, false);
    }

    /**
     * 解码
     *
     * @param solution
     * @param ruleName
     * @return
     */
    public static double[] decode(Solution solution, String ruleName, boolean showSchedule) {

        // 开始仿真
        Config config = Config.getInstance().loadConfig("config.xml");
        COPScheduler scheduler = new COPScheduler(config, false, ruleName);
        scheduler.start(solution);
        List<Operation> operations = scheduler.getOperations();
        // 检查是否违背供油罐生命周期约束
        if (!Operation.check(operations)) {
            System.out.println("operation error.");
            System.exit(1);
        }

        // 计算硬约束
        double delayCost = Operation.getDelayCostN(scheduler.getConfig(), operations);
        double maintenanceCost = Operation.getTankMaintenanceTime(operations);
        double hardCost = delayCost + maintenanceCost;

        // 计算软约束
        double energyCost = Operation.getEnergyCost(operations);
        double pipeMixingCost = Operation.getPipeMixingCost(operations);
        double tankMixingCost = Operation.getTankMixingCost(operations);
        double numberOfChange = Operation.getNumberOfChange(operations);
        double numberOfTankUsed = Operation.getNumberOfTankUsed(operations);

        // 绘制甘特图
        if (showSchedule) {
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

            Operation.plotSchedule2(operations);
            Operation.creatSangSen(operations);
        }
        double[] freeTimes = Operation.getTankMaxFreeTime(operations);

        return new double[]{hardCost, energyCost, pipeMixingCost, tankMixingCost, numberOfChange, numberOfTankUsed};
    }

    /**
     * 解码两条管道的能耗
     *
     * @param solution
     * @param ruleName
     * @return
     */
    public static double[] decodePipelineEnergyConsumption(Solution solution, String ruleName) {

        // 开始仿真
        Config config = Config.getInstance().loadConfig("config.xml");
        COPScheduler scheduler = new COPScheduler(config, false, ruleName);
        scheduler.start(solution);
        List<Operation> operations = scheduler.getOperations();
        // 检查是否违背供油罐生命周期约束
        if (!Operation.check(operations)) {
            System.out.println("operation error.");
            System.exit(1);
        }

        // 计算硬约束
        double delayCost = Operation.getDelayCostN(scheduler.getConfig(), operations);
        double maintenanceCost = Operation.getTankMaintenanceTime(operations);
        double hardCost = delayCost + maintenanceCost;

        // 计算软约束
        double pipeOneEC = Operation.getPipelineOneEnergyCost(operations);
        double pipeTwoEC = Operation.getPipelineTwoEnergyCost(operations);
        double totalEC = Operation.getEnergyCost(operations);

        // 输出结果
        System.out.println("============================================================================");
        System.out.println("hardCost :" + hardCost);
        System.out.println("----------------------------------------------------------------------------");
        System.out.println("pipeOneEC :" + pipeOneEC);
        System.out.println("pipeTwoEC :" + pipeTwoEC);
        System.out.println("totalEC :" + totalEC);
        System.out.println("============================================================================");

        return new double[]{hardCost, pipeOneEC, pipeTwoEC, totalEC};
    }
}
