package com.canvas.gante;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;

import org.ejml.data.DenseMatrix64F;

import com.sim.common.MatrixHelper;

public class CanvasGante extends Canvas {
    private static final long serialVersionUID = 1L;

    private final static int margin_left = 20;// 图像左边距
    private final static int margin_top = 100;// 图像上边距
    private final static int margin_right = 20;// 图像右边距
    private final static int margin_buttom = 80;// 图像下边距

    private final static int label_width = 40;// 任务标签
    private final static int block_width = 40;// 块高度
    private final static int block_height = 25;// 块高度
    private final static int num_x_divide = 10;// x轴刻度个数

    private double[][] data;

    public double[][] getData() {
        return data;
    }

    public void setData(double[][] data) {
        this.data = data;
    }

    // 颜色数组，以区分不同的原油类型
    private final static Color[] colors = {new Color(192, 255, 32), new Color(220, 20, 60), new Color(0, 0, 255),
            new Color(95, 158, 160), new Color(50, 205, 50), new Color(255, 51, 0), new Color(255, 215, 0),
            new Color(255, 174, 201), new Color(188, 143, 143), new Color(204, 102, 255), new Color(75, 0, 130)};

    public void paint(Graphics g) {
        super.paint(g);

        // 切记，这里要获取Graphics的长和宽，而不是Form的长和宽
        int width = g.getClipBounds().width;
        int height = g.getClipBounds().height;

        DenseMatrix64F dataMatrix = new DenseMatrix64F(data);
        double[] col_max = MatrixHelper.getColMax(dataMatrix).data;
        int numOfOilType = (int) col_max[4];// 原油种类数
        int maxTime = (int) col_max[3];// 最后的任务的完成时间
        int numOfTasks = (int) col_max[0];// 任务的种类数

        double scale_x = 1.0 * (width - margin_left - margin_right - label_width) / maxTime;// 图像大小变化尺度
        double scale_y = 1.0 * (height - margin_top - margin_buttom - label_width) / 600 * 100;// 图像大小变化尺度【参考原始比例】

        // 绘制详细的调度数据
        for (int i = 0; i < data.length; i++) {
            // 使用不同的颜色填充封闭的矩形区域
            int color = (int) data[i][4] - 1;
            int tank = (int) data[i][1];
            // 计算矩形区域所在位置和宽度
            int data_x = (int) (margin_left + label_width + data[i][2] * scale_x);
            int data_y = (int) (margin_top + 1.0 * (data[i][0] - 1) * scale_y);
            int data_width = (int) (1.0 * (data[i][3] - data[i][2]) * scale_x);
            // 不显示停运
            if (color >= 0) {
                // 绘制矩形区域，并设置矩形区域的标注
                g.setColor(colors[color]);
                g.fill3DRect(data_x, data_y, data_width, block_height, false);
                g.setColor(Color.black);
                g.drawString("T" + tank + "", data_x, data_y);
            } else {
                // 绘制矩形区域，并设置矩形区域的标注
                g.setColor(Color.black);
                g.fill3DRect(data_x, data_y, data_width, block_height, false);
            }

        }

        // 标识x轴刻度
        for (int i = 0; i < num_x_divide; i++) {
            int x1 = (int) (margin_left + label_width
                    + (i + 1) * (width - margin_left - margin_right - label_width) / num_x_divide);
            int y1 = (int) (margin_top + numOfTasks * scale_y - 10);
            int x2 = (int) (margin_left + label_width
                    + (i + 1) * (width - margin_left - margin_right - label_width) / num_x_divide);
            int y2 = (int) (margin_top + numOfTasks * scale_y);

            g.setColor(Color.black);
            g.drawLine(x1, y1, x2, y2);
            g.drawString((i + 1) * maxTime / 10.0 + "", x2 - 15, y2 + 15);
        }

        // 标识y轴刻度
        for (int i = 0; i < numOfTasks; i++) {
            int x = margin_left;
            int y = (int) (margin_top + 1.0 * i * scale_y + block_height / 1.5);
            if (i < 4) {
                g.drawString("DS" + (i + 1) + "", x, y);
            } else {
                g.drawString("PIPE" + (i - 3) + "", x, y);
            }
        }

        // 绘制坐标轴
        int x1 = margin_left + label_width;
        int y1 = (int) (margin_top + numOfTasks * scale_y);
        int x2 = width - margin_right;
        int y2 = (int) (margin_top + numOfTasks * scale_y);
        g.setColor(Color.black);
        g.drawLine(x1, y1, x2, y2);// x轴
        int x3 = margin_left + label_width;
        int y3 = margin_top;
        int x4 = margin_left + label_width;
        int y4 = (int) (margin_top + numOfTasks * scale_y);
        g.drawLine(x3, y3, x4, y4);// y轴

        // 绘制legend
        for (int i = 0; i < numOfOilType; i++) {
            // 计算矩形区域所在位置和宽度
            int x = (int) (margin_left + label_width + i * 3 * block_height);
            int y = height - margin_buttom;

            // 使用不同的颜色填充封闭的矩形区域
            g.setColor(colors[i]);
            g.fill3DRect(x, y, block_width, block_height, true);

            // 设置标签
            int oilType = i + 1;
            g.setColor(Color.black);
            g.drawString("#" + oilType + "", x, y);
        }
    }
}