package com.sim.oil.cop;

import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.uma.jmetal.solution.DoubleSolution;

import com.models.DSObject;
import com.models.FactObject;
import com.models.Fragment;
import com.models.TankObject;
import com.rules.AbstractRule;
import com.rules.RuleFactory;
import com.sim.common.ArrayHelper;
import com.sim.common.CloneUtils;
import com.sim.common.MathUtil;
import com.sim.experiment.Config;
import com.sim.experiment.ISimulationScheduler;
import com.sim.operation.Operation;
import com.sim.operation.OperationType;

/**
 * 关键在于冲突检查
 * 
 * @author Administrator
 */
public class COPOilScheduleSimulationScheduler implements ISimulationScheduler {

	private Logger logger = LogManager.getLogger(COPOilScheduleSimulationScheduler.class.getName());

	private List<Operation> operations = new LinkedList<>();// 最终的决策序列
	private DoubleSolution solution;// 决策指令序列
	private boolean plotEachStep;// 是否输出每一步的调度
	private String ruleName = "";// 规则名称

	// 当前决策序号
	private int loc = 0;
	private Config config;

	// 系统状态栈
	private Stack<Fragment> fragmentStack = new Stack<>();// 基因栈
	public Stack<Integer[][]> policyStack = new Stack<>();// 策略栈
	private Stack<Operation> operationStack = new Stack<>();// 决策栈
	private Stack<Config> configStack = new Stack<>();// 配置栈

	/**
	 * 新建一个和当前配置相同的配置
	 * 
	 * @return
	 */
	private Config newConfig() {
		return CloneUtils.clone(config);
	}

	/**
	 * 移除栈顶的配置，并更改config的指向
	 */
	private void removeConfig() {
		configStack.pop();
		config = configStack.peek();
	}

	/**
	 * 向栈中插入新的配置，并更改config的指向
	 * 
	 * @param newConfig
	 */
	private void addConfig(Config newConfig) {
		configStack.push(newConfig);
		config = configStack.peek();
	}

	/**
	 * 获取最终的决策序列
	 * 
	 * @return
	 */
	public List<Operation> getOperations() {
		return operations;
	}

	/**
	 * 单步调度
	 * 
	 * @param config   调度的配置信息
	 * @param ruleName 规则名称
	 */
	public COPOilScheduleSimulationScheduler(Config config, String ruleName) {
		this.config = config;
		this.plotEachStep = false;
		this.ruleName = ruleName;
	}

	/**
	 * 单步调度
	 * 
	 * @param config       调度的配置信息
	 * @param showEachStep 是否显示每一步的结果
	 * @param ruleName     规则名称
	 */
	public COPOilScheduleSimulationScheduler(Config config, boolean showEachStep, String ruleName) {
		this.config = config;
		this.plotEachStep = showEachStep;
		this.ruleName = ruleName;
	}

	/**
	 * 开始
	 */
	public void start(DoubleSolution solution) {
		// 清空决策队列
		getOperations().clear();
		fragmentStack.clear();
		operationStack.clear();
		configStack.clear();
		policyStack.clear();
		loc = 0;

		initSimulation(solution);
		process();
	}

	/**
	 * 获取各个蒸馏塔的炼油持续时间【负值代表已经延误】
	 * 
	 * @return
	 */
	public double[] getFeedingLastTime() {
		double[] deadlines = getFeedingEndTime();
		double tmp = Double.MAX_VALUE;
		for (int i = 0; i < deadlines.length; i++) {
			int ds = i + 1;
			int pipe = getCurrentPipe(ds);
			double currentTime = getCurrentTime(pipe);
			if (deadlines[i] - currentTime < tmp) {
				deadlines[i] = deadlines[i] - currentTime;
			}
		}
		return deadlines;
	}

	/**
	 * 获取最需要转运原油的蒸馏塔
	 * 
	 * @return
	 */
	public int getMostEmergencyDS() {
		double[] oilEndTime = getFeedingEndTime();
		List<Double> oilEndTimeList = new ArrayList<>();
		for (int i = 0; i < oilEndTime.length; i++) {
			oilEndTimeList.add(oilEndTime[i]);
		}
		DoubleSummaryStatistics stat = oilEndTimeList.stream().mapToDouble(x -> x).summaryStatistics();
		double minTime = stat.getMin();
		int ds = oilEndTimeList.indexOf(minTime) + 1;
		return ds;
	}

