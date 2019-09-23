package com.sim.operation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.canvas.gante.ChartFrame;
import com.sim.common.CSVHelper;
import com.sim.common.MathHelper;
import com.sim.common.MatlabPlotHelper;
import com.sim.common.ValuesPair;
import com.sim.experiment.Config;

/**
 * 操作
 * 
 * @author Administrator
 *
 */
public class Operation {
	private OperationType type = OperationType.Charging;
	private static ChartFrame chartFrame = null;

	private static Logger logger = LogManager.getLogger(Operation.class.getName());

	private int tank = 0;
	private int ds = 0;
	private double start = 0;
	private double end = 0;
	private double vol = 0;
	private int oil = 0;
	private double speed = 0;
	private int site = 0;

	private boolean flag = false;

	public Operation() {
		super();
	}

	/**
	 * 操作
	 * 
	 * @param type
	 * @param tank
	 * @param ds
	 * @param start
	 * @param end
	 * @param vol
	 * @param oil
	 * @param speed
	 */
	public Operation(OperationType type, int tank, int ds, double start, double end, double vol, int oil, double speed,
			int site) {
		super();
		this.type = type;
		this.tank = tank;
		this.ds = ds;
		this.start = start;
		this.end = end;
		this.vol = vol;
		this.oil = oil;
		this.speed = speed;
		this.site = site;// 所在的港口
	}

	public int getSite() {
		return site;
	}

