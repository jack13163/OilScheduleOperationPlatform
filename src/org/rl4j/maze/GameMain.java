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

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

class GameBoard extends JFrame
{
	private static final long serialVersionUID = -6746198472689601833L;
	private GridLayout grid;
    private JPanel chessboard;
    private int curX = 0, curY = 0;
    
    public void dialog(String msg, String title){
    	JOptionPane.showMessageDialog(chessboard, msg, title,JOptionPane.WARNING_MESSAGE);
    }
    
    public void shiftSoilder(int x , int y){
    	assert(chessboard.getComponentCount() == 100);
    	Component[] components = chessboard.getComponents();
    	JLabel labelLast = (JLabel)components[curX * 10 + curY];
    	labelLast.setIcon(null);
    	//
    	JLabel labelCur = (JLabel)components[x * 10 + y];
    	labelCur.setIcon(new ImageIcon("img/dunk.jpg"));
    	curX = x;
    	curY = y;
    	return;
    }
    
    public void setTrap(Point[] traps){
    	Component[] components = chessboard.getComponents();
    	for( Point trap : traps ){
    		JLabel label = (JLabel)components[ trap.getX() * 10 + trap.getY() ];
    		label.setIcon(new ImageIcon("img/trap.jpg"));
    	}
    }
    
    GameBoard (int x, int y)
    {
    	chessboard = new JPanel ();
        grid = new GridLayout (10, 10);
        chessboard.setLayout (grid);
        JLabel[][] label = new JLabel[10][10];
        //
        ImageIcon image = new ImageIcon("img/dunk.jpg");
        ImageIcon imageDst = new ImageIcon("img/princess.png");
        this.curX = x;
        this.curY = y;
        //
        for ( int i = 0; i < label.length; i++ )
        {
            for ( int j = 0; j < label[i].length; j++ )
            {
                label[i][j] = new JLabel ();
                label[i][j].setOpaque(true);
                if( i == x && j == y ){
                	label[i][j] = new JLabel (image);
                }
                else if( i == 9 && j == 9 ){
                	label[i][j] = new JLabel (imageDst);
                }
                else if (( i + j ) % 2 == 0)
                    label[i][j].setBackground (Color.white);
                else
                    label[i][j].setBackground (Color.gray);
                chessboard.add (label[i][j]);
            }
        }
        add (chessboard, BorderLayout.CENTER);
        setBounds (10, 10, 1000, 1000);
        setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
        setVisible (true);
    }
}

public class GameMain {
    public static QLearning.QLConfiguration QL_CONFIG =
            new QLearning.QLConfiguration(
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
	
    public static DQNFactoryStdDense.Configuration DQN_NET =
            DQNFactoryStdDense.Configuration.builder()
                    .updater(new Adam(0.001))
                    .numLayer(2)
                    .numHiddenNodes(16)
                    .build();
    
    public static void learning() throws IOException {

        DataManager manager = new DataManager();

        GameMDP mdp = new GameMDP();

        QLearningDiscreteDense<GameState> dql = new QLearningDiscreteDense<GameState>(mdp, DQN_NET, QL_CONFIG, manager);

        DQNPolicy<GameState> pol = dql.getPolicy();

        dql.train();

        pol.save("data/game1.policy");

        mdp.close();

    }
    
    public static Point playByStep(GameMDP mdp, DQNPolicy<GameState> policy) throws IOException{
    	Point ret = new Point();
    	GameState curState = mdp.getCurState();
        //
        INDArray input = Learning.getInput(mdp, curState);
        int action = policy.nextAction(input).intValue();
        ret.setX((int)curState.getX());
        ret.setY((int)curState.getY());
        //
        mdp.step(action);
    	//
    	return ret;
    }
    
    public static GameMDP initMDP(){
    	GameMDP mdp = new GameMDP();
    	return mdp;
    }
    
    public static boolean isSuccess(GameMDP mdp){
    	boolean ret= false;
    	//
    	GameState state = mdp.getCurState();
    	if( (int)state.getX()  == 9 && (int)state.getY() == 9)ret = true;
    	//
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
		GameBoard board = new GameBoard(x,y);
		board.setTrap(mdp.getTraps());
		//
		return board;
    }
    
	public static void main(String[] args) throws IOException, InterruptedException {
		learning();

		GameMDP mdp = initMDP();
		GameBoard board = initGameBoard(mdp);
		//
		boolean success = false, trap = false;
		DQNPolicy<GameState> policy = DQNPolicy.load("data/game.policy");
		while( true ){
			Point p = playByStep(mdp ,policy);
			board.shiftSoilder(p.getX(), p.getY());
			//
			success = isSuccess(mdp);
			if( success ){
				board.dialog("成功救到公主", "Game Over");
				board = initGameBoard(mdp);
			}
			trap = isTraped(mdp);
			if( trap ){
				board.dialog("掉入陷阱", "Game Over");
				board = initGameBoard(mdp);
			}
			//
			Thread.sleep(1000);
		}
	}
}
