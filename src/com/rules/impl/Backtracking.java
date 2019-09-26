package com.rules.impl;

import java.util.List;

import org.uma.jmetal.solution.DoubleSolution;

import com.models.FactObject;
import com.models.Fragment;
import com.rules.AbstractRule;
import com.sim.common.CodeHelper;
import com.sim.experiment.Config;
import com.sim.experiment.ISimulationScheduler;
import com.sim.oil.op.OPOilScheduleSimulationScheduler;
import com.sim.operation.Operation;

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

	/**
	 * 判断是否满足定理1
	 * 
	 * @return
	 */
	private boolean enterUnsafeState(FactObject factObject) {
		OPOilScheduleSimulationScheduler scheduler = (OPOilScheduleSimulationScheduler) _scheduler;

		if (Operation.getHardCost(scheduler.getOperations()) > 0) {
			return true;
		}

		Config config = factObject.getConfig();
		double[] usableTime = scheduler.getDeadlineTime();

		for (int i = 0; i < usableTime.length; i++) {
			if (usableTime[i] <= config.RT) {
				return true;
			}
		}
		return false;
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

		// 3.进入不可行状态，标记
		if (enterUnsafeState(factObject)) {
			// 回溯，将高熔点塔的所有策略标记为0，低熔点塔的所有策略将会自动在回溯后标记
			ds = config.HighOilDS;
			tank = 0;
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

				// 6.未找到可用策略，则利用高熔点管道停运回溯
				if (policies[ds - 1][tank] == 0) {
					ds = config.HighOilDS;
					tank = 0;
				}

				// 5.解码转运体积
				vol = vols[ds - 1][tank];
				if (tank != 0 && vol == 0.0) {
					throw new Exception("停运异常");
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
		if (tank == 0 && ds == config.HighOilDS) {
			preemptiveScheduling(scheduler);
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

	/**
	 * 计算所有的最大体积
	 * 
	 * @return
	 */
	public double[][] calculateMaxVolume(FactObject factObjects) {
		OPOilScheduleSimulationScheduler scheduler = (OPOilScheduleSimulationScheduler) _scheduler;
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
		DoubleSolution solution = ((DoubleSolution) factObjects.getSolution());
		double code = solution.getVariableValue(loc * 2 + 1).doubleValue();
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
					// 4. 保证供油罐占用不冲突的安全体积
					double safe_vol = scheduler.getMaxSafeVolume(tank, ds, chargingSpeed);
					// 5.判断体积是否低于下限
					vols[i][j] = scheduler.getVolume(fp_vol, rt_vol, safe_vol, capacity);
					if (scheduler.filterCondition(vols[i][j], fp_vol)) {
						vols[i][j] = 0;// 【会出现可选该罐，但体积为0的情况】
					}
				}
			}
		}
		return vols;
	}

	/**
	 * 抢占式调度策略
	 * 
	 * @param scheduler
	 */
	private void preemptiveScheduling(OPOilScheduleSimulationScheduler scheduler) {

		try {
			// 1.设置当前所有的策略为不可用
			Integer[][] currentPolicies = scheduler.policyStack.peek();
			for (int i = 0; i < currentPolicies.length; i++) {
				for (int j = 0; j < currentPolicies[i].length; j++) {
					currentPolicies[i][j] = 0;
				}
			}

			// 2.获取最需要转运原油的蒸馏塔
			emergencyDs = scheduler.getMostEmergencyDS();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public String toString() {
		return "Backtracking";
	}
}
