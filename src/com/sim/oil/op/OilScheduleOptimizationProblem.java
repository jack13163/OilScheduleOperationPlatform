package com.sim.oil.op;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.uma.jmetal.problem.impl.AbstractDoubleProblem;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.solution.impl.DefaultDoubleSolution;

import com.models.DSObject;
import com.models.FPObject;
import com.sim.common.CloneUtils;
import com.sim.experiment.Config;
import com.sim.operation.Operation;
import com.sim.ui.RealtimeChart;

/**
 * 原油短期生产调度的无约束求解方法
 * 
 * @author Administrator
 *
 */
public class OilScheduleOptimizationProblem extends AbstractDoubleProblem {
	private static final long serialVersionUID = 1L;
	private static Logger logger = LogManager.getLogger(OilScheduleOptimizationProblem.class.getName());

	private Config config;
	private boolean showEachStep;
	private String ruleName = "";

	/**
	 * Creates a new instance of oil schedule problem.
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
		this.showEachStep = showEachStep;
		this.ruleName = ruleName;

		Map<String, Object> conf = getProblemConfig();
		int numOfVariables = (int) conf.get("numberOfVariables");
		setNumberOfVariables(numOfVariables);// 决策变量的个数
		setNumberOfObjectives(5);// 目标个数
		setNumberOfConstraints(0);// 约束个数
		setName("OneStep");// 问题名
		List<Double> lowerLimit = (List<Double>) conf.get("lowerLimit");
		List<Double> upperLimit = (List<Double>) conf.get("upperLimit");
		setLowerLimit(lowerLimit);
		setUpperLimit(upperLimit);
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
		numberOfVariables = (N2 + N1 * 2) * 2;
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
	 * 评价适应值
	 */
	@Override
	public void evaluate(DoubleSolution solution) {
		double[] result = decode(solution);
		for (int i = 0; i < result.length; i++) {
			// 设置目标值
			solution.setObjective(i, result[i]);
		}
	}

	/**
	 * 解码【需要保证解码操作的原子性】
	 * 
	 * @param solution
	 * @param config
	 * @return
	 */
	public double[] decode(DoubleSolution solution) {

		// java类锁：确保多个对象访问一个代码块时的进程同步
		synchronized (OilScheduleOptimizationProblem.class) {
			// 开始仿真
			OPOilScheduleSimulationScheduler controller = new OPOilScheduleSimulationScheduler(CloneUtils.clone(config),
					showEachStep, ruleName);
			controller.start(solution);
			List<Operation> operations = controller.getOperations();
			// 检查是否违背供油罐生命周期约束
			if (!Operation.check(operations)) {
				logger.fatal("operation error.");
				System.exit(1);
			}

			// 计算硬约束
			double hardCost = Operation.getHardCost(operations);
			if (hardCost != 0.0) {
				logger.fatal("optimization problem's hardCost don't equals to 0.");
			}

			// 计算软约束
			double energyCost = Operation.getEnergyCost(operations);
			double pipeMixingCost = Operation.getPipeMixingCost(operations);
			double tankMixingCost = Operation.getTankMixingCost(operations);
			double numberOfChange = Operation.getNumberOfChange(operations);
			double numberOfTankUsed = Operation.getNumberOfTankUsed(operations);

			if (Config.ShowDetail) {
				// 输出详细调度
				logger.info("============================================================================");
				logger.info("detail schedule :");
				Operation.printOperation(operations);
				logger.info("============================================================================");

				// 输出代价
				logger.info("============================================================================");
				logger.info("cost :");
				logger.info("hardCost :" + hardCost);
				logger.info("----------------------------------------------------------------------------");
				logger.info("energyCost :" + energyCost);
				logger.info("pipeMixingCost :" + pipeMixingCost);
				logger.info("tankMixingCost :" + tankMixingCost);
				logger.info("numberOfChange :" + numberOfChange);
				logger.info("numberOfTankUsed :" + numberOfTankUsed);
				logger.info("============================================================================");

				// 绘制甘特图
				Operation.plotSchedule2(operations);
			}

			// 绘制实时图像
			if (Config.ShowHardCostChart) {
				RealtimeChart.getInstance().plot(hardCost);
			}

			return new double[] { energyCost, pipeMixingCost, tankMixingCost, numberOfChange, numberOfTankUsed };
		}
	}

	@Override
	public DoubleSolution createSolution() {
		return new DefaultDoubleSolution(this);
	}
}
