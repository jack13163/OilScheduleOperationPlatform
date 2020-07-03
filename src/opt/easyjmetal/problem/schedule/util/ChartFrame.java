package opt.easyjmetal.problem.schedule.util;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class ChartFrame extends JFrame {
    // 窗口大小
    private final static int width = 1100;
    private final static int height = 480;

    private CanvasGante canvas;// canvas绘图区
    private JMenuBar menubar;// 菜单条

    public ChartFrame() {
        setTitle("原油短期详细调度图");
        canvas = new CanvasGante();

        // 将窗口定位到屏幕中间
        int screenWidth = (int) java.awt.Toolkit.getDefaultToolkit().getScreenSize().width;
        int screenHeight = (int) java.awt.Toolkit.getDefaultToolkit().getScreenSize().height;
        setBounds(screenWidth / 2 - width / 2, screenHeight / 2 - height / 2, width, height);
        add(canvas);

        // 菜单条
        menubar = new JMenuBar();

        JMenu menu = new JMenu("文件(F)");
        menu.setMnemonic(KeyEvent.VK_F);    //设置快速访问符

        JMenuItem item = new JMenuItem("保存(S)", KeyEvent.VK_S);
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));// 快捷键
        item.addActionListener((e) -> {
            //把图像保存为文件
            JFileChooser chooser = new JFileChooser();//文件保存对话框
            chooser.setCurrentDirectory(new File("."));
            if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                File oFile = chooser.getSelectedFile();
                try {
                    //保存图像文件
                    savePic(oFile.getAbsolutePath());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            // 保存后自动重绘
            canvas.repaint();
        });
        menu.add(item);

        menubar.add(menu);
        setJMenuBar(menubar);
        addComponentListener(new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent e) {
                // 界面大小变化后自动重绘
                canvas.repaint();
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                // 界面移动后自动重绘
                canvas.repaint();
            }

            @Override
            public void componentShown(ComponentEvent e) {
                // 界面出现后自动重绘
                canvas.repaint();
            }

            @Override
            public void componentHidden(ComponentEvent e) {

            }
        });
        setVisible(true);
    }

    public void updateCanvas(double[][] data) {
        canvas.setData(data);
        canvas.repaint();
    }

    /**
     * 保存图像
     *
     * @param filepath
     */
    public void savePic(String filepath) {
        Dimension imagesize = canvas.getSize();
        BufferedImage myImage = new BufferedImage(imagesize.width, imagesize.height, BufferedImage.TYPE_INT_RGB);
        Graphics graphics1 = myImage.createGraphics();
        graphics1.setClip(new Rectangle(0, 0, imagesize.width,imagesize.height));
        canvas.drawGante(graphics1);
        graphics1.dispose();

        try {
            // ImageIO.write(myImage, "jpg", new File(filepath));
            // 将结果画出来
            ImageWriter writer = ImageIO.getImageWritersByFormatName("tiff").next();
            writer.setOutput(new FileImageOutputStream(new File(filepath)));
            writer.write(myImage);
            writer.dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
