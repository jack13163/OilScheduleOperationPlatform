package org.rl4j.maze;

import org.rl4j.maze.util.Point;

import javax.swing.*;
import java.awt.*;

public class GameBoard extends JFrame {
    private static final long serialVersionUID = -6746198472689601833L;
    private GridLayout grid;
    private JPanel chessboard;
    private int curX = 0, curY = 0;

    public void dialog(String msg, String title) {
        JOptionPane.showMessageDialog(chessboard, msg, title, JOptionPane.WARNING_MESSAGE);
    }

    /**
     * 移动樱木花道到指定的位置（x,y）
     *
     * @param x
     * @param y
     */
    public void shiftSoilder(int x, int y) {
        // 10×10的迷宫
        assert (chessboard.getComponentCount() == 100);

        // 设置当前位置的图片为空
        Component[] components = chessboard.getComponents();
        JLabel labelLast = (JLabel) components[curX * 10 + curY];
        labelLast.setIcon(null);

        // 设置（x,y）位置的图片
        JLabel labelCur = (JLabel) components[x * 10 + y];
        labelCur.setIcon(new ImageIcon("img/dunk.jpg"));
        curX = x;
        curY = y;
    }

    /**
     * 设置陷阱
     *
     * @param traps
     */
    public void setTrap(Point[] traps) {
        Component[] components = chessboard.getComponents();
        for (Point trap : traps) {
            JLabel label = (JLabel) components[trap.getX() * 10 + trap.getY()];
            label.setIcon(new ImageIcon("img/trap.jpg"));
        }
    }

    /**
     * 初始化迷宫，并设置樱木花道和公主的位置
     *
     * @param x
     * @param y
     */
    public GameBoard(int x, int y) {
        chessboard = new JPanel();
        grid = new GridLayout(10, 10);
        chessboard.setLayout(grid);
        JLabel[][] label = new JLabel[10][10];
        ImageIcon image = new ImageIcon("img/dunk.jpg");
        ImageIcon imageDst = new ImageIcon("img/princess.png");

        this.curX = x;
        this.curY = y;

        for (int i = 0; i < label.length; i++) {
            for (int j = 0; j < label[i].length; j++) {
                label[i][j] = new JLabel();
                label[i][j].setOpaque(true);

                if (i == x && j == y) {
                    // 樱木的位置
                    label[i][j] = new JLabel(image);
                } else if (i == 9 && j == 9) {
                    // 公主的位置
                    label[i][j] = new JLabel(imageDst);
                } else if ((i + j) % 2 == 0) {
                    label[i][j].setBackground(Color.white);
                } else {
                    label[i][j].setBackground(Color.gray);
                }

                chessboard.add(label[i][j]);
            }
        }
        add(chessboard, BorderLayout.CENTER);
        setBounds(10, 10, 1000, 1000);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }
}
