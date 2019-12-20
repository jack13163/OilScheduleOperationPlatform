package org.rl4j.maze;


import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.deeplearning4j.gym.StepReply;
import org.deeplearning4j.rl4j.mdp.MDP;
import org.deeplearning4j.rl4j.space.ArrayObservationSpace;
import org.deeplearning4j.rl4j.space.DiscreteSpace;
import org.deeplearning4j.rl4j.space.ObservationSpace;
import org.rl4j.maze.util.Point;

import java.util.LinkedList;
import java.util.List;


@Slf4j
@Getter
public class GameMDP implements MDP<GameState,Integer, DiscreteSpace>{
	
	private double reward;
	private int step;
	private GameState curState;
	private Point[] traps;
	private DiscreteSpace actionSpace = new DiscreteSpace(4);
	private ObservationSpace<GameState> observationSpace = new ArrayObservationSpace<>(new int[]{2});
	private List<Point> trace = new LinkedList<>();

	public GameMDP(){
		this(new Point[]{new Point(3,3),new Point(7,3)});
	}
	
	public GameMDP(Point[] traps){
		this.traps = traps;
	}
	
	
	@Override
	public void close() {
		reward = 0.0;
	}

	@Override
	public DiscreteSpace getActionSpace() {
		return actionSpace;
	}

	@Override
	public ObservationSpace<GameState> getObservationSpace() {
		return observationSpace;
	}

	private boolean isTraped(){
		Point curPoint = new Point((int)curState.getX(), (int)curState.getY());
		for( Point trap : traps ){
			if( trap.equals(curPoint) ){
				return true;	//掉入陷阱
			}
		}
		return false;
	}
	
	
	private boolean isOutOfBox(){
		if( curState.getX() < 0.0 || curState.getX() > 9.0
				|| curState.getY() < 0.0 || curState.getY() > 9.0 ){
			return true;
		}
		return false;
	}
	
	private boolean isSuccess(){
		if( curState.getX() == 9.0 && curState.getY() == 9.0 ){
			return true;
		}
		return false;
	}
	
	@Override
	public boolean isDone() {
		boolean bIsTraped = isTraped();
//		boolean bIsOutOfBox = isOutOfBox();
		boolean bIsSuccess = isSuccess();
		boolean bIsDone = bIsSuccess || bIsTraped;
		if( bIsDone ){
			log.info("Game Over, Total Step:{}, Trap State:{}, Success State:{}" , step, bIsTraped, bIsSuccess);
			for( Point t : trace ){
				log.info(t.toString() + "->");
			}
		}
		return bIsDone;
	}

	@Override
	public MDP<GameState, Integer, DiscreteSpace> newInstance() {
		MDP<GameState, Integer, DiscreteSpace> ret = new GameMDP();
		return ret;
	}

	@Override
	public GameState reset() {
		double randX = (int)(Math.random()*10);
		double randY = (int)(Math.random()*10);
		log.info("reset X:{},Y:{}", randX, randY);
		this.curState = new GameState(randX,randY);
		this.reward = 0.0;
		this.step = 0;
		this.trace.clear();
		return curState;
	}

	@Override
	public StepReply<GameState> step(Integer action) {
		double curX = curState.getX();
		double curY = curState.getY();
		switch(action){
			case 0:curState.setX(curX - 1.0);break;//左
			case 1:curState.setX(curX + 1.0);break;//右
			case 2:curState.setY(curY - 1.0);break;//上
			case 3:curState.setY(curY + 1.0);break;//下
			default:throw new RuntimeException("Out of Action Space");
		}
		boolean bIsOutOfBox = isOutOfBox();
		if( bIsOutOfBox ){
			switch(action){
			case 0:curState.setX(curX + 1.0);break;//左
			case 1:curState.setX(curX - 1.0);break;//右
			case 2:curState.setY(curY + 1.0);break;//上
			case 3:curState.setY(curY - 1.0);break;//下
			default:throw new RuntimeException("Out of Action Space");
			}
		}
		if( isTraped() ){
			reward -= 1.0;
		}else if(isSuccess()){
			reward += 1.0;
		}else if( bIsOutOfBox ){
			reward -= 0.2;
		}else{
			reward -= 0.1;
		}
		++step;
		this.trace.add(new Point((int)curState.getX(), (int)curState.getY()));
		return new StepReply<>(curState, reward, isDone(), null);
	}

}