	/**
	 * 获取最紧急的蒸馏塔【解码时参考】
	 * 
	 * @return
	 */
	public int getMostNeedOilDS() {
		double[] oilEndTime = getFeedingEndTime();
		int ds = -1;
		double tmp = Double.MAX_VALUE;
		for (int i = 0; i < oilEndTime.length; i++) {
			int pipe = getCurrentPipe(i);
			double currentTime = getCurrentTime(pipe);
			if (oilEndTime[i] - currentTime < tmp) {
				tmp = oilEndTime[i] - currentTime;
				ds = i + 1;
			}
		}
		if (tmp <= 24) {
			return ds;
		} else {
			return -1;
		}
	}

	/**
	 * 计算最大安全转运体积，避免罐的占用冲突
	 * 
	 * @param tank
	 * @param ds
	 * @param chargingSpeed
	 * @return
	 */
	public double getMaxSafeVolume(int tank, int ds, double chargingSpeed) {
		double vol = 0;

		// 1.当前时间T
		int pipe = getCurrentPipe(ds);
		double currentTime = getCurrentTime(pipe);

		// 2.炼油结束时间T1
		double[] feedEndTimes = getFeedingEndTime();
		double feedEndTime = feedEndTimes[ds - 1];

		// 3供油罐开始被用到的时刻T2
		Map<Integer, Double> usingTimes = getDeadlineTimeOfAllTanks(currentTime);
		double feedingSpeed = Config.getInstance().getDSs().get(ds - 1).getSpeed();
		if (usingTimes.containsKey(tank)) {
			vol = MathUtil.round(feedingSpeed * (usingTimes.get(tank) - feedEndTime), config.Precision);
		} else {
			vol = Double.MAX_VALUE;
		}

		return vol;
	}

	/**
	 * 计算不正常情况下最大安全转运体积，避免罐的占用冲突
	 * 
	 * @param tank
	 * @param ds
	 * @param chargingSpeed
	 * @return
	 */
	public double getMaxSafeVolumeUnnormal(int tank, int ds, double chargingSpeed) {
		double vol = 0;

		// 1.当前时间T
		int pipe = getCurrentPipe(ds);
		double currentTime = getCurrentTime(pipe);

		// 2.供油罐开始被用到的时刻T2
		Map<Integer, Double> usingTimes = getDeadlineTimeOfAllTanks(currentTime);
		double feedingSpeed = Config.getInstance().getDSs().get(ds - 1).getSpeed();

		if (usingTimes.containsKey(tank)) {
			vol = MathUtil.round(
					MathUtil.divide(feedingSpeed * chargingSpeed * (usingTimes.get(tank) - currentTime - config.RT),
							feedingSpeed + chargingSpeed),
					config.Precision);
		} else {
			vol = Double.MAX_VALUE;
		}

		return vol;
	}

	/**
	 * 在尽量满足赶上蒸馏塔结束炼油时间的同时，如果实在赶不上，就直接运
	 * 
	 * @param ds
	 * @param chargingSpeed
	 * @return
	 */
	public double getRTVolume(int ds, double chargingSpeed) {
		double deadlineTime = getFeedingEndTime()[ds - 1];// 截至时刻
		int pipe = getCurrentPipe(ds);
		double currentTime = getCurrentTime(pipe);
		double volume = MathUtil.round(chargingSpeed * (deadlineTime - currentTime - config.RT), config.Precision);
		return volume;
	}

	/**
	 * 确定最多的转运体积
	 * 
	 * @param fp_vol
	 * @param safe_vol
	 * @param capacity
	 * @return
	 */
	public double getVolume(double fp_vol, double safe_vol, double capacity) {
		// 必须要考虑的约束：容量、进料包、优化体积【都有上限】
		double limit = MathUtil.round(Math.min(fp_vol, Math.min(capacity, safe_vol)), config.Precision);// 消除精度问题
		return limit;
	}

