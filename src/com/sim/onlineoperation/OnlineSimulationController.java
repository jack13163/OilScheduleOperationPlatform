package com.sim.onlineoperation;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.models.DSObject;
import com.models.FPObject;
import com.models.Fragment;
import com.models.TankObject;
import com.sim.common.MathHelper;
import com.sim.oil.Config;
import com.sim.operation.Operation;
import com.sim.operation.OperationType;

/**
 * 关键在于冲突检查
 * 
 * @author Administrator
 */
public class OnlineSimulationController {

	// 日志记录
	private Logger logger = LogManager.getLogger(OnlineSimulationController.class.getName());

	private double currentTime;

	private List<Operation> operations;

	public List<Operation> getOperations() {
		return operations;
	}

	public OnlineSimulationController() {

		operations = new LinkedList<>();
	}

	/**
	 * 判断是否调度结束
	 * 
	 * @return
	 */
	public boolean isFinished() {
		int count = 0;
		for (DSObject dsObject : Config.getInstance().getDSs()) {
			if (dsObject.getNextOilVolume() < 0) {
				count++;
			}
		}
		if (count < Config.getInstance().getDSs().size()) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * 获取所有罐的最近将要注油的截止时间
	 * 
	 * @return
	 */
	private Map<Integer, Double> getChargingDeadlineTimeOfAllTanks() {
		Operation.sortOperation(operations);

		Map<Integer, Double> times = new HashMap<>();
		for (Operation operation : operations) {
			if (operation.getType() == OperationType.Charging && operation.getStart() > currentTime) {
				if (!times.containsKey(operation.getTank())) {
					times.put(operation.getTank(), operation.getStart());
				}
			}
		}

		return times;
	}

	/**
	 * 获取所有罐的最近将要炼油的时间
	 * 
	 * @param currentTime
	 * @return
	 */
	private Map<Integer, Double> getFeedingDeadlineTimeOfAllTanks(double currentTime) {
		Operation.sortOperation(operations);

		Map<Integer, Double> times = new HashMap<>();
		for (Operation operation : operations) {
			if (operation.getType() == OperationType.Feeding && operation.getStart() > currentTime) {
				if (!times.containsKey(operation.getTank())) {
					times.put(operation.getTank(), operation.getStart());
				}
			}
		}

		return times;
	}

	/**
	 * 获取所有罐的最近将要使用的时间
	 * 
	 * @param currentTime
	 * @return
	 */
	private Map<Integer, Double> getDeadlineTimeOfAllTanks(double currentTime) {
		Operation.sortOperation(operations);

		Map<Integer, Double> times = new HashMap<>();
		for (Operation operation : operations) {
			if (operation.getStart() > currentTime) {
				if (!times.containsKey(operation.getTank())) {
					times.put(operation.getTank(), operation.getStart());
				}
			}
		}

		return times;
	}

	/**
	 * 计算最大可转运体积， 保证不冲突【以最大速度转运】
	 * 
	 * @return
	 */
	public Double getMaxVolumeWithMaxSpeed(int tank, int ds) {

		// 确定转运和炼油速度
		double[] chargingSpeeds = Config.getInstance().getPipes().get(0).getChargingSpeed();
		double chargingSpeed = chargingSpeeds[chargingSpeeds.length - 1];// 【以最大速度转运】

		return getMaxVolume(tank, ds, chargingSpeed);
	}

	/**
	 * 计算最大可转运体积， 保证不冲突【以最小速度转运】
	 * 
	 * @return
	 */
	public Double getMaxVolumeWithMinSpeed(int tank, int ds) {

		// 确定转运和炼油速度
		double[] chargingSpeeds = Config.getInstance().getPipes().get(0).getChargingSpeed();
		double chargingSpeed = chargingSpeeds[0];// 【以最小速度转运】

		return getMaxVolume(tank, ds, chargingSpeed);
	}

	/**
	 * 计算最大可转运体积， 保证不冲突【供规则引擎调用】
	 * 
	 * @return
	 */
	public Double getMaxVolume(int tank, int ds, double chargingSpeed) {

		double vol = Double.MIN_VALUE;

		try {
			// 确定炼油速度
			double feedingSpeed = Config.getInstance().getDSs().get(ds - 1).getSpeed();

			// 采用倒推的方法确定最大转运体积
			if (getChargingDeadlineTimeOfAllTanks().containsKey(tank)) {
				double deadline = getChargingDeadlineTimeOfAllTanks().get(tank);

				if (deadline - currentTime > Config.getInstance().RT) {

					double vol1 = chargingSpeed * feedingSpeed * (deadline - currentTime - Config.getInstance().RT)
							/ (chargingSpeed + feedingSpeed);

					// 关键问题：理想情况下从后往前推没有太大问题，但是，现实是，由于转运过去的原油不是立即就开始
					// 用于蒸馏塔炼油，因此，可能会延后，如果延后的时间过久，就会导致当前的feeding过程与下一个决策的
					// charging过程相互重叠，最终导致发生了冲突，因此，还需要添加一个考虑因素vol2。
					double vol2 = 0.0;

					if (getFeedingDeadlineTimeOfAllTanks(currentTime).containsKey(tank)) {
						vol2 = (getFeedingDeadlineTimeOfAllTanks(currentTime).get(tank) - getFeedingEndTime()[ds - 1])
								/ Config.getInstance().getDSs().get(ds - 1).getSpeed();
					} else {
						vol2 = Double.MAX_VALUE;
					}

					vol = Math.min(vol1, vol2);

				} else {

					// 不能转运原油
					vol = Double.MIN_VALUE;
				}
			} else {
				vol = Double.MAX_VALUE;
			}
		} catch (Exception e) {
			logger.fatal("calculate volume error.");
			System.exit(1);
		}

		return vol;
	}

	/**
	 * 获取管道的停运时间，假设高熔点管道和低熔点管道都可以停运。 tank=0代表这是一个停运操作，根据ds判断是高熔点管道停运还是低熔点管道停运
	 * 
	 * @return
	 */
	public double[] getChargingEndTime() {
		// 管道的个数
		int numOfPipes = Config.getInstance().getPipes().size();
		double[] chargingEndTime = new double[numOfPipes];

		// 求各个蒸馏塔的注油结束时间
		for (Operation operation : operations) {
			if (operation.getType() == OperationType.Charging || operation.getType() == OperationType.Stop) {
				// 高熔点管道转运
				if (operation.getDs() != Config.getInstance().HighOilDS) {
					// 最晚注油结束时间
					if (chargingEndTime[0] < operation.getEnd()) {
						chargingEndTime[0] = operation.getEnd();
					}
				} else {
					if (chargingEndTime[1] < operation.getEnd()) {
						chargingEndTime[1] = operation.getEnd();
					}
				}
			}
		}

		return chargingEndTime;
	}

	/**
	 * 获取所有塔的炼油结束时间
	 * 
	 * @return
	 */
	private double[] getFeedingEndTime() {

		double[] feedEndTime = new double[Config.getInstance().getDSs().size()];

		// 求各个蒸馏塔的炼油结束时间
		for (int i = 0; i < Config.getInstance().getDSs().size(); i++) {
			int ds = i + 1;
			for (Operation operation : operations) {
				if (operation.getDs() == ds && operation.getType() == OperationType.Feeding) {
					// 最晚炼油结束时间
					if (feedEndTime[i] < operation.getEnd()) {
						feedEndTime[i] = operation.getEnd();
					}
				}
			}
		}

		// 标记已经完成炼油计划的塔
		for (int i = 0; i < Config.getInstance().getDSs().size(); i++) {
			// 完成炼油计划的蒸馏塔不在考虑范围
			if (Config.getInstance().getDSs().get(i).getNextOilVolume() == -1) {
				feedEndTime[i] = Double.MAX_VALUE;
				continue;
			}
		}
		return feedEndTime;
	}

	private enum TankState {
		empty, hotting, charging, feeding, waiting
	}

	/**
	 * 获取当前时刻供油罐的状态，是否可用
	 * 
	 * @param currentTime
	 */
	private Map<Integer, TankState> getTankStatus(double currentTime) {
		// 按照开始时间排序
		Operation.sortOperation(operations);

		Map<Integer, TankState> tankState = new HashMap<Integer, TankState>();
		TableModel model = getTankStateModel(currentTime);

		int rowCount = model.getRowCount();
		int colCount = model.getColumnCount();

		// 逐个罐地确定状态
		for (int i = 0; i < rowCount; i++) {

			int tank = i + 1;
			String state = model.getValueAt(i, colCount - 1).toString();// 读取你获取行号的某一列的值（也就是字段）

			// 确定罐当前时刻的状态，先确定最简单的两种类型，接着根据上次操作的类型确定是等待还是空闲
			if (state.equals("waiting")) {
				tankState.put(tank, TankState.waiting);
			} else if (state.equals("hotting")) {
				tankState.put(tank, TankState.hotting);
			} else if (state.equals("feeding")) {
				tankState.put(tank, TankState.feeding);
			} else if (state.equals("charging")) {
				tankState.put(tank, TankState.charging);
			} else if (state.equals("empty")) {
				tankState.put(tank, TankState.empty);
			}
		}

		return tankState;
	}

	/**
	 * 获取供油罐的最早释放时间，为停运提供参考
	 * 
	 * @return
	 */
	private double getEarliestAvailableTime(double currentTime) {
		Map<Integer, Double> availableTimes = new HashMap<Integer, Double>();

		// 逐个罐地确定状态
		for (int i = 0; i < Config.getInstance().getTanks().size(); i++) {
			int tank = i + 1;
			for (Operation operation : operations) {
				// 找到对应的罐
				if (operation.getTank() == tank && operation.getEnd() > currentTime) {

					// 加热或炼油结束后可用
					if (operation.getType() == OperationType.Hoting || operation.getType() == OperationType.Feeding) {

						availableTimes.put(tank, operation.getEnd());
						break;
					}
				}
			}
		}

		double time = Double.MAX_VALUE;

		for (Double value : availableTimes.values()) {
			if (time > value) {
				time = value;
			}
		}
		return time;
	}

	/**
	 * 获取供油罐的状态
	 * 
	 * @return
	 */
	public DefaultTableModel getTankStateModel(double currentTime) {
		Operation.sortOperation(operations);

		Object[] columnNames = { "供油罐", "容量", "原油种类", "原油体积", "开始时间", "结束时间", "状态" };

		DefaultTableModel model = new DefaultTableModel();
		model.setColumnIdentifiers(columnNames);

		for (int i = 0; i < Config.getInstance().getTanks().size(); i++) {
			int tank = i + 1;
			Object[] data = { tank, Config.getInstance().getTanks().get(tank - 1).getCapacity(), 0, 0, 0, 0, "empty" };

			// 记录前一决策和后一决策
			Operation lastOperation = null;
			Operation nextOperation = null;

			for (Operation operation : operations) {
				// 找到对应的罐
				if (operation.getTank() == tank) {
					if (operation.getStart() > currentTime) {
						nextOperation = operation;
						break;
					} else {
						// 标记上一状态
						lastOperation = operation;
					}
				}
			}

			// 没有任何决策记录
			if (lastOperation == null && nextOperation == null) {
				// 空罐的特殊处理
				data[2] = 0;
				data[3] = 0;
				data[4] = 0;
				data[5] = 0;
				data[6] = "empty";
			} else {
				// 前一个决策非空
				if (lastOperation != null) {
					// 根据上次操作的类型确定是等待还是空闲
					if (lastOperation.getStart() <= currentTime && currentTime < lastOperation.getEnd()) {
						if (lastOperation.getType() == OperationType.Hoting) {
							// 注油状态
							data[2] = lastOperation.getOil();
							data[3] = MathHelper.precision(
									lastOperation.getVol()
											- (currentTime - lastOperation.getStart()) * lastOperation.getSpeed(),
									Config.getInstance().NumOfDivide);// 计算剩余体积
							data[4] = lastOperation.getStart();
							data[5] = lastOperation.getEnd();
							data[6] = "hoting";
						} else if (lastOperation.getType() == OperationType.Charging) {
							// 注油状态
							data[2] = lastOperation.getOil();
							data[3] = MathHelper.precision(
									(currentTime - lastOperation.getStart()) * lastOperation.getSpeed(),
									Config.getInstance().NumOfDivide);// 计算注油体积
							data[4] = lastOperation.getStart();
							data[5] = lastOperation.getEnd();
							data[6] = "charging";
						} else if (lastOperation.getType() == OperationType.Feeding) {
							// 供油状态
							data[2] = lastOperation.getOil();
							data[3] = MathHelper.precision(
									lastOperation.getVol()
											- (currentTime - lastOperation.getStart()) * lastOperation.getSpeed(),
									Config.getInstance().NumOfDivide);// 计算剩余体积
							data[4] = lastOperation.getStart();
							data[5] = lastOperation.getEnd();
							data[6] = "feeding";
						}
					} else {
						if (lastOperation.getType() == OperationType.Charging) {

							// 等待状态
							data[2] = lastOperation.getOil();
							data[3] = MathHelper.precision(lastOperation.getVol(), Config.getInstance().NumOfDivide);
							data[4] = lastOperation.getStart();
							data[5] = lastOperation.getEnd();
							data[6] = "waiting";
						} else {
							// 等待状态
							data[2] = 0;
							data[3] = 0;
							data[4] = 0;
							data[5] = 0;
							data[6] = "empty";
						}
					}
				} else if (nextOperation.getType() == OperationType.Feeding) {

					// 前一决策为空，初始库存的特殊处理
					data[2] = nextOperation.getOil();
					data[3] = MathHelper.precision(nextOperation.getVol(), Config.getInstance().NumOfDivide);
					data[4] = nextOperation.getStart();
					data[5] = nextOperation.getEnd();
					data[6] = "waiting";
				}
			}

			model.addRow(data);

		}
		return model;
	}

	/**
	 * 获取各个蒸馏塔进料包大小
	 * 
	 * @return
	 */
	public TableModel getFpModel() {
		Object[] columnNames = { "蒸馏塔", "原油种类", "原油体积", "所在位置" };

		DefaultTableModel model = new DefaultTableModel();
		model.setColumnIdentifiers(columnNames);
		for (int i = 0; i < Config.getInstance().getDSs().size(); i++) {
			int ds = i + 1;

			List<FPObject> fpObjects = Config.getInstance().getDSs().get(i).getFps();

			for (FPObject fpObject : fpObjects) {
				// 忽略已经完成的进料包
				if (fpObject.getVolume() > 0) {
					Object[] data = new Object[columnNames.length];
					data[0] = ds;
					data[1] = fpObject.getOiltype();
					data[2] = fpObject.getVolume();
					data[3] = fpObject.getSite();
					model.addRow(data);
				}
			}
		}
		return model;
	}

	/**
	 * 获取成本
	 * 
	 * @return
	 */
	public TableModel getCostModel() {
		Object[] columnNames = { "约束违背值", "切换次数", "罐底混合成本", "管道混合成本", "能耗成本", "用罐个数" };

		DefaultTableModel model = new DefaultTableModel();
		model.setColumnIdentifiers(columnNames);
		Object[] data = new Object[columnNames.length];
		data[0] = Operation.getHardCost(operations);
		data[1] = Operation.getNumberOfChange(operations);
		data[2] = Operation.getTankMixingCost(operations);
		data[3] = Operation.getPipeMixingCost(operations);
		data[4] = Operation.getEnergyCost(operations);
		data[5] = Operation.getNumberOfTankUsed(operations);
		model.addRow(data);
		return model;
	}

	/**
	 * 初始化仿真
	 */
	public void initSimulation() {

		// 加载初始化配置
		Config.getInstance().loadConfig();

		// 炼油结束时间
		double[] feedEndTime = new double[Config.getInstance().getDSs().size()];

		// 执行初始指派(低熔点塔)
		for (int i = 0; i < Config.getInstance().getTanks().size(); i++) {
			TankObject tankObject = Config.getInstance().getTanks().get(i);
			double vol = MathHelper.precision(tankObject.getVolume(), Config.getInstance().Precision);
			int tank = i + 1;

			if (vol > 0) {
				if (Config.HotTank != tank) {

					int oiltype = Config.getInstance().getTanks().get(i).getOiltype();
					int ds = tankObject.getAssign();
					double speed = Config.getInstance().getDSs().get(ds - 1).getSpeed();
					double feedTime = MathHelper.precision(vol / speed, Config.getInstance().Precision);

					Operation feeding = new Operation(OperationType.Feeding, tank, ds,
							MathHelper.precision(feedEndTime[ds - 1], Config.getInstance().Precision),
							MathHelper.precision(feedEndTime[ds - 1] + feedTime, Config.getInstance().Precision), vol,
							oiltype, speed, 0);// 初始库存就在炼油厂内，用0表示
					operations.add(feeding);

					// 更新炼油结束时间
					feedEndTime[ds - 1] += feedTime;
				}
			}
		}
		// 执行初始指派(高熔点塔)
		int tank = Config.HotTank;
		int ds = Config.getInstance().HighOilDS;
		TankObject tankObject = Config.getInstance().getTanks().get(tank - 1);
		double vol = MathHelper.precision(tankObject.getVolume(), Config.getInstance().Precision);
		double vPipe = Config.getInstance().getPipes().get(1).getVol();
		int oiltype = Config.getInstance().getTanks().get(tank - 1).getOiltype();
		double feedingSpeed = Config.getInstance().getDSs().get(Config.getInstance().HighOilDS - 1).getSpeed();
		double feedingTime = MathHelper.precision(vPipe / feedingSpeed, Config.getInstance().Precision);

		double paperSpeed = 625;// 为了对比论文而设置
		double hotingTime = MathHelper.precision(vol / paperSpeed, Config.getInstance().Precision);
		double chargingTime = MathHelper.precision(vPipe / paperSpeed, Config.getInstance().Precision);
		Operation hoting = new Operation(OperationType.Hoting, tank, Config.getInstance().HighOilDS, 0, hotingTime, vol,
				oiltype, paperSpeed, 2);
		Operation charging = new Operation(OperationType.Charging, tank, Config.getInstance().HighOilDS,
				MathHelper.precision(hotingTime, Config.getInstance().Precision),
				MathHelper.precision(hotingTime + chargingTime, Config.getInstance().Precision), vPipe, oiltype,
				paperSpeed, 2);
		// 加热管道后的低熔点原油会重新供给蒸馏塔炼油
		ds = Config.getInstance().getTanks().get(tank - 1).getAssign();
		feedingSpeed = Config.getInstance().getDSs().get(ds - 1).getSpeed();
		feedingTime = MathHelper.precision(vPipe / feedingSpeed, Config.getInstance().Precision);
		Operation feeding = new Operation(OperationType.Feeding, tank, ds,
				MathHelper.precision(feedEndTime[ds - 1], Config.getInstance().Precision),
				MathHelper.precision(feedEndTime[ds - 1] + feedingTime, Config.getInstance().Precision), vPipe, oiltype,
				feedingSpeed, 0);

		operations.add(hoting);
		operations.add(charging);
		operations.add(feeding);
	}

	/**
	 * 下一步决策
	 * 
	 * @param fragment
	 * @return
	 * @throws Exception
	 */
	public boolean doOperation(Fragment fragment) throws Exception {

		// 获取下一步操作
		int tank = fragment.getTank();
		int ds = fragment.getDs();
		double speed = fragment.getSpeed();
		double vol = fragment.getVolume();

		double[] feedEndTimes = getFeedingEndTime();
		double[] chargingEndTimes = getChargingEndTime();

		// 判断需要转运的管道和转运结束时间
		double currentTime = (ds != Config.getInstance().HighOilDS) ? chargingEndTimes[0] : chargingEndTimes[1];

		// 判断是否停运
		if (tank == 0) {
			double availableTime = getEarliestAvailableTime(currentTime);
			Operation stoping = new Operation(OperationType.Stop, tank, ds,
					MathHelper.precision(currentTime, Config.getInstance().Precision),
					MathHelper.precision(availableTime, Config.getInstance().Precision), vol, 0, 0, 0);

			// 更新进料包和炼油结束时间
			operations.add(stoping);
		} else {

			// 1.判断当前供油罐是否可以使用
			Map<Integer, TankState> tankState = getTankStatus(currentTime);
			if (tankState.containsKey(tank) && tankState.get(tank) != TankState.empty) {
				throw new Exception("确保您选择的供油罐为空。");
			}

			// 2.判断转运的原油是否超过炼油计划
			if (vol > Config.getInstance().getDSs().get(ds - 1).getNextOilVolume()) {
				throw new Exception("炼油计划中，蒸馏塔" + ds + "不需要转运那么多体积的原油。");
			}

			// 3.判断是否满足供油罐容量约束
			if (vol > Config.getInstance().getTanks().get(tank - 1).getCapacity()) {
				throw new Exception(
						"make sure the volume of oil to translated is smaller than the capacity of the tank you selected.");
			}

			// 确定原油类型
			int oiltype = Config.getInstance().getDSs().get(ds - 1).getNextOilType();
			// 确定下一原油来自于哪一个港口
			int site = Config.getInstance().getDSs().get(ds - 1).getWhereNextOilFrom();
			// 确定注油时间
			double chargingTime = MathHelper.precision(vol / speed, Config.getInstance().Precision);

			// 确定炼油速率和炼油时间
			double feedingSpeed = Config.getInstance().getDSs().get(ds - 1).getSpeed();
			double feedingTime = MathHelper.precision(vol / feedingSpeed, Config.getInstance().Precision);

			// 确定开始炼油时间
			double feedingStartTime = feedEndTimes[ds - 1];

			// 4.判断是否满足驻留时间约束
			if (currentTime + chargingTime + Config.getInstance().RT > feedEndTimes[ds - 1]) {
				// 当不满足驻留时间约束时，会采取后退的方式
				feedingStartTime = currentTime + chargingTime + Config.getInstance().RT;
			}

			// 5.判断本次注油是否和别的操作有冲突
			Map<Integer, Double> usingTimes = getDeadlineTimeOfAllTanks(currentTime);
			if (usingTimes.containsKey(tank) && usingTimes.get(tank) < feedingStartTime + feedingTime) {
				throw new Exception("供油罐占用冲突。");
			}

			// 确定操作
			Operation charging = new Operation(OperationType.Charging, tank, ds,
					MathHelper.precision(currentTime, Config.getInstance().Precision),
					MathHelper.precision(currentTime + chargingTime, Config.getInstance().Precision), vol, oiltype,
					speed, site);
			Operation feeding = new Operation(OperationType.Feeding, tank, ds,
					MathHelper.precision(feedingStartTime, Config.getInstance().Precision),
					MathHelper.precision(feedingStartTime + feedingTime, Config.getInstance().Precision), vol, oiltype,
					feedingSpeed, site);

			// 更新进料包和炼油结束时间
			operations.add(charging);
			operations.add(feeding);
			Config.getInstance().getDSs().get(ds - 1).updateOilVolume(vol);
		}

		return true;
	}
}