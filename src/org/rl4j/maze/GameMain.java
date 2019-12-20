package org.rl4j.maze;

import org.deeplearning4j.rl4j.learning.Learning;
import org.deeplearning4j.rl4j.learning.sync.qlearning.QLearning;
import org.deeplearning4j.rl4j.learning.sync.qlearning.discrete.QLearningDiscreteDense;
import org.deeplearning4j.rl4j.network.dqn.DQNFactoryStdDense;
import org.deeplearning4j.rl4j.policy.DQNPolicy;
import org.deeplearning4j.rl4j.util.DataManager;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.learning.config.Adam;
import org.rl4j.maze.util.Point;

import java.io.IOException;

public class GameMain {
	// 深度学习网络配置
    public static QLearning.QLConfiguration QL_CONFIG = new QLearning.QLConfiguration(
                    123,   	//Random seed
                    30,	//Max step Every epoch 批次下最大执行的步数
                    100*2000, //Max step            总执行的部署
                    100*2000, //Max size of experience replay 记忆数据
                    40,    //size of batches
                    10,   //target update (hard) 每10次更新一次参数
                    0,     //num step noop warmup   步数从0开始
                    0.01,  //reward scaling
                    0.9,  //gamma
                    1.0,  //td-error clipping
                    0.1f,  //min epsilon
                    100,  //num step for eps greedy anneal
                    false   //double DQN
            );

    // 网络结构参数配置
    public static DQNFactoryStdDense.Configuration DQN_NET = DQNFactoryStdDense.Configuration.builder()
                    .updater(new Adam(0.001))
                    .numLayer(2)
                    .numHiddenNodes(16)
                    .build();

	/**
	 * 训练Deep Q-Learn Network
	 * @throws IOException
	 */
	public static void learning() throws IOException {

    	// 初始化
        DataManager manager = new DataManager();
		GameMDP mdp = new GameMDP();
        QLearningDiscreteDense<GameState> dql = new QLearningDiscreteDense<GameState>(mdp, DQN_NET, QL_CONFIG, manager);
        DQNPolicy<GameState> pol = dql.getPolicy();

        // 开始训练
        dql.train();

		// 保存模型
        pol.save("data/game1.policy");
        mdp.close();
    }
    
    public static Point playByStep(GameMDP mdp, DQNPolicy<GameState> policy) throws IOException{
    	Point ret = new Point();
    	GameState curState = mdp.getCurState();

        // 前向传递
        INDArray input = Learning.getInput(mdp, curState);
        int action = policy.nextAction(input).intValue();

        // 返回的状态是之前的还是之后的？
        ret.setX((int)curState.getX());
        ret.setY((int)curState.getY());

        // 执行策略
        mdp.step(action);
    	return ret;
    }
    
    public static boolean isSuccess(GameMDP mdp){
    	boolean ret= false;
    	GameState state = mdp.getCurState();
    	if( (int)state.getX()  == 9 && (int)state.getY() == 9){
    		ret = true;
		}
    	return ret;
    }
    
    public static boolean isTraped(GameMDP mdp){
    	boolean ret = false;
    	GameState state = mdp.getCurState();
    	Point[] traps = mdp.getTraps();
    	int curX = (int)state.getX();
    	int curY = (int)state.getY();
    	for( Point trap : traps ){
    		if( curX == trap.getX() && curY == trap.getY() ){
    			ret = true;
    			break;
    		}
    	}
    	return ret;
    }
    
    public static GameBoard initGameBoard(GameMDP mdp){
    	Learning.InitMdp<GameState> initMdp = Learning.initMdp(mdp, null);
		GameState initState = initMdp.getLastObs();
		int x = (int)initState.getX();
		int y = (int)initState.getY();

		// 初始化游戏界面
		GameBoard board = new GameBoard(x,y);

		// 设置陷阱
		board.setTrap(mdp.getTraps());
		return board;
    }
    
	public static void main(String[] args) throws IOException, InterruptedException {
		// 训练网络
    	//learning();

		// 初始化
		GameMDP mdp = new GameMDP();
		GameBoard board = initGameBoard(mdp);

		// 加载网络
		boolean success = false, trap = false;
		DQNPolicy<GameState> policy = DQNPolicy.load("data/game.policy");

		while( true ){
			Point p = playByStep(mdp ,policy);
			board.shiftSoilder(p.getX(), p.getY());
			// 判断是否成功到达终点
			success = isSuccess(mdp);
			if( success ){
				board.dialog("success", "Game Over");
				board = initGameBoard(mdp);
			}

			// 判断是否陷入陷阱
			trap = isTraped(mdp);
			if( trap ){
				board.dialog("fail", "Game Over");
				board = initGameBoard(mdp);
			}

			// 休眠指定的时间
			Thread.sleep(500);
		}
	}
}
