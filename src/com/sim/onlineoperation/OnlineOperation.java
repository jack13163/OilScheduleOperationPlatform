package com.sim.onlineoperation;

import java.util.List;
import java.util.Stack;

import javax.swing.table.TableModel;

import com.models.Fragment;
import com.sim.operation.Operation;

public class OnlineOperation {

	OnlineSimulationController controller;// 控制器
	private Stack<Fragment> fragments;// 基因片段

	public Stack<Fragment> getFragments() {
		return fragments;
	}

	public void setFragments(Stack<Fragment> fragments) {
		this.fragments = fragments;
	}

	public OnlineOperation() {
		fragments = new Stack<>();
		controller = new OnlineSimulationController();
	}

	public OnlineOperation(Stack<Fragment> fragments) {
		super();
		this.fragments = fragments;
		controller = new OnlineSimulationController();
	}

	/**
	 * 开始
	 */
	public void start() {
		// 清空决策队列
		controller.getOperations().clear();
		controller.initSimulation();
	}

	/**
	 * 重置
	 */
	public void reset() {
		// 清空决策队列
		fragments.clear();
	}

	/**
	 * 此处为下一步决策做一下数据的校验工作
	 * 
	 * @param fragment
	 * @throws Exception
	 */
	public boolean next(Fragment fragment) throws Exception {
		try {
			boolean flag = controller.doOperation(fragment);

			if (flag) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}

	/**
	 * 获取供油罐的状态
	 * 
	 * @return
	 */
	public TableModel getTankState(double currentTime) {
		return controller.getTankStateModel(currentTime);
	}

	/**
	 * 获取各个蒸馏塔未完成的炼油计划
	 * 
	 * @return
	 */
	public TableModel getFp() {
		return controller.getFpModel();
	}

	/**
	 * 获取成本
	 * 
	 * @return
	 */
	public TableModel getCost() {
		return controller.getCostModel();
	}

	/**
	 * 获取所有的决策
	 * 
	 * @return
	 */
	public List<Operation> getOperations() {
		return controller.getOperations();
	}

	/**
	 * 获取转运结束时间
	 * 
	 * @return
	 */
	public double getChargingEndTime(boolean isHighOil) {
		if (isHighOil) {
			return controller.getChargingEndTime()[1];
		} else {
			return controller.getChargingEndTime()[0];
		}
	}

	/**
	 * 向前回退一步
	 * 
	 * @return
	 * @throws Exception
	 */
	public Fragment last() {
		try {
			start();

			for (Fragment fragment : fragments) {
				next(fragment);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (fragments.isEmpty()) {
			return null;
		} else {
			return fragments.peek();
		}
	}
}