	/**
	 * 确定最多的转运体积
	 * 
	 * @param fp_vol
	 * @param rt_vol
	 * @param safe_vol
	 * @param capacity
	 * @return
	 */
	public double getVolume(double fp_vol, double rt_vol, double safe_vol, double capacity) {
		// 必须要考虑的约束：容量、进料包、优化体积【都有上限】
		double limit = MathUtil.round(Math.min(safe_vol, Math.min(fp_vol, Math.min(capacity, rt_vol))),
				config.Precision);// 消除精度问题
		return limit;
	}

	/**
	 * 过滤掉较差的策略
	 * 
	 * @param safe_vol
	 * @return
	 */
	public boolean filterCondition(double vol, double fp_vol) {
		// 解码时，默认转运记录的体积不得小于一定大小【配置文件指定】
		if (vol < config.VolMin && fp_vol != vol) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 获取未完成炼油计划的蒸馏塔
	 * 
	 * @return
	 */
	public List<Integer> getDSSet() {
		List<Integer> dss = new ArrayList<Integer>();

		for (int i = 0; i < config.getDSs().size(); i++) {
			int ds = i + 1;
			if (config.getDSs().get(ds - 1).getNextOilVolume() > 0) {
				dss.add(ds);
			}
		}
		return dss;
	}

	/**
	 * 根据管道注油结束时间自动计算当前管道
	 * 
	 * @param ds
	 * @return
	 */
	public int getCurrentPipe(int ds) {
		int pipe = -1;
		if (ds == config.HighOilDS) {
			pipe = 1;
		} else {
			pipe = 0;
		}
		return pipe;
	}

	/**
	 * 计算管道注油结束时间当前系统时间
	 * 
	 * @param pipe
	 * @return
	 */
	public double getCurrentTime(int pipe) {
		double[] chargingEndTimes = getChargingEndTime();
		return chargingEndTimes[pipe];
	}

	/**
	 * 获取当前时刻供油罐的状态，是否可用
	 * 
	 * @param currentTime
	 */
	public List<Integer> getTankSet(double currentTime) {
		// 按照开始时间排序
		Operation.sortOperation(operations);

		List<Integer> tankSet = new LinkedList<>();
		TableModel model = getTankStateModel(currentTime);

		int rowCount = model.getRowCount();
		int colCount = model.getColumnCount();

		// 逐个罐地确定状态
		for (int i = 0; i < rowCount; i++) {
			int tank = i + 1;
			String state = model.getValueAt(i, colCount - 1).toString();// 读取你获取行号的某一列的值（也就是字段）

			// 找到所有空闲的供油罐
			if (state.equals("empty")) {
				tankSet.add(tank);
			}
		}

		return tankSet;
	}

	/**
	 * 获取转运速度
	 * 
	 * @param ds
	 * @return
	 */
	public double[] getChargingSpeed(int ds) {
		int pipe = (ds == config.HighOilDS) ? 1 : 0;
		return config.getPipes().get(pipe).getChargingSpeed();
	}

	/**
	 * 获取转运速度
	 * 
	 * @param pipe
	 * @return
	 */
	public double[] getCharingSpeed(int pipe) {
		return config.getPipes().get(pipe).getChargingSpeed();
	}

	/**
	 * 单次解码
	 * 
	 * @return
	 */
	public Fragment getFragment() {
		// 将规则对象加载到工作内存空间【必须要传入三个参数，分别为config/solution/loc】
		FactObject factObject = new FactObject();
		factObject.setConfig(config);
		factObject.setSolution(solution);
		factObject.setLoc(loc);

		AbstractRule rule = new RuleFactory().getRule(ruleName, this);
		Fragment nextFragment = rule.fireAllRule(factObject);
		return nextFragment;
	}

	/**
	 * 生成推荐策略【应对高熔点管道停运导致的回溯】
	 * 
	 * @param fragment
	 */
	public Integer[][] generateRecommendPolicy() {

		int rows = config.getDSs().size();
		int cols = config.getTanks().size() + 1;
		Integer[][] policyMap = new Integer[rows][cols];

		// 1.初始化标记
		for (int i = 0; i < policyMap.length; i++) {
			for (int j = 0; j < policyMap[i].length; j++) {
				policyMap[i][j] = 0;// 标记不可用
			}
		}

		// 2.生成未完成炼油任务的蒸馏塔的策略【解码时需要筛选】
		List<Integer> pipeOneList = getTankSet(getCurrentTime(0));
		List<Integer> pipeTwoList = getTankSet(getCurrentTime(1));
		List<Integer> dss = getDSSet();
		for (int i = 0; i < dss.size(); i++) {
			int ds = dss.get(i);

			if (ds != config.HighOilDS) {
				// 当所有的供油罐都是空罐时，不可停运
				if (pipeOneList.size() < config.getTanks().size()) {
					policyMap[ds - 1][0] = 1;// 标记某一行的若干个元素
				}
				// 低熔点管道
				for (int j = 0; j < pipeOneList.size(); j++) {
					int tank = pipeOneList.get(j);
					policyMap[ds - 1][tank] = 1;
				}
			} else {
				// 高熔点管道【不可停运】
				for (int j = 0; j < pipeTwoList.size(); j++) {
					int tank = pipeTwoList.get(j);
					policyMap[ds - 1][tank] = 1;
				}
			}
		}

		return policyMap;
	}

	/**
	 * 管道选择策略
	 * 
	 * @return
	 */
	public int selectPipe() {
		List<Integer> dss = getDSSet();
		if (dss.contains(config.HighOilDS) && dss.size() == 1) {
			return 1;
		} else if (!dss.contains(config.HighOilDS)) {
			return 0;
		}
		return ArrayHelper.Arraysort(getChargingEndTime())[0];
	}

	/**
	 * 选择新的应对策略
	 * 
	 * @return
	 */
	private boolean existPolicies() {
		Integer[][] policies = policyStack.peek();

		// 判断是否所有的推荐策略都已经尝试过
		int sum = 0;
		for (int i = 0; i < policies.length; i++) {
			for (int j = policies[i].length - 1; j >= 0; j--) {
				sum += policies[i][j];
			}
		}

		if (sum == 0) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * 处理输入的决策指令
	 * 
	 * @return
	 */
	private boolean process() {
		// 最长决策指令序列长度
		int steps = solution.getNumberOfVariables() / 2;

		// 绘出初始的图像
		if (plotEachStep) {
			Operation.plotSchedule2(operations);
		}

		while (loc < steps && !isFinished()) {

			// 策略栈生成策略，决策栈然后将可行的策略入栈【执行之前标记策略已经使用过，切记！！！】
			if (policyStack.size() == fragmentStack.size()) {
				// 生成所有可能的策略，并将策略入栈
				policyStack.push(generateRecommendPolicy());
			} else {
				// 回溯【高熔点管道优先，抢占式调度】
				while (!existPolicies()) {
					last();
					preemptiveScheduling();
				}
			}

			if (policyStack.size() != fragmentStack.size() + 1) {
				// 两个栈正常情况下应该保持大小相差1
				logger.fatal("出错啦：系统栈异常");
				System.exit(1);
			}

			try {
				next();
			} catch (Exception e) {
				if (e.getMessage().equals("高熔点管道不允许停运")) {
					// 标记回溯
					Integer[][] policies = policyStack.peek();
					for (int i = 0; i < policies.length; i++) {
						if (i + 1 != config.HighOilDS) {
							for (int j = 0; j < policies[i].length; j++) {
								policies[i][j] = 0;
							}
						}
					}
				} else if (e.getMessage().equals("供油罐占用冲突")) {
					logger.fatal("不应该再存在供油罐占用冲突错误");
					e.printStackTrace();
				} else {
					logger.fatal("存在其他错误");
					e.printStackTrace();
				}
			}
		}

		return true;
	}

	/**
	 * 抢占式调度策略
	 */
	private void preemptiveScheduling() {
		try {
			// 不再考虑低熔点塔
			Integer[][] policyMap = policyStack.peek();
			for (int i = 0; i < policyMap.length; i++) {
				if (i + 1 != config.HighOilDS) {
					for (int j = 0; j < policyMap[i].length; j++) {
						policyMap[i][j] = 0;
					}
				}
			}
		} catch (Exception e) {
			logger.fatal("严重错误");
			System.exit(1);
		}
	}

	/**
	 * 进行下一步决策
	 * 
	 * @param fragment
	 * @throws Exception
	 */
	private void next() throws Exception {
		try {
			Fragment nextFragment = getFragment();
			if (nextFragment.getTank() != 0 && nextFragment.getVolume() == 0) {
				logger.fatal("解码错误：非停运指令但是解码体积为0");
				System.exit(1);
			}

			doOperation(nextFragment);

			// 绘出执行完下一步操作后的调度图
			if (plotEachStep) {
				Operation.plotSchedule2(operations);
			}
		} catch (Exception e) {
			throw new Exception(e.getMessage());// 消息继续向上层传递
		}
	}

	/**
	 * 向前回退一步
	 * 
	 * @return
	 */
	private void last() {
		// 回退一步【loc==0时，无法后退】
		if (loc > 0) {
			loc--;
			policyStack.pop();
			fragmentStack.pop();
			Operation operation = null;
			do {
				operation = operationStack.pop();
				operations.remove(operation);// 移除决策
			} while (operation.getType() == OperationType.Feeding);
			removeConfig();// 移除栈顶的配置，并将config指向新的栈顶配置

			// 绘出返回后的调度计划甘特图
			if (plotEachStep) {
				Operation.plotSchedule2(operations);
			}
		}
	}

	/**
	 * 判断是否调度结束
	 * 
	 * @return
	 */
	private boolean isFinished() {
		int count = 0;
		for (DSObject dsObject : config.getDSs()) {
			if (dsObject.getNextOilVolume() < 0) {
				count++;
			}
		}
		if (count < config.getDSs().size()) {
			return false;
		} else {
			return true;
		}
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
	 * 获取管道的停运时间，假设高熔点管道和低熔点管道都可以停运。 tank=0代表这是一个停运操作，根据ds判断是高熔点管道停运还是低熔点管道停运
	 * 
	 * @return
	 */
	private double[] getChargingEndTime() {
		// 管道的个数
		int numOfPipes = config.getPipes().size();
		double[] chargingEndTime = new double[numOfPipes];

		// 求各个蒸馏塔的注油结束时间
		for (Operation operation : operations) {
			if (operation.getType() == OperationType.Charging || operation.getType() == OperationType.Stop) {
				// 高熔点管道转运
				if (operation.getDs() != config.HighOilDS) {
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
	public double[] getFeedingEndTime() {

		double[] feedEndTime = new double[config.getDSs().size()];

		// 求各个蒸馏塔的炼油结束时间
		for (int i = 0; i < config.getDSs().size(); i++) {
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
		for (int i = 0; i < config.getDSs().size(); i++) {
			// 完成炼油计划的蒸馏塔不在考虑范围
			if (config.getDSs().get(i).getNextOilVolume() == -1) {
				feedEndTime[i] = Double.MAX_VALUE;
				continue;
			}
		}
		return feedEndTime;
	}

	/**
	 * 获取供油罐的最早释放时间，为停运提供参考
	 * 
	 * @return
	 */
	public double getEarliestAvailableTime(double currentTime) {
		Map<Integer, Double> availableTimes = new HashMap<Integer, Double>();

		// 逐个罐地确定状态
		for (int i = 0; i < config.getTanks().size(); i++) {
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
	private DefaultTableModel getTankStateModel(double currentTime) {
		Operation.sortOperation(operations);

		DefaultTableModel model = new DefaultTableModel();
		Object[] columnNames = { "供油罐", "容量", "原油种类", "原油体积", "开始时间", "结束时间", "状态" };
		model.setColumnIdentifiers(columnNames);

		for (int i = 0; i < config.getTanks().size(); i++) {
			int tank = i + 1;
			Object[] data = { tank, config.getTanks().get(tank - 1).getCapacity(), 0, 0, 0, 0, "empty" };

			// 记录前一决策和后一决策
			Operation lastOperation = null;
			Operation nextOperation = null;
			for (Operation operation : operations) {
				if (operation.getTank() == tank) {
					if (operation.getStart() > currentTime) {
						nextOperation = operation;
						break;
					} else {
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
							data[3] = MathUtil.round(
									lastOperation.getVol()
											- (currentTime - lastOperation.getStart()) * lastOperation.getSpeed(),
									config.NumOfDivide);// 计算剩余体积
							data[4] = lastOperation.getStart();
							data[5] = lastOperation.getEnd();
							data[6] = "hoting";
						} else if (lastOperation.getType() == OperationType.Charging) {
							// 注油状态
							data[2] = lastOperation.getOil();
							data[3] = MathUtil.round(
									(currentTime - lastOperation.getStart()) * lastOperation.getSpeed(),
									config.NumOfDivide);// 计算注油体积
							data[4] = lastOperation.getStart();
							data[5] = lastOperation.getEnd();
							data[6] = "charging";
						} else if (lastOperation.getType() == OperationType.Feeding) {
							// 供油状态
							data[2] = lastOperation.getOil();
							data[3] = MathUtil.round(
									lastOperation.getVol()
											- (currentTime - lastOperation.getStart()) * lastOperation.getSpeed(),
									config.NumOfDivide);// 计算剩余体积
							data[4] = lastOperation.getStart();
							data[5] = lastOperation.getEnd();
							data[6] = "feeding";
						}
					} else {
						if (lastOperation.getType() == OperationType.Charging) {

							// 等待状态
							data[2] = lastOperation.getOil();
							data[3] = MathUtil.round(lastOperation.getVol(), config.NumOfDivide);
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
					data[3] = MathUtil.round(nextOperation.getVol(), config.NumOfDivide);
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
	 * 初始化仿真
	 */
	private void initSimulation(DoubleSolution solution) {
		this.solution = solution;

		// 初始配置入栈
		config.loadConfig();
		Config config_Clone = CloneUtils.clone(config);
		configStack.push(config_Clone);

		// 炼油结束时间
		double[] feedEndTime = new double[config.getDSs().size()];

		// 执行初始指派(低熔点塔)
		for (int i = 0; i < config.getTanks().size(); i++) {
			TankObject tankObject = config.getTanks().get(i);
			double vol = MathUtil.round(tankObject.getVolume(), config.Precision);
			int tank = i + 1;

			if (vol > 0) {
				if (Config.HotTank != tank) {

					int oiltype = config.getTanks().get(i).getOiltype();
					int ds = tankObject.getAssign();
					double speed = config.getDSs().get(ds - 1).getSpeed();
					double feedTime = MathUtil.round(vol / speed, config.Precision);

					Operation feeding = new Operation(OperationType.Feeding, tank, ds,
							MathUtil.round(feedEndTime[ds - 1], config.Precision),
							MathUtil.round(feedEndTime[ds - 1] + feedTime, config.Precision), vol, oiltype, speed, 0);// 初始库存就在炼油厂内，用0表示
					operations.add(feeding);

					// 更新炼油结束时间
					feedEndTime[ds - 1] += feedTime;
				}
			}
		}
		// 执行初始指派(高熔点塔)
		int tank = Config.HotTank;
		int ds = config.HighOilDS;
		TankObject tankObject = config.getTanks().get(tank - 1);
		double vol = MathUtil.round(tankObject.getVolume(), config.Precision);
		double vPipe = config.getPipes().get(1).getVol();
		int oiltype = config.getTanks().get(tank - 1).getOiltype();
		double feedingSpeed = config.getDSs().get(config.HighOilDS - 1).getSpeed();
		double hotingTime = MathUtil.divide(vol, Config.HotingSpeed);
		double chargingTime = MathUtil.divide(vPipe, Config.HotingSpeed);
		double feedingTime = MathUtil.divide(vPipe, feedingSpeed);

		Operation hoting = new Operation(OperationType.Hoting, tank, config.HighOilDS, 0,
				MathUtil.round(hotingTime, config.Precision), vol, oiltype, Config.HotingSpeed, 0);
		Operation charging = new Operation(OperationType.Charging, tank, config.HighOilDS,
				MathUtil.round(hotingTime, config.Precision),
				MathUtil.round(hotingTime + chargingTime, config.Precision), vPipe, oiltype, Config.HotingSpeed, 0);
		// 加热管道后的低熔点原油会重新供给蒸馏塔炼油
		ds = config.getTanks().get(tank - 1).getAssign();
		feedingSpeed = Config.getInstance().getDSs().get(ds - 1).getSpeed();
		feedingTime = MathUtil.divide(vPipe, feedingSpeed);
		Operation feeding = new Operation(OperationType.Feeding, tank, ds,
				MathUtil.round(feedEndTime[ds - 1], config.Precision),
				MathUtil.round(MathUtil.add(feedEndTime[ds - 1], feedingTime), config.Precision), vPipe, oiltype,
				feedingSpeed, 0);

		operations.add(hoting);
		operations.add(charging);
		operations.add(feeding);
	}

	/**
	 * 记录
	 * 
	 * @param col
	 * @param row
	 */
	private void record(int col, int row) {
		try {
			policyStack.peek()[row - 1][col] = 0;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 执行决策
	 * 
	 * @param fragment
	 * @throws Exception
	 */
	private void doOperation(Fragment fragment) throws Exception {

		// 获取下一步操作
		int tank = fragment.getTank();
		int ds = fragment.getDs();
		double speed = fragment.getSpeed();
		double vol = fragment.getVolume();
		// 标记已经执行过
		record(tank, ds);

		double[] feedEndTimes = getFeedingEndTime();

		// 判断需要转运的管道和转运结束时间
		int pipe = getCurrentPipe(ds);
		double currentTime = getCurrentTime(pipe);

		// 复制一份未变化的配置【更新并入栈，原来的配置信息不能变化】
		Config config_Clone = newConfig();

		// 判断是否停运
		if (tank == 0) {

			// 3.高熔点管道不允许停运
			if (ds == config.HighOilDS) {
				throw new Exception("高熔点管道不允许停运");// 【注意：本注释内容不可变】
			}

			double availableTime = getEarliestAvailableTime(currentTime);
			Operation stoping = new Operation(OperationType.Stop, tank, ds,
					MathUtil.round(currentTime, config.Precision), MathUtil.round(availableTime, config.Precision), vol,
					0, 0, 0);

			// 更新进料包和炼油结束时间
			operations.add(stoping);
			operationStack.push(stoping);
		} else {

			// 确定原油类型
			int oiltype = config.getDSs().get(ds - 1).getNextOilType();
			// 确定下一原油来自于哪一个港口
			int site = config.getDSs().get(ds - 1).getWhereNextOilFrom();
			// 确定注油时间
			double chargingTime = MathUtil.divide(vol, speed);

			// 确定炼油速率和炼油时间
			double feedingSpeed = config.getDSs().get(ds - 1).getSpeed();
			double feedingTime = MathUtil.divide(vol, feedingSpeed);

			// 确定开始炼油时间
			double feedingStartTime = Math.max(feedEndTimes[ds - 1], currentTime + chargingTime + config.RT);

			// 2.判断本次注油是否和别的操作有冲突
			Map<Integer, Double> usingTimes = getDeadlineTimeOfAllTanks(currentTime);
			double feedEndTime = MathUtil.round(MathUtil.add(feedingStartTime, feedingTime), config.Precision);
			if (usingTimes.containsKey(tank) && usingTimes.get(tank) < feedEndTime) {
				Operation.plotSchedule2(operations);
				throw new Exception("供油罐占用冲突");// 【不允许】
			}

			// 确定操作
			Operation charging = new Operation(OperationType.Charging, tank, ds,
					MathUtil.round(currentTime, config.Precision), MathUtil.add(currentTime, chargingTime), vol,
					oiltype, speed, site);
			Operation feeding = new Operation(OperationType.Feeding, tank, ds, feedingStartTime, feedEndTime, vol,
					oiltype, feedingSpeed, site);

			// 更新进料包和炼油结束时间
			operations.add(charging);
			operations.add(feeding);
			operationStack.push(charging);// 注意顺序，先charging后feeding
			operationStack.push(feeding);
			config_Clone.getDSs().get(ds - 1).updateOilVolume(vol);// 更新新的配置
		}
		fragmentStack.push(fragment);// 决策入栈
		addConfig(config_Clone);// 将新的配置压入配置栈中，并更新config的指向

		loc++;
	}
}