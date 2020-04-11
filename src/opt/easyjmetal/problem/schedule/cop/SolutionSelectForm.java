package opt.easyjmetal.problem.schedule.cop;

import opt.easyjmetal.algorithm.cmoeas.util.Utils;
import opt.easyjmetal.core.Solution;
import opt.easyjmetal.util.JMException;
import opt.javasim.SimulationProcess;
import opt.jmetal.problem.oil.sim.common.JTableHelper;
import opt.jmetal.problem.oil.sim.common.ParetoHelper;
import opt.jmetal.problem.oil.sim.oil.Config;
import opt.jmetal.problem.oil.sim.ui.multicombobox.KeyValuePair;
import opt.jmetal.problem.oil.sim.ui.multicombobox.MultiComboBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SolutionSelectForm extends JFrame {

    private static final long serialVersionUID = 1L;
    private static Logger logger = LogManager.getLogger(SolutionSelectForm.class.getName());

    // UI皮肤
    private static final String[] themes = {"com.jtattoo.plaf.smart.SmartLookAndFeel",
            "com.jtattoo.plaf.mcwin.McWinLookAndFeel", "com.jtattoo.plaf.luna.LunaLookAndFeel",
            "com.jtattoo.plaf.aluminium.AluminiumLookAndFeel", "com.jtattoo.plaf.bernstein.BernsteinLookAndFeel",
            "com.jtattoo.plaf.hifi.HiFiLookAndFeel", "com.jtattoo.plaf.mint.MintLookAndFeel",
            "com.jtattoo.plaf.aero.AeroLookAndFeel", "com.jtattoo.plaf.fast.FastLookAndFeel",
            "com.jtattoo.plaf.acryl.AcrylLookAndFeel"};

    // 公共UI
    private JMenuBar menubar; // 菜单条
    private JMenu menuSystem; // 菜单
    private JMenuItem itemTop, itemExit; // 菜单项
    private Box VMainPanel;// 父容器
    private JTabbedPane tabbedPane;// 选项卡
    private boolean onTop = false;// 是否在最前

    // 运行部分UI
    private MultiComboBox cbAlgorithmsForExperiment;
    private MultiComboBox cbProblemsForExperiment;
    private JTextField txtRuns;
    public JButton btnStartForExperiment;

    public static void main(String[] args) {

        // 指定log4j2.xml文件的位置
        ConfigurationSource source;
        String relativePath = "log4j2.xml";
        File log4jFile = new File(relativePath);
        try {
            if (log4jFile.exists()) {
                source = new ConfigurationSource(new FileInputStream(log4jFile), log4jFile);
                Configurator.initialize(null, source);
            } else {
                System.out.println("loginit failed");
                System.exit(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }

        // 初始化GUI界面，控制权交给用户
        new SolutionSelectForm("可视化非支配解集");
    }

    /**
     * 更换UI风格
     *
     * @param style
     */
    private void changeUIStyle(String style) {

        try {
            UIManager.setLookAndFeel(style);// 更换界面风格
            SwingUtilities.updateComponentTreeUI(this);// 更新UI界面
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public SolutionSelectForm(String s) {
        // 设置默认UI风格
        changeUIStyle(themes[4]);
        setIconImage(new ImageIcon("img/plan.png").getImage());

        // 关闭窗口事件
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int result = JOptionPane.showConfirmDialog(null, "确认退出?", "确认", JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.INFORMATION_MESSAGE);
                if (result == JOptionPane.OK_OPTION) {
                    // 关闭线程池
                    SimulationProcess.cachedThreadPool.shutdown();
                    System.exit(0);
                }
            }
        });
        // 设置窗体大小并居中
        setWindowSizeAndCenter(this, 1200, 800);
        setTitle(s);
        menubar = new JMenuBar();
        menuSystem = new JMenu("系统(S)");
        menuSystem.setMnemonic('S'); // 设置菜单的键盘操作方式是Alt + S键
        itemTop = new JMenuItem("最前(T)");

        itemTop.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setAlwaysOnTop(!onTop);
                onTop = !onTop;
                if (onTop) {
                    itemTop.setText("取消最前(T)");
                } else {
                    itemTop.setText("最前(T)");
                }
            }
        });
        itemExit = new JMenuItem("退出(E)");
        itemExit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        // 设置菜单项的键盘操作方式是Ctrl+T和Ctrl+E键
        KeyStroke Ctrl_cutKey = KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_MASK);
        itemTop.setAccelerator(Ctrl_cutKey);
        Ctrl_cutKey = KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK);
        itemExit.setAccelerator(Ctrl_cutKey);

        menuSystem.add(itemTop);
        menuSystem.add(itemExit);
        menubar.add(menuSystem); // 将菜单添加到菜单条上
        setJMenuBar(menubar);

        JMenu menuUIStyle = new JMenu("换肤");
        menuUIStyle.setMnemonic('S');
        menubar.add(menuUIStyle);

        JMenuItem menuItemStyle1 = new JMenuItem("风格1");
        menuItemStyle1.addActionListener((e) -> {
            changeUIStyle(themes[0]);
        });
        menuUIStyle.add(menuItemStyle1);

        JMenuItem menuItemStyle2 = new JMenuItem("风格2");
        menuItemStyle2.addActionListener((e) -> {
            changeUIStyle(themes[1]);
        });
        menuUIStyle.add(menuItemStyle2);

        JMenuItem menuItemStyle3 = new JMenuItem("风格3");
        menuItemStyle3.addActionListener((e) -> {
            changeUIStyle(themes[2]);
        });
        menuUIStyle.add(menuItemStyle3);

        JMenuItem menuItemStyle4 = new JMenuItem("风格4");
        menuItemStyle4.addActionListener((e) -> {
            changeUIStyle(themes[3]);
        });
        menuUIStyle.add(menuItemStyle4);

        JMenuItem menuItemStyle5 = new JMenuItem("风格5");
        menuItemStyle5.addActionListener((e) -> {
            changeUIStyle(themes[4]);
        });
        menuUIStyle.add(menuItemStyle5);

        // tab标签栏
        VMainPanel = Box.createVerticalBox();
        getContentPane().add(VMainPanel, BorderLayout.CENTER);
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setBackground(Color.WHITE);
        VMainPanel.add(tabbedPane);
        tabbedPane.addTab("分析实验结果", createExperimentUI());
        tabbedPane.setSelectedIndex(0);// 设置默认选中的选项卡

        Box HBox12 = Box.createHorizontalBox();
        VMainPanel.add(HBox12);

        setVisible(true);
    }

    /**
     * 创建实验模块面板
     */
    private JComponent createExperimentUI() {
        // 创建一个垂直的盒子
        Box box = Box.createVerticalBox();

        Box HBox5 = Box.createHorizontalBox();
        JLabel lblIndex = new JLabel("研究问题：");
        HBox5.add(lblIndex);
        List<KeyValuePair> problemList = new LinkedList<>();
        problemList.add(new KeyValuePair("EDF_PS", "EDF_PS"));
        problemList.add(new KeyValuePair("EDF_TSS", "EDF_TSS"));
        List<KeyValuePair> defaultValue1 = new LinkedList<>();
        defaultValue1.add(new KeyValuePair("EDF_PS", "EDF_PS"));
        defaultValue1.add(new KeyValuePair("EDF_TSS", "EDF_TSS"));
        cbProblemsForExperiment = new MultiComboBox(problemList, defaultValue1);
        HBox5.add(cbProblemsForExperiment);
        box.add(HBox5);

        // 运行算法复选框
        Box HBox4 = Box.createHorizontalBox();
        JLabel lblSu2 = new JLabel("算法：        ");
        HBox4.add(lblSu2);
        List<KeyValuePair> algorithmList = new LinkedList<>();
        algorithmList.add(new KeyValuePair("NSGAII_CDP", "NSGAII_CDP"));
        algorithmList.add(new KeyValuePair("ISDEPLUS_CDP", "ISDEPLUS_CDP"));
        algorithmList.add(new KeyValuePair("NSGAIII_CDP", "NSGAIII_CDP"));
        algorithmList.add(new KeyValuePair("MOEAD_CDP", "MOEAD_CDP"));
        algorithmList.add(new KeyValuePair("MOEAD_IEpsilon", "MOEAD_IEpsilon"));
        algorithmList.add(new KeyValuePair("MOEAD_Epsilon", "MOEAD_Epsilon"));
        algorithmList.add(new KeyValuePair("MOEAD_SR", "MOEAD_SR"));
        algorithmList.add(new KeyValuePair("C_MOEAD", "C_MOEAD"));
        algorithmList.add(new KeyValuePair("PPS_MOEAD", "PPS_MOEAD"));
        List<KeyValuePair> defaultValue2 = new LinkedList<>();
        defaultValue2.add(new KeyValuePair("NSGAII_CDP", "NSGAII_CDP"));
        defaultValue2.add(new KeyValuePair("ISDEPLUS_CDP", "ISDEPLUS_CDP"));
        defaultValue2.add(new KeyValuePair("NSGAIII_CDP", "NSGAIII_CDP"));
        defaultValue2.add(new KeyValuePair("MOEAD_CDP", "MOEAD_CDP"));
        defaultValue2.add(new KeyValuePair("MOEAD_IEpsilon", "MOEAD_IEpsilon"));
        defaultValue2.add(new KeyValuePair("MOEAD_Epsilon", "MOEAD_Epsilon"));
        defaultValue2.add(new KeyValuePair("MOEAD_SR", "MOEAD_SR"));
        defaultValue2.add(new KeyValuePair("C_MOEAD", "C_MOEAD"));
        defaultValue2.add(new KeyValuePair("PPS_MOEAD", "PPS_MOEAD"));
        cbAlgorithmsForExperiment = new MultiComboBox(algorithmList, defaultValue2);
        HBox4.add(cbAlgorithmsForExperiment);
        // 添加标签到面板
        box.add(HBox4);

        // 运行次数
        Box HBox3 = Box.createHorizontalBox();
        JLabel label = new JLabel("运行次数：");
        HBox3.add(label);
        txtRuns = new JTextField();
        txtRuns.setText("10");
        txtRuns.setColumns(10);
        HBox3.add(txtRuns);
        box.add(HBox3);

        // 创建指标表格
        Box HBox8 = Box.createHorizontalBox();
        final JTable resultTable = new JTable();
        JTableHelper.setTableColumnCenter(resultTable);
        resultTable.addMouseListener(new MouseListener() {
            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseClicked(MouseEvent e) {
                // 点击几次，这里是双击事件
                if (e.getClickCount() == 2) {
                    // 查找出指定的解
                    double[][] tofind = new double[1][5];
                    int row = resultTable.getSelectedRow();
                    // 读取对应的记录
                    for (int i = 0; i < 5; i++) {
                        tofind[0][i] = Double.parseDouble(resultTable.getValueAt(row, i + 1).toString());
                    }

                    try {
                        Utils.getSolutionFromDB(cbAlgorithmsForExperiment.getText().split(","),
                                cbProblemsForExperiment.getText().split(","),
                                Integer.parseInt(txtRuns.getText()),
                                tofind,
                                new Utils.ToDo() {
                                    @Override
                                    public void dosomething(Solution solution, String rule) {
                                        COPDecoder.decode(solution, rule, true);
                                    }
                                });
                    } catch (JMException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        JTableHelper.setTableColumnCenter(resultTable);
        JScrollPane scroll = new JScrollPane(resultTable);
        scroll.setPreferredSize(new Dimension(100, 600));
        HBox8.add(scroll);
        box.add(HBox8);

        Box HBox10 = Box.createHorizontalBox();
        btnStartForExperiment = new JButton("开始实验");
        btnStartForExperiment.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // 获取用户输入
                final int runtimes = Integer.parseInt(txtRuns.getText());
                final String[] algorithmNames = cbAlgorithmsForExperiment.getText().split(",");
                final String[] problemNames = cbProblemsForExperiment.getText().split(",");

                // 开辟新的线程运行实验【防止UI线程阻塞】
                Thread mainThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // 生成pareto前沿面
                            String path = Utils.generateOilScheduleParetoFront(algorithmNames, problemNames, runtimes);
                            // 标记优秀的解
                            paintNodominanceSolution(resultTable, path);
                        } catch (Exception e) {
                            logger.fatal(e.getMessage());
                        }
                    }
                });

                mainThread.setName("Experiment");
                mainThread.setDaemon(true);// 作为后台守护进程运行
                mainThread.start();
            }
        });
        HBox10.add(btnStartForExperiment);
        box.add(HBox10);

        return box;
    }

    /**
     * 获取运行结果，并高亮显示非支配解【目标函数值】
     *
     * @param resultTable
     * @param filePath
     * @throws IOException
     */
    protected void paintNodominanceSolution(JTable resultTable, String filePath) throws IOException {
        // 准备数据
        final DefaultTableModel mm = JTableHelper.showTableWithNo(filePath, false);

        // 重新指定列名
        String[] columnNames = {"序号", "能耗成本", "管道混合成本", "罐底混合成本", "切换次数", "用罐个数"};
        mm.setColumnIdentifiers(columnNames);

        // 不要在UI线程外更新操作UI，这里SwingUtilities会找到UI线程并执行更新UI操作
        SwingUtilities.invokeLater(() -> {
            resultTable.setModel(mm);
            boolean[] flags = new boolean[mm.getRowCount()];
            for (int i = 0; i < mm.getRowCount(); i++) {
                Map<String, Double> referenceCost = Config.getInstance().loadConfig().referenceCost;
                Map<String, Double> resultCost = new HashMap<String, Double>();
                resultCost.put("energyCost", Double.parseDouble(mm.getValueAt(i, 1).toString()));
                resultCost.put("pipeMix", Double.parseDouble(mm.getValueAt(i, 2).toString()));
                resultCost.put("tankMix", Double.parseDouble(mm.getValueAt(i, 3).toString()));
                resultCost.put("chargeTime", Double.parseDouble(mm.getValueAt(i, 4).toString()));
                resultCost.put("tankUsed", Double.parseDouble(mm.getValueAt(i, 5).toString()));

                // 标记支配解
                flags[i] = ParetoHelper.dominanceComparison(referenceCost, resultCost);
            }

            // 突出显示非支配解
            JTableHelper.setRowsColor(resultTable, flags);
        });
    }

    /**
     * 自动将窗口放到屏幕正中间
     *
     * @param frame
     * @param width
     * @param height
     */
    public void setWindowSizeAndCenter(JFrame frame, int width, int height) {
        int screenWidth = (int) Toolkit.getDefaultToolkit().getScreenSize().width;
        int screenHeight = (int) Toolkit.getDefaultToolkit().getScreenSize().height;
        frame.setBounds(screenWidth / 2 - width / 2, screenHeight / 2 - height / 2, width, height);
    }
}