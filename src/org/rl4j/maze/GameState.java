package org.rl4j.maze;

import org.deeplearning4j.rl4j.space.Encodable;

public class GameState implements Encodable{
	
	private double x;
	private double y;

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public GameState(double x, double y){
		this.x = x;
		this.y = y;
	}
	
	@Override
	public double[] toArray() {
		double[] ret = new double[2];
		ret[0] = x;
		ret[1] = y;
		return ret;
	}
}