	public void setSite(int site) {
		this.site = site;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public int getTank() {
		return tank;
	}

	public void setTank(int tank) {
		this.tank = tank;
	}

	public int getDs() {
		return ds;
	}

	public void setDs(int ds) {
		this.ds = ds;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	public int getOil() {
		return oil;
	}

	public void setOil(int oil) {
		this.oil = oil;
	}

	public boolean isFlag() {
		return flag;
	}

	public void setFlag(boolean flag) {
		this.flag = flag;
	}

	public OperationType getType() {
		return type;
	}

	public void setType(OperationType type) {
		this.type = type;
	}

	public Double getStart() {
		return start;
	}

	public void setStart(double start) {
		this.start = start;
	}

	public double getEnd() {
		return end;
	}

	public void setEnd(double end) {
		this.end = end;
	}

	public double getVol() {
		return vol;
	}

	public void setVol(double vol) {
		this.vol = vol;
	}

	@Override
	public String toString() {
		return ds + " " + tank + " " + start + " " + end + " " + oil + " " + vol + " " + speed;
	}

	/**
	 * 将operation按照开始时间排序
	 * 
	 * @param operations
	 */
	public static void sortOperation(List<Operation> operations) {

		if (operations != null) {
			Collections.sort((List<Operation>) operations, new Comparator<Operation>() {

				@Override
				public int compare(Operation o1, Operation o2) {
					return o1.getStart().compareTo(o2.getStart());
				}

			});
		}
	}

	/**
	 * 输出operation列表
	 * 
	 * @param operations
	 */
	public static void printOperation(List<Operation> operations) {
		if (operations != null) {
			Collections.sort((List<Operation>) operations, new Comparator<Operation>() {

				@Override
				public int compare(Operation o1, Operation o2) {
					return o1.getStart().compareTo(o2.getStart());
				}

			});
		}

		for (Operation operation : operations) {
			if (operation.getType() == OperationType.Charging && operation.getDs() != Config.getInstance().HighOilDS) {
				System.out.println("[pipe1]" + "Charging: " + operation);
			}
			if (operation.getType() == OperationType.Stop && operation.getDs() != Config.getInstance().HighOilDS) {
				System.out.println("[pipe1]" + "Stop: " + operation);
			}
		}
		for (Operation operation : operations) {
			if (operation.getType() == OperationType.Hoting) {
				System.out.println("[pipe2]" + "Hoting: " + operation);
			}
			if (operation.getType() == OperationType.Charging && operation.getDs() == Config.getInstance().HighOilDS) {
				System.out.println("[pipe2]" + "Charging: " + operation);
			}
		}
		for (int i = 0; i < 4; i++) {
			for (Operation operation : operations) {
				if (operation.getType() == OperationType.Feeding && operation.getDs() == (i + 1)) {
					System.out.println("[ds" + operation.getDs() + "]" + "Feeding: " + operation);
				}
			}
		}
	}

	/**
	 * 检查operations是否合理 输入的operation已经按照开始时间排序
	 * 
	 * @param operations
	 * @return
	 */
	public static boolean check(List<Operation> operations) {

		for (int i = 0; i < Config.getInstance().getTanks().size(); i++) {
			List<Operation> opers = new LinkedList<>();
			double lastEnd = 0.0;
			OperationType lastType = null;

			int tank = i + 1;

			for (Operation operation : operations) {
				if (operation.getTank() == tank) {
					opers.add(operation);
				}
			}

			// 排序【否则，若出现中间插入的决策可能导致出错】
			sortOperation(opers);

			for (Operation operation : opers) {

				// 各个operation不重叠，同时charging和feeding交叉进行
				// 这里不考虑驻留时间约束，因为该仿真一定满足驻留时间约束
				if (operation.getStart() >= lastEnd && lastType != operation.getType()) {
					lastEnd = operation.getEnd();
					lastType = operation.getType();
				} else {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 计算用罐个数
	 * 
	 * @param operations
	 * @return
	 */
	public static int getNumberOfTankUsed(List<Operation> operations) {

		List<Integer> tanks = new LinkedList<>();

		for (Operation operation : operations) {

			int tank = operation.getTank();
			if (tank != 0 && !tanks.contains(tank)) {
				tanks.add(tank);
			}
		}

		return tanks.size();
	}

	/**
	 * 计算切换次数
	 * 
	 * @param operations
	 * @return
	 */
	public static int getNumberOfChange(List<Operation> operations) {

		List<Operation> opers = new LinkedList<>();

		for (Operation operation : operations) {
			if (operation.getType() == OperationType.Feeding) {
				opers.add(operation);
			}
		}

		return opers.size();
	}

	/**
	 * 计算罐底混合成本
	 * 
	 * @param operations
	 * @return
	 */
	public static double getTankMixingCost(List<Operation> operations) {

		double result = 0.0;

		// 成本矩阵
		double[][] costMatrix = MathHelper.stringToDouble(new CSVHelper().readCSV("data/TankMixingCost.csv", true));

		// 有多少个供油罐就有多少个原油类型列表
		List<List<Integer>> oilTypeLists = new LinkedList<>();
		for (int i = 0; i < Config.getInstance().getTanks().size(); i++) {
			List<Integer> oilTypeList = new LinkedList<>();
			oilTypeLists.add(oilTypeList);
		}

		for (Operation operation : operations) {
			if (operation.getType() == OperationType.Feeding) {

				// 各个供油罐相互独立，因而需要分开计算
				int tank = operation.getTank();
				oilTypeLists.get(tank - 1).add(operation.getOil());
			}
		}

		// 逐个供油罐计算混合成本后累加
		for (int i = 0; i < Config.getInstance().getTanks().size(); i++) {
			result += getMixingCost(oilTypeLists.get(i), costMatrix);
		}

		return result;
	}

	/**
	 * 计算管道混合成本
	 * 
	 * @param operations
	 * @return
	 */
	public static double getPipeMixingCost(List<Operation> operations) {

		double result = 0.0;

		// 成本矩阵
		double[][] costMatrix = MathHelper.stringToDouble(new CSVHelper().readCSV("data/PipeMixingCost.csv", true));

		// 多少条管道就有多少个原油类型列表
		List<List<Integer>> oilTypeLists = new LinkedList<>();
		for (int i = 0; i < Config.getInstance().getPipes().size(); i++) {
			List<Integer> oilTypeList = new LinkedList<>();
			oilTypeLists.add(oilTypeList);
		}

		for (Operation operation : operations) {
			if (operation.getType() == OperationType.Charging) {

				// 来自于站点1代表是通过低熔点管道转运而来，来自站点2代表是通过高熔点管道转运而来
				// 两条管道相互独立，因而需要分开计算
				if (operation.getSite() == 1) {
					oilTypeLists.get(0).add(operation.getOil());
				} else if (operation.getSite() == 2) {
					oilTypeLists.get(1).add(operation.getOil());
				}
			}
		}

		// 逐个管道计算混合成本后累加
		for (int i = 0; i < Config.getInstance().getPipes().size(); i++) {
			result += getMixingCost(oilTypeLists.get(i), costMatrix);
		}

		return result;
	}

	/**
	 * 计算混合成本
	 * 
	 * @param oilTypeList
	 * @param costMatrix
	 * @return
	 */
	private static double getMixingCost(List<Integer> oilTypeList, double[][] costMatrix) {
		double result = 0.0;

		// 若只有一种原油类型，或者列表为空，则混合成本为0
		if (oilTypeList.size() > 1) {
			int lastOilType = oilTypeList.get(0);
			List<ValuesPair> pairs = new LinkedList<>();

			for (int i = 1; i < oilTypeList.size(); i++) {

				int oilType = oilTypeList.get(i);

				ValuesPair pair = new ValuesPair();
				pair.put(lastOilType, oilType);
				pairs.add(pair);

				lastOilType = oilType;
			}

			for (int i = 0; i < pairs.size(); i++) {
				int last = pairs.get(i).getLast();
				int next = pairs.get(i).getNext();

				result += costMatrix[last][next];
			}
		}

		return result;
	}

	/**
	 * 计算能耗成本
	 * 
	 * @param operations
	 * @return
	 */
	public static double getEnergyCost(List<Operation> operations) {

		double result = 0.0;

		// 各个管道不同转运速度对应的成本不同
		List<Map<Double, Double>> maps = new LinkedList<Map<Double, Double>>();

		for (int i = 0; i < Config.getInstance().getPipes().size(); i++) {
			// 建立转运速度与转运成本之间的映射关系
			Map<Double, Double> map = new HashMap<>();

			double[] speed = Config.getInstance().getPipes().get(i).getChargingSpeed();
			double[] cost = Config.getInstance().getPipes().get(i).getCost();
			for (int j = 0; j < speed.length; j++) {
				map.put(speed[j], cost[j]);
			}

			maps.add(map);
		}

		// 遍历转运决策序列计算成本【暂不考虑加热管道那部分的成本，认为其是固定成本】
		for (Operation operation : operations) {
			if (operation.getType() == OperationType.Charging) {

				double cost = 0.0;
				// 来自于站点1代表是通过低熔点管道转运而来，来自站点2代表是通过高熔点管道转运而来
				// 两条管道的转运成本不同，因而需要分开计算
				if (operation.getSite() == 1) {
					// 成本=单位时间成本*总时间
					cost = maps.get(0).get(operation.getSpeed()) * (operation.getEnd() - operation.getStart());
				} else if (operation.getSite() == 2) {
					cost = maps.get(1).get(operation.getSpeed()) * (operation.getEnd() - operation.getStart());
				}
				result += cost;
			}
		}

		return MathHelper.precision(result, Config.getInstance().Precision);
	}

	/**
	 * 计划供油结束时间（低熔点塔）
	 * 
	 * @param operations
	 * @return
	 */
	private static double getPlanFeedingFinishTime(List<Operation> operations) {
		double result = 0.0;

		for (Operation operation : operations) {
			if (operation.getType() == OperationType.Feeding) {
				result += operation.getEnd() - operation.getStart();
			}
		}

		return result;
	}

	/**
	 * 实际供油结束时间（低熔点塔）
	 * 
	 * @param operations
	 * @return
	 */
	private static double getFeedingFinishTime(List<Operation> operations) {
		double[] max = new double[4];

		for (Operation operation : operations) {
			if (operation.getType() == OperationType.Feeding) {
				int ds = operation.getDs();
				if (operation.getEnd() > max[ds - 1]) {
					max[ds - 1] = operation.getEnd();
				}
			}
		}

		return MathHelper.sum(max);
	}

	/**
	 * 计算硬约束成本（蒸馏塔停止炼油成本）
	 * 
	 * @param operations
	 * @return
	 */
	public static double getHardCost(List<Operation> operations) {
		return MathHelper.precision(getFeedingFinishTime(operations) - getPlanFeedingFinishTime(operations), 2);
	}

	/**
	 * 绘制调度的甘特图【matlab】 格式：蒸馏塔/管道 油罐 开始时间 结束时间 原油类型
	 * 
	 * @param operations
	 */
	public static void plotSchedule(List<Operation> operations) {

		try {
			int size = operations.size();
			double[][] data = new double[size][5];
			String labels = "DS1 DS2 DS3 DS4 Pipe1 Pipe2";

			for (int i = 0; i < operations.size(); i++) {
				Operation operation = operations.get(i);

				if (operation.getType() == OperationType.Charging || operation.getType() == OperationType.Stop) {
					data[i][0] = operation.getDs() == Config.getInstance().HighOilDS ? 6.0 : 5.0;
				} else if (operation.getType() == OperationType.Hoting) {
					data[i][0] = 6.0;
				} else {
					data[i][0] = (double) operation.getDs();
				}

				data[i][1] = (double) operation.getTank();
				data[i][2] = (double) operation.getStart();
				data[i][3] = (double) operation.getEnd();
				data[i][4] = (double) operation.getOil();

				logger.info(data[i][0] + "," + data[i][1] + "," + data[i][2] + "," + data[i][3] + "," + data[i][4]);
			}

			MatlabPlotHelper.getInstance().gante(labels, data);
		} catch (Exception e) {
			logger.fatal("Matlab plot module error.");
			System.exit(1);
		}

		// 为了防止刷新过快，让线程休眠一段时间
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * 绘制调度的甘特图【canvas】 格式：蒸馏塔/管道 油罐 开始时间 结束时间 原油类型
	 * 
	 * @param operations
	 */
	public static void plotSchedule2(List<Operation> operations) {

		try {
			int size = operations.size();
			double[][] data = new double[size][5];

			for (int i = 0; i < operations.size(); i++) {
				Operation operation = operations.get(i);

				if (operation.getType() == OperationType.Charging || operation.getType() == OperationType.Stop) {
					data[i][0] = operation.getDs() == Config.getInstance().HighOilDS ? 6.0 : 5.0;
				} else if (operation.getType() == OperationType.Hoting) {
					data[i][0] = 6.0;
				} else {
					data[i][0] = (double) operation.getDs();
				}

				data[i][1] = (double) operation.getTank();
				data[i][2] = (double) operation.getStart();
				data[i][3] = (double) operation.getEnd();
				data[i][4] = (double) operation.getOil();

				logger.info(data[i][0] + "," + data[i][1] + "," + data[i][2] + "," + data[i][3] + "," + data[i][4]);
			}

			if (chartFrame == null) {
				chartFrame = new ChartFrame();
			} else if (!chartFrame.frame.isVisible()) {
				chartFrame.frame.setVisible(true);
			}
			chartFrame.updateCanvas(data);
		} catch (Exception e) {
			logger.fatal("Java Canvas plot module error.");
			e.printStackTrace();
			System.exit(1);
		}

		// 为了防止刷新过快，让线程休眠一段时间
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * 创建桑葚图的数据：蒸馏塔/管道 油罐 开始时间 结束时间 原油类型
	 * 
	 * @param operations
	 */
	public static void creatSangSen(List<Operation> operations) {
		sortOperation(operations);
		List<String> colors = new ArrayList<String>();
		colors.add("#4f81bd");
		colors.add("#E489C4");
		colors.add("#77D0B4");
		colors.add("#FC8F62");
		colors.add("#FB5C4E");
		colors.add("#3DBE5D");
		colors.add("#8D9FCC");
		colors.add("#F00029");
		colors.add("#FFFF33");
		colors.add("#c0504d");
		colors.add("#9bbb59");
		colors.add("#8064a2");

		StringBuilder sbBuilder = new StringBuilder("[");

		for (int i = 0; i < operations.size(); i++) {
			Operation operation = operations.get(i);
			String colorCode = colors.get(operation.getOil());
			if (operation.getType() == OperationType.Charging) {
				int pipe = operation.getDs() == Config.getInstance().HighOilDS ? 2 : 1;
				sbBuilder.append("[" + "'#" + operation.getOil() + "'," + "'Pipe" + pipe + "'," + operation.getVol()
						+ ",'" + colorCode + "'],");
				sbBuilder.append("[" + "'Pipe" + pipe + "'," + "'TK" + operation.getTank() + "'," + operation.getVol()
						+ ",'" + colorCode + "'],");
			} else if (operation.getType() == OperationType.Feeding) {
				sbBuilder.append("[" + "'TK" + operation.getTank() + "'," + "'DS" + operation.getDs() + "',"
						+ operation.getVol() + ",'" + colorCode + "'],");
			}
		}
		sbBuilder.append("]");
		System.out.println(sbBuilder.toString());
	}
}
