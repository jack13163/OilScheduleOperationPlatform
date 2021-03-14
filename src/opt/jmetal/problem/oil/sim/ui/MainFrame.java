package opt.jmetal.problem.oil.sim.ui;

import opt.jmetal.problem.oil.analysis.CMetrics;
import opt.jmetal.problem.oil.analysis.MatlabScriptHelper;
import opt.jmetal.problem.oil.analysis.RunTimeAnalysis;
import opt.jmetal.problem.oil.chart.util.ChartHelper;
import opt.jmetal.problem.oil.models.Fragment;
import opt.jmetal.problem.oil.sim.common.ExcelHelper;
import opt.jmetal.problem.oil.sim.common.JTableHelper;
import opt.jmetal.problem.oil.sim.common.ParetoHelper;
import opt.jmetal.problem.oil.sim.experiment.ExperimentConfig;
import opt.jmetal.problem.oil.sim.experiment.ExperimentGenerateReferenceParetoSetAndFrontFromDoubleSolutions;
import opt.jmetal.problem.oil.sim.experiment.TestProblemsExperimentConfig;
import opt.jmetal.problem.oil.sim.oil.Config;
import opt.jmetal.problem.oil.sim.oil.cop.COPOilScheduleIndividualDecode;
import opt.jmetal.problem.oil.sim.oil.cop.OilScheduleConstrainedOptimizationProblem;
import opt.jmetal.problem.oil.sim.oil.op.OPOilScheduleIndividualDecode;
import opt.jmetal.problem.oil.sim.oil.op.OilScheduleOptimizationProblem;
import opt.jmetal.problem.oil.sim.onlineoperation.OnlineOperation;
import opt.jmetal.problem.oil.sim.operation.Operation;
import opt.jmetal.problem.oil.sim.ui.multicombobox.KeyValuePair;
import opt.jmetal.problem.oil.sim.ui.multicombobox.MultiComboBox;
import opt.javasim.SimulationProcess;
import opt.jmetal.problem.Problem;
import opt.jmetal.solution.DoubleSolution;
import opt.jmetal.util.experiment.component.ComputeQualityIndicators;
import opt.jmetal.util.experiment.component.GenerateLatexTablesWithStatistics;
import opt.jmetal.util.experiment.util.ExperimentProblem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

    // UIƤ��
    private static final String[] themes = {
            "com.jtattoo.plaf.smart.SmartLookAndFeel",
            "com.jtattoo.plaf.mcwin.McWinLookAndFeel",
            "com.jtattoo.plaf.luna.LunaLookAndFeel",
            "com.jtattoo.plaf.aluminium.AluminiumLookAndFeel",
            "com.jtattoo.plaf.bernstein.BernsteinLookAndFeel",
            "com.jtattoo.plaf.hifi.HiFiLookAndFeel",
            "com.jtattoo.plaf.mint.MintLookAndFeel",
            "com.jtattoo.plaf.aero.AeroLookAndFeel",
            "com.jtattoo.plaf.fast.FastLookAndFeel",
            "com.jtattoo.plaf.acryl.AcrylLookAndFeel"};

    // UI����Ĺ���ģ��
    private enum UIComponent {
        SINGRUN, Experiment, Online, Test
    }

    // ����UI
    private JMenuBar menubar; // �˵���
    private JMenu menuSystem; // �˵�
    private JMenuItem itemTop, itemExit; // �˵���
    private Box VMainPanel;// ������
    public JProgressBar progressBar;// ������
    private InfoUtil tool;// ��ʾ��
    private JTabbedPane tabbedPane;// ѡ�
    private boolean onTop = false;// �Ƿ�����ǰ

    // ���в���UI
    private JTextField txtPopSizeForSingleRun;
    private JTextField txtEvaluationForSingleRun;
    private JCheckBox cbShowDetail;
    private JCheckBox cbShowHardCostChart;
    public JButton btnStartForSingleRun;
    private JButton btnSaveSingleRunResult;
    private JComboBox<String> cbAlgorithmForRun;
    private JComboBox<String> cbProblemForRun;

    // ʵ�鲿��UI
    private JTextField txtPopSizeForExperiment;
    private JTextField txtEvaluationForExperiment;
    private JTextField txtRuns;
    public JButton btnStartForExperiment;
    private MultiComboBox cbAlgorithmsForExperiment;
    private MultiComboBox cbProblemsForExperiment;

    // ���Բ���UI
    private JTextField txtRunsForTest;
    private JTextField txtEvaluationForTest;
    private JTextField txtPopSizeForTest;
    public JButton btnStartForTest;
    private MultiComboBox cbListProblemsForTest;
    private MultiComboBox cbListAlgorithmsForTest;

    // �����Ż�����UI
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
     * ����UI���
     *
     * @param style
     */
    private void changeUIStyle(String style) {

        try {
            UIManager.setLookAndFeel(style);// ����������
            SwingUtilities.updateComponentTreeUI(this);// ����UI����
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public MainFrame(String s) {
        // ����Ĭ��UI���
        changeUIStyle(themes[4]);
        setIconImage(new ImageIcon("img/plan.png").getImage());

        // ���½�֪ͨ����
        tool = new InfoUtil();

        // �رմ����¼�
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int result = JOptionPane.showConfirmDialog(null, "ȷ���˳�?", "ȷ��", JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.INFORMATION_MESSAGE);
                if (result == JOptionPane.OK_OPTION) {
                    // �ر��̳߳�
                    SimulationProcess.cachedThreadPool.shutdown();
                    System.exit(0);
                }
            }
        });
        // ���ô����С������
        setWindowSizeAndCenter(this, 1200, 800);
        setTitle(s);
        menubar = new JMenuBar();
        menuSystem = new JMenu("ϵͳ(S)");
        menuSystem.setMnemonic('S'); // ���ò˵��ļ��̲�����ʽ��Alt + S��
        itemTop = new JMenuItem("��ǰ(T)");

        itemTop.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setAlwaysOnTop(!onTop);
                onTop = !onTop;
                if (onTop) {
                    itemTop.setText("ȡ����ǰ(T)");
                } else {
                    itemTop.setText("��ǰ(T)");
                }
            }
        });
        itemExit = new JMenuItem("�˳�(E)");
        itemExit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        // ���ò˵���ļ��̲�����ʽ��Ctrl+T��Ctrl+E��
        KeyStroke Ctrl_cutKey = KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_MASK);
        itemTop.setAccelerator(Ctrl_cutKey);
        Ctrl_cutKey = KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK);
        itemExit.setAccelerator(Ctrl_cutKey);

        menuSystem.add(itemTop);
        menuSystem.add(itemExit);
        menubar.add(menuSystem); // ���˵���ӵ��˵�����
        setJMenuBar(menubar);

        JMenu menuUIStyle = new JMenu("����");
        menuUIStyle.setMnemonic('S');
        menubar.add(menuUIStyle);

        JMenuItem menuItemStyle1 = new JMenuItem("���1");
        menuItemStyle1.addActionListener((e) -> {
            changeUIStyle(themes[0]);
        });
        menuUIStyle.add(menuItemStyle1);

        JMenuItem menuItemStyle2 = new JMenuItem("���2");
        menuItemStyle2.addActionListener((e) -> {
            changeUIStyle(themes[1]);
        });
        menuUIStyle.add(menuItemStyle2);

        JMenuItem menuItemStyle3 = new JMenuItem("���3");
        menuItemStyle3.addActionListener((e) -> {
            changeUIStyle(themes[2]);
        });
        menuUIStyle.add(menuItemStyle3);

        JMenuItem menuItemStyle4 = new JMenuItem("���4");
        menuItemStyle4.addActionListener((e) -> {
            changeUIStyle(themes[3]);
        });
        menuUIStyle.add(menuItemStyle4);

        JMenuItem menuItemStyle5 = new JMenuItem("���5");
        menuItemStyle5.addActionListener((e) -> {
            changeUIStyle(themes[4]);
        });
        menuUIStyle.add(menuItemStyle5);

        // tab��ǩ��
        VMainPanel = Box.createVerticalBox();
        getContentPane().add(VMainPanel, BorderLayout.CENTER);
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setBackground(Color.WHITE);
        VMainPanel.add(tabbedPane);
        tabbedPane.addTab("����ģ��", createRunUI());
        tabbedPane.addTab("ʵ��ģ��", createExperimentUI());
        tabbedPane.addTab("����ģ��", createOnlineOperationUI());
        tabbedPane.addTab("����ģ��", createTestUI());
        tabbedPane.setSelectedIndex(UIComponent.SINGRUN.ordinal());// ����Ĭ��ѡ�е�ѡ�

        Box HBox12 = Box.createHorizontalBox();
        VMainPanel.add(HBox12);

        // ������
        progressBar = new JProgressBar();
        HBox12.add(progressBar);
        progressBar.setValue(0);
        // ���ưٷֱ��ı����������м���ʾ�İٷ�����
        progressBar.setStringPainted(true);
        // ��ӽ��ȸı�֪ͨ
        progressBar.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                logger.info("��ǰ����ֵ: " + progressBar.getValue() + "; " + "���Ȱٷֱ�: " + progressBar.getPercentComplete());
            }
        });

        setVisible(true);
    }

    /**
     * ���������Ż�ģ�����
     *
     * @return
     */
    private Component createOnlineOperationUI() {
        // ���ߵ�����
        OnlineOperation onlineOperation = new OnlineOperation();
        Config.getInstance().loadConfig();

        // ����һ����ֱ�ĺ���
        Box box = Box.createVerticalBox();

        // �������͹�״̬���
        JPanel panelTankStateTable = new JPanel(new BorderLayout());
        tankStateTable = new JTable();
        tankStateTable.setEnabled(false);
        JTableHelper.setTableColumnCenter(tankStateTable);
        panelTankStateTable.add(tankStateTable.getTableHeader(), BorderLayout.NORTH);
        panelTankStateTable.add(tankStateTable, BorderLayout.CENTER);
        box.add(panelTankStateTable);

        // �����ָ��ߣ��������־��ߺ;��ߵ�Ч��
        JSeparator sep1 = new JSeparator(SwingConstants.CENTER);
        sep1.setPreferredSize(new Dimension(this.getWidth(), 20));
        sep1.setBackground(new Color(153, 153, 153));
        box.add(sep1);

        // ��������ɵ����ͼƻ����
        JPanel panelOilPlanTable = new JPanel(new BorderLayout());
        oilPlanTable = new JTable();
        oilPlanTable.setEnabled(false);
        JTableHelper.setTableColumnCenter(oilPlanTable);
        panelOilPlanTable.add(oilPlanTable.getTableHeader(), BorderLayout.NORTH);
        panelOilPlanTable.add(oilPlanTable, BorderLayout.CENTER);
        box.add(panelOilPlanTable);

        // �����ָ��ߣ��������־��ߺ;��ߵ�Ч��
        JSeparator sep2 = new JSeparator(SwingConstants.CENTER);
        sep2.setPreferredSize(new Dimension(this.getWidth(), 20));
        sep2.setBackground(new Color(153, 153, 153));
        box.add(sep2);

        Box HBox1 = Box.createHorizontalBox();
        JLabel lblTank = new JLabel("���͹ޣ�    ");
        HBox1.add(lblTank);
        String[] template = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15).toString()
                .replace("[", "").replace("]", "").split(", ");
        int numOfTanks = Config.getInstance().getTanks().size();
        String tanks[] = Arrays.copyOfRange(template, 0, numOfTanks + 1);
        cbTank = new JComboBox<String>(tanks);
        cbTank.addActionListener((e) -> {
            // ��ͣ�˲����������UI
            int tank = Integer.parseInt(((JComboBox<?>) (e.getSource())).getSelectedItem().toString());
            // ����ĳһ�еı���ɫ��tank=0�򲻸���
            JTableHelper.setOneRowBackgroundColor(tankStateTable, tank, Color.red);
        });
        HBox1.add(cbTank);
        box.add(HBox1);

        Box HBox2 = Box.createHorizontalBox();
        JLabel lblDs = new JLabel("��������    ");
        HBox2.add(lblDs);
        int numOfDs = Config.getInstance().getDSs().size();
        String dss[] = Arrays.copyOfRange(template, 1, numOfDs + 1);
        cbDs = new JComboBox<String>(dss);
        JLabel lblSpeed = new JLabel("ת���ٶȣ�");
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

            // ���µ�ǰʱ��ϵͳ�еĹ��͹޵�״̬
            double currentTime = onlineOperation.getChargingEndTime(ds == Config.getInstance().HighOilDS);
            JTableHelper.showTableInSwing(tankStateTable, onlineOperation.getTankState(currentTime));

            // ��ͣ�˲����������UI
            int tank = Integer.parseInt(cbTank.getSelectedItem().toString());
            // ����ĳһ�б���ɫ��tank=0�򲻸ı䱳��ɫ
            JTableHelper.setOneRowBackgroundColor(tankStateTable, tank, Color.red);
        });
        cbDs.setSelectedIndex(0);// Ĭ��ѡ�е�һ����������б�������
        HBox2.add(cbDs);
        box.add(HBox2);
        Box HBox3 = Box.createHorizontalBox();
        HBox3.add(lblSpeed);
        HBox3.add(cbSpeed);
        box.add(HBox3);

        Box HBox4 = Box.createHorizontalBox();
        JLabel lblVolume = new JLabel("ת�������");
        HBox4.add(lblVolume);
        txtVolume = new JTextField();
        txtVolume.setColumns(10);
        txtVolume.setText("20000");
        HBox4.add(txtVolume);
        box.add(HBox4);

        // �����ָ��ߣ��������־��ߺ;��ߵ�Ч��
        JSeparator sep3 = new JSeparator(SwingConstants.CENTER);
        sep3.setPreferredSize(new Dimension(this.getWidth(), 20));
        sep3.setBackground(new Color(153, 153, 153));
        box.add(sep3);

        // ����ָ����
        JPanel panelValueTable = new JPanel(new BorderLayout());
        String[] columnNames = {"Լ��Υ��ֵ", "�л�����", "�޵׻�ϳɱ�", "�ܵ���ϳɱ�", "�ܺĳɱ�", "�ù޸���"};
        Object[][] rowData = {{0, 0, 0, 0, 0, 0}};
        costTable = new JTable(rowData, columnNames);
        costTable.setEnabled(false);
        JTableHelper.setTableColumnCenter(costTable);
        panelValueTable.add(costTable.getTableHeader(), BorderLayout.NORTH);
        panelValueTable.add(costTable, BorderLayout.CENTER);
        box.add(panelValueTable);

        Box HBox10 = Box.createHorizontalBox();
        JButton btnLastStep = new JButton("��һ��");
        JButton btnReset = new JButton("��ʼ");
        JButton btnNextStep = new JButton("��һ��");

        btnLastStep.setEnabled(false);
        btnLastStep.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btnLastStep.setEnabled(false);
                try {

                    if (!onlineOperation.getFragments().isEmpty()) {

                        // ������һ��
                        Fragment lastFragment = onlineOperation.getFragments().pop();
                        onlineOperation.last();

                        // ��ʾ��ϸ����
                        if (cbShowEachStep.isSelected()) {
                            Operation.plotSchedule2(onlineOperation.getOperations());
                        }

                        // ������һ������Ϣ
                        cbTank.setSelectedIndex(lastFragment.getTank());
                        cbDs.setSelectedIndex(lastFragment.getDs() - 1);
                        cbSpeed.setSelectedItem(lastFragment.getSpeed());
                        txtVolume.setText(lastFragment.getVolume() + "");

                        // �ж��Ƿ��ܹ�������һ��
                        if (onlineOperation.getFragments().isEmpty()) {
                            btnLastStep.setEnabled(false);

                            // ���µ�ǰʱ��ϵͳ�еĹ��͹޵�״̬
                            JTableHelper.showTableInSwing(tankStateTable, onlineOperation.getTankState(0));
                        } else {
                            btnLastStep.setEnabled(true);

                            // ���µ�ǰʱ��ϵͳ�еĹ��͹޵�״̬
                            double currentTime = onlineOperation.getChargingEndTime(
                                    onlineOperation.getFragments().peek().getDs() == Config.getInstance().HighOilDS);
                            JTableHelper.showTableInSwing(tankStateTable, onlineOperation.getTankState(currentTime));
                        }

                        // ����ĳһ�б���ɫ��tank=0�򲻸ı䱳��ɫ
                        JTableHelper.setOneRowBackgroundColor(tankStateTable, lastFragment.getTank(), Color.red);
                        // ���µ�ǰϵͳ��δ������ͼƻ�
                        JTableHelper.showTableInSwing(oilPlanTable, onlineOperation.getFp());
                        // ���³ɱ�
                        JTableHelper.showTableInSwing(costTable, onlineOperation.getCost());
                    }
                } catch (Exception ex) {
                    String errorMessage = ex.getMessage();
                    tool.show("����", errorMessage);
                }
            }
        });

        btnReset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // ��ʼ�����Ż�
                onlineOperation.start();
                // ���µ�ǰʱ��ϵͳ�еĹ��͹޵�״̬
                JTableHelper.showTableInSwing(tankStateTable, onlineOperation.getTankState(0));
                // ���µ�ǰϵͳ��δ������ͼƻ�
                JTableHelper.showTableInSwing(oilPlanTable, onlineOperation.getFp());

                // ��ʼ��ѡ��
                cbTank.setSelectedIndex(0);
                cbDs.setSelectedIndex(0);
                cbSpeed.setSelectedIndex(0);
                txtVolume.setText("");

                if (btnReset.getText().equals("��ʼ")) {
                    tabbedPane.setEnabled(false);
                    btnReset.setText("����");
                    btnNextStep.setEnabled(true);
                    btnLastStep.setEnabled(false);
                    // ���ӻ���ϸ����
                    Operation.plotSchedule2(onlineOperation.getOperations());
                } else {
                    tabbedPane.setEnabled(true);
                    btnReset.setText("��ʼ");
                    btnNextStep.setEnabled(false);
                    btnLastStep.setEnabled(false);
                }
            }
        });

        btnNextStep.setEnabled(false);
        btnNextStep.addActionListener(new ActionListener() {
            @Override
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
                    // ������һ��
                    boolean flag = onlineOperation.next(fragment);

                    if (flag) {
                        onlineOperation.getFragments().push(fragment);
                        // ��ʾ��ϸ����
                        Operation.plotSchedule2(onlineOperation.getOperations());

                        // ���µ�ǰʱ��ϵͳ�еĹ��͹޵�״̬
                        double currentTime = onlineOperation
                                .getChargingEndTime(fragment.getDs() == Config.getInstance().HighOilDS);
                        JTableHelper.showTableInSwing(tankStateTable, onlineOperation.getTankState(currentTime));
                        // ���µ�ǰϵͳ��δ������ͼƻ�
                        JTableHelper.showTableInSwing(oilPlanTable, onlineOperation.getFp());
                        // ���³ɱ�
                        JTableHelper.showTableInSwing(costTable, onlineOperation.getCost());
                    }
                } catch (Exception ex) {
                    String errorMessage = ex.getMessage();
                    tool.show("����", errorMessage);
                }

                if (!onlineOperation.getFragments().isEmpty()) {
                    btnLastStep.setEnabled(true);
                } else {
                    btnLastStep.setEnabled(false);
                }
                btnNextStep.setEnabled(true);

                // ��ʼ��
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

        // �����ָ��ߣ��������־��ߺ;��ߵ�Ч��
        JSeparator sep4 = new JSeparator(SwingConstants.CENTER);
        sep3.setPreferredSize(new Dimension(this.getWidth(), 20));
        sep3.setBackground(new Color(153, 153, 153));
        box.add(sep4);

        Box HBox11 = Box.createHorizontalBox();
        JLabel lblPath = new JLabel("ָ��·����");
        HBox11.add(lblPath);
        JButton btnSelectFile = new JButton("��ѡ��Ҫִ�е�ָ�������ļ�[txt]");
        btnSelectFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) { // ��ť����¼�
                JFileChooser chooser = new JFileChooser(); // ����ѡ����
                chooser.setCurrentDirectory(new File("data")); // Ĭ�ϴ�data
                chooser.setMultiSelectionEnabled(false); // �Ƿ��ѡ
                int returnVal = chooser.showOpenDialog(btnSelectFile); // �Ƿ���ļ�ѡ���
                if (returnVal == JFileChooser.APPROVE_OPTION) { // ��������ļ�����
                    String filepath = chooser.getSelectedFile().getAbsolutePath(); // ��ȡ����·��
                    /* ��ȡ���� */
                    try {
                        BufferedReader br = new BufferedReader(
                                new InputStreamReader(new FileInputStream(new File(filepath)), "UTF-8"));
                        String lineTxt = null;
                        fragmentList = new LinkedList<>();
                        while ((lineTxt = br.readLine()) != null) {
                            // �����Զ��ŷָ�
                            String[] names = lineTxt.split(",");
                            Fragment fragment = new Fragment();
                            fragment.setTank(Integer.parseInt(names[0].trim()));
                            fragment.setDs(Integer.parseInt(names[1].trim()));
                            fragment.setSpeed(Integer.parseInt(names[2].trim()));
                            fragment.setVolume(Integer.parseInt(names[3].trim()));
                            fragmentList.add(fragment);
                        }
                        br.close();

                        // ��ʼ�����Ż�
                        onlineOperation.start();
                        // ���µ�ǰʱ��ϵͳ�еĹ��͹޵�״̬
                        JTableHelper.showTableInSwing(tankStateTable, onlineOperation.getTankState(0));
                        // ���µ�ǰϵͳ��δ������ͼƻ�
                        JTableHelper.showTableInSwing(oilPlanTable, onlineOperation.getFp());
                        // ����UI
                        Fragment fragment = fragmentList.peek();
                        updateInputFragment(fragment);
                        btnStartFragmentList.setEnabled(true);
                        btnRunAllFragment.setEnabled(true);
                        // ��ʾ��ϸ����
                        Operation.plotSchedule2(onlineOperation.getOperations());
                    } catch (Exception ex) {
                        System.err.println("read errors :" + ex);
                    }
                }
            }
        });
        HBox11.add(btnSelectFile);
        btnStartFragmentList = new JButton("����ָ������");
        btnStartFragmentList.setEnabled(false);
        btnStartFragmentList.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) { // ��ť����¼�
                Fragment fragment = null;
                try {
                    fragment = fragmentList.remove();
                    // ������һ��
                    boolean flag = onlineOperation.next(fragment);

                    if (flag) {
                        onlineOperation.getFragments().push(fragment);
                        // ��ʾ��ϸ����
                        Operation.plotSchedule2(onlineOperation.getOperations());

                        // ���µ�ǰʱ��ϵͳ�еĹ��͹޵�״̬
                        double currentTime = onlineOperation
                                .getChargingEndTime(fragment.getDs() == Config.getInstance().HighOilDS);
                        JTableHelper.showTableInSwing(tankStateTable, onlineOperation.getTankState(currentTime));
                        // ���µ�ǰϵͳ��δ������ͼƻ�
                        JTableHelper.showTableInSwing(oilPlanTable, onlineOperation.getFp());
                        // ���³ɱ�
                        JTableHelper.showTableInSwing(costTable, onlineOperation.getCost());
                        lastTime = currentTime;
                        System.out.println(lastTime);
                    }

                    if (fragmentList.isEmpty()) {
                        // ��ʾִ�н���
                        btnStartFragmentList.setEnabled(false);
                        String message = "��ǰʱ�䣺" + lastTime + "\n" + "\nָ��ִ�н���";
                        tool.show("��ʾ", message);
                    } else {
                        // ����UI
                        fragment = fragmentList.peek();
                        updateInputFragment(fragment);
                    }
                } catch (Exception ex) {
                    String errorMessage = "��ǰʱ�䣺" + lastTime + "\n" + ex.getMessage() + "\nָ�" + fragment + "����\n"
                            + "���޸�ָ�������ļ�";
                    tool.show("����", errorMessage);
                    btnStartFragmentList.setEnabled(false);
                }
            }
        });
        HBox11.add(btnStartFragmentList);
        btnRunAllFragment = new JButton("һ��������");
        btnRunAllFragment.setEnabled(false);
        btnRunAllFragment.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) { // ��ť����¼�
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
     * �������������
     *
     * @param fragment
     */
    private void updateInputFragment(Fragment fragment) {
        // ��ȡ��ʼʱ�̹��͹޵�״̬
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
     * ��������ģ�����
     */
    private JComponent createRunUI() {
        // ����һ����ֱ�ĺ���
        Box box = Box.createVerticalBox();

        Box HBox1 = Box.createHorizontalBox();
        JLabel lblIndex = new JLabel("�������ԣ�");
        HBox1.add(lblIndex);
        String problemNames[] = {"EDF_PS", "EDF_TSS", "BT"};
        cbProblemForRun = new JComboBox<String>(problemNames);
        HBox1.add(cbProblemForRun);
        box.add(HBox1);

        Box HBox2 = Box.createHorizontalBox();
        JLabel lblSu = new JLabel("�㷨��        ");
        HBox2.add(lblSu);
        String[] algorithms = {"NSGAII", "NSGAIII", "cMOEAD", "SPEA2", "MoCell"};
        cbAlgorithmForRun = new JComboBox<String>(algorithms);
        HBox2.add(cbAlgorithmForRun);
        box.add(HBox2);

        Box HBox3 = Box.createHorizontalBox();
        JLabel lblPopSize = new JLabel("��Ⱥ��ģ��");
        HBox3.add(lblPopSize);
        txtPopSizeForSingleRun = new JTextField();
        txtPopSizeForSingleRun.setText("100");
        HBox3.add(txtPopSizeForSingleRun);
        txtPopSizeForSingleRun.setColumns(10);
        box.add(HBox3);

        Box HBox4 = Box.createHorizontalBox();
        JLabel lblEvaluation = new JLabel("���۴�����");
        HBox4.add(lblEvaluation);
        txtEvaluationForSingleRun = new JTextField();
        txtEvaluationForSingleRun.setText("10000");
        txtEvaluationForSingleRun.setColumns(10);
        HBox4.add(txtEvaluationForSingleRun);
        box.add(HBox4);

        Box HBox7 = Box.createHorizontalBox();
        cbShowDetail = new JCheckBox("��ʾ���е���ϸ����");
        cbShowDetail.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Config.ShowDetail = cbShowDetail.isSelected();
            }
        });
        cbShowDetail.setSelected(false);
        cbShowHardCostChart = new JCheckBox("��ʾӲԼ��");
        cbShowHardCostChart.addActionListener((e) -> {
            Config.ShowHardCostChart = cbShowHardCostChart.isSelected();
        });
        cbShowHardCostChart.setSelected(false);
        cbShowEachStep = new JCheckBox("��ʾÿһ��");
        cbShowEachStep.addActionListener((e) -> {
            Config.ShowEachStep = cbShowEachStep.isSelected();
        });
        cbShowEachStep.setSelected(false);
        HBox7.add(cbShowDetail);
        HBox7.add(cbShowHardCostChart);
        HBox7.add(cbShowEachStep);
        box.add(HBox7);

        // ����ָ����
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
                // ������Σ�������˫���¼�
                if (e.getClickCount() == 2) {
                    int row = resultTable.getSelectedRow();
                    // չʾ��ϸ����
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
        btnStartForSingleRun = new JButton("��ʼ����");
        btnStartForSingleRun.addActionListener((e) -> {
            btnSaveSingleRunResult.setEnabled(false);
            Config.ShowDetail = cbShowDetail.isSelected();
            Config.ShowHardCostChart = cbShowHardCostChart.isSelected();
            Config.ShowEachStep = cbShowEachStep.isSelected();

            final int popSize = Integer.parseInt(txtPopSizeForSingleRun.getText());
            final int evaluation = Integer.parseInt(txtEvaluationForSingleRun.getText());
            final String algorithm = cbAlgorithmForRun.getSelectedItem().toString();
            final String problemName = cbProblemForRun.getSelectedItem().toString();

            // ���ý���������Сֵ �� ���ֵ
            progressBar.setMinimum(0);
            progressBar.setMaximum(evaluation);
            progressBar.setValue(0);

            Thread mainThread = new Thread(new Runnable() {
                @Override
                public void run() {

                    // ����UI
                    disabledUI();
                    try {
                        // ��������"EDF_PS", "EDF_TSS", "BT"
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
                            logger.fatal("δ��������⡣");
                            System.exit(1);
                        }

                        // �������еĽ��·��
                        String filePath = "result" + "/SingleRun/data/" + algorithm + "/" + problemName + "/" + "FUN" + 0 + ".tsv";// 0�������еı�ţ���ΪSingleRunģʽ���㷨ֻ����һ��
                        // ��ȡ�������н������������ʾ��֧���
                        paintNodominanceSolution(resultTable, filePath);
                    } catch (IOException e) {
                        logger.fatal(e.getMessage());
                        System.exit(1);
                    }
                    // ȡ������UI
                    enabledUI();
                    btnSaveSingleRunResult.setEnabled(true);
                }
            });
            mainThread.setName("SingleRun");
            mainThread.setDaemon(true);// ��Ϊ��̨�ػ���������
            mainThread.start();
        });
        HBox10.add(btnStartForSingleRun);
        btnSaveSingleRunResult = new JButton("������excel");
        btnSaveSingleRunResult.setEnabled(false);
        btnSaveSingleRunResult.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    ExcelHelper.exportTable(resultTable, new File("data/singlerun.csv"));
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                tool.show("֪ͨ", "�Ѿ�д�뵽: data/singlerun.csv");
            }
        });
        HBox10.add(btnSaveSingleRunResult);
        box.add(HBox10);

        return box;
    }

    /**
     * ����ʵ��ģ�����
     */
    private JComponent createExperimentUI() {
        // ����һ����ֱ�ĺ���
        Box box = Box.createVerticalBox();

        Box HBox5 = Box.createHorizontalBox();
        JLabel lblIndex = new JLabel("�������ԣ�");
        HBox5.add(lblIndex);
        List<KeyValuePair> problemList = new LinkedList<>();
        problemList.add(new KeyValuePair("EDF_PS", "EDF_PS"));
        problemList.add(new KeyValuePair("EDF_TSS", "EDF_TSS"));
        problemList.add(new KeyValuePair("BT", "BT"));
        List<KeyValuePair> defaultValue1 = new LinkedList<>();
        defaultValue1.add(new KeyValuePair("EDF_PS", "EDF_PS"));
        defaultValue1.add(new KeyValuePair("EDF_TSS", "EDF_TSS"));
        cbProblemsForExperiment = new MultiComboBox(problemList, defaultValue1);
        HBox5.add(cbProblemsForExperiment);
        box.add(HBox5);

        // �����㷨��ѡ��
        Box HBox4 = Box.createHorizontalBox();
        JLabel lblSu2 = new JLabel("�㷨��        ");
        HBox4.add(lblSu2);
        List<KeyValuePair> algorithmList = new LinkedList<>();
        algorithmList.add(new KeyValuePair("NSGAII", "NSGAII"));
        algorithmList.add(new KeyValuePair("NSGAIII", "NSGAIII"));
        algorithmList.add(new KeyValuePair("cMOEAD", "cMOEAD"));
        algorithmList.add(new KeyValuePair("SPEA2", "SPEA2"));
        algorithmList.add(new KeyValuePair("MoCell", "MoCell"));
        List<KeyValuePair> defaultValue2 = new LinkedList<>();
        defaultValue2.add(new KeyValuePair("NSGAII", "NSGAII"));
        defaultValue2.add(new KeyValuePair("NSGAIII", "NSGAIII"));
        defaultValue2.add(new KeyValuePair("SPEA2", "SPEA2"));
        cbAlgorithmsForExperiment = new MultiComboBox(algorithmList, defaultValue2);
        HBox4.add(cbAlgorithmsForExperiment);
        // ��ӱ�ǩ�����
        box.add(HBox4);

        Box HBox1 = Box.createHorizontalBox();
        JLabel lblPopSize = new JLabel("��Ⱥ��ģ��");
        HBox1.add(lblPopSize);
        txtPopSizeForExperiment = new JTextField();
        txtPopSizeForExperiment.setText("100");
        HBox1.add(txtPopSizeForExperiment);
        txtPopSizeForExperiment.setColumns(10);
        box.add(HBox1);

        Box HBox2 = Box.createHorizontalBox();
        JLabel lblEvaluation = new JLabel("���۴�����");
        HBox2.add(lblEvaluation);
        txtEvaluationForExperiment = new JTextField();
        txtEvaluationForExperiment.setText("25000");
        txtEvaluationForExperiment.setColumns(10);
        HBox2.add(txtEvaluationForExperiment);
        box.add(HBox2);

        // ���д���
        Box HBox3 = Box.createHorizontalBox();
        JLabel label = new JLabel("���д�����");
        HBox3.add(label);
        txtRuns = new JTextField();
        txtRuns.setText("10");
        txtRuns.setColumns(10);
        HBox3.add(txtRuns);
        box.add(HBox3);

        // ����ָ����
        Box HBox8 = Box.createHorizontalBox();
        final JTable table1 = new JTable();
        JTableHelper.setTableColumnCenter(table1);
        JScrollPane scroll = new JScrollPane(table1);
        scroll.setPreferredSize(new Dimension(100, 600));
        HBox8.add(scroll);
        box.add(HBox8);

        Box HBox10 = Box.createHorizontalBox();
        btnStartForExperiment = new JButton("��ʼʵ��");
        btnStartForExperiment.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                // �������ʵ�����ݣ���ʾ�û��Ƿ񸲸�
                if (new File("result/Experiment/data/").exists()) {
                    int result = JOptionPane.showConfirmDialog(null, "ȷ��Ҫ�������е�ʵ������?", "ȷ��", JOptionPane.OK_CANCEL_OPTION,
                            JOptionPane.INFORMATION_MESSAGE);
                    if (result != JOptionPane.OK_OPTION) {
                        return;
                    }
                }

                // ����ʾ�κο��ӻ�����
                Config.ShowHardCostChart = false;
                Config.ShowDetail = false;
                Config.ShowEachStep = false;
                // ��ȡ�û�����
                final int popSize = Integer.parseInt(txtPopSizeForExperiment.getText());
                final int evaluation = Integer.parseInt(txtEvaluationForExperiment.getText());
                final int runs = Integer.parseInt(txtRuns.getText());

                final List<String> algorithmNames = Arrays.asList(cbAlgorithmsForExperiment.getText().split(","));
                final List<String> problemNames = Arrays.asList(cbProblemsForExperiment.getText().split(","));

                // ���ý���������Сֵ �� ���ֵ
                progressBar.setMinimum(0);
                progressBar.setMaximum(evaluation);
                progressBar.setValue(0);

                // �����µ��߳�����ʵ�顾��ֹ�߳�������
                Thread mainThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // ����UI
                        disabledUI();
                        try {
                            // ��������"EDF_PS", "EDF_TSS", "BT"
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
                                ExperimentConfig.doExperimentDoubleCode(problems, algorithmNames, popSize, evaluation, runs);
                            } else {
                                logger.fatal("��ѡ��һ������");
                                return;
                            }
                        } catch (IOException e) {
                            logger.fatal(e.getMessage() + "");
                        }

                        // ȡ������UI
                        enabledUI();
                        // ��ʾ���н���
                        JOptionPane.showMessageDialog(null, "���н����������Կ�ʼ��������ˡ�", "���н���", JOptionPane.OK_CANCEL_OPTION);
                    }
                });

                mainThread.setName("Experiment");
                mainThread.setDaemon(true);// ��Ϊ��̨�ػ���������
                mainThread.start();
            }
        });
        HBox10.add(btnStartForExperiment);
        btnAnalysisExperimentResult = new JButton("�������");
        btnAnalysisExperimentResult.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    // ��ȡ�û�����
                    final int popSize = Integer.parseInt(txtPopSizeForExperiment.getText());
                    final int evaluation = Integer.parseInt(txtEvaluationForExperiment.getText());
                    final int runs = Integer.parseInt(txtRuns.getText());
                    final List<String> algorithmNames = Arrays.asList(cbAlgorithmsForExperiment.getText().split(","));
                    final List<String> problemNames = Arrays.asList(cbProblemsForExperiment.getText().split(","));

                    // ���ɽ����·��
                    String experimentBaseDirectory = "result/Experiment/";
                    String outputDirectoryName = "PF/";
                    String outputParetoFrontFileName = "FUN";
                    String outputParetoSetFileName = "VAR";
                    String summaryFileName = "QualityIndicatorSummary.csv";

                    // �ж��Ƿ��Ѿ�����������������ظ�������������ظ��õ��������ɾ��summaryFileName�ļ�
                    if (!new File(experimentBaseDirectory + summaryFileName).exists() ||
                            JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(null, "ȷ��Ҫ�������еķ��������?", "ȷ��", JOptionPane.YES_NO_CANCEL_OPTION,
                                    JOptionPane.INFORMATION_MESSAGE)) {
                        // 1.����pareto�ο�ǰ��
                        new ExperimentGenerateReferenceParetoSetAndFrontFromDoubleSolutions(null).runAnalysis(outputDirectoryName, experimentBaseDirectory,
                                outputParetoFrontFileName, outputParetoSetFileName, problemNames, algorithmNames, runs);

                        // 2.��������ָ��
                        List<String> indicators = Arrays.asList("HV", "EP", "IGD", "GD", "IGD+", "GSPREAD");
                        new ComputeQualityIndicators<>(null).runAnalysis(outputDirectoryName, experimentBaseDirectory,
                                outputParetoFrontFileName, outputParetoSetFileName, problemNames, algorithmNames, indicators, runs, popSize, evaluation);

                        // 3.����latex��excelͳ�Ʊ��
                        new GenerateLatexTablesWithStatistics(null).runAnalysis(outputDirectoryName, experimentBaseDirectory,
                                outputParetoFrontFileName, outputParetoSetFileName, problemNames, algorithmNames, indicators, runs);

                        // 4.����matlab�ű�
                        ExcelHelper.exportTable(table1, new File("data/experiment.csv"));
                        MatlabScriptHelper.Generate5DPlotMatlabScript("result/Experiment/PF/oilschedule.pf");
                        MatlabScriptHelper.GenerateBoxPlotMatlabScript("result/runTimes.csv");
                        MatlabScriptHelper.GenerateConvergenceMatlabScript("result/Experiment/", problemNames,
                                algorithmNames, Arrays.asList("EP", "IGD+", "HV", "GSPREAD", "GD", "IGD"));

                        // 5.��������ʱ�������excel���
                        RunTimeAnalysis.GenerateRunTimeReport(problemNames, algorithmNames);

                        // 6.����Cָ��
                        CMetrics.calculateCMetrics(algorithmNames, problemNames);

                        String message = "���ɷ����������·����\r\n";
                        message += System.getProperty("user.dir") + "/" + experimentBaseDirectory;
                        tool.show("��������������", message);
                    }

                    // ��ʾ���н�������ķ�֧��⼯
                    try {
                        paintNodominanceSolution(table1, "result/Experiment/PF/oilschedule.pf");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                    // ��ʾ�����������
                    createParameterInputUI();
                } catch (Exception ex) {
                    if (ex instanceof FileNotFoundException) {
                        tool.show("����", "ʵ�������ļ������ڣ�����������ʵ����ٷ���");
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
     * ���������ģ�顿
     * ���н�����������������
     *
     * @return
     */
    private void createParameterInputUI() {

        JFrame parametersInputFrame = new JFrame();
        Box box = Box.createVerticalBox();
        Box hBox1 = Box.createHorizontalBox();
        JLabel lblIndex = new JLabel("�о����⣺");
        hBox1.add(lblIndex);
        List<KeyValuePair> selectedProblems = cbProblemsForExperiment.getSelectedValues();
        List<KeyValuePair> defaultProblems = new LinkedList<>();
        defaultProblems.add(selectedProblems.get(0));
        MultiComboBox problemsMCB = new MultiComboBox(selectedProblems, defaultProblems);
        hBox1.add(problemsMCB);
        box.add(hBox1);

        // �����㷨��ѡ��
        Box hBox2 = Box.createHorizontalBox();
        JLabel lblSu2 = new JLabel("�㷨��        ");
        hBox2.add(lblSu2);
        List<KeyValuePair> selectedAlgorithms = cbAlgorithmsForExperiment.getSelectedValues();
        List<KeyValuePair> defaultAlgorithms = new LinkedList<>();
        defaultAlgorithms.add(selectedAlgorithms.get(0));
        MultiComboBox algorithmsMCB = new MultiComboBox(selectedAlgorithms, defaultAlgorithms);
        hBox2.add(algorithmsMCB);
        box.add(hBox2);

        // �����㷨��ѡ��
        Box hBox3 = Box.createHorizontalBox();
        JLabel lblRunId = new JLabel("������ţ�");
        hBox3.add(lblRunId);
        JTextField txtRunId = new JTextField();
        txtRunId.setText("0");
        txtRunId.setColumns(10);
        hBox3.add(txtRunId);
        box.add(hBox3);

        // �����㷨��ѡ��
        Box hBox4 = Box.createHorizontalBox();
        JLabel lblMetrics = new JLabel("ָ�꣺        ");
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
        JButton btnAnalysis = new JButton("���ӻ�");
        btnAnalysis.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int runId = Integer.parseInt(txtRunId.getText().trim());
                JFrame frame = ChartHelper.createLineChart(problemsMCB.getText(), algorithmsMCB.getText(), MetricsMCB.getText(),
                        runId);
                // ���ô��ڴ�С������
                setWindowSizeAndCenter(frame, 800, 600);
            }
        });
        hBox5.add(btnAnalysis);
        box.add(hBox5);
        parametersInputFrame.add(box);

        // ���ô��ڴ�С������
        setWindowSizeAndCenter(parametersInputFrame, 360, 180);
        parametersInputFrame.setVisible(true);
    }

    /*
     * ��������ģ�����
     */
    private Component createTestUI() {
        // ����һ����ֱ�ĺ���
        Box box = Box.createVerticalBox();

        Box HBox1 = Box.createHorizontalBox();
        JLabel lblPro = new JLabel("���⣺        ");
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

        // �����㷨��ѡ��
        Box HBox2 = Box.createHorizontalBox();
        JLabel lblSu2 = new JLabel("�㷨��        ");
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
        // ��ӱ�ǩ�����
        box.add(HBox2);

        Box HBox3 = Box.createHorizontalBox();
        JLabel lblPopSize = new JLabel("��Ⱥ��ģ��");
        HBox3.add(lblPopSize);
        txtPopSizeForTest = new JTextField();
        txtPopSizeForTest.setText("100");
        HBox3.add(txtPopSizeForTest);
        txtPopSizeForTest.setColumns(10);
        box.add(HBox3);

        Box HBox4 = Box.createHorizontalBox();
        JLabel lblEvaluation = new JLabel("���۴�����");
        HBox4.add(lblEvaluation);
        txtEvaluationForTest = new JTextField();
        txtEvaluationForTest.setText("10000");
        txtEvaluationForTest.setColumns(10);
        HBox4.add(txtEvaluationForTest);
        box.add(HBox4);

        // ���д���
        Box HBox5 = Box.createHorizontalBox();
        JLabel label = new JLabel("���д�����");
        HBox5.add(label);
        txtRunsForTest = new JTextField();
        txtRunsForTest.setText("5");
        txtRunsForTest.setColumns(10);
        HBox5.add(txtRunsForTest);
        box.add(HBox5);

        // ����ָ����
        Box HBox8 = Box.createHorizontalBox();
        final JTable table1 = new JTable();
        table1.setEnabled(false);
        JTableHelper.setTableColumnCenter(table1);
        JScrollPane scroll = new JScrollPane(table1);
        scroll.setPreferredSize(new Dimension(100, 600));
        HBox8.add(scroll);
        box.add(HBox8);

        Box HBox10 = Box.createHorizontalBox();
        btnStartForTest = new JButton("��ʼ����");
        btnStartForTest.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // ��ȡ�û�����
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

                // ���ý���������Сֵ �� ���ֵ
                progressBar.setMinimum(0);
                progressBar.setMaximum(evaluation);
                progressBar.setValue(0);

                // �����µ��߳�����ʵ�顾��ֹ�߳�������
                Thread mainThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // ����UI
                        disabledUI();

                        try {
                            // ��ȡ�����б�
                            List<ExperimentProblem<DoubleSolution>> problems = TestProblemsExperimentConfig
                                    .getTestProblemsList(problemList, 50, 4);
                            TestProblemsExperimentConfig.doTestExperiment(problems, algorithmList, popSize, evaluation, runs);

                            // ��ʾ���н���ܽ���Ϣ
                            final DefaultTableModel mm = JTableHelper
                                    .showTable("result/Experiment/" + "QualityIndicatorSummary.csv", true, false);
                            JTableHelper.showTableInSwing(table1, mm);
                        } catch (Exception e) {
                            logger.fatal(e.getMessage());
                            e.printStackTrace();
                            System.exit(1);
                        }

                        // ȡ������UI
                        enabledUI();
                    }
                });

                mainThread.setName("Test");
                mainThread.setDaemon(true);// ��Ϊ��̨�ػ���������
                mainThread.start();
            }
        });
        HBox10.add(btnStartForTest);
        box.add(HBox10);

        return box;
    }

    /**
     * ��ȡ���н������������ʾ��֧��⡾Ŀ�꺯��ֵ��
     *
     * @param resultTable
     * @param filePath
     * @throws IOException
     */
    protected void paintNodominanceSolution(JTable resultTable, String filePath) throws IOException {
        // ׼������
        final DefaultTableModel mm = JTableHelper.showTableWithNo(filePath, false);

        // ����ָ������
        String[] columnNames = {"���", "�ܺĳɱ�", "�ܵ���ϳɱ�", "�޵׻�ϳɱ�", "�л�����", "�ù޸���"};
        mm.setColumnIdentifiers(columnNames);

        // ��Ҫ��UI�߳�����²���UI������SwingUtilities���ҵ�UI�̲߳�ִ�и���UI����
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

                // ���֧���
                flags[i] = ParetoHelper.dominanceComparison(referenceCost, resultCost);
            }

            // ͻ����ʾ��֧���
            JTableHelper.setRowsColor(resultTable, flags);
        });
    }

    /**
     * ����UI�����Ԫ��
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
     * ȡ������UI�����Ԫ��
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
     * ���½�����
     */
    public synchronized void updateProcessBar(int evaluated) {
        // ��Ҫ��UI�߳�����²���UI������SwingUtilities���ҵ�UI�̲߳�ִ�и���UI����
        SwingUtilities.invokeLater(() -> {
            // ���½�����
            progressBar.setValue(evaluated);
        });
    }

    /**
     * �Զ������ڷŵ���Ļ���м�
     *
     * @param frame
     * @param width
     * @param height
     */
    public void setWindowSizeAndCenter(JFrame frame, int width, int height) {
        int screenWidth = (int) java.awt.Toolkit.getDefaultToolkit().getScreenSize().width;
        int screenHeight = (int) java.awt.Toolkit.getDefaultToolkit().getScreenSize().height;
        frame.setBounds(screenWidth / 2 - width / 2, screenHeight / 2 - height / 2, width, height);
    }
}
