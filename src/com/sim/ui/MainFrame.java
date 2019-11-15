package com.sim.ui;

import com.chart.util.ChartHelper;
import com.models.Fragment;
import com.sim.common.ExcelHelper;
import com.sim.common.JTableHelper;
import com.sim.common.MatlabScriptHelper;
import com.sim.common.ParetoHelper;
import com.sim.experiment.ExperimentConfig;
import com.sim.experiment.ExperimentGenerateReferenceParetoSetAndFrontFromDoubleSolutions;
import com.sim.experiment.TestProblemsExperimentConfig;
import com.sim.oil.Config;
import com.sim.oil.cop.COPOilScheduleIndividualDecode;
import com.sim.oil.cop.OilScheduleConstrainedOptimizationProblem;
import com.sim.oil.op.OPOilScheduleIndividualDecode;
import com.sim.oil.op.OilScheduleOptimizationProblem;
import com.sim.onlineoperation.OnlineOperation;
import com.sim.operation.Operation;
import com.sim.ui.multicombobox.KeyValuePair;
import com.sim.ui.multicombobox.MultiComboBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javasim.SimulationProcess;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.util.experiment.component.ComputeQualityIndicators;
import org.uma.jmetal.util.experiment.component.GenerateLatexTablesWithStatistics;
import org.uma.jmetal.util.experiment.util.ExperimentProblem;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.List;
import java.util.Queue;
import java.util.*;

public class MainFrame extends JFrame {

    private static final long serialVersionUID = 1L;
    private static Logger logger = LogManager.getLogger(MainFrame.class.getName());

    // UI皮肤
    private static final String[] themes = {"com.jtattoo.plaf.smart.SmartLookAndFeel",
            "com.jtattoo.plaf.mcwin.McWinLookAndFeel", "com.jtattoo.plaf.luna.LunaLookAndFeel",
            "com.jtattoo.plaf.aluminium.AluminiumLookAndFeel", "com.jtattoo.plaf.bernstein.BernsteinLookAndFeel",
            "com.jtattoo.plaf.hifi.HiFiLookAndFeel", "com.jtattoo.plaf.mint.MintLookAndFeel",
            "com.jtattoo.plaf.aero.AeroLookAndFeel", "com.jtattoo.plaf.fast.FastLookAndFeel",
            "com.jtattoo.plaf.acryl.AcrylLookAndFeel"};

    // UI界面的构成模块
    private enum UIComponent {
        SINGRUN, Experiment, Online, Test
    }

    ;

    // 公共UI
    private JMenuBar menubar; // 菜单条
    private JMenu menuSystem; // 菜单
    private JMenuItem itemTop, itemExit; // 菜单项
    private Box VMainPanel;// 父容器
    public JProgressBar progressBar;// 进度条
    private InfoUtil tool;// 提示框
    private JTabbedPane tabbedPane;// 选项卡
    private boolean onTop = false;// 是否在最前

    // 运行部分UI
    private JTextField txtPopSizeForSingleRun;
    private JTextField txtEvaluationForSingleRun;
    private JCheckBox cbShowDetail;
    private JCheckBox cbShowHardCostChart;
    public JButton btnStartForSingleRun;
    private JButton btnSaveSingleRunResult;
    private JComboBox<String> cbAlgorithmForRun;
    private JComboBox<String> cbProblemForRun;

    // 实验部分UI
    private JTextField txtPopSizeForExperiment;
    private JTextField txtEvaluationForExperiment;
    private JTextField txtRuns;
    public JButton btnStartForExperiment;
    private MultiComboBox cbAlgorithmsForExperiment;
    private MultiComboBox cbProblemsForExperiment;

    // 测试部分UI
    private JTextField txtRunsForTest;
    private JTextField txtEvaluationForTest;
    private JTextField txtPopSizeForTest;
    public JButton btnStartForTest;
    private MultiComboBox cbListProblemsForTest;
    private MultiComboBox cbListAlgorithmsForTest;

    // 在线优化部分UI
    private JTable tankStateTable;
    private JTable oilPlanTable;
    private JTable costTable;
    private JCheckBox cbShowEachStep;
    private Queue<Fragment> fragmentList;
    private JTextField txtVolume;
    private JComboBox<String> cbSpeed;
    private JComboBox<String> cbTank;
    private JComboBox<String> cbDs;
    private JButton btnStartFragmentList;
    private double lastTime = 0.0;
    private JButton btnRunAllFragment;
    private JButton btnAnalysisExperimentResult;

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

