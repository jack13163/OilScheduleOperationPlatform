package com.sim.common;

public class CodeHelper {

	private int start;
	private int end;

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public CodeHelper(int start, int end) {
		super();
		this.start = start;
		this.end = end;
	}

	/**
	 * 将一个闭区间[0,1]之间实数映射到整数空间
	 * 
	 * @param num
	 * @param start
	 * @param end
	 * @return
	 * @throws Exception
	 */
	public int getInteger(double num) throws Exception {
		if (num < 0 || num > 1) {
			throw new Exception("请输入[0,1]之间的随机数");
		}
		if (num == 1) {
			num = 0;
		}
		return (int) (Math.floor(num * (end - start)) + start);
	}

	/**
	 * 将一个1到48的整数映射到[0,1]的实数空间
	 * 
	 * @param tank
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public double getReal(int num) throws Exception {
		if (num < start || num > end) {
			throw new Exception("请输入[" + start + "," + end + "]之间的随机数");
		}
		double gap = 1.0 / (end - start + 1);
		return gap * (num - 0.5);
	}

	/**
	 * 实数映射为行列坐标
	 * 
	 * @param num
	 * @param rows
	 * @param cols
	 * @return
	 * @throws Exception
	 */
	private static int mapToInteger(double num, int rows, int cols) throws Exception {
		if (num < 0 || num > 1) {
			throw new Exception("请输入[0,1]之间的随机数");
		}
		if (num == 1) {
			num = 0;
		}
		int total = rows * cols;
		return (int) (Math.floor(num * total) + 1);
	}

	/**
	 * 获取实数的行坐标【蒸馏塔】
	 * 
	 * @param num
	 * @param rows
	 * @param cols
	 * @return
	 * @throws Exception
	 */
	public static int getRow(double num, int rows, int cols) throws Exception {
		if (num < 0 || num > 1) {
			throw new Exception("请输入[0,1]之间的随机数");
		}
		if (num == 1) {
			num = 0;
		}
		int mid = mapToInteger(num, rows, cols);
		return (mid - 1) / cols + 1;
	}

	/**
	 * 获取实数的列坐标【供油罐】
	 * 
	 * @param num
	 * @param rows
	 * @param cols
	 * @return
	 * @throws Exception
	 */
	public static int getCol(double num, int rows, int cols) throws Exception {
		if (num < 0 || num > 1) {
			throw new Exception("请输入[0,1]之间的随机数");
		}
		if (num == 1) {
			num = 0;
		}
		int mid = mapToInteger(num, rows, cols);
		return (mid - 1) % cols + 1;
	}

	/**
	 * 行列坐标映射为实数
	 * 
	 * @param row
	 * @param col
	 * @param rows
	 * @param cols
	 * @return
	 * @throws Exception
	 */
	public static double mapToReal(int row, int col, int rows, int cols) throws Exception {
		int num = (row - 1) * cols + col;
		int total = rows * cols;
		if (num < 1 || num > total) {
			throw new Exception("请输入[" + 1 + "," + total + "]之间的随机数");
		}
		return 1.0 / total * (num - 0.5);
	}
}
