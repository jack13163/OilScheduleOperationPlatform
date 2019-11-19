package com.sim.oil.cop;

import com.sim.common.CSVHelper;
import com.sim.oil.Config;
import com.sim.operation.Operation;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.solution.impl.DefaultDoubleSolution;

import java.util.List;

public class COPOilScheduleIndividualDecode {

	/**
	 * 解码
	 * @param path
	 * @param row
	 * @param ruleName
	 */
	public static void decode(String path, int row, String ruleName) {
		// 读取对应的记录
		CSVHelper csvHelper = new CSVHelper();
		csvHelper.setSeperator(' ');
		List<String[]> lines = csvHelper.readCSV(path, false);
		String[] content = lines.get(row);

		// 解码
		DoubleSolution solution = getDoubleSolution(content, ruleName);
		COPOilScheduleIndividualDecode.decode(solution, ruleName);
	}

	/**
	 * 解码
	 * 
	 * @param solution
	 * @param ruleName
	 * @return
	 */
	private static double[] decode(DoubleSolution solution, String ruleName) {

		// 开始仿真
		COPOilScheduleSimulationScheduler scheduler = new COPOilScheduleSimulationScheduler(Config.getInstance(), false, ruleName);
		scheduler.start(solution);
		List<Operation> operations = scheduler.getOperations();
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
		Operation.creatSangSen(operations);
		double[] freeTimes = Operation.getTankMaxFreeTime(operations);

		return new double[] { hardCost, energyCost, pipeMixingCost, tankMixingCost, numberOfChange, numberOfTankUsed };
	}

	/**
	 * 构建solution
	 * 
	 * @param solutioncode
	 * @param ruleName
	 * @return
	 */
	private static DoubleSolution getDoubleSolution(String[] solutioncode, String ruleName) {
		DoubleSolution solution = new DefaultDoubleSolution(new OilScheduleConstrainedOptimizationProblem(ruleName));
		for (int i = 0; i < solutioncode.length - 1; i++) {// 忽略最后一个空格
			Double value = Double.parseDouble(solutioncode[i]);
			solution.setVariableValue(i, value);
		}
		return solution;
	}
}