    public MainFrame(String s) {
        // 设置默认UI风格
        changeUIStyle(themes[4]);
        setIconImage(new ImageIcon("img/plan.png").getImage());

        // 右下角通知窗口
        tool = new InfoUtil();

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
        setWindowSizeAndCenter(1200, 800);
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
        tabbedPane.addTab("运行模式", createRunUI());
        tabbedPane.addTab("实验模式", createExperimentUI());
        tabbedPane.addTab("在线模式", createOnlineOperationUI());
        tabbedPane.addTab("测试模式", createTestUI());
        tabbedPane.setSelectedIndex(UIComponent.SINGRUN.ordinal());// 设置默认选中的选项卡

        Box HBox12 = Box.createHorizontalBox();
        VMainPanel.add(HBox12);

        // 进度条
        progressBar = new JProgressBar();
        HBox12.add(progressBar);
        progressBar.setValue(0);
        // 绘制百分比文本（进度条中间显示的百分数）
        progressBar.setStringPainted(true);
        // 添加进度改变通知
        progressBar.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                logger.info("当前进度值: " + progressBar.getValue() + "; " + "进度百分比: " + progressBar.getPercentComplete());
            }
        });

        setVisible(true);
    }

    /**
     * 创建在线优化模块界面
     *
     * @return
     */
    private Component createOnlineOperationUI() {
        // 在线调度器
        OnlineOperation onlineOperation = new OnlineOperation();
        Config.getInstance().loadConfig();

        // 创建一个垂直的盒子
        Box box = Box.createVerticalBox();

        // 创建供油罐状态表格
        JPanel panelTankStateTable = new JPanel(new BorderLayout());
        tankStateTable = new JTable();
        tankStateTable.setEnabled(false);
        JTableHelper.setTableColumnCenter(tankStateTable);
        panelTankStateTable.add(tankStateTable.getTableHeader(), BorderLayout.NORTH);
        panelTankStateTable.add(tankStateTable, BorderLayout.CENTER);
        box.add(panelTankStateTable);

        // 创建分割线，用来区分决策和决策的效果
        JSeparator sep1 = new JSeparator(SwingConstants.CENTER);
        sep1.setPreferredSize(new Dimension(this.getWidth(), 20));
        sep1.setBackground(new Color(153, 153, 153));
        box.add(sep1);

        // 创建待完成的炼油计划表格
        JPanel panelOilPlanTable = new JPanel(new BorderLayout());
        oilPlanTable = new JTable();
        oilPlanTable.setEnabled(false);
        JTableHelper.setTableColumnCenter(oilPlanTable);
        panelOilPlanTable.add(oilPlanTable.getTableHeader(), BorderLayout.NORTH);
        panelOilPlanTable.add(oilPlanTable, BorderLayout.CENTER);
        box.add(panelOilPlanTable);

        // 创建分割线，用来区分决策和决策的效果
        JSeparator sep2 = new JSeparator(SwingConstants.CENTER);
        sep2.setPreferredSize(new Dimension(this.getWidth(), 20));
        sep2.setBackground(new Color(153, 153, 153));
        box.add(sep2);

        Box HBox1 = Box.createHorizontalBox();
        JLabel lblTank = new JLabel("供油罐：    ");
        HBox1.add(lblTank);
        String[] template = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15).toString()
                .replace("[", "").replace("]", "").split(", ");
        int numOfTanks = Config.getInstance().getTanks().size();
        String tanks[] = Arrays.copyOfRange(template, 0, numOfTanks + 1);
        cbTank = new JComboBox<String>(tanks);
        cbTank.addActionListener((e) -> {
            // 非停运操作将会更新UI
            int tank = Integer.parseInt(((JComboBox<?>) (e.getSource())).getSelectedItem().toString());
            // 设置某一行的背景色，tank=0则不更新
            JTableHelper.setOneRowBackgroundColor(tankStateTable, tank, Color.red);
        });
        HBox1.add(cbTank);
        box.add(HBox1);

        Box HBox2 = Box.createHorizontalBox();
        JLabel lblDs = new JLabel("蒸馏塔：    ");
        HBox2.add(lblDs);
        int numOfDs = Config.getInstance().getDSs().size();
        String dss[] = Arrays.copyOfRange(template, 1, numOfDs + 1);
        cbDs = new JComboBox<String>(dss);
        JLabel lblSpeed = new JLabel("转运速度：");
        cbSpeed = new JComboBox<String>();
        cbDs.addActionListener((e) -> {
            cbSpeed.removeAllItems();
            int ds = Integer.parseInt(((JComboBox<?>) (e.getSource())).getSelectedItem().toString());
            double[] speeds;

            if (ds != Config.getInstance().HighOilDS) {
                speeds = Config.getInstance().getPipes().get(0).getChargingSpeed();
            } else {
                speeds = Config.getInstance().getPipes().get(1).getChargingSpeed();
            }
            String[] speeds2 = new String[speeds.length];
            for (int i = 0; i < speeds.length; i++) {
                speeds2[i] = speeds[i] + "";
            }
            cbSpeed.setModel(new DefaultComboBoxModel<String>(speeds2));

            // 更新当前时刻系统中的供油罐的状态
            double currentTime = onlineOperation.getChargingEndTime(ds == Config.getInstance().HighOilDS);
            JTableHelper.showTableInSwing(tankStateTable, onlineOperation.getTankState(currentTime));

            // 非停运操作将会更新UI
            int tank = Integer.parseInt(cbTank.getSelectedItem().toString());
            // 设置某一行背景色，tank=0则不改变背景色
            JTableHelper.setOneRowBackgroundColor(tankStateTable, tank, Color.red);
        });
        cbDs.setSelectedIndex(0);// 默认选中第一项，驱动下拉列表级联更新
        HBox2.add(cbDs);
        box.add(HBox2);
        Box HBox3 = Box.createHorizontalBox();
        HBox3.add(lblSpeed);
        HBox3.add(cbSpeed);
        box.add(HBox3);

        Box HBox4 = Box.createHorizontalBox();
        JLabel lblVolume = new JLabel("转运体积：");
        HBox4.add(lblVolume);
        txtVolume = new JTextField();
        txtVolume.setColumns(10);
        txtVolume.setText("20000");
        HBox4.add(txtVolume);
        box.add(HBox4);

        // 创建分割线，用来区分决策和决策的效果
        JSeparator sep3 = new JSeparator(SwingConstants.CENTER);
        sep3.setPreferredSize(new Dimension(this.getWidth(), 20));
        sep3.setBackground(new Color(153, 153, 153));
        box.add(sep3);

        // 创建指标表格
        JPanel panelValueTable = new JPanel(new BorderLayout());
        Object[] columnNames = {"约束违背值", "切换次数", "罐底混合成本", "管道混合成本", "能耗成本", "用罐个数"};
        Object[][] rowData = {{0, 0, 0, 0, 0, 0}};
        costTable = new JTable(rowData, columnNames);
        costTable.setEnabled(false);
        JTableHelper.setTableColumnCenter(costTable);
        panelValueTable.add(costTable.getTableHeader(), BorderLayout.NORTH);
        panelValueTable.add(costTable, BorderLayout.CENTER);
        box.add(panelValueTable);

        Box HBox10 = Box.createHorizontalBox();
        JButton btnLastStep = new JButton("上一步");
        JButton btnReset = new JButton("开始");
        JButton btnNextStep = new JButton("下一步");

        btnLastStep.setEnabled(false);
        btnLastStep.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                btnLastStep.setEnabled(false);
                try {

                    if (!onlineOperation.getFragments().isEmpty()) {

                        // 返回上一步
                        Fragment lastFragment = onlineOperation.getFragments().pop();
                        onlineOperation.last();

                        // 显示详细调度
                        if (cbShowEachStep.isSelected()) {
                            Operation.plotSchedule2(onlineOperation.getOperations());
                        }

                        // 回显上一步的信息
                        cbTank.setSelectedIndex(lastFragment.getTank());
                        cbDs.setSelectedIndex(lastFragment.getDs() - 1);
                        cbSpeed.setSelectedItem(lastFragment.getSpeed());
                        txtVolume.setText(lastFragment.getVolume() + "");

                        // 判断是否还能够进行上一步
                        if (onlineOperation.getFragments().isEmpty()) {
                            btnLastStep.setEnabled(false);

                            // 更新当前时刻系统中的供油罐的状态
                            JTableHelper.showTableInSwing(tankStateTable, onlineOperation.getTankState(0));
                        } else {
                            btnLastStep.setEnabled(true);

                            // 更新当前时刻系统中的供油罐的状态
                            double currentTime = onlineOperation.getChargingEndTime(
                                    onlineOperation.getFragments().peek().getDs() == Config.getInstance().HighOilDS);
                            JTableHelper.showTableInSwing(tankStateTable, onlineOperation.getTankState(currentTime));
                        }

                        // 设置某一行背景色，tank=0则不改变背景色
                        JTableHelper.setOneRowBackgroundColor(tankStateTable, lastFragment.getTank(), Color.red);
                        // 更新当前系统的未完成炼油计划
                        JTableHelper.showTableInSwing(oilPlanTable, onlineOperation.getFp());
                        // 更新成本
                        JTableHelper.showTableInSwing(costTable, onlineOperation.getCost());
                    }
                } catch (Exception ex) {
                    String errorMessage = ex.getMessage();
                    tool.show("错误", errorMessage);
                }
            }
        });

        btnReset.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // 开始在线优化
                onlineOperation.start();
                // 更新当前时刻系统中的供油罐的状态
                JTableHelper.showTableInSwing(tankStateTable, onlineOperation.getTankState(0));
                // 更新当前系统的未完成炼油计划
                JTableHelper.showTableInSwing(oilPlanTable, onlineOperation.getFp());

                // 初始化选项
                cbTank.setSelectedIndex(0);
                cbDs.setSelectedIndex(0);
                cbSpeed.setSelectedIndex(0);
                txtVolume.setText("");

                if (btnReset.getText().equals("开始")) {
                    tabbedPane.setEnabled(false);
                    btnReset.setText("重置");
                    btnNextStep.setEnabled(true);
                    btnLastStep.setEnabled(false);
                    // 可视化详细调度
                    Operation.plotSchedule2(onlineOperation.getOperations());
                } else {
                    tabbedPane.setEnabled(true);
                    btnReset.setText("开始");
                    btnNextStep.setEnabled(false);
                    btnLastStep.setEnabled(false);
                }
            }
        });

        btnNextStep.setEnabled(false);
        btnNextStep.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                btnNextStep.setEnabled(false);
                Fragment fragment = new Fragment();
                fragment.setDs(Integer.parseInt(cbDs.getSelectedItem().toString()));
                fragment.setTank(Integer.parseInt(cbTank.getSelectedItem().toString()));
                fragment.setSpeed(Double.parseDouble(cbSpeed.getSelectedItem().toString()));
                if (txtVolume.getText().trim().equals("")) {
                    fragment.setVolume(0);
                } else {
                    fragment.setVolume(Double.parseDouble(txtVolume.getText()));
                }

                try {
                    // 进行下一步
                    boolean flag = onlineOperation.next(fragment);

                    if (flag) {
                        onlineOperation.getFragments().push(fragment);
                        // 显示详细调度
                        Operation.plotSchedule2(onlineOperation.getOperations());

                        // 更新当前时刻系统中的供油罐的状态
                        double currentTime = onlineOperation
                                .getChargingEndTime(fragment.getDs() == Config.getInstance().HighOilDS);
                        JTableHelper.showTableInSwing(tankStateTable, onlineOperation.getTankState(currentTime));
                        // 更新当前系统的未完成炼油计划
                        JTableHelper.showTableInSwing(oilPlanTable, onlineOperation.getFp());
                        // 更新成本
                        JTableHelper.showTableInSwing(costTable, onlineOperation.getCost());
                    }
                } catch (Exception ex) {
                    String errorMessage = ex.getMessage();
                    tool.show("错误", errorMessage);
                }

                if (!onlineOperation.getFragments().isEmpty()) {
                    btnLastStep.setEnabled(true);
                } else {
                    btnLastStep.setEnabled(false);
                }
                btnNextStep.setEnabled(true);

                // 初始化
                cbTank.setSelectedIndex(0);
                cbDs.setSelectedIndex(0);
                cbSpeed.setSelectedIndex(0);
                txtVolume.setText("");
            }
        });
        HBox10.add(btnLastStep);
        HBox10.add(btnReset);
        HBox10.add(btnNextStep);
        box.add(HBox10);

        // 创建分割线，用来区分决策和决策的效果
        JSeparator sep4 = new JSeparator(SwingConstants.CENTER);
        sep3.setPreferredSize(new Dimension(this.getWidth(), 20));
        sep3.setBackground(new Color(153, 153, 153));
        box.add(sep4);

        Box HBox11 = Box.createHorizontalBox();
        JLabel lblPath = new JLabel("指令路径：");
        HBox11.add(lblPath);
        JButton btnSelectFile = new JButton("请选择要执行的指令序列文件[txt]");
        btnSelectFile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { // 按钮点击事件
                JFileChooser chooser = new JFileChooser(); // 设置选择器
                chooser.setCurrentDirectory(new File("data")); // 默认打开data
                chooser.setMultiSelectionEnabled(false); // 是否多选
                int returnVal = chooser.showOpenDialog(btnSelectFile); // 是否打开文件选择框
                if (returnVal == JFileChooser.APPROVE_OPTION) { // 如果符合文件类型
                    String filepath = chooser.getSelectedFile().getAbsolutePath(); // 获取绝对路径
                    /* 读取数据 */
                    try {
                        BufferedReader br = new BufferedReader(
                                new InputStreamReader(new FileInputStream(new File(filepath)), "UTF-8"));
                        String lineTxt = null;
                        fragmentList = new LinkedList<>();
                        while ((lineTxt = br.readLine()) != null) {
                            // 数据以逗号分隔
                            String[] names = lineTxt.split(",");
                            Fragment fragment = new Fragment();
                            fragment.setTank(Integer.parseInt(names[0].trim()));
                            fragment.setDs(Integer.parseInt(names[1].trim()));
                            fragment.setSpeed(Integer.parseInt(names[2].trim()));
                            fragment.setVolume(Integer.parseInt(names[3].trim()));
                            fragmentList.add(fragment);
                        }
                        br.close();

                        // 开始在线优化
                        onlineOperation.start();
                        // 更新当前时刻系统中的供油罐的状态
                        JTableHelper.showTableInSwing(tankStateTable, onlineOperation.getTankState(0));
                        // 更新当前系统的未完成炼油计划
                        JTableHelper.showTableInSwing(oilPlanTable, onlineOperation.getFp());
                        // 更新UI
                        Fragment fragment = fragmentList.peek();
                        updateInputFragment(fragment);
                        btnStartFragmentList.setEnabled(true);
                        btnRunAllFragment.setEnabled(true);
                        // 显示详细调度
                        Operation.plotSchedule2(onlineOperation.getOperations());
                    } catch (Exception ex) {
                        System.err.println("read errors :" + ex);
                    }
                }
            }
        });
        HBox11.add(btnSelectFile);
        btnStartFragmentList = new JButton("运行指令序列");
        btnStartFragmentList.setEnabled(false);
        btnStartFragmentList.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { // 按钮点击事件
                Fragment fragment = null;
                try {
                    fragment = fragmentList.remove();
                    // 进行下一步
                    boolean flag = onlineOperation.next(fragment);

                    if (flag) {
                        onlineOperation.getFragments().push(fragment);
                        // 显示详细调度
                        Operation.plotSchedule2(onlineOperation.getOperations());

                        // 更新当前时刻系统中的供油罐的状态
                        double currentTime = onlineOperation
                                .getChargingEndTime(fragment.getDs() == Config.getInstance().HighOilDS);
                        JTableHelper.showTableInSwing(tankStateTable, onlineOperation.getTankState(currentTime));
                        // 更新当前系统的未完成炼油计划
                        JTableHelper.showTableInSwing(oilPlanTable, onlineOperation.getFp());
                        // 更新成本
                        JTableHelper.showTableInSwing(costTable, onlineOperation.getCost());
                        lastTime = currentTime;
                        System.out.println(lastTime);
                    }

                    if (fragmentList.isEmpty()) {
                        // 提示执行结束
                        btnStartFragmentList.setEnabled(false);
                        String message = "当前时间：" + lastTime + "\n" + "\n指令执行结束";
                        tool.show("提示", message);
                    } else {
                        // 更新UI
                        fragment = fragmentList.peek();
                        updateInputFragment(fragment);
                    }
                } catch (Exception ex) {
                    String errorMessage = "当前时间：" + lastTime + "\n" + ex.getMessage() + "\n指令：" + fragment + "出错\n"
                            + "请修改指令序列文件";
                    tool.show("错误", errorMessage);
                    btnStartFragmentList.setEnabled(false);
                }
            }
        });
        HBox11.add(btnStartFragmentList);
        btnRunAllFragment = new JButton("一次性运行");
        btnRunAllFragment.setEnabled(false);
        btnRunAllFragment.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { // 按钮点击事件
                Thread thread = new Thread(() -> {
                    while (!fragmentList.isEmpty()) {
                        btnStartFragmentList.doClick();
                    }
                    Operation.creatSangSen(onlineOperation.getOperations());
                });
                thread.start();
            }
        });
        HBox11.add(btnRunAllFragment);
        box.add(HBox11);

        return box;

    }

    /**
     * 更新下拉输入框
     *
     * @param fragment
     */
    private void updateInputFragment(Fragment fragment) {
        // 获取初始时刻供油罐的状态
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    cbTank.setSelectedIndex(fragment.getTank());
                    cbDs.setSelectedIndex(fragment.getDs() - 1);
                    for (int i = 0; i < cbSpeed.getItemCount(); i++) {
                        double speed = Double.parseDouble(cbSpeed.getItemAt(i) + "");
                        if (speed == fragment.getSpeed()) {
                            cbSpeed.setSelectedIndex(i);
                            break;
                        }
                    }
                    txtVolume.setText(fragment.getVolume() + "");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 创建运行模块面板
     */
    private JComponent createRunUI() {
        // 创建一个垂直的盒子
        Box box = Box.createVerticalBox();

        Box HBox1 = Box.createHorizontalBox();
        JLabel lblIndex = new JLabel("问题：        ");
        HBox1.add(lblIndex);
        String problemNames[] = {"EDF_PS", "EDF_TSS", "BT"};
        cbProblemForRun = new JComboBox<String>(problemNames);
        HBox1.add(cbProblemForRun);
        box.add(HBox1);

        Box HBox2 = Box.createHorizontalBox();
        JLabel lblSu = new JLabel("算法：        ");
        HBox2.add(lblSu);
        String[] algorithms = {"NSGAII", "NSGAIII", "cMOEAD", "SPEA2", "MoCell"};
        cbAlgorithmForRun = new JComboBox<String>(algorithms);
        HBox2.add(cbAlgorithmForRun);
        box.add(HBox2);

        Box HBox3 = Box.createHorizontalBox();
        JLabel lblPopSize = new JLabel("种群规模：");
        HBox3.add(lblPopSize);
        txtPopSizeForSingleRun = new JTextField();
        txtPopSizeForSingleRun.setText("100");
        HBox3.add(txtPopSizeForSingleRun);
        txtPopSizeForSingleRun.setColumns(10);
        box.add(HBox3);

        Box HBox4 = Box.createHorizontalBox();
        JLabel lblEvaluation = new JLabel("评价次数：");
        HBox4.add(lblEvaluation);
        txtEvaluationForSingleRun = new JTextField();
        txtEvaluationForSingleRun.setText("10000");
        txtEvaluationForSingleRun.setColumns(10);
        HBox4.add(txtEvaluationForSingleRun);
        box.add(HBox4);

        Box HBox7 = Box.createHorizontalBox();
        cbShowDetail = new JCheckBox("显示可行的详细调度");
        cbShowDetail.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Config.ShowDetail = cbShowDetail.isSelected();
            }
        });
        cbShowDetail.setSelected(false);
        cbShowHardCostChart = new JCheckBox("显示硬约束");
        cbShowHardCostChart.addActionListener((e) -> {
            Config.ShowHardCostChart = cbShowHardCostChart.isSelected();
        });
        cbShowHardCostChart.setSelected(false);
        cbShowEachStep = new JCheckBox("显示每一步");
        cbShowEachStep.addActionListener((e) -> {
            Config.ShowEachStep = cbShowEachStep.isSelected();
        });
        cbShowEachStep.setSelected(false);
        HBox7.add(cbShowDetail);
        HBox7.add(cbShowHardCostChart);
        HBox7.add(cbShowEachStep);
        box.add(HBox7);

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
                    int row = resultTable.getSelectedRow();
                    // 展示详细调度
                    String algorithmName = cbAlgorithmForRun.getSelectedItem().toString();
                    String path = "result/SingleRun/data/" + algorithmName + "/"
                            + cbProblemForRun.getSelectedItem().toString() + "/VAR0.tsv";
                    String ruleName = cbProblemForRun.getSelectedItem().toString();
                    if (ruleName.equals("EDF_PS") || ruleName.equals("EDF_TSS")) {
                        COPOilScheduleIndividualDecode.decode(path, row, ruleName);
                    } else if (ruleName.equals("BT")) {
                        OPOilScheduleIndividualDecode.decode(path, row, ruleName);
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
        btnStartForSingleRun = new JButton("开始运行");
        btnStartForSingleRun.addActionListener((e) -> {
            btnSaveSingleRunResult.setEnabled(false);
            Config.ShowDetail = cbShowDetail.isSelected();
            Config.ShowHardCostChart = cbShowHardCostChart.isSelected();
            Config.ShowEachStep = cbShowEachStep.isSelected();

            final int popSize = Integer.parseInt(txtPopSizeForSingleRun.getText());
            final int evaluation = Integer.parseInt(txtEvaluationForSingleRun.getText());
            final String algorithm = cbAlgorithmForRun.getSelectedItem().toString();
            final String problemName = cbProblemForRun.getSelectedItem().toString();

            // 设置进度条的最小值 和 最大值
            progressBar.setMinimum(0);
            progressBar.setMaximum(evaluation);
            progressBar.setValue(0);

            Thread mainThread = new Thread(new Runnable() {
                @Override
                public void run() {

                    // 禁用UI
                    disabledUI();
                    try {
                        // 定义问题"EDF_PS", "EDF_TSS", "BT"
                        if (problemName.equals("EDF_PS") || problemName.equals("EDF_TSS")) {
                            Problem<DoubleSolution> problem = new OilScheduleConstrainedOptimizationProblem(
                                    Config.ShowEachStep, problemName);
                            ExperimentConfig.singleRunDoubleCode(new ExperimentProblem<DoubleSolution>(problem),
                                    algorithm, popSize, evaluation, 1);
                        } else if (problemName.equals("BT")) {
                            Problem<DoubleSolution> problem = new OilScheduleOptimizationProblem(Config.ShowEachStep,
                                    problemName);
                            ExperimentConfig.singleRunDoubleCode(new ExperimentProblem<DoubleSolution>(problem),
                                    algorithm, popSize, evaluation, 1);
                        } else {
                            logger.fatal("未定义的问题。");
                            System.exit(1);
                        }

                        // 单次运行的结果路径
                        String filePath = "result" + "/SingleRun/data/" + algorithm + "/" + problemName + "/" + "FUN"
                                + 0 + ".tsv";// 0代表运行的标号，因为SingleRun模式下算法只运行一次
                        // 获取单次运行结果，并高亮显示非支配解
                        paintNodominanceSolution(resultTable, filePath);
                    } catch (IOException e) {
                        logger.fatal(e.getMessage());
                        System.exit(1);
                    }
                    // 取消禁用UI
                    enabledUI();
                    btnSaveSingleRunResult.setEnabled(true);
                }
            });
            mainThread.setName("SingleRun");
            mainThread.setDaemon(true);// 作为后台守护进程运行
            mainThread.start();
        });
        HBox10.add(btnStartForSingleRun);
        btnSaveSingleRunResult = new JButton("导出到excel");
        btnSaveSingleRunResult.setEnabled(false);
        btnSaveSingleRunResult.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    ExcelHelper.exportTable(resultTable, new File("data/singlerun.csv"));
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                tool.show("通知", "已经写入到: data\\singlerun.csv");
            }
        });
        HBox10.add(btnSaveSingleRunResult);
        box.add(HBox10);

        return box;
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
        problemList.add(new KeyValuePair("BT", "BT"));
        List<KeyValuePair> defaultValue1 = new LinkedList<>();
        defaultValue1.add(new KeyValuePair("EDF_PS", "EDF_PS"));
        cbProblemsForExperiment = new MultiComboBox(problemList, defaultValue1);
        HBox5.add(cbProblemsForExperiment);
        box.add(HBox5);

        // 运行算法复选框
        Box HBox4 = Box.createHorizontalBox();
        JLabel lblSu2 = new JLabel("算法：        ");
        HBox4.add(lblSu2);
        List<KeyValuePair> algorithmList = new LinkedList<>();
        algorithmList.add(new KeyValuePair("NSGAII", "NSGAII"));
        algorithmList.add(new KeyValuePair("NSGAIII", "NSGAIII"));
        algorithmList.add(new KeyValuePair("cMOEAD", "cMOEAD"));
        algorithmList.add(new KeyValuePair("SPEA2", "SPEA2"));
        algorithmList.add(new KeyValuePair("MoCell", "MoCell"));
        List<KeyValuePair> defaultValue2 = new LinkedList<>();
        defaultValue2.add(new KeyValuePair("NSGAII", "NSGAII"));
        cbAlgorithmsForExperiment = new MultiComboBox(algorithmList, defaultValue2);
        HBox4.add(cbAlgorithmsForExperiment);
        // 添加标签到面板
        box.add(HBox4);

        Box HBox1 = Box.createHorizontalBox();
        JLabel lblPopSize = new JLabel("种群规模：");
        HBox1.add(lblPopSize);
        txtPopSizeForExperiment = new JTextField();
        txtPopSizeForExperiment.setText("100");
        HBox1.add(txtPopSizeForExperiment);
        txtPopSizeForExperiment.setColumns(10);
        box.add(HBox1);

        Box HBox2 = Box.createHorizontalBox();
        JLabel lblEvaluation = new JLabel("评价次数：");
        HBox2.add(lblEvaluation);
        txtEvaluationForExperiment = new JTextField();
        txtEvaluationForExperiment.setText("10000");
        txtEvaluationForExperiment.setColumns(10);
        HBox2.add(txtEvaluationForExperiment);
        box.add(HBox2);

        // 运行次数
        Box HBox3 = Box.createHorizontalBox();
        JLabel label = new JLabel("运行次数：");
        HBox3.add(label);
        txtRuns = new JTextField();
        txtRuns.setText("5");
        txtRuns.setColumns(10);
        HBox3.add(txtRuns);
        box.add(HBox3);

        // 创建指标表格
        Box HBox8 = Box.createHorizontalBox();
        final JTable table1 = new JTable();
        JTableHelper.setTableColumnCenter(table1);
        JScrollPane scroll = new JScrollPane(table1);
        scroll.setPreferredSize(new Dimension(100, 600));
        HBox8.add(scroll);
        box.add(HBox8);

        Box HBox10 = Box.createHorizontalBox();
        btnStartForExperiment = new JButton("开始实验");
        btnStartForExperiment.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // 不显示任何可视化界面
                Config.ShowHardCostChart = false;
                Config.ShowDetail = false;
                Config.ShowEachStep = false;
                // 获取用户输入
                final int popSize = Integer.parseInt(txtPopSizeForExperiment.getText());
                final int evaluation = Integer.parseInt(txtEvaluationForExperiment.getText());
                final int runs = Integer.parseInt(txtRuns.getText());

                final List<String> algorithmNames = Arrays.asList(cbAlgorithmsForExperiment.getText().split(","));
                final List<String> problemNames = Arrays.asList(cbProblemsForExperiment.getText().split(","));

                // 设置进度条的最小值 和 最大值
                progressBar.setMinimum(0);
                progressBar.setMaximum(evaluation);
                progressBar.setValue(0);

                // 开辟新的线程运行实验【防止线程阻塞】
                Thread mainThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // 禁用UI
                        disabledUI();
                        try {
                            // 定义问题"EDF_PS", "EDF_TSS", "BT"
                            List<ExperimentProblem<DoubleSolution>> problems = new LinkedList<>();
                            if (!problemNames.isEmpty()) {
                                if (problemNames.contains("EDF_PS")) {
                                    problems.add(new ExperimentProblem<DoubleSolution>(
                                            new OilScheduleConstrainedOptimizationProblem("EDF_PS")));
                                }
                                if (problemNames.contains("EDF_TSS")) {
                                    problems.add(new ExperimentProblem<DoubleSolution>(
                                            new OilScheduleConstrainedOptimizationProblem("EDF_TSS")));
                                }
                                if (problemNames.contains("BT")) {
                                    problems.add(new ExperimentProblem<DoubleSolution>(
                                            new OilScheduleOptimizationProblem("BT")));
                                }
                                ExperimentConfig.doExperimentDoubleCode(problems, algorithmNames, popSize, evaluation,
                                        runs);
                            } else {
                                logger.fatal("请选择一个问题");
                                return;
                            }
                        } catch (IOException e) {
                            logger.fatal(e.getMessage() + "");
                        }
                        // 取消禁用UI
                        enabledUI();
                    }
                });

                mainThread.setName("Experiment");
                mainThread.setDaemon(true);// 作为后台守护进程运行
                mainThread.start();
            }
        });
        HBox10.add(btnStartForExperiment);
        btnAnalysisExperimentResult = new JButton("结果分析");
        btnAnalysisExperimentResult.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                try {
                    // 获取用户输入
                    final int popSize = Integer.parseInt(txtPopSizeForExperiment.getText());
                    final int evaluation = Integer.parseInt(txtEvaluationForExperiment.getText());
                    final int runs = Integer.parseInt(txtRuns.getText());
                    final List<String> algorithmNames = Arrays.asList(cbAlgorithmsForExperiment.getText().split(","));
                    final List<String> problemNames = Arrays.asList(cbProblemsForExperiment.getText().split(","));

                    // 1.生成pareto参考前沿
                    String experimentBaseDirectory = "result/Experiment/";
                    String outputDirectoryName = "PF/";
                    String outputParetoFrontFileName = "FUN";
                    String outputParetoSetFileName = "VAR";
                    new ExperimentGenerateReferenceParetoSetAndFrontFromDoubleSolutions(null).runAnalysis(outputDirectoryName, experimentBaseDirectory,
                            outputParetoFrontFileName, outputParetoSetFileName, problemNames, algorithmNames, runs);

                    // 2.计算性能指标
                    List<String> indicators = Arrays.asList("HV", "EP", "IGD", "GD", "IGD+", "GSPREAD");
                    new ComputeQualityIndicators<>(null).runAnalysis(outputDirectoryName, experimentBaseDirectory,
                            outputParetoFrontFileName, outputParetoSetFileName, problemNames, algorithmNames, indicators, runs, popSize, evaluation);
                    // 显示指标值
                    final DefaultTableModel mm = JTableHelper.showTable(experimentBaseDirectory + "QualityIndicatorSummary.csv", true, false);
                    JTableHelper.showTableInSwing(table1, mm);

                    // 3.生成latex统计表格
                    new GenerateLatexTablesWithStatistics(null).runAnalysis(outputDirectoryName, experimentBaseDirectory,
                            outputParetoFrontFileName, outputParetoSetFileName, problemNames, algorithmNames,indicators, runs);

                    // 4.生成matlab脚本
                    ExcelHelper.exportTable(table1, new File("data/experiment.csv"));
                    MatlabScriptHelper.Generate5DPlotMatlabScript("result/Experiment/PF/oilschedule.pf");
                    MatlabScriptHelper.GenerateBoxPlotMatlabScript("result/runTimes.csv");
                    MatlabScriptHelper.GenerateConvergenceMatlabScript("result/Experiment/", problemNames,
                            algorithmNames, Arrays.asList("EP", "IGD+", "HV", "GSPREAD", "GD", "IGD"));

                    String message = "生成分析结果保存路径：\r\n";
                    message += System.getProperty("user.dir") +"/" +  experimentBaseDirectory;
                    tool.show("分析结果生成完成", message);

                    // 显示结果分析界面
                    createParameterInputUI();
                } catch (Exception ex) {
                    if (ex instanceof FileNotFoundException) {
                        tool.show("错误", "实验数据文件不存在，请重新运行实验后再分析");
                    } else {
                        ex.printStackTrace();
                    }
                }
            }
        });
        HBox10.add(btnAnalysisExperimentResult);
        box.add(HBox10);

        return box;
    }

    /**
     * 【结果分析模块】
     * 运行结果分析参数输入界面
     *
     * @return
     */
    private void createParameterInputUI() {

        JFrame parametersInputFrame = new JFrame();
        Box box = Box.createVerticalBox();
        Box hBox1 = Box.createHorizontalBox();
        JLabel lblIndex = new JLabel("研究问题：");
        hBox1.add(lblIndex);
        List<KeyValuePair> selectedProblems = cbProblemsForExperiment.getSelectedValues();
        List<KeyValuePair> defaultProblems = new LinkedList<>();
        defaultProblems.add(selectedProblems.get(0));
        MultiComboBox problemsMCB = new MultiComboBox(selectedProblems, defaultProblems);
        hBox1.add(problemsMCB);
        box.add(hBox1);

        // 运行算法复选框
        Box hBox2 = Box.createHorizontalBox();
        JLabel lblSu2 = new JLabel("算法：        ");
        hBox2.add(lblSu2);
        List<KeyValuePair> selectedAlgorithms = cbAlgorithmsForExperiment.getSelectedValues();
        List<KeyValuePair> defaultAlgorithms = new LinkedList<>();
        defaultAlgorithms.add(selectedAlgorithms.get(0));
        MultiComboBox algorithmsMCB = new MultiComboBox(selectedAlgorithms, defaultAlgorithms);
        hBox2.add(algorithmsMCB);
        box.add(hBox2);

        // 运行算法复选框
        Box hBox3 = Box.createHorizontalBox();
        JLabel lblRunId = new JLabel("运行序号：");
        hBox3.add(lblRunId);
        JTextField txtRunId = new JTextField();
        txtRunId.setText("0");
        txtRunId.setColumns(10);
        hBox3.add(txtRunId);
        box.add(hBox3);

        // 运行算法复选框
        Box hBox4 = Box.createHorizontalBox();
        JLabel lblMetrics = new JLabel("指标：        ");
        hBox4.add(lblMetrics);
        List<KeyValuePair> selectedMetrics = new LinkedList<>();
        selectedMetrics.add(new KeyValuePair("HV", "HV"));
        selectedMetrics.add(new KeyValuePair("IGD+", "IGD+"));
        selectedMetrics.add(new KeyValuePair("EP", "EP"));
        selectedMetrics.add(new KeyValuePair("IGD", "IGD"));
        selectedMetrics.add(new KeyValuePair("GD", "GD"));
        selectedMetrics.add(new KeyValuePair("GSPREAD", "GSPREAD"));
        //selectedMetrics.add(new KeyValuePair("C", "C"));
        List<KeyValuePair> defaultMetrics = new LinkedList<>();
        defaultMetrics.add(selectedMetrics.get(0));
        MultiComboBox MetricsMCB = new MultiComboBox(selectedMetrics, defaultMetrics);
        hBox4.add(MetricsMCB);
        box.add(hBox4);

        Box hBox5 = Box.createHorizontalBox();
        JButton btnAnalysis = new JButton("可视化");
        btnAnalysis.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int runId = Integer.parseInt(txtRunId.getText().trim());
                ChartHelper.createLineChart(problemsMCB.getText(), algorithmsMCB.getText(), MetricsMCB.getText(),
                        runId);
            }
        });
        hBox5.add(btnAnalysis);
        box.add(hBox5);

        parametersInputFrame.add(box);
        parametersInputFrame.setSize(360, 180);
        parametersInputFrame.setVisible(true);
    }

    /*
     * 创建测试模块界面
     */
    private Component createTestUI() {
        // 创建一个垂直的盒子
        Box box = Box.createVerticalBox();

        Box HBox1 = Box.createHorizontalBox();
        JLabel lblPro = new JLabel("问题：        ");
        HBox1.add(lblPro);
        List<KeyValuePair> problemList = new LinkedList<>();
        problemList.add(new KeyValuePair("C1_DTLZ1", "C1_DTLZ1"));

        problemList.add(new KeyValuePair("C1_DTLZ3-3", "C1_DTLZ3-3"));
        problemList.add(new KeyValuePair("C1_DTLZ3-5", "C1_DTLZ3-5"));
        problemList.add(new KeyValuePair("C1_DTLZ3-8", "C1_DTLZ3-8"));
        problemList.add(new KeyValuePair("C1_DTLZ3-10", "C1_DTLZ3-10"));
        problemList.add(new KeyValuePair("C1_DTLZ3-15", "C1_DTLZ3-15"));

        problemList.add(new KeyValuePair("C2_DTLZ2", "C2_DTLZ2"));
        problemList.add(new KeyValuePair("C3_DTLZ1", "C3_DTLZ1"));
        problemList.add(new KeyValuePair("C3_DTLZ4", "C3_DTLZ4"));

        problemList.add(new KeyValuePair("ConvexC2_DTLZ2-3", "ConvexC2_DTLZ2-3"));
        problemList.add(new KeyValuePair("ConvexC2_DTLZ2-5", "ConvexC2_DTLZ2-5"));
        problemList.add(new KeyValuePair("ConvexC2_DTLZ2-8", "ConvexC2_DTLZ2-8"));
        problemList.add(new KeyValuePair("ConvexC2_DTLZ2-10", "ConvexC2_DTLZ2-10"));
        problemList.add(new KeyValuePair("ConvexC2_DTLZ2-15", "ConvexC2_DTLZ2-15"));

        problemList.add(new KeyValuePair("Binh2", "Binh2"));
        problemList.add(new KeyValuePair("ConstrEx", "ConstrEx"));
        problemList.add(new KeyValuePair("Golinski", "Golinski"));
        problemList.add(new KeyValuePair("Srinivas", "Srinivas"));
        problemList.add(new KeyValuePair("Tanaka", "Tanaka"));
        problemList.add(new KeyValuePair("Water", "Water"));

        List<KeyValuePair> defaultValue1 = new LinkedList<>();
        defaultValue1.add(new KeyValuePair("C1_DTLZ1", "C1_DTLZ1"));
        cbListProblemsForTest = new MultiComboBox(problemList, defaultValue1);
        HBox1.add(cbListProblemsForTest);
        box.add(HBox1);

        // 运行算法复选框
        Box HBox2 = Box.createHorizontalBox();
        JLabel lblSu2 = new JLabel("算法：        ");
        HBox2.add(lblSu2);
        List<KeyValuePair> algorithmList = new LinkedList<>();
        algorithmList.add(new KeyValuePair("NSGAII", "NSGAII"));
        algorithmList.add(new KeyValuePair("NSGAIII", "NSGAIII"));
        algorithmList.add(new KeyValuePair("cMOEAD", "cMOEAD"));
        algorithmList.add(new KeyValuePair("SPEA2", "SPEA2"));
        algorithmList.add(new KeyValuePair("MoCell", "MoCell"));
        List<KeyValuePair> defaultValue2 = new LinkedList<>();
        defaultValue2.add(new KeyValuePair("NSGAII", "NSGAII"));
        cbListAlgorithmsForTest = new MultiComboBox(algorithmList, defaultValue2);
        HBox2.add(cbListAlgorithmsForTest);
        // 添加标签到面板
        box.add(HBox2);

        Box HBox3 = Box.createHorizontalBox();
        JLabel lblPopSize = new JLabel("种群规模：");
        HBox3.add(lblPopSize);
        txtPopSizeForTest = new JTextField();
        txtPopSizeForTest.setText("100");
        HBox3.add(txtPopSizeForTest);
        txtPopSizeForTest.setColumns(10);
        box.add(HBox3);

        Box HBox4 = Box.createHorizontalBox();
        JLabel lblEvaluation = new JLabel("评价次数：");
        HBox4.add(lblEvaluation);
        txtEvaluationForTest = new JTextField();
        txtEvaluationForTest.setText("10000");
        txtEvaluationForTest.setColumns(10);
        HBox4.add(txtEvaluationForTest);
        box.add(HBox4);

        // 运行次数
        Box HBox5 = Box.createHorizontalBox();
        JLabel label = new JLabel("运行次数：");
        HBox5.add(label);
        txtRunsForTest = new JTextField();
        txtRunsForTest.setText("5");
        txtRunsForTest.setColumns(10);
        HBox5.add(txtRunsForTest);
        box.add(HBox5);

        // 创建指标表格
        Box HBox8 = Box.createHorizontalBox();
        final JTable table1 = new JTable();
        table1.setEnabled(false);
        JTableHelper.setTableColumnCenter(table1);
        JScrollPane scroll = new JScrollPane(table1);
        scroll.setPreferredSize(new Dimension(100, 600));
        HBox8.add(scroll);
        box.add(HBox8);

        Box HBox10 = Box.createHorizontalBox();
        btnStartForTest = new JButton("开始测试");
        btnStartForTest.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // 获取用户输入
                final int popSize = Integer.parseInt(txtPopSizeForTest.getText());
                final int evaluation = Integer.parseInt(txtEvaluationForTest.getText());
                final int runs = Integer.parseInt(txtRunsForTest.getText());
                final List<String> problemList = new LinkedList<>();
                for (String s : cbListProblemsForTest.getText().split(",")) {
                    problemList.add(s);
                }
                final List<String> algorithmList = new LinkedList<>();
                for (String s : cbListAlgorithmsForTest.getText().split(",")) {
                    algorithmList.add(s);
                }

                // 设置进度条的最小值 和 最大值
                progressBar.setMinimum(0);
                progressBar.setMaximum(evaluation);
                progressBar.setValue(0);

                // 开辟新的线程运行实验【防止线程阻塞】
                Thread mainThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // 禁用UI
                        disabledUI();

                        try {
                            // 获取问题列表
                            List<ExperimentProblem<DoubleSolution>> problems = TestProblemsExperimentConfig
                                    .getTestProblemsList(problemList, 50, 4);
                            TestProblemsExperimentConfig.doTestExperiment(problems, algorithmList, popSize, evaluation, runs);

                            // 显示运行结果总结信息
                            final DefaultTableModel mm = JTableHelper
                                    .showTable("result/Experiment/" + "QualityIndicatorSummary.csv", true, false);
                            JTableHelper.showTableInSwing(table1, mm);
                        } catch (Exception e) {
                            logger.fatal(e.getMessage());
                            e.printStackTrace();
                            System.exit(1);
                        }

                        // 取消禁用UI
                        enabledUI();
                    }
                });

                mainThread.setName("Test");
                mainThread.setDaemon(true);// 作为后台守护进程运行
                mainThread.start();
            }
        });
        HBox10.add(btnStartForTest);
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
                Map<String, Double> referenceCost = Config.getInstance().referenceCost;
                Map<String, Double> resultCost = new HashMap<String, Double>();
                resultCost.put("energyCost", Double.parseDouble(mm.getValueAt(i, 1) + ""));
                resultCost.put("pipeMix", Double.parseDouble(mm.getValueAt(i, 2) + ""));
                resultCost.put("tankMix", Double.parseDouble(mm.getValueAt(i, 3) + ""));
                resultCost.put("chargeTime", Double.parseDouble(mm.getValueAt(i, 4) + ""));
                resultCost.put("tankUsed", Double.parseDouble(mm.getValueAt(i, 5) + ""));

                // 标记支配解
                flags[i] = ParetoHelper.dominanceComparison(referenceCost, resultCost);
            }

            // 突出显示非支配解
            JTableHelper.setRowsColor(resultTable, flags);
        });
    }

    /**
     * 禁用UI界面的元素
     */
    public void disabledUI() {
        if (tabbedPane.getSelectedIndex() == UIComponent.SINGRUN.ordinal()) {
            btnStartForSingleRun.setEnabled(false);
            cbProblemForRun.setEnabled(false);
            cbAlgorithmForRun.setEnabled(false);
            cbShowHardCostChart.setEnabled(false);
            cbShowDetail.setEnabled(false);
            cbShowEachStep.setEnabled(false);
            txtEvaluationForSingleRun.setEnabled(false);
            txtPopSizeForSingleRun.setEnabled(false);
        } else if (tabbedPane.getSelectedIndex() == UIComponent.Experiment.ordinal()) {
            btnStartForExperiment.setEnabled(false);
            cbAlgorithmsForExperiment.setEnabled(false);
            txtEvaluationForExperiment.setEnabled(false);
            txtPopSizeForExperiment.setEnabled(false);
            txtRuns.setEnabled(false);
            cbAlgorithmsForExperiment.setEnabled(false);
            cbProblemsForExperiment.setEnabled(false);
        } else if (tabbedPane.getSelectedIndex() == UIComponent.Online.ordinal()) {

        } else if (tabbedPane.getSelectedIndex() == UIComponent.Test.ordinal()) {
            btnStartForTest.setEnabled(false);
            txtRunsForTest.setEnabled(false);
            txtEvaluationForTest.setEnabled(false);
            txtPopSizeForTest.setEnabled(false);
            btnStartForTest.setEnabled(false);
            cbListProblemsForTest.setEnabled(false);
            cbListAlgorithmsForTest.setEnabled(false);
        }
        tabbedPane.setEnabled(false);
    }

    /**
     * 取消禁用UI界面的元素
     */
    public void enabledUI() {
        if (tabbedPane.getSelectedIndex() == UIComponent.SINGRUN.ordinal()) {
            btnStartForSingleRun.setEnabled(true);
            cbProblemForRun.setEnabled(true);
            cbAlgorithmForRun.setEnabled(true);
            cbShowHardCostChart.setEnabled(true);
            cbShowDetail.setEnabled(true);
            cbShowEachStep.setEnabled(true);
            txtEvaluationForSingleRun.setEnabled(true);
            txtPopSizeForSingleRun.setEnabled(true);
        } else if (tabbedPane.getSelectedIndex() == UIComponent.Experiment.ordinal()) {
            btnStartForExperiment.setEnabled(true);
            cbAlgorithmsForExperiment.setEnabled(true);
            txtEvaluationForExperiment.setEnabled(true);
            txtPopSizeForExperiment.setEnabled(true);
            txtRuns.setEnabled(true);
            cbAlgorithmsForExperiment.setEnabled(true);
            cbProblemsForExperiment.setEnabled(true);
        } else if (tabbedPane.getSelectedIndex() == UIComponent.Online.ordinal()) {

        } else if (tabbedPane.getSelectedIndex() == UIComponent.Test.ordinal()) {
            btnStartForTest.setEnabled(true);
            txtRunsForTest.setEnabled(true);
            txtEvaluationForTest.setEnabled(true);
            txtPopSizeForTest.setEnabled(true);
            btnStartForTest.setEnabled(true);
            cbListProblemsForTest.setEnabled(true);
            cbListAlgorithmsForTest.setEnabled(true);
        }
        tabbedPane.setEnabled(true);
    }

    /**
     * 更新进度条
     */
    public synchronized void updateProcessBar(int evaluated) {
        // 不要在UI线程外更新操作UI，这里SwingUtilities会找到UI线程并执行更新UI操作
        SwingUtilities.invokeLater(() -> {
            // 更新进度条
            progressBar.setValue(evaluated);
        });
    }

    /**
     * 自动将窗口放到屏幕正中间
     *
     * @param width
     * @param height
     */
    public void setWindowSizeAndCenter(int width, int height) {
        int screenWidth = (int) java.awt.Toolkit.getDefaultToolkit().getScreenSize().width;
        int screenHeight = (int) java.awt.Toolkit.getDefaultToolkit().getScreenSize().height;
        setBounds(screenWidth / 2 - width / 2, screenHeight / 2 - height / 2, width, height);
    }
}