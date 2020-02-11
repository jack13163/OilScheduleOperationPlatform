package opt.easyjmetal.problem.schedule.util;

import opt.jmetal.problem.oil.canvas.gante.CanvasGante;

import javax.swing.*;

public class ChartFrame {
    // 窗口大小
    private final static int width = 900;
    private final static int height = 600;

    public JFrame frame;
    private CanvasGante canvas;

    public ChartFrame() {
        frame = new JFrame("原油调度甘特图");
        canvas = new CanvasGante();
        // 将窗口定位到屏幕中间
        int screenWidth = (int) java.awt.Toolkit.getDefaultToolkit().getScreenSize().width;
        int screenHeight = (int) java.awt.Toolkit.getDefaultToolkit().getScreenSize().height;
        frame.setBounds(screenWidth / 2 - width / 2, screenHeight / 2 - height / 2, width, height);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
        frame.add(canvas);
    }

    public void updateCanvas(double[][] data) {
        canvas.setData(data);
        canvas.repaint();
    }
}
